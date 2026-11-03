package mx.edu.uttt.pulsocheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import mx.edu.uttt.pulsocheck.data.local.database.MobileDatabase
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateReadingRepository
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.presentation.MobileHomeViewModel
import mx.edu.uttt.pulsocheck.presentation.MobileHomeViewModelFactory
import mx.edu.uttt.pulsocheck.ui.theme.PulsoCheckTheme

/** Punto de entrada de la aplicación para teléfono. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Construye Room y el repositorio que abastece el historial.
        val database = MobileDatabase.obtenerInstancia(applicationContext)
        val repository = RoomHeartRateReadingRepository(database.heartRateReadingDao())
        val viewModel = ViewModelProvider(
            this,
            MobileHomeViewModelFactory(repository),
        )[MobileHomeViewModel::class.java]

        setContent {
            val readings = viewModel.readings.collectAsState().value

            PulsoCheckTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MobileHomeScreen(
                        readings = readings,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

/** Historial sencillo de las lecturas que el reloj sincronizó al teléfono. */
@Composable
private fun MobileHomeScreen(
    readings: List<HeartRateReading>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text(
            text = "PulsoCheck",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Historial de frecuencia cardiaca",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Lecturas recibidas en los últimos cinco días: ${readings.size}",
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )

        if (readings.isEmpty()) {
            Text(
                text = "Aún no hay lecturas. Cuando el reloj sincronice datos, aparecerán aquí.",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    items = readings,
                    key = { reading -> reading.id },
                ) { reading ->
                    ReadingCard(reading)
                }
            }
        }
    }
}

/** Presenta una lectura y el momento en que fue tomada por el reloj. */
@Composable
private fun ReadingCard(reading: HeartRateReading) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${reading.beatsPerMinute} lpm",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = formatReadingTime(reading.measuredAtEpochMillis),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

/** Convierte la marca de tiempo del reloj a una fecha legible en el teléfono. */
private fun formatReadingTime(epochMillis: Long): String {
    val formatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy, HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    return formatter.format(Instant.ofEpochMilli(epochMillis))
}
