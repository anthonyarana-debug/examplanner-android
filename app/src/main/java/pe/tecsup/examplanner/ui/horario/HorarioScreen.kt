package pe.tecsup.examplanner.ui.horario

import androidx.compose.foundation.background
import pe.tecsup.examplanner.ui.theme.AppColors
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.tecsup.examplanner.data.models.Horario

private val Azul = Color(0xFF1565C0)
private val Fondo = Color(0xFFF5F7FA)
private val DIAS = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorarioScreen(viewModel: HorarioViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.mensaje, ui.error) {
        (ui.mensaje ?: ui.error)?.let { snackbar.showSnackbar(it); viewModel.limpiarMensajes() }
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(AppColors.HeaderGradient)) {
                TopAppBar(
                    title = { Text("Horario", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, titleContentColor = Color.White
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = Azul) {
                Icon(Icons.Default.Add, "Agregar clase", tint = Color.White)
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).background(Fondo)) {
            if (ui.isLoading) {
                CircularProgressIndicator(color = Azul, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { ProximaClaseCard(ui.proxima) }

                    if (ui.clases.isEmpty()) {
                        item {
                            Column(
                                Modifier.fillMaxWidth().padding(top = 48.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🗓️", fontSize = 52.sp)
                                Spacer(Modifier.height(12.dp))
                                Text("Aún no tienes clases", fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp, color = Color(0xFF424242))
                                Text("Toca + para agregar tu horario", fontSize = 13.sp, color = Color(0xFF757575))
                            }
                        }
                    } else {
                        val porDia = ui.clases.groupBy { it.dia }.toSortedMap()
                        porDia.forEach { (dia, clases) ->
                            item {
                                Text(
                                    DIAS.getOrElse(dia) { "Día" },
                                    fontWeight = FontWeight.Bold, fontSize = 14.sp,
                                    color = Azul, modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            items(clases.sortedBy { it.horaInicio }) { c ->
                                ClaseCard(c, onEliminar = { viewModel.eliminarClase(c.id) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddClaseDialog(
            onAdd = { curso, codigo, aula, dia, ini, fin ->
                viewModel.agregarClase(curso, codigo, aula, dia, ini, fin); showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }
}

@Composable
private fun ProximaClaseCard(prox: pe.tecsup.examplanner.data.models.ProximaClaseResponse?) {
    val clase = prox?.clase
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (prox?.enCurso == true) Color(0xFF2E7D32) else Azul
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                if (prox?.enCurso == true) "Clase en curso" else "Próxima clase",
                color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp
            )
            Spacer(Modifier.height(4.dp))
            if (clase == null) {
                Text("Sin clases registradas", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            } else {
                Text(clase.curso, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${clase.diaNombre} · ${clase.horaInicio.take(5)}–${clase.horaFin.take(5)}" +
                        if (clase.aula.isNotBlank()) " · ${clase.aula}" else "",
                    color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp
                )
                prox?.minutosParaInicio?.let { min ->
                    if (prox.enCurso != true) {
                        Spacer(Modifier.height(4.dp))
                        Text("Empieza en ${formatoMinutos(min)}",
                            color = Color.White.copy(alpha = 0.95f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun formatoMinutos(min: Int): String {
    val dias = min / 1440
    val horas = (min % 1440) / 60
    val mins = min % 60
    return when {
        dias > 0 -> "$dias d ${horas} h"
        horas > 0 -> "$horas h $mins min"
        else -> "$mins min"
    }
}

@Composable
private fun ClaseCard(c: Horario, onEliminar: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(c.curso, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF212121))
                Spacer(Modifier.height(2.dp))
                Text(
                    "${c.horaInicio.take(5)}–${c.horaFin.take(5)}" +
                        (if (c.aula.isNotBlank()) " · Aula ${c.aula}" else "") +
                        (if (c.codigo.isNotBlank()) " · ${c.codigo}" else ""),
                    fontSize = 12.sp, color = Color(0xFF757575)
                )
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFF9E9E9E), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddClaseDialog(
    onAdd: (String, String, String, Int, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var curso by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var aula by remember { mutableStateOf("") }
    var dia by remember { mutableIntStateOf(0) }
    var ini by remember { mutableStateOf("") }
    var fin by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onAdd(curso, codigo, aula, dia, ini, fin) },
                enabled = curso.isNotBlank() && ini.isNotBlank() && fin.isNotBlank()
            ) { Text("Agregar", color = Azul, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Gray) } },
        title = { Text("Nueva clase", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(curso, { curso = it }, label = { Text("Curso") }, singleLine = true)
                OutlinedTextField(codigo, { codigo = it }, label = { Text("Código (opcional)") }, singleLine = true)
                OutlinedTextField(aula, { aula = it }, label = { Text("Aula (opcional)") }, singleLine = true)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = DIAS[dia], onValueChange = {}, readOnly = true,
                        label = { Text("Día") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DIAS.forEachIndexed { i, nombre ->
                            DropdownMenuItem(text = { Text(nombre) }, onClick = { dia = i; expanded = false })
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(ini, { ini = it }, label = { Text("Inicio HH:MM") },
                        singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(fin, { fin = it }, label = { Text("Fin HH:MM") },
                        singleLine = true, modifier = Modifier.weight(1f))
                }
                Text("Ej. 15:30 y 18:00", fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }
        }
    )
}
