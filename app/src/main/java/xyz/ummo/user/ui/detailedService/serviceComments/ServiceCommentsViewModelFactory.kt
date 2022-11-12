package xyz.ummo.user.ui.detailedService.serviceComments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import xyz.ummo.user.data.repo.serviceSomments.ServiceCommentsRepo

class ServiceCommentsViewModelFactory(
    private val mServiceId: String,
    private val serviceCommentsRepo: ServiceCommentsRepo
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ServiceCommentsViewModel(mServiceId, serviceCommentsRepo) as T //TODO: Check cast
    }
}