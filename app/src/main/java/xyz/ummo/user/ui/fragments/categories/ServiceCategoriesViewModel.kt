package xyz.ummo.user.ui.fragments.categories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.ummo.user.data.entity.ServiceCategoryEntity
import xyz.ummo.user.data.repo.serviceCategories.ServiceCategoriesRepo
import java.io.IOException

class ServiceCategoriesViewModel(private val serviceCategoriesRepo: ServiceCategoriesRepo) :
    ViewModel() {
    val serviceCategoriesLiveData: MutableLiveData<ArrayList<ServiceCategoryEntity>> =
        MutableLiveData()

    suspend fun saveAllServiceCategoriesFromServer() {
        withContext(Dispatchers.IO) {
            try {
                serviceCategoriesRepo.saveServiceCategoriesInRoom()
            } catch (IOE: IOException) {
                IOE.printStackTrace()
            }
        }
    }

    suspend fun getLocallyStoredServiceCategories() {
        withContext(Dispatchers.IO) {
            serviceCategoriesLiveData.postValue(serviceCategoriesRepo.getLocallyStoredServiceCategories())
        }
    }
}