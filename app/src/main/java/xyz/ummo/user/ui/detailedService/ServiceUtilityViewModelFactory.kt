package xyz.ummo.user.ui.detailedService

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.ummo.user.data.repo.serviceUtility.ServiceUtilityRepo

class ServiceUtilityViewModelFactory(private val serviceUtilityRepo: ServiceUtilityRepo) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ServiceUtilityViewModel(serviceUtilityRepo) as T
    }
}