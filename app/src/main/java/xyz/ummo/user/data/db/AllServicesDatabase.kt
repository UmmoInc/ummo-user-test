package xyz.ummo.user.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.ummo.user.data.dao.ServiceDao
import xyz.ummo.user.data.entity.ServiceEntity

@Database(
    entities = [ServiceEntity::class],
    version = 1
)
abstract class AllServicesDatabase : RoomDatabase() {

    abstract fun getAllServicesDao(): ServiceDao

    companion object {
        @Volatile
        /** Other threads can immediately see when a thread changes this instance**/
        private var instance: AllServicesDatabase? = null
        private val LOCK = Any()

        /** We'll use this to sync this instance **/
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AllServicesDatabase::class.java,
                "all_services_db.db"
            ).build()
    }
}