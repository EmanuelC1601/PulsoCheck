package mx.edu.uttt.pulsocheck.domain.model

/**
 * Representa un episodio detectado por PulsoCheck.
 *
 * El episodio comienza cuando la regla de seguimiento lo confirma.
 * Mientras siga activo, todavía no conocemos su hora de finalización.
 */
data class HeartRateEpisode(
    // Identificador único para guardar y sincronizar el episodio.
    val id: String,

    // Hora de la primera lectura que formó parte del episodio.
    val startedAtEpochMillis: Long,

    // Hora en que terminó; permanece vacía mientras siga activo.
    val endedAtEpochMillis: Long?,

    // Identificadores de las lecturas relacionadas con este episodio.
    val readingIds: List<String>,

    // Estado actual del episodio.
    val status: EpisodeStatus,
) {
    init {
        // Evita crear episodios sin identificador.
        require(id.isNotBlank()) {
            "El episodio necesita un identificador."
        }

        // La hora de inicio debe ser una fecha válida.
        require(startedAtEpochMillis > 0) {
            "La hora de inicio debe ser válida."
        }

        // Un episodio debe estar respaldado por lecturas.
        require(readingIds.isNotEmpty()) {
            "El episodio debe tener al menos una lectura asociada."
        }

        // No permitimos asociar dos veces la misma lectura.
        require(readingIds.distinct().size == readingIds.size) {
            "El episodio no debe contener lecturas duplicadas."
        }

        // Si existe hora de cierre, no puede ser anterior al inicio.
        require(
            endedAtEpochMillis == null ||
                    endedAtEpochMillis >= startedAtEpochMillis
        ) {
            "La hora de cierre no puede ser anterior al inicio."
        }

        // Un episodio cerrado necesita una hora de cierre.
        require(status != EpisodeStatus.CLOSED || endedAtEpochMillis != null) {
            "Un episodio cerrado debe tener hora de cierre."
        }

        // Un episodio activo todavía no debe tener hora de cierre.
        require(status != EpisodeStatus.ACTIVE || endedAtEpochMillis == null) {
            "Un episodio activo no puede tener hora de cierre."
        }
    }
}

/**
 * Indica si el episodio sigue abierto o ya terminó.
 */
enum class EpisodeStatus {
    ACTIVE,
    CLOSED,
}