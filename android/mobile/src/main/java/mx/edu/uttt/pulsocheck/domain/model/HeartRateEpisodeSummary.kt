package mx.edu.uttt.pulsocheck.domain.model

/**
 * Resumen de un episodio para mostrarlo en el teléfono.
 * Las lecturas completas se conservan en la base de datos del reloj.
 */
data class HeartRateEpisodeSummary(
    val episodeId: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val readingCount: Int,
    val status: EpisodeStatus,
) {
    // Permite mostrar claramente los episodios creados por el botón de prueba.
    val isDemo: Boolean
        get() = episodeId.startsWith(DEMO_EPISODE_PREFIX)

    init {
        require(episodeId.isNotBlank()) {
            "El episodio necesita un identificador."
        }
        require(startedAtEpochMillis > 0) {
            "La fecha de inicio debe ser válida."
        }
        require(readingCount > 0) {
            "El episodio debe incluir al menos una lectura."
        }
        require(status != EpisodeStatus.CLOSED || endedAtEpochMillis != null) {
            "Un episodio cerrado debe tener fecha de cierre."
        }
        require(status != EpisodeStatus.ACTIVE || endedAtEpochMillis == null) {
            "Un episodio activo no puede tener fecha de cierre."
        }
    }

    private companion object {
        const val DEMO_EPISODE_PREFIX = "demo-episode-"
    }
}

/** Estado que el teléfono muestra para el episodio recibido. */
enum class EpisodeStatus {
    ACTIVE,
    CLOSED,
}
