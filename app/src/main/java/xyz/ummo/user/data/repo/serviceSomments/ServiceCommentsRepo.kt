package xyz.ummo.user.data.repo.serviceSomments

import android.app.Activity
import android.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
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
    private var serviceComments = ArrayList<ServiceCommentEntity>()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(2, TimeUnit.MINUTES)
        .readTimeout(2, TimeUnit.MINUTES)
        .writeTimeout(2, TimeUnit.MINUTES)
        .build()

    /** Auth JWT **/
    val jwt: String = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        .getString("jwt", "").toString()

    init {
        serviceCommentDao = serviceCommentsDatabase.serviceCommentDao()!!
    }

    /** We need to populate [serviceComments] with data from the API **/
    private fun fetchServiceComments(serviceID: String): String {
        val request = Request.Builder()
            .url("${activity.resources.getString(R.string.serverUrl)}/product/comments?_id=$serviceID")
            .header("jwt", jwt)
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            response.body!!.string()
        }
    }

    private suspend fun parseServiceCommentStringReturnServiceCommentEntityArrayList(serviceID: String): ArrayList<ServiceCommentEntity> {
        val serviceCommentResponse = withContext(Dispatchers.IO) {
            fetchServiceComments(serviceID)
        }

        if (serviceCommentResponse.isNotEmpty()) {
            val serviceCommentsJSONArray =
                JSONObject(serviceCommentResponse).getJSONArray("payload")
            var serviceComment: JSONObject
            var serviceId: String
            var commentString: String
            var commentDateTime: String
            var userObject: JSONObject

            try {
                for (i in 0 until serviceCommentsJSONArray.length()) {
                    serviceComment = serviceCommentsJSONArray[i] as JSONObject

                    Timber.e("SERVICE COMMENT -> $serviceComment")
                    serviceId = serviceComment.getString("_id")
                    commentString = serviceComment.getString("service_comment")
                    commentDateTime = serviceComment.getString("comment_date")
                    userObject = serviceComment.getJSONObject("user_contact")

                    serviceCommentEntity =
                        ServiceCommentEntity(
                            serviceId,
                            commentString,
                            commentDateTime,
                            userObject.getString("name")
                        )

                    serviceComments.add(serviceCommentEntity)
                    Timber.e("SERVICE COMMENTS -> $serviceComments")
                }
            } catch (jse: JSONException) {
                Timber.e("FAILED TO PARSE SERVICE COMMENT -> $jse")
            }
        }
        return serviceComments
    }

    suspend fun saveServiceCommentsToRoom(serviceID: String) {
        val mServiceCommentsArrayList =
            parseServiceCommentStringReturnServiceCommentEntityArrayList(serviceID)
        for (serviceComment in mServiceCommentsArrayList) {
            Timber.e("SAVING SERVICE COMMENT IN ROOM -> ${serviceCommentEntity.serviceComment}")
            serviceCommentDao.upsertServiceComment(serviceCommentEntity)
        }
    }

    fun getServiceCommentsFromRoom(): ArrayList<ServiceCommentEntity> {
        return serviceCommentDao.serviceComment as ArrayList<ServiceCommentEntity>
    }
}