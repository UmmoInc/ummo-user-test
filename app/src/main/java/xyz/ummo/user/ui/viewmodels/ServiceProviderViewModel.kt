package xyz.ummo.user.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.data.repo.AppRepository

class ServiceProviderViewModel (application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)

    val serviceProviderEntityLiveData: LiveData<ServiceProviderEntity>

    fun addServiceProvider(serviceProviderEntity: ServiceProviderEntity?) {
        appRepository.insertServiceProvider(serviceProviderEntity)
        Timber.e("ADDING SERVICE PROVIDER TO ROOM -> $serviceProviderEntity")
    }

    fun getServiceProviders(): LiveData<ServiceProviderEntity>? {
        return appRepository.serviceProviderEntityLiveData
    }

    fun getServiceProviderList(): List<ServiceProviderEntity> {
        return appRepository.serviceProviders
    }

    init {
        serviceProviderEntityLiveData = appRepository.serviceProviderEntityLiveData
    }
}