package pe.tecsup.examplanner.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import pe.tecsup.examplanner.MainActivity
import pe.tecsup.examplanner.R

/**
 * Widget de pantalla de inicio que muestra las 3 cosas más urgentes
 * (tareas + exámenes). Lee los datos guardados por la app en SharedPreferences.
 */
class PendientesWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            actualizarWidget(context, appWidgetManager, id)
        }
    }

    companion object {

        fun actualizarWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_pendientes)

            // Leer datos guardados por la app
            val prefs = context.getSharedPreferences("examplanner_widget", Context.MODE_PRIVATE)
            val linea1 = prefs.getString("urgente_1", null)
            val linea2 = prefs.getString("urgente_2", null)
            val linea3 = prefs.getString("urgente_3", null)
            val vacio = prefs.getBoolean("vacio", true)

            if (vacio || linea1 == null) {
                views.setTextViewText(R.id.widget_item1, "Sin pendientes 🎉")
                views.setTextViewText(R.id.widget_item2, "")
                views.setTextViewText(R.id.widget_item3, "")
            } else {
                views.setTextViewText(R.id.widget_item1, linea1)
                views.setTextViewText(R.id.widget_item2, linea2 ?: "")
                views.setTextViewText(R.id.widget_item3, linea3 ?: "")
            }

            // Al tocar el widget, abrir la app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }

        /**
         * Llamado desde la app para refrescar todos los widgets activos.
         */
        fun refrescarTodos(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, PendientesWidget::class.java)
            )
            for (id in ids) {
                actualizarWidget(context, manager, id)
            }
        }
    }
}
