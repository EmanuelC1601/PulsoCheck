package mx.edu.uttt.pulsocheck.domain.model

/** Lectura que el teléfono recibe del reloj para mostrarla en el historial. */
data class HeartRateReading(
    val id: String,
    val beatsPerMinute: Int,
    val measuredAtEpochMillis: Long,
)
