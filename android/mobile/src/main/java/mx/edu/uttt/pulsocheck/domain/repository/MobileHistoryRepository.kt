package mx.edu.uttt.pulsocheck.domain.repository

/** Define la operación para borrar el historial guardado en el teléfono. */
interface MobileHistoryRepository {

    // Borra lecturas, episodios y respuestas en una sola operación.
    suspend fun borrarHistorialCompleto()
}