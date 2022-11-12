package xyz.ummo.user.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.ummo.user.data.dao.CategoryDao
import xyz.ummo.user.data.entity.ServiceCategoryEntity

@Database(
    entities = [ServiceCategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ServiceCategoryDatabase : RoomDatabase() {
    abstract fun serviceCategoryDao() : CategoryDao?

    companion object {
        @Volatile
        /** Other threads can immediately see when a thread changes this instance**/
        private var instance: ServiceCategoryDatabase? = null
        /** To be used for syncing this instance to ensure that only one instance exists**/
        private val LOCK = Any()
        /** We'll use this to sync this DB instance **/
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ServiceCategoryDatabase::class.java,
            "categories_db.db"
        ).fallbackToDestructiveMigration().build()
    }
}