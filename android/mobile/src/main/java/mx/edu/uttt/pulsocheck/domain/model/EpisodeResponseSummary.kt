package mx.edu.uttt.pulsocheck.domain.model

/** Respuesta que la persona registró en el reloj para un episodio. */
data class EpisodeResponseSummary(
    val episodeId: String,
    val feeling: FeelingOption,
    val answeredAtEpochMillis: Long,
) {
    init {
        require(episodeId.isNotBlank()) {
            "La respuesta debe estar asociada a un episodio."
        }
        require(answeredAtEpochMillis > 0) {
            "La fecha de la respuesta debe ser válida."
        }
    }
}

/** Opciones que la persona puede elegir en el reloj. */
enum class FeelingOption {
    GOOD,
    OKAY,
    BAD,
}