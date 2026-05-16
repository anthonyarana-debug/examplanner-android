package pe.tecsup.examplanner.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// DataStore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "examplanner_prefs")

object RetrofitClient {

    // ← URL de producción del backend ya desplegado
    private const val BASE_URL = "https://api.stackpe.online/"

    val TOKEN_KEY = stringPreferencesKey("access_token")
    val REFRESH_KEY = stringPreferencesKey("refresh_token")
    val NOMBRE_KEY = stringPreferencesKey("nombre")
    val EMAIL_KEY = stringPreferencesKey("email")
    val CANVAS_KEY = stringPreferencesKey("canvas_conectado")

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val authInterceptor = Interceptor { chain ->
        val token = appContext?.let { ctx ->
            runBlocking {
                ctx.dataStore.data.first()[TOKEN_KEY]
            }
        }
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ExamPlannerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExamPlannerApi::class.java)
    }
}
