package xyz.ummo.user.ui.fragments.delegatedService

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import timber.log.Timber
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.repo.AppRepository

class DelegatedServiceViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository: AppRepository = AppRepository(application)
    val delegatedServiceEntityLiveData: LiveData<DelegatedServiceEntity>?
        get() = appRepository.delegatedServiceEntityLiveData

    fun insertDelegatedService(delegatedServiceEntity: DelegatedServiceEntity) {
        appRepository.insertDelegatedService(delegatedServiceEntity)
        Timber.e("insertDelegatedService: SERVICE->%s", delegatedServiceEntity.serviceId)
    }

    fun updateDelegatedService(delegatedServiceEntity: DelegatedServiceEntity?) {
        appRepository.updateDelegatedService(delegatedServiceEntity)
    }

    fun deleteAllDelegatedServices() {
        appRepository.deleteAllDelegatedServices()
    }

    fun getDelegatedServiceById(id: String?): LiveData<DelegatedServiceEntity> {
        Timber.e("getDelegatedServiceById: %s", id)
        return appRepository.getDelegatedServiceById(id)
    }

    fun getDelegatedServiceByProductId(productId: String?): LiveData<DelegatedServiceEntity> {
        return appRepository.getDelegatedServiceByProductId(productId)
    }

    companion object {
        private const val TAG = "DelegatedServiceVM"
    }

    init {
        //  delegatedServiceEntityLiveData = appRepository.getDelegatedServiceEntity();
    }
}