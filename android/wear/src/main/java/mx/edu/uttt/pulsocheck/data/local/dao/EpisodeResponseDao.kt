package mx.edu.uttt.pulsocheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.data.local.entity.EpisodeResponseEntity

/** Operaciones de base de datos para las respuestas de la persona. */
@Dao
interface EpisodeResponseDao {

    // Como episodeId es la clave primaria, guardar otra respuesta la actualiza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(respuesta: EpisodeResponseEntity)

    // Permite observar las respuestas para mostrarlas en el historial.
    @Query(
        """
        SELECT * FROM episode_responses
        ORDER BY answered_at_epoch_millis DESC
        """
    )
    fun observarTodas(): Flow<List<EpisodeResponseEntity>>

    // Busca la respuesta asociada a un episodio.
    @Query(
        """
        SELECT * FROM episode_responses
        WHERE episode_id = :episodeId
        LIMIT 1
        """
    )
    suspend fun buscarPorEpisodio(episodeId: String): EpisodeResponseEntity?
}