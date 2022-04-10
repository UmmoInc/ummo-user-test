package xyz.ummo.user.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.ummo.user.data.dao.ServiceDao
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.utils.Converters
import xyz.ummo.user.data.utils.ServiceBenefitsTypeConverter
import xyz.ummo.user.data.utils.ServiceCostTypeConverter

@Database(
    entities = [ServiceEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    Converters::class,
    ServiceCostTypeConverter::class,
    ServiceBenefitsTypeConverter::class
)
abstract class AllServicesDatabase : RoomDatabase() {

    abstract fun serviceDao(): ServiceDao?

    companion object {
        @Volatile
        /** Other threads can immediately see when a thread changes this instance**/
        private var instance: AllServicesDatabase? = null
        private val LOCK = Any()
        /** To be used for syncing this instance to ensure that only one instance exists**/

        /** We'll use this to sync this DB instance **/
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AllServicesDatabase::class.java,
                "all_services_db.db"
            ).fallbackToDestructiveMigration().build()
    }
}