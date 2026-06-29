package pe.tecsup.examplanner

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.notificaciones.NotificacionesHelper
import pe.tecsup.examplanner.ui.theme.ExamPlannerTheme

class MainActivity : ComponentActivity() {

    // Lanzador para pedir el permiso de notificaciones (Android 13+)
    private val permisoNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* concedido o no, la app sigue funcionando igual */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar Retrofit con el contexto para el interceptor JWT
        RetrofitClient.init(this)

        // Crear canal de notificaciones y pedir permiso si hace falta
        NotificacionesHelper.crearCanal(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

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
