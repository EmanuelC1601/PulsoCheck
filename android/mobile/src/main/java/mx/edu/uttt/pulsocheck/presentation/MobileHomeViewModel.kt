package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
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
import mx.edu.uttt.pulsocheck.domain.repository.MobileHistoryRepository

/** Prepara lecturas y episodios recientes para la pantalla móvil. */
class MobileHomeViewModel(
    readingRepository: HeartRateReadingRepository,
    episodeRepository: HeartRateEpisodeSummaryRepository,
    responseRepository: EpisodeResponseSummaryRepository,
    private val historyRepository: MobileHistoryRepository,
) : ViewModel() {
    private val _borrandoHistorial = MutableStateFlow(false)
    val borrandoHistorial: StateFlow<Boolean> = _borrandoHistorial.asStateFlow()

    private val _mensajeHistorial = MutableStateFlow<String?>(null)
    val mensajeHistorial: StateFlow<String?> = _mensajeHistorial.asStateFlow()

    /** Borra del teléfono las lecturas, episodios y respuestas guardadas. */
    fun borrarHistorialCompleto() {
        // Evita iniciar dos borrados si se pulsa varias veces seguidas.
        if (_borrandoHistorial.value) return

        viewModelScope.launch {
            _borrandoHistorial.value = true
            _mensajeHistorial.value = null

            try {
                historyRepository.borrarHistorialCompleto()
                _mensajeHistorial.value = "Se borró el historial guardado en este teléfono."
            } catch (cancelled: CancellationException) {
                // Conserva el comportamiento normal si Android cancela la tarea.
                throw cancelled
            } catch (_: Exception) {
                _mensajeHistorial.value =
                    "No se pudo borrar el historial. Intenta de nuevo."
            } finally {
                _borrandoHistorial.value = false
            }
        }
    }

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
