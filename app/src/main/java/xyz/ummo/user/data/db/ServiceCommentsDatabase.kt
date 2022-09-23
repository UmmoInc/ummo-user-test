package xyz.ummo.user.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import xyz.ummo.user.data.dao.ServiceCommentDao
import xyz.ummo.user.data.entity.ServiceCommentEntity

@Database(
    entities = [ServiceCommentEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ServiceCommentsDatabase : RoomDatabase() {
    abstract fun serviceCommentDao(): ServiceCommentDao?

    companion object {
        @Volatile
        private var instance: ServiceCommentsDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ServiceCommentsDatabase::class.java,
                "service_comments_db.db"
            ).fallbackToDestructiveMigration().build()
    }
}