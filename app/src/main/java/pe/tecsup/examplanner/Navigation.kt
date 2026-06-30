package pe.tecsup.examplanner

import kotlinx.coroutines.delay

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.graphics.graphicsLayer

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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.data.api.dataStore
import pe.tecsup.examplanner.data.repository.ExamPlannerRepository
import pe.tecsup.examplanner.ui.auth.LoginScreen
import pe.tecsup.examplanner.ui.auth.RegistroScreen
import pe.tecsup.examplanner.ui.main.MainShell

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
        // Mostrar el splash completo (mínimo 1.2s) aunque el token se lea al instante
        delay(1200)
        startDestination = if (!token.isNullOrBlank()) Routes.HOME else Routes.LOGIN
    }

    // Splash mientras lee el token
    if (startDestination == null) {
        // Animación de entrada: fade + escala suave del logo
        val animVisible = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            animVisible.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1976D2), Color(0xFF0D47A1))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.graphicsLayer {
                    alpha = animVisible.value
                    val s = 0.85f + 0.15f * animVisible.value
                    scaleX = s
                    scaleY = s
                }
            ) {
                // Logo real de la app (calendario + check)
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "ExamPlanner",
                    modifier = Modifier.size(140.dp)
                )
                Text(
                    text = "ExamPlanner",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tecsup · Semana de exámenes bajo control",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.8f),
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
                onGoToRegistro = { navController.navigate(Routes.REGISTRO) }
            )
        }

        composable(Routes.REGISTRO) {
            RegistroScreen(
                onRegistroSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            MainShell(
                onLogout = {
                    scope.launch {
                        val prefs = dataStore.data.first()
                        val refreshToken = prefs[RetrofitClient.REFRESH_KEY] ?: ""
                        ExamPlannerRepository(context).cerrarSesion(refreshToken)
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
