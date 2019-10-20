package xyz.ummo.user.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Ignore;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import xyz.ummo.user.data.entity.DelegatedServiceEntity;

@Dao
public interface DelegatedServiceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDelegatedService(DelegatedServiceEntity delegatedServiceEntity);

    @Query("SELECT * FROM delegated_service")
    LiveData<DelegatedServiceEntity> getDelegatedService();

    @Query("SELECT * FROM delegated_service WHERE service_id = :serviceId")
    LiveData<DelegatedServiceEntity> getDelegatedServiceById(String serviceId);

    @Query("SELECT * FROM delegated_service WHERE delegated_product_id = :delegatedProductId")
    LiveData<DelegatedServiceEntity> getDelegatedServiceByProductId(String delegatedProductId);

    @Update
    void updateDelegatedService(DelegatedServiceEntity delegatedServiceEntity);

    @Query("DELETE FROM delegated_service")
    void deleteAllDelegatedServices();
}
