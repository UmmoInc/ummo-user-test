package xyz.ummo.user.ui.fragments.profile;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import xyz.ummo.user.data.entity.ProfileEntity;
import xyz.ummo.user.data.model.Profile;
import xyz.ummo.user.data.repo.AppRepository;

public class ProfileViewModel extends AndroidViewModel {

    private AppRepository appRepository;
    private LiveData<ProfileEntity> profileEntityLiveData;
    private static final String TAG = "ProfileViewModel";

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        appRepository = new AppRepository(application);
        profileEntityLiveData = appRepository.getProfileEntityLiveData();
    }

    public void insertProfile(ProfileEntity profileEntity){
        appRepository.insertProfile(profileEntity);
        Log.e(TAG, "insertProfile: PROFILE->"+profileEntity.getProfileName());
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
