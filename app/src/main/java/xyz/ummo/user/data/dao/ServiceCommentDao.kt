package xyz.ummo.user.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xyz.ummo.user.data.entity.ServiceCommentEntity

@Dao
interface ServiceCommentDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun upsertServiceComment(serviceCommentEntity: ServiceCommentEntity)

    @get:Query("SELECT * FROM service_comment")
    val serviceComment: List<ServiceCommentEntity>

    @Query("SELECT * FROM service_comment WHERE service_id = :service_id")
    fun getServiceCommentsByServiceId(service_id: String?): Flow<List<ServiceCommentEntity>>

    @Query("DELETE FROM service_comment")
    fun deleteServiceComment()
}