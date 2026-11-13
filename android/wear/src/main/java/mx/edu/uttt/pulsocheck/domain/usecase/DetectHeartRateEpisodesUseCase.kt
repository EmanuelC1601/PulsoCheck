package mx.edu.uttt.pulsocheck.domain.usecase

import mx.edu.uttt.pulsocheck.domain.model.EpisodeStatus
import mx.edu.uttt.pulsocheck.domain.model.HeartRateEpisode
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading
import mx.edu.uttt.pulsocheck.domain.model.ReadingOrigin

/**
 * Aplica la regla de PulsoCheck a una lista de lecturas.
 *
 * Esta clase no accede al sensor ni a la base de datos. Más adelante,
 * esas capas le entregarán las lecturas que se guardaron en el reloj.
 */
class DetectHeartRateEpisodesUseCase {

    /**
     * Devuelve los episodios encontrados, ordenados por fecha.
     *
     * @param readings Lecturas guardadas.
     * @param thresholdBpm Límite configurado, en latidos por minuto.
     * @param maximumGapMillis Separación máxima entre lecturas consecutivas.
     */
    operator fun invoke(
        readings: List<HeartRateReading>,
        thresholdBpm: Int,
        maximumGapMillis: Long,
    ): List<HeartRateEpisode> {
        require(thresholdBpm > 0) {
            "El límite de seguimiento debe ser mayor que cero."
        }
        require(maximumGapMillis > 0) {
            "La separación máxima debe ser mayor que cero."
        }

        // Elimina registros repetidos por sincronización y los ordena por hora.
        val orderedReadings = readings
            // Las pruebas no participan en el análisis de lecturas reales.
            .filter { it.origin != ReadingOrigin.DEMO_TEST }
            .distinctBy { it.id }
            .sortedBy { it.measuredAtEpochMillis }

        val episodes = mutableListOf<HeartRateEpisode>()
        val highReadingsInARow = mutableListOf<HeartRateReading>()
        val recoveryReadingsInARow = mutableListOf<HeartRateReading>()

        // Estos valores permanecen vacíos hasta confirmar un episodio.
        var activeEpisodeId: String? = null
        var episodeStartTime: Long? = null
        val episodeReadingIds = mutableListOf<String>()

        var previousReading: HeartRateReading? = null

        for (reading in orderedReadings) {
            // Un registro inválido interrumpe cualquier secuencia consecutiva.
            val readingIsInvalid =
                reading.id.isBlank() ||
                        reading.beatsPerMinute <= 0 ||
                        reading.measuredAtEpochMillis <= 0

            if (readingIsInvalid) {
                highReadingsInARow.clear()
                recoveryReadingsInARow.clear()
                previousReading = null
                continue
            }

            // Una separación grande reinicia los conteos consecutivos.
            // La falta de datos no cuenta como recuperación del episodio.
            val previous = previousReading
            if (
                previous != null &&
                reading.measuredAtEpochMillis - previous.measuredAtEpochMillis >
                maximumGapMillis
            ) {
                highReadingsInARow.clear()
                recoveryReadingsInARow.clear()
            }

            if (activeEpisodeId == null) {
                // Antes de abrir un episodio buscamos tres lecturas altas seguidas.
                if (reading.beatsPerMinute > thresholdBpm) {
                    highReadingsInARow.add(reading)

                    if (highReadingsInARow.size == 3) {
                        val firstHighReading = highReadingsInARow.first()

                        // El identificador depende de la primera lectura.
                        // Así permanece igual si volvemos a procesar el historial.
                        activeEpisodeId = "episode-${firstHighReading.id}"
                        episodeStartTime = firstHighReading.measuredAtEpochMillis
                        episodeReadingIds.addAll(
                            highReadingsInARow.map { it.id }
                        )

                        highReadingsInARow.clear()
                    }
                } else {
                    // Una lectura dentro del límite rompe la secuencia alta.
                    highReadingsInARow.clear()
                }
            } else {
                // Mientras el episodio está activo, guardamos las lecturas asociadas.
                if (episodeReadingIds.lastOrNull() != reading.id) {
                    episodeReadingIds.add(reading.id)
                }

                if (reading.beatsPerMinute <= thresholdBpm) {
                    recoveryReadingsInARow.add(reading)

                    if (recoveryReadingsInARow.size == 2) {
                        // Dos lecturas seguidas dentro del límite cierran el episodio.
                        episodes.add(
                            HeartRateEpisode(
                                id = activeEpisodeId,
                                startedAtEpochMillis = requireNotNull(episodeStartTime),
                                endedAtEpochMillis = reading.measuredAtEpochMillis,
                                readingIds = episodeReadingIds.toList(),
                                status = EpisodeStatus.CLOSED,
                            )
                        )

                        activeEpisodeId = null
                        episodeStartTime = null
                        episodeReadingIds.clear()
                        recoveryReadingsInARow.clear()
                    }
                } else {
                    // Una lectura alta interrumpe la secuencia de recuperación.
                    recoveryReadingsInARow.clear()
                }
            }

            previousReading = reading
        }

        // Si los datos terminan antes de observar recuperación, queda activo.
        if (activeEpisodeId != null) {
            episodes.add(
                HeartRateEpisode(
                    id = activeEpisodeId,
                    startedAtEpochMillis = requireNotNull(episodeStartTime),
                    endedAtEpochMillis = null,
                    readingIds = episodeReadingIds.toList(),
                    status = EpisodeStatus.ACTIVE,
                )
            )
        }

        return episodes
    }
}
