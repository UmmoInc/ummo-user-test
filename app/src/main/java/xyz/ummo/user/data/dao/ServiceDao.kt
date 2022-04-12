package xyz.ummo.user.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.ummo.user.data.entity.ServiceEntity

@Dao
interface ServiceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertService(serviceEntity: ServiceEntity)

    @get:Query("SELECT * FROM service")
    val serviceLiveData: LiveData<ServiceEntity?>?

    @get:Query("SELECT * FROM service")
    val serviceListData: List<ServiceEntity>

    @Query("SELECT * FROM service WHERE service_id = :serviceId ")
    fun getServiceLiveDataById(serviceId: String?): LiveData<ServiceEntity>

    @Query("SELECT * FROM service WHERE delegatable = :delegatable")
    fun getDelegatableServices(delegatable: Boolean = true): List<ServiceEntity>

    @Query("SELECT * FROM service WHERE delegatable = :delegatable")
    fun getNonDelegatableServices(delegatable: Boolean = false): List<ServiceEntity>
    
    @Query("SELECT * FROM service WHERE bookmarked = :bookmarked")
    fun getBookmarkedServicesList(bookmarked: Boolean = true): List<ServiceEntity>

    @Update
    fun updateService(serviceEntity: ServiceEntity?)

    @Query("UPDATE service SET useful_count = :approvalCount WHERE service_id = :serviceId")
    fun incrementApprovalCount(serviceId: String?, approvalCount: Int)

    @Query("DELETE FROM service")
    fun deleteServices()

    @Query(
        "SELECT * FROM service WHERE service.service_name LIKE :searchQuery " +
                "OR service.service_description LIKE :searchQuery " +
                "OR service.service_category LIKE :searchQuery " +
                "OR service.service_provider LIKE :searchQuery"
    )
    suspend fun searchRoomDB(searchQuery: String): List<ServiceEntity>
}
