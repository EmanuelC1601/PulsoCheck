package mx.edu.uttt.pulsocheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.data.local.entity.EpisodeResponseSummaryEntity

/** Consultas de Room para respuestas recibidas del reloj. */
@Dao
interface EpisodeResponseSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(response: EpisodeResponseSummaryEntity)

    @Query(
        """
        SELECT * FROM mobile_episode_responses
        ORDER BY answered_at_epoch_millis DESC
        """,
    )
    fun observarTodas(): Flow<List<EpisodeResponseSummaryEntity>>

    @Query(
        """
        SELECT * FROM mobile_episode_responses
        WHERE episode_id = :episodeId
        LIMIT 1
        """,
    )
    suspend fun buscarPorEpisodio(
        episodeId: String,
    ): EpisodeResponseSummaryEntity?

    /** Borra todas las respuestas guardadas localmente en el teléfono. */
    @Query("DELETE FROM mobile_episode_responses")
    suspend fun eliminarTodas()
}