package pe.tecsup.examplanner.ui.academico

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import pe.tecsup.examplanner.data.models.*

private val Azul = Color(0xFF1565C0)
private val Fondo = Color(0xFFF5F7FA)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcademicoScreen(viewModel: AcademicoViewModel = viewModel()) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Académico", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Azul, titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).background(Fondo)) {
            TabRow(selectedTabIndex = tab, containerColor = Color.White, contentColor = Azul) {
                Tab(tab == 0, { tab = 0 }, text = { Text("Notas") })
                Tab(tab == 1, { tab = 1 }, text = { Text("Anuncios") })
                Tab(tab == 2, { tab = 2 }, text = { Text("Materiales") })
            }
            when (tab) {
                0 -> NotasTab(ui)
                1 -> AnunciosTab(ui)
                2 -> MaterialesTab(ui)
            }
        }
    }
}

// ── NOTAS ────────────────────────────────────────────────────────────────────

@Composable
private fun NotasTab(ui: AcademicoUiState) {
    when {
        ui.cargandoNotas -> Cargando()
        ui.errorNotas != null -> EstadoVacio("📊", "Notas no disponibles", ui.errorNotas)
        else -> {
            val notas = ui.notas
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Azul)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Promedio general", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                notas?.promedioGeneral?.let { "%.1f".format(it) } ?: "—",
                                color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${notas?.totalCursos ?: 0} cursos",
                                color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp
                            )
                        }
                    }
                }
                items(notas?.notas ?: emptyList()) { n -> NotaCard(n) }
            }
        }
    }
}

@Composable
private fun NotaCard(n: NotaCurso) {
    val nota = n.notaActual
    val color = when {
        nota == null -> Color(0xFF9E9E9E)
        nota >= 70 -> Color(0xFF2E7D32)
        nota >= 60 -> Color(0xFFF57C00)
        else -> Color(0xFFC62828)
    }
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(n.curso, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                color = Color(0xFF212121), modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) {
                Text(
                    nota?.let { "%.0f".format(it) } ?: "s/n",
                    color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ── ANUNCIOS ─────────────────────────────────────────────────────────────────

@Composable
private fun AnunciosTab(ui: AcademicoUiState) {
    when {
        ui.cargandoAnuncios -> Cargando()
        ui.errorAnuncios != null -> EstadoVacio("📣", "Anuncios no disponibles", ui.errorAnuncios)
        ui.anuncios.isEmpty() -> EstadoVacio("📣", "Sin anuncios", "No hay anuncios recientes.")
        else -> {
            val uri = LocalUriHandler.current
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ui.anuncios) { a ->
                    Card(
                        Modifier.fillMaxWidth().clickable { a.url?.let { uri.openUri(it) } },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text(a.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF212121))
                            Spacer(Modifier.height(2.dp))
                            Row {
                                Surface(color = Azul.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                                    Text(a.curso, color = Azul, fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                }
                                a.fecha?.take(10)?.let {
                                    Spacer(Modifier.width(8.dp))
                                    Text(it, color = Color(0xFF9E9E9E), fontSize = 11.sp,
                                        modifier = Modifier.align(Alignment.CenterVertically))
                                }
                            }
                            a.mensaje?.takeIf { it.isNotBlank() }?.let {
                                Spacer(Modifier.height(6.dp))
                                Text(it.take(180) + if (it.length > 180) "…" else "",
                                    fontSize = 13.sp, color = Color(0xFF616161))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── MATERIALES ───────────────────────────────────────────────────────────────

@Composable
private fun MaterialesTab(ui: AcademicoUiState) {
    when {
        ui.cargandoMateriales -> Cargando()
        ui.errorMateriales != null -> EstadoVacio("📚", "Materiales no disponibles", ui.errorMateriales)
        ui.materiales.isEmpty() -> EstadoVacio("📚", "Sin materiales", "No hay módulos publicados.")
        else -> {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ui.materiales) { curso ->
                    CursoMaterialCard(curso)
                }
            }
        }
    }
}

@Composable
private fun CursoMaterialCard(curso: CursoMateriales) {
    val uri = LocalUriHandler.current
    var expandido by remember { mutableStateOf(false) }
    val rotacion by animateFloatAsState(if (expandido) 180f else 0f, label = "rot")

    // Contar items totales del curso para mostrar en el resumen
    val totalItems = curso.modulos.sumOf { it.items.size }

    Card(
        Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Cabecera clickable
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expandido = !expandido }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Azul.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📚", fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(curso.curso, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Azul)
                    Text(
                        "${curso.modulos.size} semanas · $totalItems recursos",
                        fontSize = 12.sp, color = Color(0xFF9E9E9E)
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = if (expandido) "Contraer" else "Expandir",
                    tint = Azul,
                    modifier = Modifier.rotate(rotacion)
                )
            }

            // Contenido colapsable
            AnimatedVisibility(visible = expandido) {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
                    curso.modulos.forEach { modulo ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            modulo.nombre,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color(0xFF424242),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        modulo.items.forEach { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { item.url?.let { uri.openUri(it) } }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(iconoTipo(item.tipo), fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    item.titulo, fontSize = 13.sp, color = Color(0xFF616161),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Default.OpenInNew, null,
                                    tint = Color(0xFFBDBDBD), modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun iconoTipo(tipo: String?): String = when (tipo) {
    "File" -> "📄"; "Page" -> "📃"; "ExternalUrl" -> "🔗"
    "Discussion" -> "💬"; "Assignment" -> "📝"; "Quiz" -> "❓"; else -> "•"
}

// ── Comunes ──────────────────────────────────────────────────────────────────

@Composable
private fun Cargando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Azul)
    }
}

@Composable
private fun EstadoVacio(emoji: String, titulo: String, subtitulo: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(emoji, fontSize = 52.sp)
            Spacer(Modifier.height(12.dp))
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF424242))
            subtitulo?.let {
                Spacer(Modifier.height(6.dp))
                Text(it, fontSize = 13.sp, color = Color(0xFF757575))
            }
        }
    }
}
