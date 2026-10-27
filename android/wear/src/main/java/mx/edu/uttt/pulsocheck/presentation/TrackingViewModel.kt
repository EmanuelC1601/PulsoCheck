package mx.edu.uttt.pulsocheck.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.uttt.pulsocheck.data.sensor.HealthServicesHeartRateDataSource
import mx.edu.uttt.pulsocheck.domain.config.HeartRateMonitoringConfig
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.model.ReadingOrigin
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateEpisodeRepository
import mx.edu.uttt.pulsocheck.domain.repository.HeartRateReadingRepository
import mx.edu.uttt.pulsocheck.domain.usecase.DetectHeartRateEpisodesUseCase

/** Estados que la pantalla puede mostrar durante el seguimiento. */
enum class TrackingStatus {
    IDLE,
    CHECKING_CAPABILITY,
    LISTENING,
    UNSUPPORTED,
    PERMISSION_REQUIRED,
    ERROR,
}

/**
 * Coordina las lecturas, el almacenamiento y la detección de episodios.
 *
 * La pantalla observa los estados de este ViewModel. No accede directamente
 * al sensor, al repositorio ni al caso de uso.
 */
class TrackingViewModel(
    private val readingRepository: HeartRateReadingRepository,
    private val episodeRepository: HeartRateEpisodeRepository,
    private val heartRateDataSource: HealthServicesHeartRateDataSource,
    private val monitoringConfig: HeartRateMonitoringConfig =
        HeartRateMonitoringConfig(),
    private val detectEpisodes: DetectHeartRateEpisodesUseCase =
        DetectHeartRateEpisodesUseCase(),
) : ViewModel() {

    // La lista de lecturas guardadas se mantiene como estado observable.
    private val _lecturas = MutableStateFlow<List<HeartRateReading>>(emptyList())
    val lecturas: StateFlow<List<HeartRateReading>> = _lecturas.asStateFlow()

    // Los episodios se calculan a partir de las lecturas guardadas.
    private val _episodios = MutableStateFlow<List<HeartRateEpisode>>(emptyList())
    val episodios: StateFlow<List<HeartRateEpisode>> = _episodios.asStateFlow()

    // La pantalla observa este estado para mostrar qué sucede con el sensor.
    private val _estadoSeguimiento = MutableStateFlow(TrackingStatus.IDLE)
    val estadoSeguimiento: StateFlow<TrackingStatus> =
        _estadoSeguimiento.asStateFlow()

    // Guardamos la tarea para poder cancelar la lectura cuando se detenga.
    private var tareaDeLectura: Job? = null

    init {
        // Room emite los episodios guardados cada vez que la tabla cambia.
        viewModelScope.launch {
            episodeRepository.observarTodos().collect { episodiosGuardados ->
                _episodios.value = episodiosGuardados
            }
        }

        // Room emite una lista nueva cada vez que cambian las lecturas.
        viewModelScope.launch {
            readingRepository.observarTodas().collect { lecturasGuardadas ->
                _lecturas.value = lecturasGuardadas

                // Aplicamos la regla del dominio usando los parámetros configurados.
                val episodiosDetectados = detectEpisodes(
                    readings = lecturasGuardadas,
                    thresholdBpm = monitoringConfig.thresholdBpm,
                    maximumGapMillis = monitoringConfig.maximumGapMillis,
                )

                // Al guardar, el flujo de Room actualiza el estado de episodios.
                episodeRepository.guardarTodos(episodiosDetectados)
            }
        }
    }

    /** Comprueba el sensor y comienza a guardar las lecturas que reciba. */
    fun iniciarSeguimiento() {
        // Evita registrar dos veces el mismo listener del sensor.
        if (tareaDeLectura?.isActive == true) return

        tareaDeLectura = viewModelScope.launch {
            _estadoSeguimiento.value = TrackingStatus.CHECKING_CAPABILITY

            try {
                // Algunos relojes no ofrecen frecuencia cardiaca a esta API.
                if (!heartRateDataSource.admiteFrecuenciaCardiaca()) {
                    _estadoSeguimiento.value = TrackingStatus.UNSUPPORTED
                    return@launch
                }

                _estadoSeguimiento.value = TrackingStatus.LISTENING

                // Cada valor nuevo se convierte al modelo propio de PulsoCheck.
                heartRateDataSource.observarFrecuenciaCardiaca().collect { bpm ->
                    val reading = HeartRateReading(
                        id = UUID.randomUUID().toString(),
                        beatsPerMinute = bpm,
                        measuredAtEpochMillis = System.currentTimeMillis(),
                        origin = ReadingOrigin.HEALTH_SERVICES_ON_WATCH,
                    )

                    // Al guardar, Room emite la lista actualizada y se recalculan episodios.
                    readingRepository.guardar(reading)
                }
            } catch (error: SecurityException) {
                // Puede ocurrir si se revoca el permiso desde Ajustes del reloj.
                _estadoSeguimiento.value = TrackingStatus.PERMISSION_REQUIRED
            } catch (error: CancellationException) {
                // Detener el seguimiento cancela la corrutina; eso es normal.
                throw error
            } catch (error: Exception) {
                // Un error del sensor o del servicio se refleja como estado de error.
                _estadoSeguimiento.value = TrackingStatus.ERROR
            }
        }
    }

    /** Cancela la escucha activa y libera el sensor. */
    fun detenerSeguimiento() {
        tareaDeLectura?.cancel()
        tareaDeLectura = null
        _estadoSeguimiento.value = TrackingStatus.IDLE
    }

    /** Actualiza el estado si la persona rechaza el permiso del sensor. */
    fun indicarPermisoRequerido() {
        _estadoSeguimiento.value = TrackingStatus.PERMISSION_REQUIRED
    }
}
