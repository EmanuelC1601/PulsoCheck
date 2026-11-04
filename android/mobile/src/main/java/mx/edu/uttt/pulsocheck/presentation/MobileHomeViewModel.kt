package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateEpisodeSummaryRepository
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponseSummary
import mx.edu.uttt.pulsocheck.domain.repository.EpisodeResponseSummaryRepository

/** Prepara lecturas y episodios recientes para la pantalla móvil. */
class MobileHomeViewModel(
    readingRepository: HeartRateReadingRepository,
    episodeRepository: HeartRateEpisodeSummaryRepository,
    responseRepository: EpisodeResponseSummaryRepository,
) : ViewModel() {

    private val _readings = MutableStateFlow<List<HeartRateReading>>(emptyList())
    val readings: StateFlow<List<HeartRateReading>> = _readings.asStateFlow()

    private val _episodes =
        MutableStateFlow<List<HeartRateEpisodeSummary>>(emptyList())
    val episodes: StateFlow<List<HeartRateEpisodeSummary>> =
        _episodes.asStateFlow()

    // La pantalla observa las respuestas recibidas y guardadas en Room.
    private val _responses = MutableStateFlow<List<EpisodeResponseSummary>>(emptyList())
    val responses: StateFlow<List<EpisodeResponseSummary>> = _responses.asStateFlow()

    init {
        // Ambas consultas usan la misma ventana de cinco días.
        val fiveDaysAgo =
            System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)

        // Room actualiza la lista automáticamente cuando llegan nuevas lecturas.
        viewModelScope.launch {
            readingRepository.observarDesde(fiveDaysAgo).collect { savedReadings ->
                _readings.value = savedReadings
            }
        }

        // Este flujo se actualiza cuando el servicio guarda un episodio recibido.
        viewModelScope.launch {
            episodeRepository.observarDesde(fiveDaysAgo).collect { savedEpisodes ->
                _episodes.value = savedEpisodes
            }
        }
        // Room actualiza esta lista cuando el servicio móvil guarda una respuesta.
        viewModelScope.launch {
            responseRepository.observarTodas().collect { savedResponses ->
                _responses.value = savedResponses
            }
        }
    }
}