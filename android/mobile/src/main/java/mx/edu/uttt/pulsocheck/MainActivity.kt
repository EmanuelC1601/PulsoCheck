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
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateEpisodeSummaryRepository
import mx.edu.uttt.pulsocheck.domain.model.EpisodeStatus
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary
import mx.edu.uttt.pulsocheck.data.repository.RoomEpisodeResponseSummaryRepository
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponseSummary
import mx.edu.uttt.pulsocheck.domain.model.FeelingOption

/** Punto de entrada de la aplicación para teléfono. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Room guarda las lecturas y los episodios en tablas separadas.
        val database = MobileDatabase.obtenerInstancia(applicationContext)
        val readingRepository =
            RoomHeartRateReadingRepository(database.heartRateReadingDao())
        val episodeRepository = RoomHeartRateEpisodeSummaryRepository(
            database.heartRateEpisodeSummaryDao(),
        )
        val responseRepository = RoomEpisodeResponseSummaryRepository(
            database.episodeResponseSummaryDao(),
        )

        // El ViewModel observa los tres repositorios de la pantalla.
        val viewModel = ViewModelProvider(
            this,
            MobileHomeViewModelFactory(
                readingRepository = readingRepository,
                episodeRepository = episodeRepository,
                responseRepository = responseRepository,
            ),
        )[MobileHomeViewModel::class.java]

        setContent {
            val readings = viewModel.readings.collectAsState().value
            val episodes = viewModel.episodes.collectAsState().value
            val responses = viewModel.responses.collectAsState().value

            PulsoCheckTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MobileHomeScreen(
                        readings = readings,
                        episodes = episodes,
                        responses = responses,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

/** Muestra episodios y lecturas recibidos del reloj. */
@Composable
private fun MobileHomeScreen(
    readings: List<HeartRateReading>,
    episodes: List<HeartRateEpisodeSummary>,
    responses: List<EpisodeResponseSummary>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "PulsoCheck",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Historial de los últimos cinco días",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        item {
            Text(
                text = "Episodios (${episodes.size})",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (episodes.isEmpty()) {
            item {
                Text("Aún no se han recibido episodios del reloj.")
            }
        } else {
            items(
                items = episodes,
                key = { episode -> "episode-${episode.episodeId}" },
            ) { episode ->
                EpisodeCard(
                    episode = episode,
                    response = responses.firstOrNull { response ->
                        response.episodeId == episode.episodeId
                    },
                )
            }
        }

        item {
            Text(
                text = "Lecturas (${readings.size})",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (readings.isEmpty()) {
            item {
                Text("Aún no hay lecturas recibidas del reloj.")
            }
        } else {
            items(
                items = readings,
                key = { reading -> "reading-${reading.id}" },
            ) { reading ->
                ReadingCard(reading)
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
/** Presenta un resumen; no lo interpreta como diagnóstico. */
@Composable
private fun EpisodeCard(
    episode: HeartRateEpisodeSummary,
    response: EpisodeResponseSummary?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (episode.status == EpisodeStatus.ACTIVE) {
                    "Episodio activo"
                } else {
                    "Episodio cerrado"
                },
                style = MaterialTheme.typography.titleLarge,
            )
            Text("${episode.readingCount} lecturas")
            Text("Inicio: ${formatReadingTime(episode.startedAtEpochMillis)}")

            episode.endedAtEpochMillis?.let { endedAt ->
                Text("Fin: ${formatReadingTime(endedAt)}")
            }

            // La respuesta es opcional: quizá la persona aún no la registró.
            if (response == null) {
                Text("Sin respuesta registrada")
            } else {
                val feelingText = when (response.feeling) {
                    FeelingOption.GOOD -> "Bien"
                    FeelingOption.OKAY -> "Regular"
                    FeelingOption.BAD -> "Mal"
                }

                Text("Cómo se sintió: $feelingText")
                Text(
                    "Respuesta: ${formatReadingTime(response.answeredAtEpochMillis)}",
                )
            }
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
