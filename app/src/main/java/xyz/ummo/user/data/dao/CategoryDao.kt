package xyz.ummo.user.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.ummo.user.data.entity.ServiceCategoryEntity

@Dao
interface CategoryDao {

    /** 1. Create & Update Category -> Upsert **/
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertCategory(serviceCategoryEntity: ServiceCategoryEntity)

    /** 2. Read Category -> Find **/
    @get:Query("SELECT * FROM service_category")
//    val serviceCategoryLiveData: LiveData<ServiceCategoryEntity>
    val serviceCategoryList: List<ServiceCategoryEntity>

    /** 3. Delete Category -> Delete **/
    @Query("DELETE FROM service_category")
    fun deleteServiceCategories()

}