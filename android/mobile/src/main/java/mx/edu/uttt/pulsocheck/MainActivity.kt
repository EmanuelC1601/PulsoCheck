package mx.edu.uttt.pulsocheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import mx.edu.uttt.pulsocheck.ui.theme.BrandCoral
import mx.edu.uttt.pulsocheck.ui.theme.BrandNavy
import mx.edu.uttt.pulsocheck.ui.theme.BrandTeal
import mx.edu.uttt.pulsocheck.data.repository.RoomHeartRateEpisodeSummaryRepository
import mx.edu.uttt.pulsocheck.domain.model.EpisodeStatus
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisodeSummary
import mx.edu.uttt.pulsocheck.data.repository.RoomEpisodeResponseSummaryRepository
import mx.edu.uttt.pulsocheck.domain.model.EpisodeResponseSummary
import mx.edu.uttt.pulsocheck.domain.model.FeelingOption
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import java.time.LocalDate
import mx.edu.uttt.pulsocheck.data.repository.RoomMobileHistoryRepository

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
        val historyRepository = RoomMobileHistoryRepository(database)

        // El ViewModel observa los tres repositorios de la pantalla.
        val viewModel = ViewModelProvider(
            this,
            MobileHomeViewModelFactory(
                historyRepository = historyRepository,
                readingRepository = readingRepository,
                episodeRepository = episodeRepository,
                responseRepository = responseRepository,
            ),
        )[MobileHomeViewModel::class.java]

        setContent {
            val readings = viewModel.readings.collectAsState().value
            val episodes = viewModel.episodes.collectAsState().value
            val responses = viewModel.responses.collectAsState().value
            val borrandoHistorial = viewModel.borrandoHistorial.collectAsState().value
            val mensajeHistorial = viewModel.mensajeHistorial.collectAsState().value
            val notificationPermissionGranted = remember {
                mutableStateOf(
                    Build.VERSION.SDK_INT < 33 ||
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) == PackageManager.PERMISSION_GRANTED,
                )
            }


            val notificationPermissionLauncher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    notificationPermissionGranted.value = granted
                }

            PulsoCheckTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MobileHomeScreen(
                        showNotificationPermissionButton =
                            !notificationPermissionGranted.value,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notificationPermissionLauncher.launch(
                                    Manifest.permission.POST_NOTIFICATIONS,
                                )
                            }
                        },
                        readings = readings,
                        episodes = episodes,
                        responses = responses,
                        borrandoHistorial = borrandoHistorial,
                        mensajeHistorial = mensajeHistorial,
                        onBorrarHistorial = viewModel::borrarHistorialCompleto,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

