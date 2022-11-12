package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_category")
data class ServiceCategoryEntity (
    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "category_name")
    var serviceCategory: String,

    @NonNull
    @ColumnInfo(name = "category_count")
    var serviceCount: Int? = null
)