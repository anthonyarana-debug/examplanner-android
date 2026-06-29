package pe.tecsup.examplanner.notificaciones

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import pe.tecsup.examplanner.data.models.Examen
import pe.tecsup.examplanner.data.models.Tarea
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * Programa recordatorios locales para tareas y exámenes.
 * - 1 aviso 1 día antes (a las 8:00 am)
 * - 1 aviso 2 horas antes
 * Todo se programa con AlarmManager (funciona con la app cerrada).
 */
object NotificacionesHelper {

    const val CHANNEL_ID = "examplanner_recordatorios"
    private const val CHANNEL_NAME = "Recordatorios"

    // Parser de la fecha ISO que envía el backend Django.
    private fun parseFecha(iso: String): Long? {
        // Posibles formatos: 2026-06-29T23:59:00-05:00  /  2026-06-29T23:59:00Z  /  con microsegundos
        val formatos = listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
        )
        for (f in formatos) {
            try {
                val sdf = SimpleDateFormat(f, Locale.US)
                if (f.endsWith("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                return sdf.parse(iso)?.time
            } catch (_: Exception) {
            }
        }
        return null
    }

    fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de tareas y exámenes próximos"
            }
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(canal)
        }
    }

    /**
     * Programa los recordatorios de todas las tareas y exámenes.
     * Se llama al cargar/sincronizar pendientes.
     */
    fun programarTodos(context: Context, tareas: List<Tarea>, examenes: List<Examen>) {
        crearCanal(context)
        val ahora = System.currentTimeMillis()

        for (t in tareas) {
            if (t.completada) continue
            val fechaMs = parseFecha(t.fechaLimite) ?: continue
            programarPar(
                context,
                baseId = "tarea_${t.id}".hashCode(),
                fechaEventoMs = fechaMs,
                ahora = ahora,
                titulo = "📋 Tarea: ${t.curso}",
                texto = t.nombre
            )
        }

        for (e in examenes) {
            val fechaMs = parseFecha(e.fecha) ?: continue
            val nombre = e.descripcion?.takeIf { it.isNotBlank() } ?: "Examen de ${e.curso}"
            programarPar(
                context,
                baseId = "examen_${e.id}".hashCode(),
                fechaEventoMs = fechaMs,
                ahora = ahora,
                titulo = "📝 Examen: ${e.curso}",
                texto = nombre
            )
        }
    }

    private fun programarPar(
        context: Context,
        baseId: Int,
        fechaEventoMs: Long,
        ahora: Long,
        titulo: String,
        texto: String
    ) {
        // Aviso 1: 1 día antes a las 8:00 am
        val unDiaAntes = Calendar.getInstance().apply {
            timeInMillis = fechaEventoMs
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        if (unDiaAntes > ahora) {
            programarAlarma(context, baseId + 1, unDiaAntes, titulo, "Mañana: $texto")
        }

        // Aviso 2: 2 horas antes
        val dosHorasAntes = fechaEventoMs - 2 * 60 * 60 * 1000
        if (dosHorasAntes > ahora) {
            programarAlarma(context, baseId + 2, dosHorasAntes, titulo, "En 2 horas: $texto")
        }
    }

    private fun programarAlarma(
        context: Context,
        requestId: Int,
        triggerAtMs: Long,
        titulo: String,
        texto: String
    ) {
        val intent = Intent(context, RecordatorioReceiver::class.java).apply {
            putExtra("titulo", titulo)
            putExtra("texto", texto)
            putExtra("notif_id", requestId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            // En Android 12+ las alarmas exactas requieren permiso especial.
            // Si no lo tenemos, usamos una alarma inexacta (igual sirve para recordatorios).
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // Fallback seguro: alarma inexacta
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
    }
}
