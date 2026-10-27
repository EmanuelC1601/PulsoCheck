package mx.edu.uttt.pulsocheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponse
import mx.edu.uttt.pulsocheck.domain.model.FeelingOption

/**
 * Guarda la respuesta de la persona y la relaciona con su episodio.
 */
@Entity(
    tableName = "episode_responses",
    foreignKeys = [
        ForeignKey(
            entity = HeartRateEpisodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["episode_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        // Ayuda a Room a buscar respuestas por episodio.
        Index(value = ["episode_id"]),
    ],
)
data class EpisodeResponseEntity(
    // Usamos el ID del episodio como clave para permitir una respuesta por episodio.
    @PrimaryKey
    @ColumnInfo(name = "episode_id")
    val episodeId: String,

    // Room guarda el nombre de la opción, por ejemplo "GOOD".
    @ColumnInfo(name = "feeling_option")
    val feelingOption: String,

    @ColumnInfo(name = "answered_at_epoch_millis")
    val answeredAtEpochMillis: Long,
) {
    /** Convierte la fila de Room al modelo de respuesta del dominio. */
    fun toDomain(): EpisodeResponse {
        return EpisodeResponse(
            episodeId = episodeId,
            feeling = FeelingOption.valueOf(feelingOption),
            answeredAtEpochMillis = answeredAtEpochMillis,
        )
    }

    companion object {
        /** Convierte la respuesta del dominio a una fila de Room. */
        fun fromDomain(response: EpisodeResponse): EpisodeResponseEntity {
            return EpisodeResponseEntity(
                episodeId = response.episodeId,
                feelingOption = response.feeling.name,
                answeredAtEpochMillis = response.answeredAtEpochMillis,
            )
        }
    }
}