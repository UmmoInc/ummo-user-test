package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_utility_store")
data class ServiceUtilityEntity(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "utility_id")
    var utilityId: Int = 0,

    @NonNull
    @ColumnInfo(name = "service_id")
    var serviceId: String,

    @NonNull
    @ColumnInfo(name = "service_name")
    var serviceName: String,

    @NonNull
    @ColumnInfo(name = "helpful")
    var helpful: Int = 0,

    @NonNull
    @ColumnInfo(name = "not_helpful")
    var notHelpful: Int = 0,

    @NonNull
    @ColumnInfo(name = "timestamp")
    var timestamp: String
)