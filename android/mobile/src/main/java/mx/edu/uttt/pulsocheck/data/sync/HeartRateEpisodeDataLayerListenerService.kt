package mx.edu.uttt.pulsocheck.data.sync

import android.util.Log
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.pulsocheck.data.local.database.MobileDatabase
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateEpisodeSummaryRepository
import mx.edu.uttt.pulsocheck.domain.model.EpisodeStatus
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary

/**
 * Recibe resúmenes de episodios del reloj aunque la app del teléfono no esté abierta.
 */
class HeartRateEpisodeDataLayerListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            // Convertimos los eventos en modelos propios antes de guardarlos.
            val incomingEpisodes = dataEvents.mapNotNull { event ->
                if (event.type != DataEvent.TYPE_CHANGED) {
                    return@mapNotNull null
                }

                val dataItem = event.dataItem
                if (!dataItem.uri.path.orEmpty().startsWith(EPISODES_PATH)) {
                    return@mapNotNull null
                }

                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                val episodeId = dataMap.getString(KEY_EPISODE_ID)
                    ?: return@mapNotNull null
                val startedAt = dataMap.getLong(KEY_STARTED_AT)
                val hasEndedAt = dataMap.getBoolean(KEY_HAS_ENDED_AT)
                val endedAtValue = dataMap.getLong(KEY_ENDED_AT)
                val readingCount = dataMap.getInt(KEY_READING_COUNT)
                val statusName = dataMap.getString(KEY_STATUS)
                    ?: return@mapNotNull null

                // Si el texto del estado no es válido, descartamos ese evento.
                val status = try {
                    EpisodeStatus.valueOf(statusName)
                } catch (_: IllegalArgumentException) {
                    return@mapNotNull null
                }

                val endedAt = if (hasEndedAt) endedAtValue else null

                // Validamos los datos antes de construir el modelo de dominio.
                if (
                    episodeId.isBlank() ||
                    startedAt <= 0 ||
                    readingCount <= 0 ||
                    (status == EpisodeStatus.CLOSED && endedAt == null) ||
                    (status == EpisodeStatus.ACTIVE && endedAt != null) ||
                    (endedAt != null && endedAt < startedAt)
                ) {
                    return@mapNotNull null
                }

                val summary = HeartRateEpisodeSummary(
                    episodeId = episodeId,
                    startedAtEpochMillis = startedAt,
                    endedAtEpochMillis = endedAt,
                    readingCount = readingCount,
                    status = status,
                )

                dataItem.uri to summary
            }

            // Room se ejecuta en segundo plano y termina antes de liberar los eventos.
            runBlocking(Dispatchers.IO) {
                val database = MobileDatabase.obtenerInstancia(applicationContext)
                val repository = RoomHeartRateEpisodeSummaryRepository(
                    database.heartRateEpisodeSummaryDao(),
                )

                val fiveDaysAgo =
                    System.currentTimeMillis() - FIVE_DAYS_MILLIS

                // El teléfono conserva únicamente los episodios de los últimos cinco días.
                repository.eliminarAnterioresA(fiveDaysAgo)

                incomingEpisodes.forEach { (dataUri, episode) ->
                    if (episode.startedAtEpochMillis >= fiveDaysAgo) {
                        repository.guardar(episode)
                    }

                    // Ya procesado el resumen, quitamos su elemento temporal del Data Layer.
                    Wearable.getDataClient(applicationContext)
                        .deleteDataItems(dataUri)
                        .addOnFailureListener { error ->
                            Log.w(
                                TAG,
                                "El episodio se guardó, pero no se limpió del Data Layer.",
                                error,
                            )
                        }
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "No se pudieron procesar episodios del reloj.", error)
        } finally {
            // Liberamos el buffer proporcionado por Google Play Services.
            dataEvents.release()
        }
    }

    private companion object {
        const val TAG = "PulsoCheckSync"
        const val EPISODES_PATH = "/pulsocheck/episodes/"

        const val KEY_EPISODE_ID = "episode_id"
        const val KEY_STARTED_AT = "started_at_epoch_millis"
        const val KEY_HAS_ENDED_AT = "has_ended_at"
        const val KEY_ENDED_AT = "ended_at_epoch_millis"
        const val KEY_READING_COUNT = "reading_count"
        const val KEY_STATUS = "episode_status"

        const val FIVE_DAYS_MILLIS = 5 * 24 * 60 * 60 * 1000L
    }
}