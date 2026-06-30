package pe.tecsup.examplanner.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Paleta central de ExamPlanner. Un solo lugar para todos los colores,
 * para mantener consistencia visual en toda la app.
 */
object AppColors {
    // Azules (marca)
    val Primary = Color(0xFF1565C0)
    val PrimaryDark = Color(0xFF0D47A1)
    val PrimaryLight = Color(0xFF42A5F5)
    val PrimaryContainer = Color(0xFFD6E4FF)

    // Acentos
    val Accent = Color(0xFFFFC107)      // dorado (del icono)
    val Success = Color(0xFF2E7D32)
    val SuccessLight = Color(0xFFE8F5E9)
    val Warning = Color(0xFFF57C00)
    val WarningLight = Color(0xFFFFF3E0)
    val Danger = Color(0xFFC62828)
    val DangerLight = Color(0xFFFFEBEE)

    // Neutros
    val Background = Color(0xFFF5F7FA)
    val Surface = Color.White
    val TextPrimary = Color(0xFF1A1C1E)
    val TextSecondary = Color(0xFF6B7280)
    val TextTertiary = Color(0xFF9CA3AF)
    val Divider = Color(0xFFE5E7EB)

    // Gradientes
    val HeaderGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1976D2), Color(0xFF0D47A1))
    )
    val PrimaryGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF2196F3), Color(0xFF1565C0))
    )
}
