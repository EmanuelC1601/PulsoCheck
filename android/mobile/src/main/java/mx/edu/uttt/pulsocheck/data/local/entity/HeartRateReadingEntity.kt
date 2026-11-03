package mx.edu.uttt.pulsocheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading

/** Fila local de Room para las lecturas sincronizadas desde el reloj. */
@Entity(tableName = "mobile_heart_rate_readings")
data class HeartRateReadingEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "beats_per_minute")
    val beatsPerMinute: Int,

    @ColumnInfo(name = "measured_at_epoch_millis")
    val measuredAtEpochMillis: Long,
) {
    /** Convierte la fila guardada al modelo que consume la aplicación. */
    fun toDomain() = HeartRateReading(
        id = id,
        beatsPerMinute = beatsPerMinute,
        measuredAtEpochMillis = measuredAtEpochMillis,
    )

    companion object {
        /** Convierte el dato recibido al formato que Room almacena. */
        fun fromDomain(reading: HeartRateReading) = HeartRateReadingEntity(
            id = reading.id,
            beatsPerMinute = reading.beatsPerMinute,
            measuredAtEpochMillis = reading.measuredAtEpochMillis,
        )
    }
}
