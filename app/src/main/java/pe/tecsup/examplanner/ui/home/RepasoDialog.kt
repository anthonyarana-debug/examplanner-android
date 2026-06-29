package pe.tecsup.examplanner.ui.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.data.models.RepasoEnlace
import pe.tecsup.examplanner.data.models.RepasoRequest
import pe.tecsup.examplanner.data.models.RepasoResponse

/**
 * Diálogo que pide a la IA un plan de repaso para una tarea/examen
 * y muestra resumen + conceptos + enlaces de búsqueda reales.
 */
@Composable
fun RepasoDialog(
    titulo: String,
    curso: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var repaso by remember { mutableStateOf<RepasoResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(titulo, curso) {
        isLoading = true
        error = null
        try {
            val resp = RetrofitClient.api.repasar(RepasoRequest(titulo = titulo, curso = curso))
            if (resp.isSuccessful) {
                repaso = resp.body()
                if (repaso?.error != null) error = repaso?.error
            } else {
                error = "No se pudo generar el repaso (${resp.code()})"
            }
        } catch (e: Exception) {
            error = "Error de conexión. Revisa tu internet."
        }
        isLoading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Encabezado
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = Color(0xFF1565C0)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Repasar",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Text(
                    text = titulo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color(0xFF424242)
                )
                Spacer(Modifier.height(12.dp))

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color(0xFF1565C0))
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Preparando tu repaso...",
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }

                    error != null -> {
                        Text(
                            text = error ?: "",
                            color = Color(0xFFC62828),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    repaso != null -> {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .weight(1f, fill = false)
                        ) {
                            // Resumen
                            repaso?.resumen?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = it,
                                    fontSize = 14.sp,
                                    color = Color(0xFF424242)
                                )
                                Spacer(Modifier.height(16.dp))
                            }

                            // Conceptos a repasar
                            if (repaso?.conceptos?.isNotEmpty() == true) {
                                Text(
                                    text = "Qué repasar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1565C0)
                                )
                                Spacer(Modifier.height(6.dp))
                                repaso?.conceptos?.forEach { concepto ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text("• ", color = Color(0xFF1565C0), fontSize = 14.sp)
                                        Text(
                                            text = concepto,
                                            fontSize = 14.sp,
                                            color = Color(0xFF424242)
                                        )
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                            }

                            // Enlaces de búsqueda
                            Text(
                                text = "Buscar material",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1565C0)
                            )
                            Spacer(Modifier.height(8.dp))
                            repaso?.enlaces?.forEach { enlace ->
                                EnlaceBoton(enlace) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(enlace.url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnlaceBoton(enlace: RepasoEnlace, onClick: () -> Unit) {
    val (icono, color) = when (enlace.tipo) {
        "YouTube" -> Icons.Default.PlayCircle to Color(0xFFE53935)
        "Google" -> Icons.Default.Search to Color(0xFF1565C0)
        else -> Icons.Default.School to Color(0xFF00897B)
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = color)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = enlace.tipo,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = color
                )
                Text(
                    text = enlace.descripcion,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
            Icon(
                Icons.Default.OpenInNew,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
