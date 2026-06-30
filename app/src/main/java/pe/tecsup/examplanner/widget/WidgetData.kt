package pe.tecsup.examplanner.widget

import android.content.Context
import pe.tecsup.examplanner.data.models.Examen
import pe.tecsup.examplanner.data.models.Tarea

/**
 * Guarda las 3 cosas más urgentes (tareas + exámenes) para que el widget
 * las muestre. Se llama desde el ViewModel al cargar pendientes.
 */
object WidgetData {

    private data class Item(val texto: String, val dias: Int, val vencida: Boolean)

    fun guardar(context: Context, tareas: List<Tarea>, examenes: List<Examen>) {
        val items = mutableListOf<Item>()

        for (t in tareas) {
            if (t.completada) continue
            items.add(Item("📋 ${t.nombre}", t.diasRestantes, t.estaVencida))
        }
        for (e in examenes) {
            val nombre = e.descripcion?.takeIf { it.isNotBlank() } ?: "Examen de ${e.curso}"
            items.add(Item("📝 $nombre", e.diasRestantes, false))
        }

        // Total de pendientes (antes de recortar a 3) para el contador
        val total = items.size

        // Ordenar: vencidas primero, luego por días restantes
        val ordenados = items.sortedWith(
            compareByDescending<Item> { it.vencida }.thenBy { it.dias }
        ).take(3)

        val prefs = context.getSharedPreferences("examplanner_widget", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("total", total)

        if (ordenados.isEmpty()) {
            editor.putBoolean("vacio", true)
            editor.remove("urgente_1")
            editor.remove("urgente_2")
            editor.remove("urgente_3")
        } else {
            editor.putBoolean("vacio", false)
            ordenados.forEachIndexed { i, item ->
                val etiqueta = when {
                    item.vencida -> "⚠ Vencida"
                    item.dias == 0 -> "Hoy"
                    item.dias == 1 -> "Mañana"
                    else -> "${item.dias}d"
                }
                // Etiqueta de tiempo PRIMERO (siempre visible), luego el nombre recortado.
                // El emoji ya viene en item.texto al inicio (📋 / 📝).
                val emoji = item.texto.take(2)
                val resto = item.texto.drop(2).trim()
                val nombreCorto = if (resto.length > 24) resto.take(22) + "…" else resto
                editor.putString("urgente_${i + 1}", "$emoji [$etiqueta] $nombreCorto")
            }
            // Limpiar las que sobren
            for (j in ordenados.size until 3) {
                editor.remove("urgente_${j + 1}")
            }
        }
        editor.apply()

        // Refrescar widgets activos
        try {
            PendientesWidget.refrescarTodos(context)
        } catch (_: Exception) {
        }
    }
}
