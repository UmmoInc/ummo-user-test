package xyz.ummo.user.ui.detailedService;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.model.Product;
import xyz.ummo.user.data.repo.AppRepository;

public class DetailedProductViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private LiveData<ProductEntity> productEntityLiveData;
    private static final String TAG = "DetailedServiceViewMode";

    public DetailedProductViewModel(@NonNull Application application) {
        super(application);

        appRepository = new AppRepository(application);
        productEntityLiveData = appRepository.getProductEntityLiveData();
    }

    public void insertProduct(ProductEntity productEntity){
        appRepository.insertProduct(productEntity);
        Log.e(TAG, "insertProduct: PRODUCT->"+productEntity.getProductId());
    }

    public void updateProduct(ProductEntity productEntity){
        appRepository.updateProduct(productEntity);
    }

    public void deleteAllProduct(){
        appRepository.deleteAllProducts();
    }

    public LiveData<ProductEntity> getProductEntityLiveDataById(String productId){
        return appRepository.getProductEntityLiveDataById(productId);
    }

    public LiveData<ProductEntity> getDelegatedProduct(Boolean isDelegated){
        return appRepository.getDelegatedProduct(isDelegated);
    }
}
