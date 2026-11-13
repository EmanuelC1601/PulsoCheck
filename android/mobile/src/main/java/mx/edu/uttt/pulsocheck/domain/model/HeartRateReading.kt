package mx.edu.uttt.pulsocheck.domain.model

/** Lectura que el teléfono recibe del reloj para mostrarla en el historial. */
data class HeartRateReading(
    val id: String,
    val beatsPerMinute: Int,
    val measuredAtEpochMillis: Long,
) {
    // Los IDs de prueba tienen un prefijo propio y así no hace falta cambiar Room.
    val isDemo: Boolean
        get() = id.startsWith(DEMO_READING_PREFIX)

    private companion object {
        const val DEMO_READING_PREFIX = "demo-reading-"
    }
}
