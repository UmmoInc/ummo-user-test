package xyz.ummo.user.ui.fragments.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.repo.AllServicesRepository

class AllServicesViewModel(private val allServicesRepository: AllServicesRepository) : ViewModel() {
    val servicesLiveDataList: MutableLiveData<ArrayList<ServiceEntity>> = MutableLiveData()
    val searchedServicesLiveDataList: MutableLiveData<List<ServiceEntity>> = MutableLiveData()

    /** A simple suspend function for retrieving services from the online API **/
    suspend fun getAllServicesFromServer() {
        withContext(Dispatchers.IO) {
            allServicesRepository.saveServicesInRoom()
        }
    }

    /** A simple suspend function for retrieving services from Room **/
    suspend fun getLocallyStoredServices() {
        withContext(Dispatchers.IO) {
            servicesLiveDataList.postValue(allServicesRepository.getLocallyStoredServices())
        }
    }

    /** We're executing our service search via the [viewModelScope] coroutine whereby we check
     *  if the [searchQuery] is not empty, if it is, we fill up [searchedServicesLiveDataList]
     *  with all services retrieved by [getLocallyStoredServices];
     *  ELSE we fill up [searchedServicesLiveDataList] with results from
     *  [AllServicesRepository.searchServices].
     *
     *  This function gets called in [AllServicesFragment]'s searchView. **/
    fun searchForServices(searchQuery: String) {
        viewModelScope.launch {
            if (searchQuery.isBlank()) {
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
}