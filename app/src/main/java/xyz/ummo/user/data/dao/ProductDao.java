package xyz.ummo.user.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import xyz.ummo.user.data.entity.ProductEntity;

@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProduct(ProductEntity productEntity);

    @Query("SELECT * FROM product")
    LiveData<ProductEntity> getProductLiveData();

    @Query("SELECT * FROM product WHERE product_id = :productId")
    LiveData<ProductEntity> getProductEntityLiveDataById(String productId);

    @Update
    void updateProduct(ProductEntity productEntity);

    @Query("DELETE FROM product")
    void deleteAllProducts();
}
