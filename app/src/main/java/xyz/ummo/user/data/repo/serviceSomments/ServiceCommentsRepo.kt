package xyz.ummo.user.data.repo.serviceSomments

import android.app.Activity
import okhttp3.OkHttpClient
import xyz.ummo.user.data.dao.ServiceCommentDao
import xyz.ummo.user.data.db.ServiceCommentsDatabase
import xyz.ummo.user.data.entity.ServiceCommentEntity
import java.util.concurrent.TimeUnit

class ServiceCommentsRepo(
    val db: ServiceCommentsDatabase,
    private val activity: Activity
) {
    private lateinit var serviceCommentEntity: ServiceCommentEntity
    private var serviceCommentDao: ServiceCommentDao
    private var serviceCommentsDatabase =
        ServiceCommentsDatabase.invoke(activity.applicationContext)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()

    init {
        serviceCommentDao = serviceCommentsDatabase.serviceCommentDao()!!
    }


}