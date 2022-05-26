package xyz.ummo.user.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.ummo.user.data.dao.ServiceUtilityDao
import xyz.ummo.user.data.entity.ServiceUtilityEntity

@Database(
    entities = [ServiceUtilityEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ServiceUtilityDatabase : RoomDatabase() {
    abstract fun serviceUtilityDao(): ServiceUtilityDao?

    companion object {
        @Volatile
        private var instance: ServiceUtilityDatabase? = null
        private var LOCK = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ServiceUtilityDatabase::class.java,
            "service_util.db"
        ).fallbackToDestructiveMigration().build()
    }
}