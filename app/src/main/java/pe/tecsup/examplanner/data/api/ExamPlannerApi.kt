package pe.tecsup.examplanner.data.api

import pe.tecsup.examplanner.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ExamPlannerApi {

    // ── AUTH ─────────────────────────────────────────────────────────────────

    /** POST /api/auth/registro/ */
    @POST("api/auth/registro/")
    suspend fun registro(@Body body: RegistroRequest): Response<AuthResponse>

    /** POST /api/auth/login/ */
    @POST("api/auth/login/")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    /** POST /api/auth/logout/ */
    @POST("api/auth/logout/")
    suspend fun logout(@Body body: Map<String, String>): Response<Map<String, String>>

    // ── CANVAS ───────────────────────────────────────────────────────────────

    /** POST /api/canvas/conectar/  (token personal para desarrollo) */
    @POST("api/canvas/conectar/")
    suspend fun conectarCanvas(@Body body: CanvasTokenRequest): Response<CanvasConectarResponse>

    /** POST /api/canvas/sincronizar/ */
    @POST("api/canvas/sincronizar/")
    suspend fun sincronizarCanvas(): Response<CanvasConectarResponse>

    // ── PENDIENTES ───────────────────────────────────────────────────────────

    /** GET /api/pendientes/ */
    @GET("api/pendientes/")
    suspend fun getPendientes(): Response<PendientesResponse>

    // ── TAREAS ───────────────────────────────────────────────────────────────

    /** GET /api/tareas/ */
    @GET("api/tareas/")
    suspend fun getTareas(): Response<List<Tarea>>

    /** POST /api/tareas/ */
    @POST("api/tareas/")
    suspend fun crearTarea(@Body body: TareaCreateRequest): Response<Tarea>

    /** PUT /api/tareas/{id}/ */
    @PUT("api/tareas/{id}/")
    suspend fun editarTarea(@Path("id") id: Int, @Body body: TareaCreateRequest): Response<Tarea>

    /** DELETE /api/tareas/{id}/ */
    @DELETE("api/tareas/{id}/")
    suspend fun eliminarTarea(@Path("id") id: Int): Response<Unit>

    /** PATCH /api/tareas/{id}/completar/ */
    @PATCH("api/tareas/{id}/completar/")
    suspend fun completarTarea(
        @Path("id") id: Int,
        @Body body: CompletarRequest
    ): Response<CompletarResponse>

    // ── EXÁMENES ─────────────────────────────────────────────────────────────

    /** GET /api/examenes/ */
    @GET("api/examenes/")
    suspend fun getExamenes(): Response<List<Examen>>

    /** POST /api/examenes/ */
    @POST("api/examenes/")
    suspend fun crearExamen(@Body body: ExamenCreateRequest): Response<Examen>
}
