package mx.edu.uttt.pulsocheck.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity

/**
 * Define las operaciones para consultar y guardar lecturas.
 *
 * Room genera la implementación de esta interfaz durante la compilación.
 */
@Dao
interface HeartRateReadingDao {

    /**
     * Guarda una lectura.
     *
     * Si ya existe una lectura con el mismo ID, la reemplaza. Esto evita
     * duplicados cuando el reloj intente sincronizarla otra vez.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(lectura: HeartRateReadingEntity)

    /**
     * Guarda varias lecturas en una sola operación.
     * Será útil cuando sincronicemos un bloque desde el teléfono.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarTodas(lecturas: List<HeartRateReadingEntity>)

    /**
     * Observa las lecturas y las ordena de la más nueva a la más antigua.
     *
     * Flow avisa cuando la tabla cambia para que luego la interfaz
     * pueda actualizarse sin repetir consultas manualmente.
     */
    @Query(
        """
        SELECT * FROM heart_rate_readings
        ORDER BY measured_at_epoch_millis DESC
        """
    )
    fun observarTodas(): Flow<List<HeartRateReadingEntity>>

    /**
     * Busca una lectura por su identificador.
     * Devuelve null si todavía no existe.
     */
    @Query(
        """
        SELECT * FROM heart_rate_readings
        WHERE id = :identificador
        LIMIT 1
        """
    )
    suspend fun buscarPorId(
        identificador: String,
    ): HeartRateReadingEntity?
}