package pe.tecsup.examplanner.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = Color(0xFF1565C0),
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    secondary        = Color(0xFF2E7D32),
    onSecondary      = Color.White,
    error            = Color(0xFFB71C1C),
    background       = Color(0xFFF5F7FA),
    surface          = Color.White,
    onBackground     = Color(0xFF212121),
    onSurface        = Color(0xFF212121)
)

@Composable
fun ExamPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme  // Forzamos light para consistencia

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color(0xFF1565C0).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
