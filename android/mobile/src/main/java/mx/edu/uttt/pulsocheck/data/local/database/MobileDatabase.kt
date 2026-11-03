package mx.edu.uttt.pulsocheck.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateReadingDao
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity

/** Base de datos del teléfono; es independiente de la base Room del reloj. */
@Database(
    entities = [HeartRateReadingEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class MobileDatabase : RoomDatabase() {

    abstract fun heartRateReadingDao(): HeartRateReadingDao

    companion object {
        @Volatile
        private var instance: MobileDatabase? = null

        /** Comparte una sola instancia de Room dentro del proceso del teléfono. */
        fun obtenerInstancia(context: Context): MobileDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MobileDatabase::class.java,
                    "pulsocheck-mobile.db",
                ).build().also { database ->
                    instance = database
                }
            }
        }
    }
}
