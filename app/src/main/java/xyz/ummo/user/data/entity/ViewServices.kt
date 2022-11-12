package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "viewed_services")
data class ViewedServices(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var vServiceId: Int,

    @ColumnInfo(name = "v_service_ref")
    var vServiceRef: String,

    @ColumnInfo(name = "v_service_date_time")
    var vServicedDateTime: String
)