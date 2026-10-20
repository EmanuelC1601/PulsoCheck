package mx.edu.uttt.pulsocheck.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.uttt.pulsocheck.domain.model.HeartRateReading

/**
 * Define las operaciones disponibles para trabajar con lecturas.
 *
 * El dominio conoce este contrato, pero no necesita saber si los datos
 * vienen de Room, de un sensor o de otra fuente.
 */
interface HeartRateReadingRepository {

    /**
     * Observa las lecturas ordenadas para actualizar la interfaz
     * cuando cambien los datos guardados.
     */
    fun observarTodas(): Flow<List<HeartRateReading>>

    /**
     * Guarda o actualiza una lectura.
     */
    suspend fun guardar(lectura: HeartRateReading)

    /**
     * Guarda o actualiza varias lecturas, por ejemplo al sincronizar.
     */
    suspend fun guardarTodas(lecturas: List<HeartRateReading>)

    /**
     * Busca una lectura por ID; devuelve null si no existe.
     */
    suspend fun buscarPorId(id: String): HeartRateReading?
}