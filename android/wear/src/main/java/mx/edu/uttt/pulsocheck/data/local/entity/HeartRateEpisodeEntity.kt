package mx.edu.uttt.pulsocheck.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import mx.edu.uttt.pulsocheck.domain.model.EpisodeStatus
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode

/**
 * Representa un episodio guardado como una fila de la tabla de Room.
 */
@Entity(tableName = "heart_rate_episodes")
data class HeartRateEpisodeEntity(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "started_at_epoch_millis")
    val startedAtEpochMillis: Long,

    @ColumnInfo(name = "ended_at_epoch_millis")
    val endedAtEpochMillis: Long?,

    // Guardamos la lista de IDs como JSON en una sola columna de texto.
    @ColumnInfo(name = "reading_ids_json")
    val readingIdsJson: String,

    // Room almacena el estado como texto, por ejemplo "ACTIVE" o "CLOSED".
    val status: String,
) {
    /** Convierte la fila de Room al modelo que usa el dominio. */
    fun toDomain(): HeartRateEpisode {
        val jsonArray = JSONArray(readingIdsJson)
        val readingIds = List(jsonArray.length()) { index ->
            jsonArray.getString(index)
        }

        return HeartRateEpisode(
            id = id,
            startedAtEpochMillis = startedAtEpochMillis,
            endedAtEpochMillis = endedAtEpochMillis,
            readingIds = readingIds,
            status = EpisodeStatus.valueOf(status),
        )
    }

    companion object {
        /** Convierte el modelo del dominio a una fila que Room puede guardar. */
        fun fromDomain(episode: HeartRateEpisode): HeartRateEpisodeEntity {
            val jsonArray = JSONArray()
            episode.readingIds.forEach { readingId ->
                jsonArray.put(readingId)
            }

            return HeartRateEpisodeEntity(
                id = episode.id,
                startedAtEpochMillis = episode.startedAtEpochMillis,
                endedAtEpochMillis = episode.endedAtEpochMillis,
                readingIdsJson = jsonArray.toString(),
                status = episode.status.name,
            )
        }
    }
}