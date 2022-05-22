package xyz.ummo.user.ui.fragments.categories

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.ummo.user.data.entity.ServiceCategoryEntity
import xyz.ummo.user.data.repo.serviceCategories.CategoriesRepo
import java.io.IOException

class ServiceCategoriesViewModel(private val categoriesRepo: CategoriesRepo) : ViewModel() {
    val serviceCategoriesLiveData: MutableLiveData<ArrayList<ServiceCategoryEntity>> = MutableLiveData()

    suspend fun getAllServiceCategoriesFromServer() {
        withContext(Dispatchers.IO) {
            try {
                categoriesRepo.saveServiceCategoriesInRoom()
            } catch (IOE: IOException) {
                IOE.printStackTrace()
            }
        }
    }

    suspend fun getLocallyStoredServiceCategories() {
        withContext(Dispatchers.IO) {
            serviceCategoriesLiveData.postValue(categoriesRepo.getLocallyStoredServiceCategories())
        }
    }
}