package mx.edu.uttt.pulsocheck.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponse

/**
 * Define las operaciones disponibles para las respuestas de la persona.
 */
interface EpisodeResponseRepository {

    /** Observa las respuestas guardadas. */
    fun observarTodas(): Flow<List<EpisodeResponse>>

    /** Guarda o actualiza una respuesta. */
    suspend fun guardar(respuesta: EpisodeResponse)

    /** Busca la respuesta asociada a un episodio. */
    suspend fun buscarPorEpisodio(episodeId: String): EpisodeResponse?
}