package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository

/** Prepara para la pantalla el historial recibido en el teléfono. */
class MobileHomeViewModel(
    repository: HeartRateReadingRepository,
) : ViewModel() {

    private val _readings = MutableStateFlow<List<HeartRateReading>>(emptyList())
    val readings: StateFlow<List<HeartRateReading>> = _readings.asStateFlow()

    init {
        // Solo consultamos los cinco días recientes solicitados para el proyecto.
        val fiveDaysAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)
        viewModelScope.launch {
            repository.observarDesde(fiveDaysAgo).collect { synchronizedReadings ->
                _readings.value = synchronizedReadings
            }
        }
    }
}
