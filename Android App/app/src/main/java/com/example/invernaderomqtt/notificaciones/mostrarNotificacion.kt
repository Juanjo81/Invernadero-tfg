import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

@RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
fun mostrarNotificacion(context: Context, titulo: String, mensaje: String) {
    // Detectar si es alerta o notificación por el título
    val esAlerta = titulo.contains("Alerta", ignoreCase = true)

    val canalId = if (esAlerta) "canal_alertas" else "canal_notificaciones"
    val nombreCanal = if (esAlerta) "Alertas Críticas" else "Notificaciones"
    val importancia = if (esAlerta) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
    val icono = if (esAlerta) android.R.drawable.ic_dialog_alert else android.R.drawable.ic_dialog_info

    // Crear canal si es necesario
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(canalId) == null) {
            val canal = NotificationChannel(canalId, nombreCanal, importancia)
            manager.createNotificationChannel(canal)
        }
    }

    // Construir notificación
    val notificacion = NotificationCompat.Builder(context, canalId)
        .setSmallIcon(icono)
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setPriority(if (esAlerta) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacion)
}