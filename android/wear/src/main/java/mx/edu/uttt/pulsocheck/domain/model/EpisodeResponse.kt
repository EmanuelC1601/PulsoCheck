package mx.edu.uttt.pulsocheck.domain.model


/**
 * Respuesta que la persona registra después de un episodio.
 *
 * Esta información describe cómo se siente; no es un diagnóstico médico.
 */
data class EpisodeResponse(
    // El episodio al que pertenece la respuesta.
    // Usaremos este dato para vincular ambas tablas en Room.
    val episodeId: String,

    // Opción elegida en la pantalla del reloj.
    val feeling: FeelingOption,

    // Momento en que la persona registró la respuesta.
    val answeredAtEpochMillis: Long,
) {
    init {
        // La respuesta siempre debe estar vinculada con un episodio.
        require(episodeId.isNotBlank()) {
            "La respuesta necesita el identificador de un episodio."
        }

        // La hora debe corresponder a una fecha válida.
        require(answeredAtEpochMillis > 0) {
            "La fecha de la respuesta debe ser válida."
        }
    }
}

/**
 * Opciones que ofrecerá el reloj para registrar cómo se siente la persona.
 */
enum class FeelingOption {
    GOOD,
    OKAY,
    BAD,
}