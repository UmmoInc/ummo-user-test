package xyz.ummo.user.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import xyz.ummo.user.data.entity.ViewedServices

@Dao
interface ViewedServicesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertViewedServices(viewedServices: ViewedServices)

    @get:Query("SELECT * FROM viewed_services")
    val viewedServicesLiveData: LiveData<ViewedServices>

    @Update
    fun updateViewedServices(viewedServices: ViewedServices?)

}