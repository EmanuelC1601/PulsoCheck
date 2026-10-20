package mx.edu.uttt.pulsocheck.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
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
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateReadingRepository
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.presentation.theme.PulsoCheckTheme

// Activity es el punto de entrada y conecta las dependencias de la aplicación.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Construye la cadena de almacenamiento: base de datos -> DAO -> repositorio.
        val database = PulsoCheckDatabase.obtenerInstancia(applicationContext)
        val repository = RoomHeartRateReadingRepository(
            database.heartRateReadingDao(),
        )
        val viewModelFactory = TrackingViewModelFactory(repository)

        // ViewModelProvider conserva el ViewModel durante cambios de configuración.
        val trackingViewModel = ViewModelProvider(
            this,
            viewModelFactory,
        )[TrackingViewModel::class.java]

        // Compose recibe el ViewModel ya configurado con el repositorio.
        setContent {
            WearApp(trackingViewModel)
        }
    }
}

/** Observa las lecturas del ViewModel y las entrega a la pantalla. */
@Composable
fun WearApp(trackingViewModel: TrackingViewModel) {
    val readings = trackingViewModel.lecturas.collectAsState().value

    PulsoCheckTheme {
        WearHomeScreen(readings)
    }
}

/** Interfaz pura: recibe datos y no consulta directamente al ViewModel ni a Room. */
@Composable
private fun WearHomeScreen(readings: List<HeartRateReading>) {
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
        WearHomeScreen(readings = emptyList())
    }
}
