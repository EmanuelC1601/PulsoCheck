package mx.edu.uttt.pulsocheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateEpisodeEntity

/** Operaciones de base de datos para los episodios. */
@Dao
interface HeartRateEpisodeDao {

    // Si el episodio ya existe, actualiza sus datos.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(episodio: HeartRateEpisodeEntity)

    // Permite guardar varios episodios en una sola operación.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodos(episodios: List<HeartRateEpisodeEntity>)

    // Room vuelve a emitir la lista cada vez que cambia la tabla.
    @Query(
        """
        SELECT * FROM heart_rate_episodes
        ORDER BY started_at_epoch_millis DESC
        """
    )
    fun observarTodos(): Flow<List<HeartRateEpisodeEntity>>

    // Busca un episodio por su identificador.
    @Query(
        """
        SELECT * FROM heart_rate_episodes
        WHERE id = :episodeId
        LIMIT 1
        """
    )
    suspend fun buscarPorId(episodeId: String): HeartRateEpisodeEntity?

    // Al eliminar un episodio, la clave foránea elimina su respuesta asociada.
    @Query("DELETE FROM heart_rate_episodes WHERE id = :episodeId")
    suspend fun eliminar(episodeId: String)
}