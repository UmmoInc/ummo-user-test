package xyz.ummo.user.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xyz.ummo.user.data.entity.ServiceUtilityEntity

@Dao
interface ServiceUtilityDao {
    /** 1. Upsert ServiceUtility **/
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun upsertServiceUtility(serviceUtilityEntity: ServiceUtilityEntity)

    /** 2. Read ServiceUtility **/
    @get:Query("SELECT * FROM service_utility_store")
    val serviceUtilityList: List<ServiceUtilityEntity>
}