/** Presenta el historial del reloj en una pantalla de resumen clara y adaptable. */
@Composable
private fun MobileHomeScreen(
    readings: List<HeartRateReading>,
    episodes: List<HeartRateEpisodeSummary>,
    responses: List<EpisodeResponseSummary>,
    showNotificationPermissionButton: Boolean,
    onRequestNotificationPermission: () -> Unit,
    borrandoHistorial: Boolean,
    mensajeHistorial: String?,
    onBorrarHistorial: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // La eliminación requiere confirmación para evitar toques accidentales.
    val mostrarConfirmacionBorrado = remember { mutableStateOf(false) }

    // Agrupamos por día local para que el resumen coincida con la hora del teléfono.
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now(zoneId)

    val dailySummaries = (0L..4L).map { daysAgo ->
        val date = today.minusDays(daysAgo)
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val startOfNextDay = date.plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        // Solo contamos lecturas cuya hora corresponde a ese día local.
        val readingsForDay = readings.filter { reading ->
            !reading.isDemo &&
                    reading.measuredAtEpochMillis >= startOfDay &&
                    reading.measuredAtEpochMillis < startOfNextDay
        }

        DailyReadingsSummary(
            date = date,
            readings = readingsForDay,
        )
    }

    val todaySummary = dailySummaries.first()
    val todayAverage = todaySummary.readings
        .map { reading -> reading.beatsPerMinute }
        .takeIf { values -> values.isNotEmpty() }
        ?.average()
        ?.toInt()
    val latestReading = readings.firstOrNull()
    val demoReadingsCount = readings.count { reading -> reading.isDemo }
    val realReadingsCount = readings.size - demoReadingsCount

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            HeaderBlock()
        }

        item {
            TodayHeroCard(
                date = today,
                averageBpm = todayAverage,
                readingsToday = todaySummary.readings.size,
                latestReading = latestReading,
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Hoy",
                    value = todaySummary.readings.size.toString(),
                    detail = "lecturas",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    title = "Episodios",
                    value = episodes.size.toString(),
                    detail = "últimos 5 días",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (showNotificationPermissionButton) {
            item {
                NotificationPermissionCard(onRequestNotificationPermission)
            }
        }

        item {
            SectionHeading(
                title = "Episodios",
                subtitle = "Eventos identificados por el seguimiento",
            )
        }

        if (episodes.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin episodios por ahora",
                    message = "Cuando el reloj detecte uno, aparecerá aquí con sus lecturas y horario.",
                )
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
            SectionHeading(
                title = "Actividad diaria",
                subtitle = "Lecturas de hoy y de los cuatro días anteriores",
            )
        }

        items(
            items = dailySummaries,
            key = { summary -> "day-${summary.date}" },
        ) { summary ->
            DailyReadingsCard(summary)
        }

        item {
            SectionHeading(
                title = "Lecturas recientes",
                subtitle = if (demoReadingsCount == 0) {
                    "$realReadingsCount lecturas recibidas del reloj"
                } else {
                    "$realReadingsCount reales · $demoReadingsCount simuladas DEMO"
                },
            )
        }

        if (readings.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Aún no hay lecturas",
                    message = "Abre PulsoCheck en el reloj e inicia el seguimiento para recibir datos aquí.",
                )
            }
        } else {
            items(
                items = readings,
                key = { reading -> "reading-${reading.id}" },
            ) { reading ->
                ReadingCard(reading)
            }
        }

        item {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "El historial del reloj se conserva aunque borres la copia de este teléfono.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { mostrarConfirmacionBorrado.value = true },
                enabled = !borrandoHistorial,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text(if (borrandoHistorial) "Borrando historial…" else "Borrar historial de este teléfono")
            }
            mensajeHistorial?.let { mensaje ->
                Text(
                    text = mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (mostrarConfirmacionBorrado.value) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionBorrado.value = false },
            title = { Text("¿Borrar este historial?") },
            text = {
                Text("Se eliminarán las lecturas, episodios y respuestas guardados en este teléfono. El reloj conservará su copia.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacionBorrado.value = false
                        onBorrarHistorial()
                    },
                    enabled = !borrandoHistorial,
                ) {
                    Text("Borrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { mostrarConfirmacionBorrado.value = false },
                    enabled = !borrandoHistorial,
                ) {
                    Text("Cancelar")
                }
            },
        )
    }
}

/** Encabezado compacto que identifica la aplicación y el periodo del historial. */
@Composable
private fun HeaderBlock() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(BrandTeal),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "♥",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "PulsoCheck",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Resumen de tu seguimiento",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Tarjeta principal con las cifras del día, sin presentarlas como diagnóstico. */
@Composable
private fun TodayHeroCard(
    date: LocalDate,
    averageBpm: Int?,
    readingsToday: Int,
    latestReading: HeartRateReading?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(BrandNavy, BrandTeal),
                    ),
                )
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "RESUMEN DE HOY",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.78f),
            )
            Text(
                text = date.format(
                    DateTimeFormatter.ofPattern(
                        "EEEE d 'de' MMMM",
                        Locale.forLanguageTag("es-MX"),
                    ),
                ).replaceFirstChar { character ->
                    character.uppercase(Locale.forLanguageTag("es-MX"))
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = averageBpm?.toString() ?: "—",
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (averageBpm == null) "sin promedio" else "lpm promedio",
                    modifier = Modifier.padding(bottom = 9.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.86f),
                )
            }
            Text(
                text = if (readingsToday == 0) {
                    "Todavía no hay lecturas sincronizadas hoy"
                } else {
                    "$readingsToday lecturas disponibles hoy"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.86f),
            )
            latestReading?.let { reading ->
                Text(
                    text = if (reading.isDemo) {
                        "Última lectura DEMO simulada: ${reading.beatsPerMinute} lpm · ${formatReadingTime(reading.measuredAtEpochMillis)}"
                    } else {
                        "Última lectura: ${reading.beatsPerMinute} lpm · ${formatReadingTime(reading.measuredAtEpochMillis)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                )
            }
        }
    }
}

