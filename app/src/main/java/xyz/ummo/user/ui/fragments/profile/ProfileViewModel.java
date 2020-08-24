package xyz.ummo.user.ui.fragments.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import timber.log.Timber;
import xyz.ummo.user.data.entity.ProfileEntity;
import xyz.ummo.user.data.repo.AppRepository;

public class ProfileViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private LiveData<ProfileEntity> profileEntityLiveData;

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        appRepository = new AppRepository(application);
        profileEntityLiveData = appRepository.getProfileEntityLiveData();
    }

    public void insertProfile(ProfileEntity profileEntity){
        appRepository.insertProfile(profileEntity);
        Timber.e("insertProfile: PROFILE->%s", profileEntity.getProfileName());
    }

    public void updateProfile(ProfileEntity profileEntity){
        appRepository.updateProfile(profileEntity);
    }

    public void deleteAllProfile(){
        appRepository.deleteProfile();
    }

    public LiveData<ProfileEntity> getProfileEntityLiveData(){
        return appRepository.getProfileEntityLiveData();
    }
}
