package pe.tecsup.examplanner.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.tecsup.examplanner.ui.academico.AcademicoScreen
import pe.tecsup.examplanner.ui.asistencias.AsistenciasScreen
import pe.tecsup.examplanner.ui.asistente.AsistenteScreen
import pe.tecsup.examplanner.ui.home.HomeScreen
import pe.tecsup.examplanner.ui.home.HomeViewModel
import pe.tecsup.examplanner.ui.horario.HorarioScreen

private val Azul = Color(0xFF1565C0)

private data class Seccion(val titulo: String, val icono: ImageVector)

private val SECCIONES = listOf(
    Seccion("Inicio", Icons.Default.Home),
    Seccion("Académico", Icons.Default.School),
    Seccion("Horario", Icons.Default.CalendarMonth),
    Seccion("Asistencia", Icons.Default.FactCheck),
    Seccion("Asistente", Icons.AutoMirrored.Filled.Chat)
)

@Composable
fun MainShell(onLogout: () -> Unit) {
    var seleccion by remember { mutableIntStateOf(0) }

    // El HomeViewModel se conserva entre cambios de pestaña
    val homeViewModel: HomeViewModel = viewModel()

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                SECCIONES.forEachIndexed { i, s ->
                    NavigationBarItem(
                        selected = seleccion == i,
                        onClick = { seleccion = i },
                        icon = { Icon(s.icono, contentDescription = s.titulo) },
                        label = { Text(s.titulo, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Azul,
                            selectedTextColor = Azul,
                            indicatorColor = Azul.copy(alpha = 0.12f),
                            unselectedIconColor = Color(0xFF9E9E9E),
                            unselectedTextColor = Color(0xFF9E9E9E)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (seleccion) {
                0 -> HomeScreen(onLogout = onLogout, viewModel = homeViewModel)
                1 -> AcademicoScreen()
                2 -> HorarioScreen()
                3 -> AsistenciasScreen()
                4 -> AsistenteScreen()
            }
        }
    }
}
