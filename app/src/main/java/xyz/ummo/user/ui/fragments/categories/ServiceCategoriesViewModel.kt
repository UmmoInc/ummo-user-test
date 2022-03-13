package xyz.ummo.user.ui.fragments.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import timber.log.Timber
import xyz.ummo.user.data.entity.ServiceCategoryEntity
import xyz.ummo.user.data.repo.AppRepository

class ServiceCategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)

    val serviceCategoryListData: List<ServiceCategoryEntity> = appRepository.serviceCategoryEntities

    fun insertCategory(serviceCategoryEntity: ServiceCategoryEntity) {
        appRepository.insertServiceCategory(serviceCategoryEntity)
        Timber.e("Inserting Category -> ${serviceCategoryEntity.serviceCategory}")
    }
}