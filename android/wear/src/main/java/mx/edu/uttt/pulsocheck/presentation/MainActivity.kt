package mx.edu.uttt.pulsocheck.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
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
import mx.edu.uttt.pulsocheck.presentation.theme.PulsoCheckTheme

// Activity es el punto de entrada de esta aplicación Wear OS.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Compose construye la pantalla del reloj.
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    // Obtiene el ViewModel que mantiene el estado de esta pantalla.
    val trackingViewModel: TrackingViewModel = viewModel()

    // Compose observa este valor y vuelve a dibujar la pantalla cuando cambia.
    val seguimientoActivo = trackingViewModel.seguimientoActivo.value

    PulsoCheckTheme {
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
                    // Encabezado con el nombre definido en strings.xml.
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

                    // El botón envía una acción al ViewModel.
                    item {
                        Button(
                            onClick = {
                                trackingViewModel.alternarSeguimiento()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec),
                        ) {
                            Text(
                                text = if (seguimientoActivo) {
                                    "Detener demo"
                                } else {
                                    "Iniciar demo"
                                }
                            )
                        }
                    }

                    // Indica el estado actual de la demostración.
                    item {
                        Text(
                            text = if (seguimientoActivo) {
                                "Demostración activa"
                            } else {
                                "Seguimiento detenido"
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

// Vista previa para revisar el diseño del reloj en Android Studio.
@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp()
}