package mx.edu.uttt.pulsocheck.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponse
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading


/** Envía lecturas y episodios del reloj al teléfono por Wearable Data Layer. */
class HeartRateDataLayerSender(context: Context) {

    // Usamos el contexto de aplicación para no retener una Activity.
    private val appContext = context.applicationContext

    /** Envía una lectura individual de frecuencia cardiaca al teléfono. */
    fun enviar(lectura: HeartRateReading) {
        try {
            val request = PutDataMapRequest
                .create("$READINGS_PATH/${lectura.id}")
                .apply {
                    dataMap.putString(KEY_READING_ID, lectura.id)
                    dataMap.putInt(KEY_BPM, lectura.beatsPerMinute)
                    dataMap.putLong(KEY_MEASURED_AT, lectura.measuredAtEpochMillis)
                }
                .asPutDataRequest()

            // Solicita una sincronización pronta entre el reloj y el teléfono.
            request.setUrgent()

            Wearable.getDataClient(appContext)
                .putDataItem(request)
                .addOnFailureListener { error ->
                    Log.w(TAG, "No se pudo sincronizar la lectura.", error)
                }
        } catch (error: Exception) {
            Log.w(TAG, "Wearable Data Layer no está disponible.", error)
        }
    }

    /**
     * Envía al teléfono cómo se sintió la persona después de un episodio.
     * La ruta usa el identificador del episodio para reemplazar una respuesta
     * anterior si vuelve a enviarse.
     */
    fun enviarRespuesta(respuesta: EpisodeResponse) {
        try {
            val request = PutDataMapRequest
                .create("$RESPONSES_PATH/${respuesta.episodeId}")
                .apply {
                    dataMap.putString(KEY_EPISODE_ID, respuesta.episodeId)
                    dataMap.putString(KEY_FEELING, respuesta.feeling.name)
                    dataMap.putLong(
                        KEY_ANSWERED_AT,
                        respuesta.answeredAtEpochMillis,
                    )
                }
                .asPutDataRequest()

            // Solicita que Google Play Services sincronice pronto el dato.
            request.setUrgent()

            Wearable.getDataClient(appContext)
                .putDataItem(request)
                .addOnFailureListener { error ->
                    Log.w(TAG, "No se pudo sincronizar la respuesta.", error)
                }
        } catch (error: Exception) {
            Log.w(TAG, "No se pudo preparar la respuesta para sincronización.", error)
        }
    }

    /**
     * Envía el resumen de un episodio.
     *
     * El mismo episodio usa siempre la misma ruta. Si sigue activo, el teléfono
     * puede reemplazar su resumen cuando lleguen nuevas lecturas.
     */
    fun enviarEpisodio(
        episodio: HeartRateEpisode,
        alEnviar: () -> Unit,
    ) {
        try {
            val request = PutDataMapRequest
                .create("$EPISODES_PATH/${episodio.id}")
                .apply {
                    dataMap.putString(KEY_EPISODE_ID, episodio.id)
                    dataMap.putLong(
                        KEY_EPISODE_STARTED_AT,
                        episodio.startedAtEpochMillis,
                    )
                    dataMap.putBoolean(
                        KEY_HAS_ENDED_AT,
                        episodio.endedAtEpochMillis != null,
                    )
                    dataMap.putLong(
                        KEY_EPISODE_ENDED_AT,
                        episodio.endedAtEpochMillis ?: 0L,
                    )
                    dataMap.putInt(
                        KEY_READING_COUNT,
                        episodio.readingIds.size,
                    )
                    dataMap.putString(
                        KEY_EPISODE_STATUS,
                        episodio.status.name,
                    )
                }
                .asPutDataRequest()

            request.setUrgent()

            Wearable.getDataClient(appContext)
                .putDataItem(request)
                .addOnSuccessListener {
                    // Informa al ViewModel que ya se envió esta versión.
                    alEnviar()
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "No se pudo sincronizar el episodio.", error)
                }
        } catch (error: Exception) {
            Log.w(TAG, "No se pudo preparar el episodio para sincronización.", error)
        }
    }

    private companion object {
        const val TAG = "PulsoCheckSync"

        const val READINGS_PATH = "/pulsocheck/readings"
        const val KEY_READING_ID = "reading_id"
        const val KEY_BPM = "beats_per_minute"
        const val KEY_MEASURED_AT = "measured_at_epoch_millis"
        const val RESPONSES_PATH = "/pulsocheck/responses"
        const val KEY_FEELING = "feeling_option"
        const val KEY_ANSWERED_AT = "answered_at_epoch_millis"

        const val EPISODES_PATH = "/pulsocheck/episodes"
        const val KEY_EPISODE_ID = "episode_id"
        const val KEY_EPISODE_STARTED_AT = "started_at_epoch_millis"
        const val KEY_HAS_ENDED_AT = "has_ended_at"
        const val KEY_EPISODE_ENDED_AT = "ended_at_epoch_millis"
        const val KEY_READING_COUNT = "reading_count"
        const val KEY_EPISODE_STATUS = "episode_status"
    }
}
