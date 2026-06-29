package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.entity.DownloadEntity

/**
 * Base de datos principal de la aplicación.
 * Define la lista de entidades y expone los DAOs correspondientes.
 */
@Database(
    entities = [DownloadEntity::class], 
    version = 1, 
    exportSchema = false
)
abstract class VibeTuneDatabase : RoomDatabase() {

    // Expone el DAO para realizar operaciones de lectura/escritura
    abstract fun downloadDao(): DownloadDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Agregar aquí las sentencias SQL de migración cuando se actualice la versión
            }
        }

        @Volatile
        private var INSTANCE: VibeTuneDatabase? = null

        /**
         * Obtiene la instancia única de la base de datos (Thread-safe).
         */
        fun getDatabase(context: Context): VibeTuneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VibeTuneDatabase::class.java,
                    "vibetune_database"
                )
                /* 
                 * Nota de evolución técnica FOSS:
                 * Para activar el cifrado con SQLCipher platicado en el informe,
                 * se encadena aquí: .openHelperFactory(SupportFactory(passphrase))
                 */
                .addMigrations(MIGRATION_1_2) // Reemplazado fallbackToDestructiveMigration para evitar pérdida de datos en prod
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}