package mx.edu.uttt.pulsocheck.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponseSummary

/** Contrato para consultar y guardar respuestas en la app móvil. */
interface EpisodeResponseSummaryRepository {

    fun observarTodas(): Flow<List<EpisodeResponseSummary>>

    suspend fun guardar(response: EpisodeResponseSummary)

    suspend fun buscarPorEpisodio(
        episodeId: String,
    ): EpisodeResponseSummary?
}