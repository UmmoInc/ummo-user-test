package xyz.ummo.user.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.ummo.user.data.entity.DelegatedServiceEntity

@Dao
interface DelegatedServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertDelegatedService(delegatedServiceEntity: DelegatedServiceEntity?)

    @get:Query("SELECT * FROM delegated_service")
    val delegatedService: LiveData<DelegatedServiceEntity?>?

    @Query("SELECT COUNT(delegation_id) FROM delegated_service")
    fun getDelegatedServicesCount(): Int

    @Query("SELECT * FROM delegated_service WHERE delegation_id = :delegationId")
    fun getDelegatedServiceById(delegationId: String?): LiveData<DelegatedServiceEntity?>?

    @Query("SELECT * FROM delegated_service WHERE delegated_product_id = :delegatedProductId")
    fun getDelegatedServiceByProductId(delegatedProductId: String?): LiveData<DelegatedServiceEntity?>?

    @Update
    fun updateDelegatedService(delegatedServiceEntity: DelegatedServiceEntity?)

    @Query("DELETE FROM delegated_service")
    fun deleteAllDelegatedServices()
}