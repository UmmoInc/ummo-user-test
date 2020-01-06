package xyz.ummo.user.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.data.entity.ProfileEntity;

@Dao
public interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProfile(ProfileEntity profileEntity);

    @Query("SELECT * FROM profile")
    LiveData<ProfileEntity> getProfileLiveData();

    @Query("SELECT * FROM profile")
    LiveData<ProfileEntity> getProfileEntityLiveDataById();

    @Update
    void updateProfile(ProfileEntity profileEntity);

    @Query("DELETE FROM profile")
    void deleteProfile();
}
