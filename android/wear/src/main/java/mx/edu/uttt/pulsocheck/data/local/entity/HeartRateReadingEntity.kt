package mx.edu.uttt.pulsocheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.model.ReadingOrigin

/**
 * Representa cómo se guarda una lectura de pulso en la base de datos.
 *
 * La entidad pertenece a la capa de datos. El modelo HeartRateReading
 * pertenece al dominio, por eso usamos funciones para convertir entre ambos.
 */
@Entity(tableName = "heart_rate_readings")
data class HeartRateReadingEntity(

    // Room usa este identificador como clave única de cada lectura.
    @PrimaryKey
    val id: String,

    // Nombre explícito de la columna para que sea fácil reconocerla en Room.
    @ColumnInfo(name = "beats_per_minute")
    val beatsPerMinute: Int,

    @ColumnInfo(name = "measured_at_epoch_millis")
    val measuredAtEpochMillis: Long,

    // Guardamos el origen como texto para no acoplar la base a un enum de Kotlin.
    val origin: String,
) {
    /**
     * Convierte una fila de Room al modelo que usa la lógica del proyecto.
     */
    fun toDomain(): HeartRateReading {
        return HeartRateReading(
            id = id,
            beatsPerMinute = beatsPerMinute,
            measuredAtEpochMillis = measuredAtEpochMillis,
            origin = ReadingOrigin.valueOf(origin),
        )
    }

    companion object {
        /**
         * Convierte una lectura del dominio en una fila que Room puede guardar.
         */
        fun fromDomain(reading: HeartRateReading): HeartRateReadingEntity {
            return HeartRateReadingEntity(
                id = reading.id,
                beatsPerMinute = reading.beatsPerMinute,
                measuredAtEpochMillis = reading.measuredAtEpochMillis,
                origin = reading.origin.name,
            )
        }
    }
}