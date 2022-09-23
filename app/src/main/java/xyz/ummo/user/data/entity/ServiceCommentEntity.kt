package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "service_comment")
data class ServiceCommentEntity(
    @NonNull
    @ColumnInfo(name = "service_id")
    var serviceId: String,

    @NonNull
    @ColumnInfo(name = "service_comment")
    var serviceComment: String? = null,

    @NonNull
    @ColumnInfo(name = "comment_date_time")
    var commentDateTime: String? = null,

    @NonNull
    @ColumnInfo(name = "user_name")
    var userName: String? = null
)