package xyz.ummo.user.ui.fragments.delegatedService;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import xyz.ummo.user.data.entity.DelegatedServiceEntity;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.repo.AppRepository;

public class DelegatedServiceViewModel extends AndroidViewModel {
    private AppRepository appRepository;
    private LiveData<DelegatedServiceEntity> delegatedServiceEntityLiveData;
    private static final String TAG = "DelegatedServiceVM";

    public DelegatedServiceViewModel(@NonNull Application application) {
        super(application);

        appRepository = new AppRepository(application);
        delegatedServiceEntityLiveData = appRepository.getDelegatedServiceEntityLiveData();
    }

    public void insertDelegatedService(DelegatedServiceEntity delegatedServiceEntity){
        appRepository.insertDelegatedService(delegatedServiceEntity);
        Log.e(TAG, "insertDelegatedService: SERVICE->"+delegatedServiceEntity.getDelegatedProductId());
    }

    public void updateDelegatedService(DelegatedServiceEntity delegatedServiceEntity){
        appRepository.updateDelegatedService(delegatedServiceEntity);
    }

    public void deleteAllDelegatedServices(){
        appRepository.deleteAllDelegatedServices();
    }

    public LiveData<DelegatedServiceEntity> getDelegatedServiceByProductId(String productId){
        return appRepository.getDelegatedServiceByProductId(productId);
    }
}
