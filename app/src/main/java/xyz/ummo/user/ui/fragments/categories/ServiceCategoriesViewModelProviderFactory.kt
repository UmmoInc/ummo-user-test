package xyz.ummo.user.ui.fragments.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.ummo.user.data.repo.serviceCategories.ServiceCategoriesRepo

class ServiceCategoriesViewModelProviderFactory(
    private val serviceCategoriesRepo: ServiceCategoriesRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ServiceCategoriesViewModel(serviceCategoriesRepo) as T
    }
}