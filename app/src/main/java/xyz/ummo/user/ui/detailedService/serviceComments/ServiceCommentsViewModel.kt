package xyz.ummo.user.ui.detailedService.serviceComments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.ummo.user.data.entity.ServiceCommentEntity
import xyz.ummo.user.data.repo.serviceSomments.ServiceCommentsRepo
import java.io.IOException

class ServiceCommentsViewModel(
    private val mServiceId: String,
    private val serviceCommentsRepo: ServiceCommentsRepo
) : ViewModel() {
    val serviceCommentsMutableLiveData: MutableLiveData<ArrayList<ServiceCommentEntity>> =
        MutableLiveData()

    suspend fun saveServiceCommentsToRoom() {

        withContext(Dispatchers.IO) {
            try {
                serviceCommentsRepo.saveServiceCommentsFromServerToRoom(mServiceId)
            } catch (IOE: IOException) {
                IOE.printStackTrace()
            }
        }
    }

    suspend fun saveServiceCommentFromUserToRoom(serviceCommentEntity: ServiceCommentEntity) {
        withContext(Dispatchers.IO) {
            try {
                serviceCommentsRepo.saveServiceCommentFromInputToRoom(serviceCommentEntity)
            } catch (IOE: IOException) {
                IOE.printStackTrace()
            }
        }
    }

    /*suspend fun getServiceCommentsFromRoom() {
        withContext(Dispatchers.IO) {
            serviceCommentsMutableLiveData.postValue(serviceCommentsRepo.getServiceCommentsFromRoom())
        }
    }*/

    suspend fun getServiceCommentsFromRoomByServiceId(serviceId: String) {
        withContext(Dispatchers.IO) {
            try {
                serviceCommentsMutableLiveData.postValue(
                    serviceCommentsRepo.getServiceCommentsFromRoomByServiceId(
                        serviceId
                    )
                )
            } catch (IOE: IOException) {
                IOE.printStackTrace()
            }
        }
    }

    val serviceComments: LiveData<List<ServiceCommentEntity>> =
        serviceCommentsRepo.getServiceCommentsFromRoomByService(mServiceId).asLiveData()
}