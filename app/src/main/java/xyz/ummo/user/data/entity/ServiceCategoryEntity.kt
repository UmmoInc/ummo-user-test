package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.ummo.user.data.model.ServiceCategory

@Entity(tableName = "service_category")
class ServiceCategoryEntity : ServiceCategory {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "category_name")
    override var serviceCategory: String? = null

    @NonNull
    @ColumnInfo(name = "category_count")
    override var serviceCount: Int? = null
}