/** Tarjeta pequeña para presentar una cifra secundaria. */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Título y descripción breve para separar las áreas de la pantalla. */
@Composable
private fun SectionHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Explica por qué una sección está vacía y qué dato aparecerá ahí. */
@Composable
private fun EmptyStateCard(title: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Solicita el permiso de avisos en una tarjeta visible, sin bloquear el resumen. */
@Composable
private fun NotificationPermissionCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Avisos de episodios",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = "Activa las notificaciones del teléfono",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onRequestPermission) {
                Text("Activar")
            }
        }
    }
}

/** Presenta una lectura y el momento en que fue tomada por el reloj. */
@Composable
private fun ReadingCard(reading: HeartRateReading) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BrandTeal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("♥", color = BrandTeal, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${reading.beatsPerMinute} lpm",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatReadingTime(reading.measuredAtEpochMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (reading.isDemo) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Text(
                        text = "DEMO",
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

/** Presenta un resumen; no lo interpreta como diagnóstico. */
@Composable
private fun EpisodeCard(
    episode: HeartRateEpisodeSummary,
    response: EpisodeResponseSummary?,
) {
    val isActive = episode.status == EpisodeStatus.ACTIVE
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isActive) BrandCoral else BrandTeal),
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = when {
                        episode.isDemo -> "Episodio de prueba · DEMO"
                        isActive -> "Episodio activo"
                        else -> "Episodio cerrado"
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isActive) BrandCoral.copy(alpha = 0.12f)
                    else BrandTeal.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = "${episode.readingCount} lecturas",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isActive) BrandCoral else BrandTeal,
                    )
                }
            }
            Text(
                text = "Inicio · ${formatReadingTime(episode.startedAtEpochMillis)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            episode.endedAtEpochMillis?.let { endedAt ->
                Text(
                    text = "Fin · ${formatReadingTime(endedAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // La respuesta es opcional: quizá la persona aún no la registró.
            if (response == null) {
                Text(
                    text = if (episode.isDemo) {
                        "Evento simulado; no requiere respuesta personal"
                    } else {
                        "Sin respuesta registrada"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val feelingText = when (response.feeling) {
                    FeelingOption.GOOD -> "Bien"
                    FeelingOption.OKAY -> "Regular"
                    FeelingOption.BAD -> "Mal"
                }
                Text(
                    text = "Cómo se sintió · $feelingText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Respuesta · ${formatReadingTime(response.answeredAtEpochMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
/** Agrupa las lecturas que pertenecen a un mismo día local. */
private data class DailyReadingsSummary(
    val date: LocalDate,
    val readings: List<HeartRateReading>,
)

/** Muestra el resumen del día sin rellenar los días vacíos con datos inventados. */
@Composable
private fun DailyReadingsCard(summary: DailyReadingsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = summary.date.format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()),
                ),
                style = MaterialTheme.typography.titleMedium,
            )

            if (summary.readings.isEmpty()) {
                Text(
                    text = "Sin lecturas registradas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val bpmValues = summary.readings.map { reading ->
                    reading.beatsPerMinute
                }
                val averageBpm = bpmValues.average().toInt()
                val minimumBpm = bpmValues.minOrNull()
                val maximumBpm = bpmValues.maxOrNull()

                Text(
                    text = "${summary.readings.size} lecturas · promedio $averageBpm lpm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Rango disponible: $minimumBpm–$maximumBpm lpm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
