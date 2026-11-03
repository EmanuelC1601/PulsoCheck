package mx.edu.uttt.pulsocheck.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading

/** Envía cada lectura del reloj al teléfono mediante Wearable Data Layer. */
class HeartRateDataLayerSender(context: Context) {

    // Guardamos el contexto de aplicación para no retener una Activity.
    private val appContext = context.applicationContext

    /** Crea un elemento de datos independiente para que cada lectura sea idempotente. */
    fun enviar(lectura: HeartRateReading) {
        try {
            val request = PutDataMapRequest
                .create("$READINGS_PATH/${lectura.id}")
                .apply {
                    // Estas claves deben coincidir con las que lee el servicio del teléfono.
                    dataMap.putString(KEY_READING_ID, lectura.id)
                    dataMap.putInt(KEY_BPM, lectura.beatsPerMinute)
                    dataMap.putLong(KEY_MEASURED_AT, lectura.measuredAtEpochMillis)
                }
                .asPutDataRequest()

            // La pantalla de historial debe recibir la lectura pronto.
            request.setUrgent()

            Wearable.getDataClient(appContext)
                .putDataItem(request)
                .addOnFailureListener { error ->
                    // La lectura ya está guardada localmente; el fallo de sincronización
                    // no debe detener el seguimiento del sensor.
                    Log.w(TAG, "No se pudo sincronizar una lectura con el teléfono.", error)
                }
        } catch (error: Exception) {
            // Algunos dispositivos no tienen Data Layer disponible.
            Log.w(TAG, "Wearable Data Layer no está disponible en este dispositivo.", error)
        }
    }

    private companion object {
        const val TAG = "PulsoCheckSync"
        const val READINGS_PATH = "/pulsocheck/readings"
        const val KEY_READING_ID = "reading_id"
        const val KEY_BPM = "beats_per_minute"
        const val KEY_MEASURED_AT = "measured_at_epoch_millis"
    }
}
