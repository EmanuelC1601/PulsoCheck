package mx.edu.uttt.pulsocheck.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode


/**
 * Define las operaciones disponibles para trabajar con episodios.
 *
 * El dominio usa este contrato sin depender directamente de Room.
 */
interface HeartRateEpisodeRepository {

    /** Observa los episodios y recibe actualizaciones cuando cambian. */
    fun observarTodos(): Flow<List<HeartRateEpisode>>

    /** Guarda o actualiza un episodio. */
    suspend fun guardar(episodio: HeartRateEpisode)

    /** Guarda o actualiza varios episodios en una operación. */
    suspend fun guardarTodos(episodios: List<HeartRateEpisode>)

    /** Busca un episodio por ID; devuelve null si no existe. */
    suspend fun buscarPorId(episodeId: String): HeartRateEpisode?

    /** Elimina un episodio y su respuesta asociada. */
    suspend fun eliminar(episodeId: String)
}