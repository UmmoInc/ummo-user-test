package xyz.ummo.user.ui.detailedService

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.ummo.user.data.entity.ServiceUtilityEntity
import xyz.ummo.user.data.repo.serviceUtility.ServiceUtilityRepo
import java.io.IOException

class ServiceUtilityViewModel(private val serviceUtilityRepo: ServiceUtilityRepo) : ViewModel() {
    val serviceUtilitiesLiveDataList: MutableLiveData<List<ServiceUtilityEntity>> =
        MutableLiveData()

    suspend fun captureServiceUtility(
        serviceId: String,
        serviceName: String,
        helpful: Int,
        notHelpful: Int,
        timeStamp: String
    ) {
        withContext(Dispatchers.IO) {
            try {
                serviceUtilityRepo.saveServiceUtility(
                    serviceId,
                    serviceName,
                    helpful,
                    notHelpful,
                    timeStamp
                )
            } catch (IOE: IOException) {
                IOE.printStackTrace()
            }
        }
    }
}