package mx.edu.uttt.pulsocheck.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import mx.edu.uttt.pulsocheck.R
import mx.edu.uttt.pulsocheck.data.local.database.PulsoCheckDatabase
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateEpisodeRepository
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateReadingRepository
import mx.edu.uttt.pulsocheck.data.sensor.HealthServicesHeartRateDataSource
import mx.edu.uttt.pulsocheck.domain.model.EpisodeStatus
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.presentation.theme.PulsoCheckTheme

// Activity es el punto de entrada y conecta las dependencias de la aplicación.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Construye la cadena de almacenamiento: base de datos -> DAO -> repositorio.
        val database = PulsoCheckDatabase.obtenerInstancia(applicationContext)
        val readingRepository = RoomHeartRateReadingRepository(
            database.heartRateReadingDao(),
        )
        // Este repositorio guarda los episodios calculados por el dominio.
        val episodeRepository = RoomHeartRateEpisodeRepository(
            database.heartRateEpisodeDao(),
        )
        val heartRateDataSource = HealthServicesHeartRateDataSource(applicationContext)
        val viewModelFactory = TrackingViewModelFactory(
            readingRepository = readingRepository,
            episodeRepository = episodeRepository,
            heartRateDataSource = heartRateDataSource,
        )

        // ViewModelProvider conserva el ViewModel durante cambios de configuración.
        val viewModel = ViewModelProvider(
            this,
            viewModelFactory,
        )[TrackingViewModel::class.java]
        trackingViewModel = viewModel

        // Compose recibe el ViewModel ya configurado con el repositorio.
        setContent {
            WearApp(viewModel)
        }
    }

    // MeasureClient es para lecturas breves mientras la pantalla está activa.
    override fun onStop() {
        trackingViewModel?.detenerSeguimiento()
        super.onStop()
    }

    // Se guarda la referencia creada en onCreate para detenerla desde onStop.
    private var trackingViewModel: TrackingViewModel? = null
}

/** Observa las lecturas del ViewModel y las entrega a la pantalla. */
@Composable
fun WearApp(trackingViewModel: TrackingViewModel) {
    val readings = trackingViewModel.lecturas.collectAsState().value
    val trackingStatus = trackingViewModel.estadoSeguimiento.collectAsState().value
    val context = LocalContext.current
    // Compose vuelve a dibujar la pantalla cuando cambia la lista de episodios.
    val episodes = trackingViewModel.episodios.collectAsState().value

    // Android requiere pedir este permiso justo cuando la persona inicia la lectura.
    val heartRatePermission = if (Build.VERSION.SDK_INT >= 36) {
        "android.permission.health.READ_HEART_RATE"
    } else {
        Manifest.permission.BODY_SENSORS
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            trackingViewModel.iniciarSeguimiento()
        } else {
            trackingViewModel.indicarPermisoRequerido()
        }
    }

    PulsoCheckTheme {
        WearHomeScreen(
            episodes = episodes,
            readings = readings,
            trackingStatus = trackingStatus,
            onStart = {
                val permissionGranted = ContextCompat.checkSelfPermission(
                    context,
                    heartRatePermission,
                ) == PackageManager.PERMISSION_GRANTED

                if (permissionGranted) {
                    trackingViewModel.iniciarSeguimiento()
                } else {
                    permissionLauncher.launch(heartRatePermission)
                }
            },
            onStop = trackingViewModel::detenerSeguimiento,
        )
    }
}

/** Interfaz pura: recibe datos y no consulta directamente al ViewModel ni a Room. */
@Composable
private fun WearHomeScreen(
    readings: List<HeartRateReading>,
    episodes: List<HeartRateEpisode>,
    trackingStatus: TrackingStatus,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    AppScaffold {
        val listState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()

        ScreenScaffold(
            scrollState = listState,
        ) { contentPadding ->
            TransformingLazyColumn(
                contentPadding = contentPadding,
                state = listState,
            ) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(text = stringResource(R.string.app_name))
                    }
                }

                item {
                    val isActive = trackingStatus == TrackingStatus.LISTENING ||
                            trackingStatus == TrackingStatus.CHECKING_CAPABILITY
                    Button(
                        onClick = if (isActive) onStop else onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                    ) {
                        Text(if (isActive) "Detener lectura" else "Iniciar lectura")
                    }
                }

                item {
                    Text(
                        text = when (trackingStatus) {
                            TrackingStatus.IDLE -> "Lectura detenida"
                            TrackingStatus.CHECKING_CAPABILITY -> "Comprobando sensor…"
                            TrackingStatus.LISTENING -> "Leyendo frecuencia cardiaca…"
                            TrackingStatus.UNSUPPORTED -> "Este reloj no ofrece lectura de pulso"
                            TrackingStatus.PERMISSION_REQUIRED -> "Se necesita permiso de frecuencia cardiaca"
                            TrackingStatus.ERROR -> "No se pudo iniciar la lectura"
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    Text(
                        text = "Lecturas guardadas: ${readings.size}",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    val latestReading = readings.firstOrNull()
                    val message = latestReading?.let { reading ->
                        "Última lectura: ${reading.beatsPerMinute} lpm"
                    } ?: "Sin lecturas guardadas"

                    Text(
                        text = message,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                item {
                    // El caso de uso puede devolver episodios anteriores y uno activo.
                    val activeEpisode = episodes.lastOrNull { episode ->
                        episode.status == EpisodeStatus.ACTIVE
                    }

                    // Explicamos el estado sin presentar el episodio como diagnóstico.
                    val episodeMessage = activeEpisode?.let { episode ->
                        "Episodio activo: ${episode.readingIds.size} lecturas"
                    } ?: "Sin episodio activo"

                    Text(
                        text = "Episodios detectados: ${episodes.size}\n$episodeMessage",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// La vista previa usa una lista vacía y no necesita abrir la base de datos.
@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    PulsoCheckTheme {
        WearHomeScreen(
            readings = emptyList(),
            episodes = emptyList(),
            trackingStatus = TrackingStatus.IDLE,
            onStart = {},
            onStop = {},
        )
    }
}
