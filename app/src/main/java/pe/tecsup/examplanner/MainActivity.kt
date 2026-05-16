package pe.tecsup.examplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.ui.theme.ExamPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Retrofit con el contexto para el interceptor JWT
        RetrofitClient.init(this)

        enableEdgeToEdge()
        setContent {
            ExamPlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExamPlannerApp()
                }
            }
        }
    }
}
