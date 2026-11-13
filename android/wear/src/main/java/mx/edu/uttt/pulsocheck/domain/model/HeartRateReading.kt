package mx.edu.uttt.pulsocheck.domain.model

/**
 * Representa una lectura de frecuencia cardiaca obtenida por PulsoCheck.
 *
 * Esta clase describe los datos de la aplicación. El sensor, la base de datos
 * y la pantalla se conectarán con este modelo en sus respectivas capas.
 */
data class HeartRateReading(
    // Identificador estable para guardar y sincronizar esta lectura.
    val id: String,

    // Frecuencia cardiaca en latidos por minuto (lpm).
    val beatsPerMinute: Int,

    // Fecha y hora de la lectura, expresadas en milisegundos desde 1970.
    val measuredAtEpochMillis: Long,

    // Identifica si el dato viene del sensor o de una prueba simulada.
    val origin: ReadingOrigin,
)

/**
 * Identifica cómo se obtuvo la lectura.
 */
enum class ReadingOrigin {
    HEALTH_SERVICES_ON_WATCH,
    // Dato artificial creado únicamente para validar la interfaz y sincronización.
    DEMO_TEST,
}
