package mx.edu.uttt.pulsocheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateEpisodeSummaryEntity

/** Consultas para guardar y observar episodios en el teléfono. */
@Dao
interface HeartRateEpisodeSummaryDao {

    // Si el episodio activo vuelve a llegar con cambios, reemplazamos su fila.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(episode: HeartRateEpisodeSummaryEntity)

    // Devuelve solo episodios recientes, del más nuevo al más antiguo.
    @Query(
        """
        SELECT * FROM mobile_heart_rate_episodes
        WHERE started_at_epoch_millis >= :desdeEpochMillis
        ORDER BY started_at_epoch_millis DESC
        """,
    )
    fun observarDesde(
        desdeEpochMillis: Long,
    ): Flow<List<HeartRateEpisodeSummaryEntity>>

    // El teléfono conserva los episodios de la ventana de cinco días.
    @Query(
        """
        DELETE FROM mobile_heart_rate_episodes
        WHERE started_at_epoch_millis < :fechaLimite
        """,
    )
    suspend fun eliminarAnterioresA(fechaLimite: Long)
    /** Borra todos los episodios guardados localmente en el teléfono. */
    @Query("DELETE FROM mobile_heart_rate_episodes")
    suspend fun eliminarTodos()
}