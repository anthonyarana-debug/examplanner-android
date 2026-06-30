package pe.tecsup.examplanner.ui.asistencias

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.tecsup.examplanner.data.models.BloqueCurso
import pe.tecsup.examplanner.data.models.CursoResumen

private val Azul = Color(0xFF1565C0)
private val Fondo = Color(0xFFF5F7FA)
private val Rojo = Color(0xFFC62828)
private val Verde = Color(0xFF2E7D32)
private val Naranja = Color(0xFFF57C00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciasScreen(viewModel: AsistenciasViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.mensaje, ui.error) {
        (ui.mensaje ?: ui.error)?.let { snackbar.showSnackbar(it); viewModel.limpiarMensajes() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asistencias", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Azul, titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                containerColor = Azul, contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Bloque") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Fondo)) {
            if (ui.isLoading) {
                CircularProgressIndicator(color = Azul, modifier = Modifier.align(Alignment.Center))
            } else if (ui.bloques.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📋", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Sin cursos registrados", fontWeight = FontWeight.Bold,
                        fontSize = 17.sp, color = Color(0xFF424242))
                    Text("Agrega un bloque (Teoría o Laboratorio)\ncon sus sesiones y duración",
                        fontSize = 13.sp, color = Color(0xFF757575))
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (ui.resumen.isNotEmpty()) {
                        item { Text("Resumen por curso", fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = Color(0xFF424242)) }
                        items(ui.resumen) { ResumenCard(it, ui.umbral) }
                    }
                    item {
                        Text("Registrar asistencia", fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = Color(0xFF424242),
                            modifier = Modifier.padding(top = 8.dp))
                    }
                    items(ui.bloques) { bloque ->
                        BloqueCard(
                            bloque = bloque,
                            onPresente = { viewModel.registrarHoy(bloque.id, "presente") },
                            onFalta = { viewModel.registrarHoy(bloque.id, "falta") },
                            onEliminar = { viewModel.eliminarBloque(bloque.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddBloqueDialog(
            onAdd = { curso, tipo, ses, dur -> viewModel.agregarBloque(curso, tipo, ses, dur); showAdd = false },
            onDismiss = { showAdd = false }
        )
    }
}

@Composable
private fun ResumenCard(c: CursoResumen, umbral: Double) {
    val pct = c.porcentajeInasistencia
    val color = when {
        c.riesgo -> Rojo
        pct >= umbral * 0.66 -> Naranja
        else -> Verde
    }
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (c.riesgo) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(c.curso, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = Color(0xFF212121), modifier = Modifier.weight(1f))
                Text("%.1f%%".format(pct), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            }
            Spacer(Modifier.height(8.dp))
            // Barra de 3 segmentos: verde (presente) · rojo (falta) · gris (por venir)
            run {
                val total = c.sesionesTotales.coerceAtLeast(1)
                val pPresente = c.presentes.toFloat() / total
                val pFalta = c.faltas.toFloat() / total
                val pPendiente = (1f - pPresente - pFalta).coerceAtLeast(0f)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                ) {
                    if (pPresente > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .weight(pPresente)
                                .background(Verde)
                        )
                    }
                    if (pFalta > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .weight(pFalta)
                                .background(Rojo)
                        )
                    }
                    if (pPendiente > 0f) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .weight(pPendiente)
                                .background(Color(0xFFE0E0E0))
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "%.1f h faltadas de %.1f h · límite %.0f%%".format(c.horasFalta, c.horasTotales, umbral),
                fontSize = 12.sp, color = Color(0xFF757575)
            )
            // Leyenda de la barra: presente · falta · por venir
            run {
                val porVenir = (c.sesionesTotales - c.registradas).coerceAtLeast(0)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LeyendaPunto(Verde, "${c.presentes} presente")
                    LeyendaPunto(Rojo, "${c.faltas} falta")
                    LeyendaPunto(Color(0xFFBDBDBD), "$porVenir por venir")
                }
            }
            if (c.riesgo) {
                Text("⚠ En riesgo: superaste el ${umbral.toInt()}% de faltas",
                    fontSize = 12.sp, color = Rojo, fontWeight = FontWeight.SemiBold)
            } else {
                Text("Puedes faltar hasta %.1f h más".format(c.horasMargen),
                    fontSize = 12.sp, color = Verde)
            }
        }
    }
}

@Composable
private fun BloqueCard(
    bloque: BloqueCurso,
    onPresente: () -> Unit,
    onFalta: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(bloque.curso, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF212121))
                    Text(
                        "${bloque.tipo} · ${bloque.totalSesiones} ses × ${bloque.duracionSesion}h = ${"%.0f".format(bloque.horasTotales)}h",
                        fontSize = 12.sp, color = Color(0xFF757575)
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFBDBDBD), modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onPresente,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Verde)
                ) { Icon(Icons.Default.Check, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Presente") }
                Button(
                    onClick = onFalta,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Rojo)
                ) { Icon(Icons.Default.Close, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Falta") }
            }
            Text("Registra la sesión de hoy", fontSize = 11.sp, color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun AddBloqueDialog(
    onAdd: (String, String, Int, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var curso by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Teoría") }
    var sesiones by remember { mutableStateOf("") }
    var duracion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val ses = sesiones.toIntOrNull() ?: 0
                    val dur = duracion.replace(",", ".").toDoubleOrNull() ?: 0.0
                    if (ses > 0 && dur > 0) onAdd(curso, tipo.ifBlank { "Teoría" }, ses, dur)
                },
                enabled = curso.isNotBlank() && sesiones.isNotBlank() && duracion.isNotBlank()
            ) { Text("Agregar", color = Azul, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } },
        title = { Text("Nuevo bloque de curso", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(curso, { curso = it }, label = { Text("Curso") }, singleLine = true)
                OutlinedTextField(tipo, { tipo = it }, label = { Text("Tipo (Teoría, Laboratorio, Taller…)") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(sesiones, { sesiones = it }, label = { Text("Nº sesiones") },
                        singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(duracion, { duracion = it }, label = { Text("Horas c/u") },
                        singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("Ej. 16 sesiones × 2.5 horas = 40 h totales", fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }
        }
    )
}

@Composable
private fun LeyendaPunto(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(texto, fontSize = 11.sp, color = Color(0xFF757575))
    }
}
