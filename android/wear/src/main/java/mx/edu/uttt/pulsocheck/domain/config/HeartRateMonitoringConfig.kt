package mx.edu.uttt.pulsocheck.domain.config

/**
 * Reúne los parámetros que usa PulsoCheck para analizar las lecturas.
 *
 * El límite es una configuración de software para el prototipo.
 * No es un criterio médico ni genera un diagnóstico.
 */
data class HeartRateMonitoringConfig(
    // Una secuencia por encima de este valor puede iniciar un episodio.
    val thresholdBpm: Int = 120,

    // Si hay una pausa mayor, el detector reinicia la secuencia consecutiva.
    val maximumGapMillis: Long = 60_000L,
) {
    init {
        // Evita configurar valores que no tengan sentido para el detector.
        require(thresholdBpm > 0) {
            "El límite de frecuencia debe ser mayor que cero."
        }

        require(maximumGapMillis > 0) {
            "La separación máxima debe ser mayor que cero."
        }
    }
}