package xyz.ummo.user.data.repo.serviceUtility

import android.app.Activity
import android.preference.PreferenceManager
import okhttp3.OkHttpClient
import xyz.ummo.user.data.dao.ServiceUtilityDao
import xyz.ummo.user.data.db.ServiceUtilityDatabase
import xyz.ummo.user.data.entity.ServiceUtilityEntity
import java.util.concurrent.TimeUnit

class ServiceUtilityRepo(val db: ServiceUtilityDatabase, private val activity: Activity) {
    private lateinit var serviceUtilityEntity: ServiceUtilityEntity
    private var serviceUtilityDao: ServiceUtilityDao
    private var serviceUtilityDatabase = ServiceUtilityDatabase.invoke(activity.applicationContext)

    private val client = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()

    private var serviceUtilityList = ArrayList<ServiceUtilityEntity>()

    init {
        serviceUtilityDao = serviceUtilityDatabase.serviceUtilityDao()!!
    }

    /** Auth JWT **/
    val jwt: String = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        .getString("jwt", "").toString()

    /** Saving Service Utility values via [serviceUtilityDao] **/
    fun saveServiceUtility(
        serviceId: String,
        serviceName: String,
        helpful: Int,
        notHelpful: Int,
        timeStamp: String
    ) {
        val serviceUtilityEntity =
            ServiceUtilityEntity(0, serviceId, serviceName, helpful, notHelpful, timeStamp)
        serviceUtilityDao.upsertServiceUtility(serviceUtilityEntity)
    }
}