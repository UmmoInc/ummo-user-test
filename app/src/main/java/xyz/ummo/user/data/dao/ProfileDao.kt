package xyz.ummo.user.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.ummo.user.data.entity.ProfileEntity

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertProfile(profileEntity: ProfileEntity?)

    @get:Query("SELECT * FROM profile")
    val profileLiveData: LiveData<ProfileEntity?>?

    @get:Query("SELECT * FROM profile")
    val profileListData: List<ProfileEntity?>?

    @get:Query("SELECT * FROM profile")
    val profileEntityLiveDataById: LiveData<ProfileEntity?>?

    @Update
    fun updateProfile(profileEntity: ProfileEntity?)

    @Query("DELETE FROM profile")
    fun deleteProfile()
}