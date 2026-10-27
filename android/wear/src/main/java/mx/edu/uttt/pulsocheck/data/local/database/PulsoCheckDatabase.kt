package mx.edu.uttt.pulsocheck.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateReadingDao
import mx.edu.uttt.pulsocheck.data.local.entity.EpisodeResponseEntity
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateEpisodeEntity
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity
import mx.edu.uttt.pulsocheck.data.local.dao.EpisodeResponseDao
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateEpisodeDao

/**
 * Declara las tablas que Room administra y la versión del esquema.
 */
@Database(
    entities = [
        HeartRateReadingEntity::class,
        HeartRateEpisodeEntity::class,
        EpisodeResponseEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class PulsoCheckDatabase : RoomDatabase() {

    // Room genera la implementación de este DAO.
    abstract fun heartRateReadingDao(): HeartRateReadingDao

    // Room proporciona el DAO para leer y guardar episodios.
    abstract fun heartRateEpisodeDao(): HeartRateEpisodeDao

    // Room proporciona el DAO para leer y guardar respuestas.
    abstract fun episodeResponseDao(): EpisodeResponseDao

    companion object {
        // Mantiene una sola instancia de la base de datos en la aplicación.
        @Volatile
        private var instancia: PulsoCheckDatabase? = null

        /**
         * Migra instalaciones con la versión 1 sin borrar las lecturas existentes.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tabla con los episodios detectados.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `heart_rate_episodes` (
                        `id` TEXT NOT NULL,
                        `started_at_epoch_millis` INTEGER NOT NULL,
                        `ended_at_epoch_millis` INTEGER,
                        `reading_ids_json` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )

                // Cada episodio puede tener una respuesta guardada.
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `episode_responses` (
                        `episode_id` TEXT NOT NULL,
                        `feeling_option` TEXT NOT NULL,
                        `answered_at_epoch_millis` INTEGER NOT NULL,
                        PRIMARY KEY(`episode_id`),
                        FOREIGN KEY(`episode_id`)
                            REFERENCES `heart_rate_episodes`(`id`)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )

                // Este índice acelera la búsqueda de una respuesta por episodio.
                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_episode_responses_episode_id`
                    ON `episode_responses` (`episode_id`)
                    """.trimIndent(),
                )
            }
        }

        /** Devuelve la instancia compartida y la crea la primera vez. */
        fun obtenerInstancia(context: Context): PulsoCheckDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    PulsoCheckDatabase::class.java,
                    "pulsocheck.db",
                )
                    // Aplica cambios de esquema sin eliminar los datos guardados.
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { baseDeDatos ->
                        instancia = baseDeDatos
                    }
            }
        }
    }
}