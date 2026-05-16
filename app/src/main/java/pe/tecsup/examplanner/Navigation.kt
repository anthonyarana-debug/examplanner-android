package pe.tecsup.examplanner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.data.api.dataStore
import pe.tecsup.examplanner.ui.auth.LoginScreen
import pe.tecsup.examplanner.ui.auth.RegistroScreen
import pe.tecsup.examplanner.ui.home.HomeScreen
import pe.tecsup.examplanner.ui.home.HomeViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val HOME = "home"
}

@Composable
fun ExamPlannerApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dataStore = context.dataStore

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val prefs = dataStore.data.first()
        val token = prefs[RetrofitClient.TOKEN_KEY]
        startDestination = if (!token.isNullOrBlank()) Routes.HOME else Routes.LOGIN
    }

    // Splash screen mientras lee el token
    if (startDestination == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "📚", fontSize = 72.sp)
                Text(
                    text = "ExamPlanner",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tecsup · Semana de exámenes bajo control",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.7f),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegistro = {
                    navController.navigate(Routes.REGISTRO)
                }
            )
        }

        composable(Routes.REGISTRO) {
            RegistroScreen(
                onRegistroSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = viewModel()
            HomeScreen(
                viewModel = homeViewModel,
                onLogout = {
                    scope.launch {
                        dataStore.data.first().let { prefs ->
                            val refreshToken = prefs[RetrofitClient.REFRESH_KEY] ?: ""
                            homeViewModel.logout(refreshToken)
                        }
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}