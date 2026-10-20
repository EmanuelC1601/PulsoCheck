package mx.edu.uttt.pulsocheck.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateReadingDao
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository

/**
 * Implementa el contrato del dominio usando Room como almacenamiento.
 */
class RoomHeartRateReadingRepository(
    private val dao: HeartRateReadingDao,
) : HeartRateReadingRepository {

    /**
     * Convierte cada entidad de Room al modelo que utiliza el dominio.
     * Flow propaga las actualizaciones del DAO a quien observa los datos.
     */
    override fun observarTodas(): Flow<List<HeartRateReading>> {
        return dao.observarTodas().map { entidades ->
            entidades.map { entidad -> entidad.toDomain() }
        }
    }

    /**
     * Convierte la lectura del dominio a entidad y la guarda con Room.
     */
    override suspend fun guardar(lectura: HeartRateReading) {
        dao.guardar(HeartRateReadingEntity.fromDomain(lectura))
    }

    /**
     * Convierte una lista de lecturas y las guarda en una operación.
     */
    override suspend fun guardarTodas(lecturas: List<HeartRateReading>) {
        val entidades = lecturas.map { lectura ->
            HeartRateReadingEntity.fromDomain(lectura)
        }

        dao.guardarTodas(entidades)
    }

    /**
     * Busca por ID y convierte el resultado al modelo de dominio.
     */
    override suspend fun buscarPorId(id: String): HeartRateReading? {
        return dao.buscarPorId(id)?.toDomain()
    }
}