package mx.edu.uttt.pulsocheck.data.sensor

import android.content.Context
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureClient
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.Availability
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.DataPointContainer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

/**
 * Lee la frecuencia cardiaca mediante Health Services.
 *
 * Esta clase vive en la capa data porque conoce la biblioteca de Android.
 * El dominio solo recibirá valores de pulso, sin depender de Health Services.
 */
class HealthServicesHeartRateDataSource(context: Context) {

    // El cliente se crea una sola vez y reutiliza el servicio del sistema.
    private val measureClient: MeasureClient =
        HealthServices.getClient(context.applicationContext).measureClient

    /**
     * Pregunta al reloj si puede entregar datos de frecuencia cardiaca.
     * No todos los dispositivos Wear OS ofrecen los mismos sensores.
     */
    suspend fun admiteFrecuenciaCardiaca(): Boolean {
        val future = measureClient.getCapabilitiesAsync()

        // Health Services responde con ListenableFuture; este bloque permite
        // esperar el resultado desde una corrutina sin bloquear la interfaz.
        return suspendCancellableCoroutine { continuation ->
            future.addListener(
                {
                    if (continuation.isActive) {
                        try {
                            val capabilities = future.get()
                            val soportaPulso =
                                DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure
                            continuation.resume(soportaPulso)
                        } catch (error: Exception) {
                            continuation.resumeWithException(error)
                        }
                    }
                },
                Executor { tarea -> tarea.run() },
            )
        }
    }

    /**
     * Emite cada lectura que llega mientras la pantalla está usando el sensor.
     * El ViewModel guardará los valores emitidos en Room.
     */
    fun observarFrecuenciaCardiaca(): Flow<Int> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(
                dataType: DeltaDataType<*, *>,
                availability: Availability,
            ) {
                // La disponibilidad puede cambiar si el reloj no está bien colocado.
                // La pantalla podrá seguir esperando una lectura cuando vuelva a estarlo.
                if (availability is DataTypeAvailability) {
                    // El estado detallado de disponibilidad se añadirá a la interfaz después.
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                // Una notificación puede incluir varios puntos; leemos cada uno.
                data.getData(DataType.HEART_RATE_BPM).forEach { punto ->
                    val latidosPorMinuto = punto.value.roundToInt()

                    // Descartamos valores inválidos antes de enviarlos a la capa superior.
                    if (latidosPorMinuto > 0) {
                        trySend(latidosPorMinuto)
                    }
                }
            }
        }

        // Health Services entrega datos solo mientras esta suscripción siga activa.
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        // Al cancelar la corrutina también quitamos el listener del sensor.
        awaitClose {
            measureClient.unregisterMeasureCallbackAsync(
                DataType.HEART_RATE_BPM,
                callback,
            )
        }
    }
}
