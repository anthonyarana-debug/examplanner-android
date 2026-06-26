package pe.tecsup.examplanner.data.repository

import android.content.Context
import pe.tecsup.examplanner.data.api.RetrofitClient
import pe.tecsup.examplanner.data.models.*

/**
 * Repositorio de las funcionalidades nuevas (notas, anuncios, materiales,
 * horarios, asistencias, asistente IA). Reutiliza el mismo Retrofit/token
 * y el sealed class Result del repositorio principal.
 */
class ExtraRepository(context: Context) {

    private val api = RetrofitClient.api

    private fun <T> ok(body: T?): Result<T> =
        if (body != null) Result.Success(body) else Result.Error("Respuesta vacía del servidor.")

    // ── ACADÉMICO ────────────────────────────────────────────────────────────

    suspend fun getNotas(): Result<NotasResponse> = try {
        val r = api.getNotas()
        if (r.isSuccessful) ok(r.body())
        else if (r.code() == 400) Result.Error("Conecta Canvas para ver tus notas.")
        else Result.Error("No se pudieron cargar las notas.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun getAnuncios(): Result<AnunciosResponse> = try {
        val r = api.getAnuncios()
        if (r.isSuccessful) ok(r.body())
        else if (r.code() == 400) Result.Error("Conecta Canvas para ver los anuncios.")
        else Result.Error("No se pudieron cargar los anuncios.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun getMateriales(): Result<MaterialesResponse> = try {
        val r = api.getMateriales()
        if (r.isSuccessful) ok(r.body())
        else if (r.code() == 400) Result.Error("Conecta Canvas para ver los materiales.")
        else Result.Error("No se pudieron cargar los materiales.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    // ── HORARIOS ─────────────────────────────────────────────────────────────

    suspend fun getHorarios(): Result<List<Horario>> = try {
        val r = api.getHorarios()
        if (r.isSuccessful) ok(r.body()) else Result.Error("No se pudo cargar el horario.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun getProximaClase(): Result<ProximaClaseResponse> = try {
        val r = api.getProximaClase()
        if (r.isSuccessful) ok(r.body()) else Result.Error("No se pudo cargar la próxima clase.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun crearHorario(req: HorarioCreateRequest): Result<Horario> = try {
        val r = api.crearHorario(req)
        if (r.isSuccessful) ok(r.body()) else Result.Error("No se pudo guardar la clase.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun eliminarHorario(id: Int): Result<Unit> = try {
        val r = api.eliminarHorario(id)
        if (r.isSuccessful || r.code() == 204) Result.Success(Unit)
        else Result.Error("No se pudo eliminar la clase.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    // ── ASISTENCIAS ──────────────────────────────────────────────────────────

    suspend fun getBloques(): Result<List<BloqueCurso>> = try {
        val r = api.getBloques()
        if (r.isSuccessful) ok(r.body()) else Result.Error("No se pudieron cargar los bloques.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun getResumenAsistencias(): Result<ResumenAsistenciaResponse> = try {
        val r = api.getResumenAsistencias()
        if (r.isSuccessful) ok(r.body()) else Result.Error("No se pudo cargar el resumen.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun crearBloque(req: BloqueCreateRequest): Result<BloqueCurso> = try {
        val r = api.crearBloque(req)
        if (r.isSuccessful) ok(r.body()) else Result.Error("No se pudo crear el bloque.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun eliminarBloque(id: Int): Result<Unit> = try {
        val r = api.eliminarBloque(id)
        if (r.isSuccessful || r.code() == 204) Result.Success(Unit)
        else Result.Error("No se pudo eliminar el bloque.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    suspend fun registrarAsistencia(req: AsistenciaCreateRequest): Result<AsistenciaRegistro> = try {
        val r = api.registrarAsistencia(req)
        if (r.isSuccessful) ok(r.body()) else Result.Error("No se pudo registrar la asistencia.")
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }

    // ── ASISTENTE IA ─────────────────────────────────────────────────────────

    suspend fun preguntarAsistente(mensaje: String): Result<String> = try {
        val r = api.preguntarAsistente(AsistenteRequest(mensaje))
        val body = r.body()
        when {
            r.isSuccessful && body?.respuesta != null -> Result.Success(body.respuesta)
            body?.error != null -> Result.Error(body.error)
            else -> Result.Error("El asistente no está disponible ahora.")
        }
    } catch (e: Exception) { Result.Error("Sin conexión al servidor.") }
}
