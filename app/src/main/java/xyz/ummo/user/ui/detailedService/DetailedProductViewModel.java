package xyz.ummo.user.ui.detailedService;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import timber.log.Timber;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.repo.AppRepository;

public class DetailedProductViewModel extends AndroidViewModel {

    private final AppRepository appRepository;
    private LiveData<ProductEntity> productEntityLiveData;
    private static final String TAG = "DetailedServiceViewMode";

    public DetailedProductViewModel(@NonNull Application application) {
        super(application);

        appRepository = new AppRepository(application);
        productEntityLiveData = appRepository.getProductEntityLiveData();
    }

    public void insertProduct(ProductEntity productEntity){
        appRepository.insertProduct(productEntity);
        Timber.e("insertProduct: PRODUCT->%s", productEntity.getProductId());
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
