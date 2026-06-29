package pe.tecsup.examplanner.data.api

import pe.tecsup.examplanner.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ExamPlannerApi {

    // ── AUTH ─────────────────────────────────────────────────────────────────

    @POST("api/auth/registro/")
    suspend fun registro(@Body body: RegistroRequest): Response<AuthResponse>

    @POST("api/auth/login/")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout/")
    suspend fun logout(@Body body: Map<String, String>): Response<Map<String, String>>

    // ── CANVAS ───────────────────────────────────────────────────────────────

    @POST("api/canvas/conectar/")
    suspend fun conectarCanvas(@Body body: CanvasTokenRequest): Response<CanvasConectarResponse>

    @POST("api/canvas/sincronizar/")
    suspend fun sincronizarCanvas(): Response<CanvasConectarResponse>

    // ── PENDIENTES ───────────────────────────────────────────────────────────

    @GET("api/pendientes/")
    suspend fun getPendientes(): Response<PendientesResponse>

    // ── TAREAS ───────────────────────────────────────────────────────────────

    @GET("api/tareas/")
    suspend fun getTareas(): Response<List<Tarea>>

    @POST("api/tareas/")
    suspend fun crearTarea(@Body body: TareaCreateRequest): Response<Tarea>

    @PUT("api/tareas/{id}/")
    suspend fun editarTarea(@Path("id") id: Int, @Body body: TareaCreateRequest): Response<Tarea>

    @DELETE("api/tareas/{id}/")
    suspend fun eliminarTarea(@Path("id") id: Int): Response<Unit>

    @PATCH("api/tareas/{id}/completar/")
    suspend fun completarTarea(
        @Path("id") id: Int,
        @Body body: CompletarRequest
    ): Response<CompletarResponse>

    // ── EXÁMENES ─────────────────────────────────────────────────────────────

    @GET("api/examenes/")
    suspend fun getExamenes(): Response<List<Examen>>

    @POST("api/examenes/")
    suspend fun crearExamen(@Body body: ExamenCreateRequest): Response<Examen>

    // ── ACADÉMICO: NOTAS / ANUNCIOS / MATERIALES ─────────────────────────────

    @GET("api/canvas/notas/")
    suspend fun getNotas(): Response<NotasResponse>

    @GET("api/canvas/anuncios/")
    suspend fun getAnuncios(): Response<AnunciosResponse>

    @GET("api/canvas/materiales/")
    suspend fun getMateriales(): Response<MaterialesResponse>

    // ── HORARIOS ─────────────────────────────────────────────────────────────

    @GET("api/horarios/")
    suspend fun getHorarios(): Response<List<Horario>>

    @POST("api/horarios/")
    suspend fun crearHorario(@Body body: HorarioCreateRequest): Response<Horario>

    @DELETE("api/horarios/{id}/")
    suspend fun eliminarHorario(@Path("id") id: Int): Response<Unit>

    @GET("api/horarios/proxima/")
    suspend fun getProximaClase(): Response<ProximaClaseResponse>

    // ── ASISTENCIAS ──────────────────────────────────────────────────────────

    @GET("api/asistencias/bloques/")
    suspend fun getBloques(): Response<List<BloqueCurso>>

    @POST("api/asistencias/bloques/")
    suspend fun crearBloque(@Body body: BloqueCreateRequest): Response<BloqueCurso>

    @DELETE("api/asistencias/bloques/{id}/")
    suspend fun eliminarBloque(@Path("id") id: Int): Response<Unit>

    @POST("api/asistencias/")
    suspend fun registrarAsistencia(@Body body: AsistenciaCreateRequest): Response<AsistenciaRegistro>

    @GET("api/asistencias/resumen/")
    suspend fun getResumenAsistencias(): Response<ResumenAsistenciaResponse>

    // ── ASISTENTE IA ─────────────────────────────────────────────────────────

    @POST("api/asistente/")
    suspend fun preguntarAsistente(@Body body: AsistenteRequest): Response<AsistenteResponse>
    // ── REPASAR (material de estudio) ─────────────────────────────────────────

    @POST("api/repasar/")
    suspend fun repasar(@Body body: RepasoRequest): Response<RepasoResponse>
}
