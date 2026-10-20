package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository

/**
 * Prepara los datos que la pantalla del reloj necesita mostrar.
 *
 * El ViewModel observa el repositorio y no conoce directamente Room.
 */
class TrackingViewModel(
    private val readingRepository: HeartRateReadingRepository,
) : ViewModel() {

    // Estado privado: solo el ViewModel puede actualizar la lista.
    private val _lecturas = MutableStateFlow<List<HeartRateReading>>(emptyList())

    // La interfaz observa este StateFlow y se refresca cuando llegan cambios.
    val lecturas: StateFlow<List<HeartRateReading>> = _lecturas.asStateFlow()

    init {
        // viewModelScope cancela esta tarea cuando el ViewModel deja de existir.
        viewModelScope.launch {
            readingRepository.observarTodas().collect { lecturasGuardadas ->
                _lecturas.value = lecturasGuardadas
            }
        }
    }
}
