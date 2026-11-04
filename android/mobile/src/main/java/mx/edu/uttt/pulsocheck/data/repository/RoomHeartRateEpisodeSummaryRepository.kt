package mx.edu.uttt.pulsocheck.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateEpisodeSummaryDao
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateEpisodeSummaryEntity
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateEpisodeSummaryRepository

/** Implementa el contrato del dominio usando la base de datos Room. */
class RoomHeartRateEpisodeSummaryRepository(
    private val dao: HeartRateEpisodeSummaryDao,
) : HeartRateEpisodeSummaryRepository {

    override fun observarDesde(
        desdeEpochMillis: Long,
    ): Flow<List<HeartRateEpisodeSummary>> {
        return dao.observarDesde(desdeEpochMillis).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun guardar(episode: HeartRateEpisodeSummary) {
        dao.guardar(HeartRateEpisodeSummaryEntity.fromDomain(episode))
    }

    override suspend fun eliminarAnterioresA(fechaLimite: Long) {
        dao.eliminarAnterioresA(fechaLimite)
    }
}