package mx.edu.uttt.pulsocheck.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.uttt.pulsocheck.data.local.dao.EpisodeResponseSummaryDao
import mx.edu.uttt.pulsocheck.data.local.entity.EpisodeResponseSummaryEntity
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponseSummary
import mx.edu.uttt.pulsocheck.domain.repository.EpisodeResponseSummaryRepository

/** Implementa el repositorio móvil usando Room. */
class RoomEpisodeResponseSummaryRepository(
    private val dao: EpisodeResponseSummaryDao,
) : EpisodeResponseSummaryRepository {

    override fun observarTodas(): Flow<List<EpisodeResponseSummary>> {
        return dao.observarTodas().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun guardar(response: EpisodeResponseSummary) {
        dao.guardar(EpisodeResponseSummaryEntity.fromDomain(response))
    }

    override suspend fun buscarPorEpisodio(
        episodeId: String,
    ): EpisodeResponseSummary? {
        return dao.buscarPorEpisodio(episodeId)?.toDomain()
    }
}