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

    @Query("SELECT service_views FROM service WHERE service_id = :serviceId")
    fun getServiceViewCountByServiceId(serviceId: String?): Int

    @Update
    fun updateService(serviceEntity: ServiceEntity?)

    @Query("UPDATE service SET useful_count = :approvalCount WHERE service_id = :serviceId")
    fun incrementApprovalCount(serviceId: String?, approvalCount: Int)

    @Query("UPDATE service SET service_views = service_views + 1 WHERE  service_id = :serviceId")
    fun incrementServiceViewCount(serviceId: String?)

    @Query("UPDATE service SET bookmarked = 1 WHERE service_id = :serviceId")
    fun addServiceBookmark(serviceId: String?)

    @Query("UPDATE service SET bookmarked = 0 WHERE service_id = :serviceId")
    fun removeServiceBookmark(serviceId: String?)

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
