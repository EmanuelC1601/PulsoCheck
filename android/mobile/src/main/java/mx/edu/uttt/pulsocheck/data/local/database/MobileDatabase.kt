package mx.edu.uttt.pulsocheck.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import mx.edu.uttt.pulsocheck.data.local.dao.EpisodeResponseSummaryDao
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateEpisodeSummaryDao
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateReadingDao
import mx.edu.uttt.pulsocheck.data.local.entity.EpisodeResponseSummaryEntity
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateEpisodeSummaryEntity
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity

/** Base local del teléfono para lecturas, episodios y respuestas. */
@Database(
    entities = [
        HeartRateReadingEntity::class,
        HeartRateEpisodeSummaryEntity::class,
        EpisodeResponseSummaryEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class MobileDatabase : RoomDatabase() {

    abstract fun heartRateReadingDao(): HeartRateReadingDao

    abstract fun heartRateEpisodeSummaryDao(): HeartRateEpisodeSummaryDao

    abstract fun episodeResponseSummaryDao(): EpisodeResponseSummaryDao

    companion object {
        @Volatile
        private var instance: MobileDatabase? = null

        // De la versión 1 a la 2 se agregó la tabla de episodios.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `mobile_heart_rate_episodes` (
                        `episode_id` TEXT NOT NULL,
                        `started_at_epoch_millis` INTEGER NOT NULL,
                        `ended_at_epoch_millis` INTEGER,
                        `reading_count` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        PRIMARY KEY(`episode_id`)
                    )
                    """.trimIndent(),
                )
            }
        }

        // De la versión 2 a la 3 se agrega la tabla de respuestas.
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `mobile_episode_responses` (
                        `episode_id` TEXT NOT NULL,
                        `feeling_option` TEXT NOT NULL,
                        `answered_at_epoch_millis` INTEGER NOT NULL,
                        PRIMARY KEY(`episode_id`),
                        FOREIGN KEY(`episode_id`)
                            REFERENCES `mobile_heart_rate_episodes`(`episode_id`)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )

                database.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_mobile_episode_responses_episode_id`
                    ON `mobile_episode_responses` (`episode_id`)
                    """.trimIndent(),
                )
            }
        }

        /** Crea la base y aplica las migraciones pendientes sin borrar los datos. */
        fun obtenerInstancia(context: Context): MobileDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MobileDatabase::class.java,
                    "pulsocheck-mobile.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { database ->
                        instance = database
                    }
            }
        }
    }
}