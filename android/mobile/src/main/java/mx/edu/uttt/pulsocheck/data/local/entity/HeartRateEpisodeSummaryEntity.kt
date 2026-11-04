package mx.edu.uttt.pulsocheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import mx.edu.uttt.pulsocheck.domain.model.EpisodeStatus
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary

/** Fila local que guarda el resumen del episodio recibido del reloj. */
@Entity(tableName = "mobile_heart_rate_episodes")
data class HeartRateEpisodeSummaryEntity(
    @PrimaryKey
    @ColumnInfo(name = "episode_id")
    val episodeId: String,

    @ColumnInfo(name = "started_at_epoch_millis")
    val startedAtEpochMillis: Long,

    @ColumnInfo(name = "ended_at_epoch_millis")
    val endedAtEpochMillis: Long?,

    @ColumnInfo(name = "reading_count")
    val readingCount: Int,

    // Room almacena el nombre del estado, por ejemplo "ACTIVE".
    val status: String,
) {
    /** Convierte la fila de Room al modelo usado por la aplicación. */
    fun toDomain() = HeartRateEpisodeSummary(
        episodeId = episodeId,
        startedAtEpochMillis = startedAtEpochMillis,
        endedAtEpochMillis = endedAtEpochMillis,
        readingCount = readingCount,
        status = EpisodeStatus.valueOf(status),
    )

    companion object {
        /** Convierte el modelo al formato que Room puede guardar. */
        fun fromDomain(episode: HeartRateEpisodeSummary) =
            HeartRateEpisodeSummaryEntity(
                episodeId = episode.episodeId,
                startedAtEpochMillis = episode.startedAtEpochMillis,
                endedAtEpochMillis = episode.endedAtEpochMillis,
                readingCount = episode.readingCount,
                status = episode.status.name,
            )
    }
}