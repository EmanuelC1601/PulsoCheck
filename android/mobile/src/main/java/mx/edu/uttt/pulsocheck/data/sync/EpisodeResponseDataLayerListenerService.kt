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
import mx.edu.uttt.pulsocheck.data.repository.RoomEpisodeResponseSummaryRepository
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponseSummary
import mx.edu.uttt.pulsocheck.domain.model.FeelingOption

/**
 * Recibe y guarda en el teléfono las respuestas registradas desde el reloj.
 */
class EpisodeResponseDataLayerListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            // Leemos únicamente los datos nuevos que pertenecen a respuestas.
            val incomingResponses = dataEvents.mapNotNull { event ->
                if (event.type != DataEvent.TYPE_CHANGED) {
                    return@mapNotNull null
                }

                val dataItem = event.dataItem
                if (!dataItem.uri.path.orEmpty().startsWith(RESPONSES_PATH)) {
                    return@mapNotNull null
                }

                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap

                val episodeId = dataMap.getString(KEY_EPISODE_ID)
                    ?: return@mapNotNull null
                val feelingName = dataMap.getString(KEY_FEELING)
                    ?: return@mapNotNull null
                val answeredAt = dataMap.getLong(KEY_ANSWERED_AT)

                // Convertimos el texto recibido a una opción válida.
                val feeling = try {
                    FeelingOption.valueOf(feelingName)
                } catch (_: IllegalArgumentException) {
                    return@mapNotNull null
                }

                // Validamos la información antes de guardarla.
                if (episodeId.isBlank() || answeredAt <= 0) {
                    return@mapNotNull null
                }

                val response = EpisodeResponseSummary(
                    episodeId = episodeId,
                    feeling = feeling,
                    answeredAtEpochMillis = answeredAt,
                )

                dataItem.uri to response
            }

            // Room requiere operaciones de escritura fuera del hilo principal.
            runBlocking(Dispatchers.IO) {
                val database = MobileDatabase.obtenerInstancia(applicationContext)
                val repository = RoomEpisodeResponseSummaryRepository(
                    database.episodeResponseSummaryDao(),
                )

                incomingResponses.forEach { (dataUri, response) ->
                    // Guarda o reemplaza la respuesta asociada a este episodio.
                    repository.guardar(response)

                    // Solo retiramos el dato temporal después de guardarlo.
                    Wearable.getDataClient(applicationContext)
                        .deleteDataItems(dataUri)
                        .addOnFailureListener { error ->
                            Log.w(
                                TAG,
                                "La respuesta se guardó, pero no se limpió del Data Layer.",
                                error,
                            )
                        }
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "No se pudieron procesar respuestas del reloj.", error)
        } finally {
            // Liberamos el buffer de eventos recibido de Google Play Services.
            dataEvents.release()
        }
    }

    private companion object {
        const val TAG = "PulsoCheckSync"
        const val RESPONSES_PATH = "/pulsocheck/responses/"

        const val KEY_EPISODE_ID = "episode_id"
        const val KEY_FEELING = "feeling_option"
        const val KEY_ANSWERED_AT = "answered_at_epoch_millis"
    }
}