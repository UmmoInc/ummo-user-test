package xyz.ummo.user.ui.fragments.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.data.repo.AppRepository
import xyz.ummo.user.workers.SocketConnectWorker

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository: AppRepository = AppRepository(application)

    /** Creating an instance of WorkManager for SocketConnectWorker **/
    private val socketConnectWorkManager = WorkManager.getInstance(application)

    val profileEntityLiveData: LiveData<ProfileEntity> = appRepository.profileEntityLiveData
    val profileEntityListData: List<ProfileEntity> = appRepository.profileEntityListData

    internal fun socketConnect() {
        socketConnectWorkManager.enqueue(OneTimeWorkRequest.from(SocketConnectWorker::class.java))
    }

    fun insertProfile(profileEntity: ProfileEntity?) {
        appRepository.insertProfile(profileEntity)
        //        Timber.e("insertProfile: PROFILE->%s", profileEntity.profileName);
    }

    fun updateProfile(profileEntity: ProfileEntity?) {
        appRepository.updateProfile(profileEntity)
    }

    fun deleteAllProfile() {
        appRepository.deleteProfile()
    }

}