package xyz.ummo.user.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.ummo.user.data.entity.ServiceProviderEntity

@Dao
interface ServiceProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertServiceProvider(serviceProviderEntity: ServiceProviderEntity?)

//    val serviceProviders: MutableCollection<out ServiceProviderEntity>

    @get:Query("SELECT * FROM service_provider")
    val serviceProviderLiveData: LiveData<ServiceProviderEntity>

    @get:Query("SELECT * FROM service_provider")
    val serviceProviderListData: List<ServiceProviderEntity>

    @Query("SELECT * FROM service_provider WHERE service_provider_id = :serviceProviderId")
    fun getServiceProviderLiveDataById(serviceProviderId: String?): LiveData<ServiceProviderEntity>

    @Update
    fun updateServiceProviders(serviceProviderEntity: ServiceProviderEntity?)

    @Query("DELETE FROM service_provider")
    fun deleteAllServiceProviders()
}