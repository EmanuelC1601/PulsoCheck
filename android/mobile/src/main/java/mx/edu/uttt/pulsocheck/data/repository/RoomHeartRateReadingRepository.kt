package mx.edu.uttt.pulsocheck.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateReadingDao
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository

/** Traduce el contrato de dominio a operaciones de Room. */
class RoomHeartRateReadingRepository(
    private val dao: HeartRateReadingDao,
) : HeartRateReadingRepository {

    override fun observarDesde(desdeEpochMillis: Long): Flow<List<HeartRateReading>> {
        return dao.observarDesde(desdeEpochMillis).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun guardar(reading: HeartRateReading) {
        dao.guardar(HeartRateReadingEntity.fromDomain(reading))
    }

    override suspend fun eliminarAnterioresA(cutoff: Long) {
        dao.eliminarAnterioresA(cutoff)
    }
}
