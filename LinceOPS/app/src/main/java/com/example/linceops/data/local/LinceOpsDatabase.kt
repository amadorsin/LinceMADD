package com.example.linceops.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.linceops.model.Servicio

@Database(
    entities = [Servicio::class, HitoPendienteEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LinceOpsDatabase : RoomDatabase() {

    abstract fun servicioDao(): ServicioDao
    abstract fun hitoPendienteDao(): HitoPendienteDao

    companion object {
        @Volatile
        private var INSTANCE: LinceOpsDatabase? = null

        fun getDatabase(context: Context): LinceOpsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinceOpsDatabase::class.java,
                    "linceops_offline_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}