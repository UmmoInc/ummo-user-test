package xyz.ummo.user.ui.fragments.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.ummo.user.data.repo.AllServicesRepository
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.utilities.Resource

class AllServicesViewModel(
    val allServicesRepository: AllServicesRepository
) : ViewModel() {

//    fun getAllServices() = viewModelScope.la
    val allServicesLiveData: MutableLiveData<ServiceObject> = MutableLiveData()

    fun getAllServices() = viewModelScope.launch {
//        allServicesLiveData.postValue()
    }
}