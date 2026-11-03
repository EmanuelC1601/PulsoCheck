package mx.edu.uttt.pulsocheck.data.sync

import android.util.Log
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mx.edu.uttt.pulsocheck.data.local.database.MobileDatabase
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateReadingRepository
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading

/** Recibe las lecturas aunque la pantalla del teléfono no esté abierta. */
class HeartRateDataLayerListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            // Copiamos los campos necesarios antes de liberar el buffer de Google Play Services.
            val incomingReadings = dataEvents.mapNotNull { event ->
                if (event.type != DataEvent.TYPE_CHANGED) return@mapNotNull null

                val dataItem = event.dataItem
                if (!dataItem.uri.path.orEmpty().startsWith(READINGS_PATH)) {
                    return@mapNotNull null
                }

                val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                val id = dataMap.getString(KEY_READING_ID) ?: return@mapNotNull null
                val bpm = dataMap.getInt(KEY_BPM)
                val measuredAt = dataMap.getLong(KEY_MEASURED_AT)

                // Ignoramos paquetes incompletos o inválidos antes de guardarlos.
                if (id.isBlank() || bpm <= 0 || measuredAt <= 0) {
                    null
                } else {
                    dataItem.uri to HeartRateReading(id, bpm, measuredAt)
                }
            }

            // Google llama este callback en segundo plano. runBlocking espera a que Room
            // termine de guardar antes de que el servicio libere los datos recibidos.
            runBlocking(Dispatchers.IO) {
                val database = MobileDatabase.obtenerInstancia(applicationContext)
                val repository = RoomHeartRateReadingRepository(
                    database.heartRateReadingDao(),
                )
                // Quitamos y rechazamos datos fuera de la ventana de cinco días.
                val cutoff = System.currentTimeMillis() - FIVE_DAYS_MILLIS
                repository.eliminarAnterioresA(cutoff)

                incomingReadings.forEach { (dataUri, reading) ->
                    if (reading.measuredAtEpochMillis >= cutoff) {
                        repository.guardar(reading)
                    }

                    // Productor-consumidor: al procesarlo, retiramos el
                    // elemento temporal para no acumular uno por cada lectura del reloj.
                    Wearable.getDataClient(applicationContext)
                        .deleteDataItems(dataUri)
                        .addOnFailureListener { error ->
                            Log.w(TAG, "La lectura se guardó, pero no se limpió del Data Layer.", error)
                        }
                }
            }
        } catch (error: Exception) {
            Log.e(TAG, "No se pudieron procesar datos recibidos del reloj.", error)
        } finally {
            dataEvents.release()
        }
    }

    private companion object {
        const val TAG = "PulsoCheckSync"
        const val READINGS_PATH = "/pulsocheck/readings/"
        const val KEY_READING_ID = "reading_id"
        const val KEY_BPM = "beats_per_minute"
        const val KEY_MEASURED_AT = "measured_at_epoch_millis"
        const val FIVE_DAYS_MILLIS = 5 * 24 * 60 * 60 * 1000L
    }
}
