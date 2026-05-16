package pe.tecsup.examplanner.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.examplanner.data.models.Tarea
import java.util.Calendar
import java.util.Locale

// ── Dialog para conectar Canvas ───────────────────────────────────────────────

@Composable
fun CanvasDialog(
    onConnect: (String) -> Unit,
    onDismiss: () -> Unit,
    mensaje: String?,
    isLoading: Boolean
) {
    var token by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "🔗 Conectar Canvas",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Cómo obtener tu token:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                text = "1. Entra a canvas.tecsup.edu.pe\n2. Ve a Cuenta → Configuración\n3. Genera un Token de Acceso\n4. Cópialo y pégalo aquí",
                                fontSize = 12.sp,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("Token personal de Canvas") },
                    placeholder = { Text("Pega aquí tu token...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4
                )

                AnimatedVisibility(visible = !mensaje.isNullOrBlank()) {
                    Text(
                        text = mensaje ?: "",
                        color = if (mensaje?.startsWith("✅") == true)
                            Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConnect(token.trim()) },
                enabled = !isLoading && token.isNotBlank(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Conectar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF757575))
            }
        }
    )
}

// ── Dialog para agregar O editar tarea manual ─────────────────────────────────

@Composable
fun AddTareaDialog(
    tareaInicial: Tarea? = null,
    onAdd: (String, String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val modoEdicion = tareaInicial != null

    var nombre by remember { mutableStateOf(tareaInicial?.nombre ?: "") }
    var curso  by remember { mutableStateOf(tareaInicial?.curso  ?: "") }
    var fecha  by remember { mutableStateOf(
        tareaInicial?.fechaLimite
            ?.removeSuffix("-05:00")
            ?.replace("T", " ")
            ?.take(16)
            ?: ""
    ) }
    var descripcion by remember { mutableStateOf(tareaInicial?.descripcion ?: "") }
    var error by remember { mutableStateOf("") }

    // --- CONFIGURACIÓN DE CALENDARIO E HORA (INTUITIVO) ---
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val horaFormat = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            val soloFecha = if (fecha.contains(" ")) fecha.substringBefore(" ") else fecha
            fecha = "$soloFecha $horaFormat"
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val mesFormat = String.format(Locale.getDefault(), "%02d", month + 1)
            val diaFormat = String.format(Locale.getDefault(), "%02d", dayOfMonth)
            fecha = "$year-$mesFormat-$diaFormat "
            timePickerDialog.show() // Abre el reloj apenas se elige el día
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    // ------------------------------------------------------

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = if (modoEdicion) "✏ Editar tarea" else "➕ Agregar tarea",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la tarea *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                OutlinedTextField(
                    value = curso,
                    onValueChange = { curso = it },
                    label = { Text("Curso *") },
                    placeholder = { Text("Ej: Construcción de Software") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // CAMBIO AQUÍ: Campo de fecha de solo lectura con botón de calendario
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { },
                    readOnly = true, // Bloquea escritura manual
                    label = { Text("Fecha límite *") },
                    placeholder = { Text("Toca el calendario ➔") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = Color(0xFF1565C0))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )
                AnimatedVisibility(visible = error.isNotBlank()) {
                    Text(text = error, color = Color(0xFFC62828), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || curso.isBlank() || fecha.isBlank()) {
                        error = "Nombre, curso y fecha son obligatorios"
                    } else {
                        val fechaFormateada = if (!fecha.contains("T") && !fecha.contains("+")) {
                            "${fecha.trim()}:00-05:00"
                        } else fecha
                        onAdd(nombre, curso, fechaFormateada, descripcion.ifBlank { null })
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
            ) {
                Text(if (modoEdicion) "Guardar" else "Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF757575))
            }
        }
    )
}

// ── Dialog para agregar examen manual ────────────────────────────────────────

@Composable
fun AddExamenDialog(
    onAdd: (String, String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var curso by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    // --- CONFIGURACIÓN DE CALENDARIO E HORA (INTUITIVO) ---
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val horaFormat = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
            val soloFecha = if (fecha.contains(" ")) fecha.substringBefore(" ") else fecha
            fecha = "$soloFecha $horaFormat"
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val mesFormat = String.format(Locale.getDefault(), "%02d", month + 1)
            val diaFormat = String.format(Locale.getDefault(), "%02d", dayOfMonth)
            fecha = "$year-$mesFormat-$diaFormat "
            timePickerDialog.show() // Abre el reloj apenas se elige el día
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    // ------------------------------------------------------

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text(
                text = "📝 Registrar examen",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = curso,
                    onValueChange = { curso = it },
                    label = { Text("Curso *") },
                    placeholder = { Text("Ej: Base de Datos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // CAMBIO AQUÍ: Campo de fecha de solo lectura con botón de calendario
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { },
                    readOnly = true, // Bloquea escritura manual
                    label = { Text("Fecha del examen *") },
                    placeholder = { Text("Toca el calendario ➔") },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", tint = Color(0xFFE53935))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción / Temas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "⚠ Si la fecha ya pasó, el sistema te avisará antes de confirmar",
                        fontSize = 12.sp,
                        color = Color(0xFFF57C00),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                AnimatedVisibility(visible = error.isNotBlank()) {
                    Text(text = error, color = Color(0xFFC62828), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (curso.isBlank() || fecha.isBlank()) {
                        error = "Curso y fecha son obligatorios"
                    } else {
                        val fechaFormateada = if (!fecha.contains("T") && !fecha.contains("+")) {
                            "${fecha.trim()}:00-05:00"
                        } else fecha
                        onAdd(curso, fechaFormateada, descripcion.ifBlank { null })
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Text("Registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF757575))
            }
        }
    )
}