package pe.tecsup.examplanner.ui.asistente

import androidx.compose.foundation.background
import pe.tecsup.examplanner.ui.theme.AppColors
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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

private val Azul = Color(0xFF1565C0)
private val Fondo = Color(0xFFF5F7FA)

private val SUGERENCIAS = listOf(
    "¿Qué priorizo esta semana?",
    "¿En qué curso estoy en riesgo de faltas?",
    "Organiza mis pendientes por urgencia"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenteScreen(viewModel: AsistenteViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var texto by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(ui.mensajes.size) {
        if (ui.mensajes.isNotEmpty()) listState.animateScrollToItem(ui.mensajes.size - 1)
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.background(AppColors.HeaderGradient)) {
                TopAppBar(
                    title = {
                        Column {
                            Text("Asistente IA", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Organización académica", fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent, titleContentColor = Color.White
                    )
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(Fondo).imePadding()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(ui.mensajes) { _, m -> Burbuja(m.texto, m.esUsuario) }
                if (ui.pensando) item { Burbuja("Pensando…", esUsuario = false, atenuado = true) }

                if (ui.mensajes.size <= 1) {
                    item {
                        Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Prueba con:", fontSize = 12.sp, color = Color(0xFF757575))
                            SUGERENCIAS.forEach { s ->
                                SuggestionChip(
                                    onClick = { viewModel.enviar(s) },
                                    label = { Text(s, fontSize = 12.sp) }
                                )
                            }
                        }
                    }
                }
            }

            Surface(color = Color.White, modifier = Modifier.border(BorderStroke(0.5.dp, Color(0xFFEEEEEE)))) {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { texto = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe tu pregunta…", color = Color(0xFF9CA3AF)) },
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Azul,
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Azul
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { viewModel.enviar(texto); texto = "" },
                        enabled = texto.isNotBlank() && !ui.pensando,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Azul)
                    ) {
                        Icon(Icons.Default.Send, "Enviar", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun Burbuja(texto: String, esUsuario: Boolean, atenuado: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (esUsuario) Azul else Color.White,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (esUsuario) 16.dp else 4.dp,
                bottomEnd = if (esUsuario) 4.dp else 16.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                texto,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (esUsuario) Color.White else Color(0xFF212121).copy(alpha = if (atenuado) 0.5f else 1f),
                fontSize = 14.sp
            )
        }
    }
}
