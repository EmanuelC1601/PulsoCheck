package mx.edu.uttt.pulsocheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponseSummary
import mx.edu.uttt.pulsocheck.domain.model.FeelingOption

/** Fila local de la respuesta vinculada a un episodio del teléfono. */
@Entity(
    tableName = "mobile_episode_responses",
    foreignKeys = [
        ForeignKey(
            entity = HeartRateEpisodeSummaryEntity::class,
            parentColumns = ["episode_id"],
            childColumns = ["episode_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["episode_id"]),
    ],
)
data class EpisodeResponseSummaryEntity(
    // Solo puede existir una respuesta por episodio.
    @PrimaryKey
    @ColumnInfo(name = "episode_id")
    val episodeId: String,

    // Guardamos el nombre de la opción, por ejemplo "GOOD".
    @ColumnInfo(name = "feeling_option")
    val feelingOption: String,

    @ColumnInfo(name = "answered_at_epoch_millis")
    val answeredAtEpochMillis: Long,
) {
    /** Convierte la fila de Room al modelo de dominio. */
    fun toDomain() = EpisodeResponseSummary(
        episodeId = episodeId,
        feeling = FeelingOption.valueOf(feelingOption),
        answeredAtEpochMillis = answeredAtEpochMillis,
    )

    companion object {
        /** Convierte una respuesta del dominio a una fila de Room. */
        fun fromDomain(response: EpisodeResponseSummary) =
            EpisodeResponseSummaryEntity(
                episodeId = response.episodeId,
                feelingOption = response.feeling.name,
                answeredAtEpochMillis = response.answeredAtEpochMillis,
            )
    }
}