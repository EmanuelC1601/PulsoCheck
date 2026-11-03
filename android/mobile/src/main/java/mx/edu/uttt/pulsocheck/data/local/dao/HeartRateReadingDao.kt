package mx.edu.uttt.pulsocheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity

/** Operaciones de Room para las lecturas que llegan al teléfono. */
@Dao
interface HeartRateReadingDao {

    // El mismo ID puede recibirse de nuevo si Data Layer reintenta la entrega.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(reading: HeartRateReadingEntity)

    // La consulta incluye solo los últimos cinco días y más recientes primero.
    @Query(
        """
        SELECT * FROM mobile_heart_rate_readings
        WHERE measured_at_epoch_millis >= :desdeEpochMillis
        ORDER BY measured_at_epoch_millis DESC
        """,
    )
    fun observarDesde(desdeEpochMillis: Long): Flow<List<HeartRateReadingEntity>>

    // La base móvil conserva solo el rango de historial acordado para el proyecto.
    @Query("DELETE FROM mobile_heart_rate_readings WHERE measured_at_epoch_millis < :cutoff")
    suspend fun eliminarAnterioresA(cutoff: Long)
}
