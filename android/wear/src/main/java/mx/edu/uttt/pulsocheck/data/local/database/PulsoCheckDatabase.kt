package mx.edu.uttt.pulsocheck.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mx.edu.uttt.pulsocheck.data.local.dao.HeartRateReadingDao
import mx.edu.uttt.pulsocheck.data.local.entity.HeartRateReadingEntity

@Database(
    entities = [HeartRateReadingEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PulsoCheckDatabase : RoomDatabase() {

    // Room proporciona el DAO que definimos para las lecturas.
    abstract fun heartRateReadingDao(): HeartRateReadingDao

    companion object {
        // Conserva la única instancia de la base durante la ejecución de la app.
        @Volatile
        private var instancia: PulsoCheckDatabase? = null

        fun obtenerInstancia(context: Context): PulsoCheckDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    PulsoCheckDatabase::class.java,
                    "pulsocheck.db",
                ).build().also { baseDeDatos ->
                    instancia = baseDeDatos
                }
            }
        }
    }
}