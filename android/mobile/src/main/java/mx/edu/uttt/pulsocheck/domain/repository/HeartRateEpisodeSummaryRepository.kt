package mx.edu.uttt.pulsocheck.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary

/** Define las operaciones de episodios que necesita la app móvil. */
interface HeartRateEpisodeSummaryRepository {

    fun observarDesde(
        desdeEpochMillis: Long,
    ): Flow<List<HeartRateEpisodeSummary>>

    suspend fun guardar(episode: HeartRateEpisodeSummary)

    suspend fun eliminarAnterioresA(fechaLimite: Long)
}