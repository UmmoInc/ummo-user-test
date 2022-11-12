package xyz.ummo.user.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.ummo.user.data.repo.allServices.AllServicesRepository
import xyz.ummo.user.data.repo.viewedServices.ViewedServicesRepo

class AllServicesViewModelProviderFactory(
    private val allServicesRepository: AllServicesRepository,
    private val viewedServicesRepo: ViewedServicesRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AllServicesViewModel(
            allServicesRepository,
            viewedServicesRepo
        ) as T //TODO: Check cast
    }
}