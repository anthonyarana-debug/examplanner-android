package pe.tecsup.examplanner.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import pe.tecsup.examplanner.data.models.Examen
import pe.tecsup.examplanner.data.models.Tarea
import pe.tecsup.examplanner.ui.theme.AppColors
import pe.tecsup.examplanner.ui.theme.entradaAnimada

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val nombre by viewModel.nombreUsuario.collectAsStateWithLifecycle(initialValue = "")
    val canvasConectado by viewModel.canvasConectado.collectAsStateWithLifecycle(initialValue = false)

    var showCanvasDialog by remember { mutableStateOf(false) }
    var showAddTareaDialog by remember { mutableStateOf(false) }
    var showAddExamenDialog by remember { mutableStateOf(false) }
    var tareaAEditar by remember { mutableStateOf<Tarea?>(null) }   // ← NUEVO
    var repasoTema by remember { mutableStateOf<Pair<String, String>?>(null) }   // titulo, curso
    var selectedTab by remember { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.accionExitosa) {
        uiState.accionExitosa?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
          Box(modifier = Modifier.background(AppColors.HeaderGradient)) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ExamPlanner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Hola, ${nombre.split(" ").first()} 👋",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (canvasConectado) viewModel.sincronizarCanvas()
                        else showCanvasDialog = true
                    }) {
                        Icon(
                            imageVector = if (canvasConectado) Icons.Default.Sync else Icons.Default.Link,
                            contentDescription = if (canvasConectado) "Sincronizar Canvas" else "Conectar Canvas",
                            tint = if (canvasConectado) Color(0xFF81C784) else Color.White
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
          }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { showAddExamenDialog = true },
                    containerColor = Color(0xFFE53935)
                ) {
                    Icon(Icons.Default.Event, contentDescription = "Agregar examen", tint = Color.White)
                }
                FloatingActionButton(
                    onClick = { showAddTareaDialog = true },
                    containerColor = Color(0xFF1565C0)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar tarea", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(visible = !canvasConectado) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Conecta Canvas para importar tus tareas automáticamente",
                            fontSize = 13.sp,
                            color = Color(0xFFF57F17),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showCanvasDialog = true }) {
                            Text("Conectar", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                        }
                    }
                }
            }

            uiState.pendientes?.let { data ->
                ProgressCard(
                    progreso = data.progresoPorcentaje.toFloat(),
                    totalPendientes = data.totalPendientes,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF1565C0)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        val count = uiState.pendientes?.tareas?.size ?: 0
                        Text("Tareas${if (count > 0) " ($count)" else ""}")
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        val count = uiState.pendientes?.examenes?.size ?: 0
                        Text("Exámenes${if (count > 0) " ($count)" else ""}")
                    }
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
            } else {
                when (selectedTab) {
                    0 -> TareasTab(
                        tareas = uiState.pendientes?.tareas ?: emptyList(),
                        mensaje = uiState.pendientes?.mensaje,
                        onCompletar = { id, completada -> viewModel.marcarCompletada(id, completada) },
                        onEliminar = { id -> viewModel.eliminarTarea(id) },
                        onEditar = { tarea -> tareaAEditar = tarea },   // ← NUEVO
                        onRepasar = { titulo, curso -> repasoTema = titulo to curso }
                    )
                    1 -> ExamenesTab(
                        examenes = uiState.pendientes?.examenes ?: emptyList(),
                        mensaje = uiState.pendientes?.mensaje,
                        onRepasar = { titulo, curso -> repasoTema = titulo to curso }
                    )
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (showCanvasDialog) {
        CanvasDialog(
            onConnect = { token ->
                viewModel.conectarCanvas(token)
                showCanvasDialog = false
            },
            onDismiss = { showCanvasDialog = false },
            mensaje = uiState.canvasMensaje,
            isLoading = uiState.canvasConectando
        )
    }

    if (showAddTareaDialog) {
        AddTareaDialog(
            onAdd = { nombre, curso, fecha, desc ->
                viewModel.crearTarea(nombre, curso, fecha, desc)
                showAddTareaDialog = false
            },
            onDismiss = { showAddTareaDialog = false }
        )
    }

    if (showAddExamenDialog) {
        AddExamenDialog(
            onAdd = { curso, fecha, desc ->
                viewModel.crearExamen(curso, fecha, desc)
                showAddExamenDialog = false
            },
            onDismiss = { showAddExamenDialog = false }
        )
    }

    // Dialog de edición — reutiliza AddTareaDialog con datos precargados   ← NUEVO
    tareaAEditar?.let { tarea ->
        AddTareaDialog(
            tareaInicial = tarea,
            onAdd = { nombre, curso, fecha, desc ->
                viewModel.editarTarea(tarea.id, nombre, curso, fecha, desc)
                tareaAEditar = null
            },
            onDismiss = { tareaAEditar = null }
        )
    }

    // Dialog de Repaso (material de estudio por IA)
    repasoTema?.let { (titulo, curso) ->
        RepasoDialog(
            titulo = titulo,
            curso = curso,
            onDismiss = { repasoTema = null }
        )
    }
}

// ── Tarjeta de progreso ───────────────────────────────────────────────────────

@Composable
fun ProgressCard(progreso: Float, totalPendientes: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progreso semanal",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1565C0),
                    fontSize = 14.sp
                )
                Text(
                    text = "${progreso.toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progreso / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF1565C0),
                trackColor = Color(0xFFBBDEFB)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$totalPendientes pendiente${if (totalPendientes != 1) "s" else ""}",
                fontSize = 12.sp,
                color = Color(0xFF1565C0).copy(alpha = 0.7f)
            )
        }
    }
}

