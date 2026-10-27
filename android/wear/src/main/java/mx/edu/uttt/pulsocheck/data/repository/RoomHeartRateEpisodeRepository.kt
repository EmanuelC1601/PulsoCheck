package mx.edu.uttt.pulsocheck.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateEpisodeDao
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateEpisodeEntity
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateEpisodeRepository

/**
 * Implementa el contrato de episodios usando Room.
 *
 * Aquí se convierten los modelos del dominio a entidades de base de datos,
 * y viceversa.
 */

class RoomHeartRateEpisodeRepository(
    private val dao: HeartRateEpisodeDao,
) : HeartRateEpisodeRepository {

    /** Convierte las entidades de Room a modelos del dominio. */
    override fun observarTodos(): Flow<List<HeartRateEpisode>> {
        return dao.observarTodos().map { entidades ->
            entidades.map { entidad -> entidad.toDomain() }
        }
    }

    /** Convierte un episodio a entidad y lo guarda. */
    override suspend fun guardar(episodio: HeartRateEpisode) {
        dao.guardar(HeartRateEpisodeEntity.fromDomain(episodio))
    }

    /** Convierte y guarda una lista de episodios. */
    override suspend fun guardarTodos(episodios: List<HeartRateEpisode>) {
        val entidades = episodios.map { episodio ->
            HeartRateEpisodeEntity.fromDomain(episodio)
        }

        dao.guardarTodos(entidades)
    }

    /** Busca un episodio y convierte el resultado al modelo del dominio. */
    override suspend fun buscarPorId(episodeId: String): HeartRateEpisode? {
        return dao.buscarPorId(episodeId)?.toDomain()
    }

    /** Delega el borrado al DAO. */
    override suspend fun eliminar(episodeId: String) {
        dao.eliminar(episodeId)
    }
}
