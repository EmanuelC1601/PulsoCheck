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
import mx.edu.uttt.pulsocheck.MainActivity
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary

/** Muestra avisos informativos cuando el teléfono recibe episodios recientes. */
class MobileEpisodeNotifier(context: Context) {

    private val appContext = context.applicationContext

    init {
        crearCanal()
    }

    fun notificarEpisodio(episodio: HeartRateEpisodeSummary) {
        // Android 13 y posteriores requieren permiso de notificaciones.
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

        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) {
            Log.i(TAG, "Las notificaciones de PulsoCheck están desactivadas.")
            return
        }

        val intent = Intent(appContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            episodio.episodeId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(
                if (episodio.isDemo) "PulsoCheck · prueba DEMO"
                else "PulsoCheck registró un episodio",
            )
            .setContentText(
                if (episodio.isDemo) "Evento simulado recibido del reloj. Abre PulsoCheck para revisarlo."
                else "Abre la aplicación para revisar el registro del reloj.",
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(appContext)
                .notify(episodio.episodeId.hashCode(), notification)
        } catch (error: SecurityException) {
            Log.w(TAG, "Android rechazó el aviso de PulsoCheck.", error)
        }
    }

    /** Android necesita un canal para que el usuario administre estos avisos. */
    private fun crearCanal() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Episodios de PulsoCheck",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Avisos informativos sobre episodios recientes del reloj."
        }

        appContext
            .getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private companion object {
        const val TAG = "PulsoCheckNotification"
        const val CHANNEL_ID = "mobile_heart_rate_episodes"
    }
}
