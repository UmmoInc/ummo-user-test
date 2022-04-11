package xyz.ummo.user.ui.fragments.search

//import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.repo.AllServicesRepository
import xyz.ummo.user.models.ServiceObject

class AllServicesViewModel(private val allServicesRepository: AllServicesRepository) : ViewModel() {
    //    private val allServicesRepo = AllServicesRepository()
//    private lateinit var serviceDao: ServiceDao
    val servicesLiveDataList: MutableLiveData<ArrayList<ServiceEntity>> = MutableLiveData()
    val searchedServicesLiveDataList: MutableLiveData<List<ServiceEntity>> = MutableLiveData()

    suspend fun getAllServicesFromServer() {

        withContext(Dispatchers.IO) {
            allServicesRepository.saveServicesInRoom()
        }
    }

    suspend fun getLocallyStoredServices() {
        withContext(Dispatchers.IO) {
            servicesLiveDataList.postValue(allServicesRepository.getLocallyStoredServices())
        }
    }

    fun searchForServices(searchQuery: String) {
        viewModelScope.launch {
            if (searchQuery.isNullOrBlank()) {
                allServicesRepository.getLocallyStoredServices().let {
                    searchedServicesLiveDataList.postValue(it)
                }
            } else {
                allServicesRepository.searchServices("%$searchQuery%").let {
                    searchedServicesLiveDataList.postValue(it)
                }
            }
        }
    }

    fun storeServicesInMutableLiveData(serviceObject: ServiceObject) {
//        val serviceObjectsArrayList = ArrayList(listOf(serviceObjects))
//        servicesLiveDataList.postValue(serviceObjectsArrayList)
    }
}