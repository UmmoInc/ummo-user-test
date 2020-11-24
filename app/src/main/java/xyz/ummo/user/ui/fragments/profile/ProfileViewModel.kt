package xyz.ummo.user.ui.fragments.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.data.repo.AppRepository

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository: AppRepository = AppRepository(application)
    val profileEntityLiveData: LiveData<ProfileEntity>
    val profileEntityListData: List<ProfileEntity>
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

    init {
        profileEntityLiveData = appRepository.profileEntityLiveData
        profileEntityListData = appRepository.profileEntityListData
    }
}