package mx.edu.uttt.pulsocheck.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading

/** Contrato que separa la pantalla de la tecnología Room. */
interface HeartRateReadingRepository {
    fun observarDesde(desdeEpochMillis: Long): Flow<List<HeartRateReading>>
    suspend fun guardar(reading: HeartRateReading)
    suspend fun eliminarAnterioresA(cutoff: Long)
}
