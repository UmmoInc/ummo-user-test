package xyz.ummo.user.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import timber.log.Timber
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.repo.AppRepository

class ServiceViewModel (application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)

    val serviceEntityLiveData: LiveData<ServiceEntity>

    fun addService(serviceEntity: ServiceEntity?) {
        appRepository.insertService(serviceEntity)
        Timber.e("ADDING SERVICE TO ROOM -> ${serviceEntity!!.serviceProvider}")
    }

    fun updateService(serviceEntity: ServiceEntity?) {
        appRepository.updateService(serviceEntity)
        Timber.e("UPDATING SERVICE IN ROOM -> ${serviceEntity!!.serviceId}")
    }

    fun getServicesList(): List<ServiceEntity> {
        return appRepository.services
    }
    init {
        serviceEntityLiveData = appRepository.serviceEntityLiveData
    }
}