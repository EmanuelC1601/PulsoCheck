package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mx.edu.uttt.pulsocheck.data.sensor.HealthServicesHeartRateDataSource
import mx.edu.uttt.pulsocheck.data.sync.HeartRateDataLayerSender
import mx.edu.uttt.pulsocheck.domain.repository.EpisodeResponseRepository
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateEpisodeRepository
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository

/**
 * Construye TrackingViewModel y le entrega el repositorio que necesita.
 */
class TrackingViewModelFactory(
    private val readingRepository: HeartRateReadingRepository,
    private val episodeRepository: HeartRateEpisodeRepository,
    private val responseRepository: EpisodeResponseRepository,
    private val heartRateDataSource: HealthServicesHeartRateDataSource,
    private val dataLayerSender: HeartRateDataLayerSender,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica que esta fábrica esté creando el ViewModel esperado.
        if (!modelClass.isAssignableFrom(TrackingViewModel::class.java)) {
            throw IllegalArgumentException("Tipo de ViewModel no compatible.")
        }

        @Suppress("UNCHECKED_CAST")
        return TrackingViewModel(
            readingRepository = readingRepository,
            episodeRepository = episodeRepository,
            responseRepository = responseRepository,
            heartRateDataSource = heartRateDataSource,
            dataLayerSender = dataLayerSender,
        ) as T
    }
}
