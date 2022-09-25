package xyz.ummo.user.ui.fragments.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.ummo.user.data.repo.allServices.AllServicesRepository

class AllServicesViewModelProviderFactory(
    private val allServicesRepository: AllServicesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AllServicesViewModel(allServicesRepository) as T //TODO: Check cast
    }
}