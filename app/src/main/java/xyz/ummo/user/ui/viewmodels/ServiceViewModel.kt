package xyz.ummo.user.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import timber.log.Timber
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.repo.AppRepository

class ServiceViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)

    private val serviceEntityLiveData: LiveData<ServiceEntity>

    fun addService(serviceEntity: ServiceEntity?) {
        appRepository.insertService(serviceEntity)
        Timber.e("ADDING SERVICE TO ROOM [ID] -> ${serviceEntity!!.serviceName}")
        Timber.e("ADDING SERVICE TO ROOM [DEL] -> ${serviceEntity.delegatable}")
        Timber.e("ADDING SERVICE TO ROOM [UPVOTE] -> ${serviceEntity.usefulCount}")
    }

    fun updateService(serviceEntity: ServiceEntity?) {
        appRepository.updateService(serviceEntity)
        Timber.e("UPDATING SERVICE IN ROOM -> ${serviceEntity!!.serviceId}")
    }

    fun getServicesList(): List<ServiceEntity> {
        return appRepository.services
    }

    fun getBookmarkedServiceList(): List<ServiceEntity> {
        return appRepository.getBookmarkedServiceList(true)
    }

    fun getServiceEntityLiveDataById(serviceId: String): LiveData<ServiceEntity> {
        return appRepository.getServiceEntityLiveDataById(serviceId)
    }

    fun getDelegatableServices(): List<ServiceEntity> {
        return appRepository.delegatableServices
    }

    fun getNonDelegatableServices(): List<ServiceEntity> {
        return appRepository.nonDelegatableServices
    }

    init {
        serviceEntityLiveData = appRepository.serviceEntityLiveData
    }
}