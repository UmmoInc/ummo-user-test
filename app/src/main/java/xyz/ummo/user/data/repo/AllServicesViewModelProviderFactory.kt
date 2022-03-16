package xyz.ummo.user.data.repo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.ummo.user.ui.fragments.search.AllServicesViewModel

class AllServicesViewModelProviderFactory(
    val allServicesRepository: AllServicesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AllServicesViewModel(allServicesRepository) as T
    }
}