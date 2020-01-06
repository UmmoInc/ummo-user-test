package xyz.ummo.user.utilities;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import xyz.ummo.user.data.entity.ServiceProviderEntity;
import xyz.ummo.user.data.repo.AppRepository;

public class ServiceProviderViewModel  extends AndroidViewModel {
    private AppRepository appRepository;
    private LiveData<ServiceProviderEntity> serviceProviderEntityLiveData;
    private static final String TAG = "ServiceProviderVM";

    public ServiceProviderViewModel(@NonNull Application application) {
        super(application);

        appRepository = new AppRepository(application);
        serviceProviderEntityLiveData = appRepository.getServiceProviderEntityLiveData();
    }

    public void insertServiceProvider(ServiceProviderEntity serviceProviderEntity){
        appRepository.insertServiceProvider(serviceProviderEntity);
        Log.e(TAG, "insertServiceProvider: SERVICE-PROVIDER->"+serviceProviderEntity.getServiceProviderName());
    }

    public void updateServiceProviders(ServiceProviderEntity serviceProviderEntity){
        appRepository.updateServiceProvider(serviceProviderEntity);
    }

    public void deleteAllServiceProviders(){
        appRepository.deleteServiceProvider();
    }

    public List<ServiceProviderEntity> getServiceProviders(){

            return appRepository.getServiceProviders();

    }

    public LiveData<ServiceProviderEntity> getServiceProviderEntityLiveData(){
        return appRepository.getServiceProviderEntityLiveData();
    }

    public LiveData<ServiceProviderEntity> getServiceProviderEntityLiveDataById(String serviceProviderId){
        return appRepository.getServiceProviderEntityLiveDataById(serviceProviderId);
    }
}