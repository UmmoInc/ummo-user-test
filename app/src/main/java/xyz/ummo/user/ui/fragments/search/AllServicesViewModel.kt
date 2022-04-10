package xyz.ummo.user.ui.fragments.search

//import androidx.lifecycle.viewModelScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.repo.AllServicesRepository
import xyz.ummo.user.models.ServiceObject

class AllServicesViewModel(private val allServicesRepository: AllServicesRepository) : ViewModel() {
    //    private val allServicesRepo = AllServicesRepository()
    val servicesLiveDataList: MutableLiveData<ArrayList<ServiceEntity>> = MutableLiveData()
    val searchedServicesLiveDataList: MutableLiveData<List<ServiceEntity>> = MutableLiveData()

    /*fun getAllServicesLiveData(): ArrayList<ServiceEntity> {
        servicesLiveDataList.addAll(appRepo.services)
        return servicesLiveDataList
    }*/

//    fun getAllServicesFromServer(): ServiceObject = allServicesRepo.getAllServices()

    /*init {
        getLocallyStoredServices()
    }*/
    suspend fun getAllServicesFromServer() {

        withContext(Dispatchers.IO) {
            allServicesRepository.saveServicesInRoom()
        }
    }

    /*private fun getLocallyStoredServices() = viewModelScope.launch {
        servicesLiveDataList.postValue(allServicesRepository.getLocallyStoredServices())
    }*/

    suspend fun getLocallyStoredServices() {
        withContext(Dispatchers.IO) {
            servicesLiveDataList.postValue(allServicesRepository.getLocallyStoredServices())
        }
    }

    suspend fun searchServices(searchQuery: String) {
        withContext(Dispatchers.IO) {
            Timber.e("SEARCHING FOR -> $searchQuery")
            searchedServicesLiveDataList.postValue(allServicesRepository.searchServices(searchQuery))
        }
    }

    fun storeServicesInMutableLiveData(serviceObject: ServiceObject) {
//        val serviceObjectsArrayList = ArrayList(listOf(serviceObjects))
//        servicesLiveDataList.postValue(serviceObjectsArrayList)
    }
}