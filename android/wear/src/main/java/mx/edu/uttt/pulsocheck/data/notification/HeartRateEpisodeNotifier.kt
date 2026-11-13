package mx.edu.uttt.pulsocheck.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import mx.edu.uttt.pulsocheck.R
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode
import mx.edu.uttt.pulsocheck.presentation.MainActivity

/**
 * Prepara los avisos locales de episodios para el reloj.
 *
 * Esta clase no lee el sensor ni decide cuándo existe un episodio.
 * Recibe el episodio ya detectado y se ocupa de mostrar su aviso.
 */
class HeartRateEpisodeNotifier(context: Context) {

    // El contexto de aplicación evita conservar una Activity en memoria.
    private val appContext = context.applicationContext

    init {
        crearCanalDeNotificaciones()
    }

    /** Muestra un aviso informativo para un episodio reciente. */
    fun notificarEpisodio(episodio: HeartRateEpisode) {
        // Desde Android 13 se necesita autorización explícita para notificar.
        if (
            Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "No se mostró el aviso porque falta el permiso.")
            return
        }

        // La persona también puede desactivar avisos desde los ajustes del reloj.
        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            Log.i(TAG, "Las notificaciones están desactivadas para PulsoCheck.")
            return
        }

        // Al tocar el aviso se abre PulsoCheck en el reloj.
        val intent = Intent(appContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            episodio.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val isDemo = episodio.id.startsWith("demo-episode-")
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            // Icono del sistema para no agregar recursos gráficos todavía.
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(if (isDemo) "PulsoCheck · prueba DEMO" else "Revisa PulsoCheck")
            .setContentText(
                if (isDemo) "Evento simulado creado para validar la aplicación."
                else "Se registró un episodio reciente. Consulta el reloj y registra cómo te sientes.",
            )
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "PulsoCheck guardó un episodio reciente. " +
                            "Abre la aplicación para consultar la información y registrar cómo te sientes.",
                ),
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            // El mismo episodio usa siempre el mismo ID de notificación.
            NotificationManagerCompat.from(appContext)
                .notify(episodio.id.hashCode(), notification)
        } catch (error: SecurityException) {
            // El permiso pudo cambiar entre la comprobación y el envío.
            Log.w(TAG, "Android rechazó el aviso de PulsoCheck.", error)
        }
    }

    /** Crea el canal que Android necesita para administrar las notificaciones. */
    private fun crearCanalDeNotificaciones() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Episodios de PulsoCheck",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Avisos informativos sobre episodios recientes."
        }

        val manager = appContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val TAG = "PulsoCheckNotification"
        const val CHANNEL_ID = "heart_rate_episodes"
    }
}
