package xyz.ummo.user.data.repo.viewedServices

import android.app.Activity
import xyz.ummo.user.data.dao.ViewedServicesDao
import xyz.ummo.user.data.db.AllServicesDatabase
import xyz.ummo.user.data.entity.ViewedServices

class ViewedServicesRepo(
    val db: AllServicesDatabase,
    private val activity: Activity
) {
    private lateinit var viewedServices: ViewedServices
    private var viewedServicesDao: ViewedServicesDao
    private var allServicesDatabase = AllServicesDatabase.invoke(activity.applicationContext)

    init {
        viewedServicesDao = allServicesDatabase.viewedServicesDao()!!
    }

    fun saveViewedServicesInRoom(mViewedServices: ViewedServices) {
        viewedServicesDao.upsertViewedServices(mViewedServices)
    }

//    fun getViewedServicesInRoom(): LiveData<>
}