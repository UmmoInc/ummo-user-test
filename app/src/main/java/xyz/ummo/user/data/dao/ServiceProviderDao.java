package xyz.ummo.user.data.dao;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.entity.ServiceProviderEntity;

@Dao
public interface ServiceProviderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertServiceProvider(ServiceProviderEntity serviceProviderEntity);

    @Query("SELECT * FROM service_provider")
    LiveData<ServiceProviderEntity> getServiceProviderLiveData();

    @Query("SELECT * FROM service_provider")
    List<ServiceProviderEntity> getServiceProviders();

    /*@Query(("SELECT COUNT(service_provider_id) FROM service_provider"))
    int getServiceProviderCount();*/

    @Query("SELECT * FROM service_provider WHERE service_provider_id = :serviceProviderId")
    LiveData<ServiceProviderEntity> getServiceProviderEntityLiveDataById(String serviceProviderId);

    /*@Query("SELECT * FROM service_provider WHERE is_delegated = :isDelegated")
    LiveData<ProductEntity> getDelegatedProduct(Boolean isDelegated);*/

    @Update
    void updateServiceProviders(ServiceProviderEntity serviceProviderEntity);

    @Query("DELETE FROM service_provider")
    void deleteAllServiceProviders();
}
