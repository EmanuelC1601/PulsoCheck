package mx.edu.uttt.pulsocheck.data.repository

import androidx.room.withTransaction
import mx.edu.uttt.pulsocheck.data.local.database.MobileDatabase
import mx.edu.uttt.pulsocheck.domain.repository.MobileHistoryRepository

/** Implementa el borrado usando la base de datos local del teléfono. */
class RoomMobileHistoryRepository(
    private val database: MobileDatabase,
) : MobileHistoryRepository {

    override suspend fun borrarHistorialCompleto() {
        // Room ejecuta estas instrucciones como una sola transacción.
        database.withTransaction {
            // Primero se borran las respuestas, que dependen de los episodios.
            database.episodeResponseSummaryDao().eliminarTodas()

            // Después se borran los episodios y las lecturas.
            database.heartRateEpisodeSummaryDao().eliminarTodos()
            database.heartRateReadingDao().eliminarTodas()
        }
    }
}