package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateEpisodeSummaryRepository
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository
import mx.edu.uttt.pulsocheck.domain.repository.EpisodeResponseSummaryRepository
import mx.edu.uttt.pulsocheck.domain.repository.MobileHistoryRepository

/** Construye el ViewModel con los repositorios de lecturas y episodios. */
class MobileHomeViewModelFactory(
    private val readingRepository: HeartRateReadingRepository,
    private val episodeRepository: HeartRateEpisodeSummaryRepository,
    private val responseRepository: EpisodeResponseSummaryRepository,
    private val historyRepository: MobileHistoryRepository,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(MobileHomeViewModel::class.java)) {
            throw IllegalArgumentException("Tipo de ViewModel no compatible.")
        }

        @Suppress("UNCHECKED_CAST")
        return MobileHomeViewModel(
            readingRepository = readingRepository,
            episodeRepository = episodeRepository,
            responseRepository = responseRepository,
            historyRepository = historyRepository,
        ) as T
    }
}