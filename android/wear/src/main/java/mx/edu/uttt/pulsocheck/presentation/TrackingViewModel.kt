package mx.edu.uttt.pulsocheck.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class TrackingViewModel : ViewModel() {

    // Estado interno: solo el ViewModel puede cambiarlo.
    private val _seguimientoActivo = mutableStateOf(false)

    // La pantalla puede leer el estado, pero no modificarlo directamente.
    val seguimientoActivo: State<Boolean> = _seguimientoActivo

    // Por ahora alterna la demostración; más adelante iniciará el sensor.
    fun alternarSeguimiento() {
        _seguimientoActivo.value = !_seguimientoActivo.value
    }
}