package xyz.ummo.user.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.ummo.user.data.entity.ServiceCategoryEntity

@Dao
interface ServiceCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategory(serviceCategoryEntity: ServiceCategoryEntity)

    @get:Query("SELECT * FROM service_category")
    val allCategories: List<ServiceCategoryEntity>

    @Query("SELECT * FROM service_category WHERE category_name = :categoryName")
    fun getCategoryByName(categoryName: String): LiveData<ServiceCategoryEntity>

    @Transaction
    fun insertCategories(serviceCategories: List<ServiceCategoryEntity>) {
//        insertCategory(serviceCategories)
    }

}