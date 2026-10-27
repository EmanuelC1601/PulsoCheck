package mx.edu.uttt.pulsocheck.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.uttt.pulsocheck.data.local.dao.EpisodeResponseDao
import mx.edu.uttt.pulsocheck.data.local.entity.EpisodeResponseEntity
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponse
import mx.edu.uttt.pulsocheck.domain.repository.EpisodeResponseRepository

/**
 * Implementa el repositorio de respuestas usando Room.
 */
class RoomEpisodeResponseRepository(
    private val dao: EpisodeResponseDao,
) : EpisodeResponseRepository {

    /** Convierte las entidades observadas a modelos del dominio. */
    override fun observarTodas(): Flow<List<EpisodeResponse>> {
        return dao.observarTodas().map { entidades ->
            entidades.map { entidad -> entidad.toDomain() }
        }
    }

    /** Convierte y guarda una respuesta. */
    override suspend fun guardar(respuesta: EpisodeResponse) {
        dao.guardar(EpisodeResponseEntity.fromDomain(respuesta))
    }

    /** Busca una respuesta y convierte el resultado al modelo del dominio. */
    override suspend fun buscarPorEpisodio(episodeId: String): EpisodeResponse? {
        return dao.buscarPorEpisodio(episodeId)?.toDomain()
    }
}