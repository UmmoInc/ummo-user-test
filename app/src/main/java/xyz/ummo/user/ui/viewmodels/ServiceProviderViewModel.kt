package xyz.ummo.user.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
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

    init {
        serviceProviderEntityLiveData = appRepository.serviceProviderEntityLiveData
    }

}