// ── Tab de Tareas ─────────────────────────────────────────────────────────────

@Composable
fun TareasTab(
    tareas: List<Tarea>,
    mensaje: String?,
    onCompletar: (Int, Boolean) -> Unit,
    onEliminar: (Int) -> Unit,
    onEditar: (Tarea) -> Unit,      // ← NUEVO
    onRepasar: (String, String) -> Unit
) {
    if (tareas.isEmpty()) {
        EmptyState(
            emoji = "✅",
            titulo = "Sin tareas pendientes",
            subtitulo = mensaje ?: "¡Buen trabajo!"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(tareas, key = { _, it -> it.id }) { index, tarea ->
              Box(Modifier.entradaAnimada(index)) {
                TareaCard(
                    tarea = tarea,
                    onCompletar = { onCompletar(tarea.id, true) },
                    onEliminar = { onEliminar(tarea.id) },
                    onEditar = { onEditar(tarea) },    // ← NUEVO
                    onRepasar = { onRepasar(tarea.nombre, tarea.curso) }
                )
              }
            }
        }
    }
}

@Composable
fun TareaCard(
    tarea: Tarea,
    onCompletar: () -> Unit,
    onEliminar: () -> Unit,
    onEditar: () -> Unit,           // ← NUEVO
    onRepasar: () -> Unit
) {
    val urgente = tarea.diasRestantes <= 1
    val pronto = tarea.diasRestantes in 2..3

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                tarea.estaVencida -> Color(0xFFFFEBEE)
                urgente           -> Color(0xFFFFF3E0)
                else              -> Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = tarea.completada,
                onCheckedChange = { if (!tarea.completada) onCompletar() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1565C0))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = tarea.nombre,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color(0xFF212121)
                )
                Text(
                    text = tarea.curso,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (badgeColor, badgeText) = when {
                        tarea.estaVencida -> Color(0xFFC62828) to "⚠ Vencida"
                        urgente           -> Color(0xFFE53935) to "⚡ Hoy"
                        pronto            -> Color(0xFFF57C00) to "🕐 ${tarea.diasRestantes} días"
                        else              -> Color(0xFF388E3C) to "📅 ${tarea.diasRestantes} días"
                    }
                    Surface(
                        color = badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (tarea.origen == "canvas") {
                        Surface(
                            color = Color(0xFF1565C0).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Canvas",
                                color = Color(0xFF1565C0),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Botón Repasar — para todas las tareas
            IconButton(onClick = onRepasar) {
                Icon(
                    Icons.Default.MenuBook,
                    contentDescription = "Repasar",
                    tint = Color(0xFF1565C0),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Botones editar y eliminar — solo tareas manuales   ← NUEVO
            if (tarea.origen == "manual") {
                IconButton(onClick = onEditar) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Tab de Exámenes ───────────────────────────────────────────────────────────

@Composable
fun ExamenesTab(
    examenes: List<Examen>,
    mensaje: String?,
    onRepasar: (String, String) -> Unit
) {
    if (examenes.isEmpty()) {
        EmptyState(
            emoji = "📝",
            titulo = "Sin exámenes próximos",
            subtitulo = mensaje ?: "Agrega tus exámenes para no olvidarlos"
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(examenes, key = { _, it -> it.id }) { index, examen ->
              Box(Modifier.entradaAnimada(index)) {
                ExamenCard(
                    examen = examen,
                    onRepasar = { onRepasar(examen.descripcion?.takeIf { it.isNotBlank() } ?: "Examen de ${examen.curso}", examen.curso) }
                )
              }
            }
        }
    }
}

@Composable
fun ExamenCard(examen: Examen, onRepasar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (examen.proximo) Color(0xFFFFEBEE) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (examen.proximo) Color(0xFFC62828) else Color(0xFF1565C0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (examen.proximo) "⚡" else "📝",
                    fontSize = 20.sp
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "Examen: ${examen.curso}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (examen.proximo) {
                    Text(
                        text = "¡En menos de 48 horas!",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                } else {
                    Text(
                        text = "Faltan ${examen.diasRestantes} días",
                        color = Color(0xFF757575),
                        fontSize = 13.sp
                    )
                }
                examen.descripcion?.takeIf { it.isNotBlank() }?.let {
                    Text(text = it, fontSize = 12.sp, color = Color(0xFF9E9E9E))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                if (examen.origen == "canvas") {
                    Surface(
                        color = Color(0xFF1565C0).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "Canvas",
                            color = Color(0xFF1565C0),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                }
                IconButton(onClick = onRepasar) {
                    Icon(
                        Icons.Default.MenuBook,
                        contentDescription = "Repasar",
                        tint = Color(0xFF1565C0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────

@Composable
fun EmptyState(emoji: String, titulo: String, subtitulo: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = emoji, fontSize = 56.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF424242)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = subtitulo,
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
        }
    }
}