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
import xyz.ummo.user.api.ServiceComment
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
    private var serviceCommentsArrayList = ArrayList<ServiceCommentEntity>()

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

    /** We need to populate [serviceCommentsArrayList] with data from the API **/
    private fun fetchServiceComments(serviceID: String): String {
        val request = Request.Builder()
            .url("${activity.resources.getString(R.string.serverUrl)}/product/comments?_id=$serviceID")
            .header("jwt", jwt)
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            response.body!!.string()
        }
    }

    private fun postServiceCommentToServer(serviceCommentEntity: ServiceCommentEntity) {
        val serviceCommentObject = JSONObject()

        try {
            serviceCommentObject.put("_id", serviceCommentEntity.serviceId)
                .put("service_comment", serviceCommentEntity.serviceComment)
                .put("comment_date", serviceCommentEntity.commentDateTime)
                .put("user_contact", serviceCommentEntity.userContact)

            object : ServiceComment(activity.applicationContext, serviceCommentObject) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {
                        Timber.e("COMMENT SENT TO SERVER!")
                    }
                }
            }

        } catch (jse: JSONException) {
            Timber.e("SERVICE-COMMENT-ERROR -> $jse")
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

                    Timber.e("COUNT OF SERVICE COMMENTS -> ${serviceCommentsJSONArray.length()}")
                    Timber.e("SERVICE COMMENT -> $serviceComment")
                    serviceId = serviceComment.getString("_id")
                    commentString = serviceComment.getString("service_comment")
                    commentDateTime = serviceComment.getString("comment_date")

                    if (serviceComment.has("user_contact")) {

                        userObject = serviceComment.getJSONObject("user_contact")
                        serviceCommentEntity =
                            ServiceCommentEntity(
                                commentString,
                                serviceId,
                                commentDateTime,
                                userObject.getString("name"),
                                userObject.getString("mobile_contact")
                            )
                    } else {
                        serviceCommentEntity =
                            ServiceCommentEntity(
                                commentString,
                                serviceId,
                                commentDateTime,
                                userName = "Anonymous",
                                userContact = "N/A"
                            )
                    }
                    serviceCommentsArrayList.add(serviceCommentEntity)
                    Timber.e("SERVICE COMMENTS -> $serviceCommentsArrayList")
                }
            } catch (jse: JSONException) {
                Timber.e("FAILED TO PARSE SERVICE COMMENT -> $jse")
            }
        }
        return serviceCommentsArrayList
    }

    suspend fun saveServiceCommentsFromServerToRoom(serviceID: String) {
        val mServiceCommentsArrayList =
            parseServiceCommentStringReturnServiceCommentEntityArrayList(serviceID)
        for (serviceComment in mServiceCommentsArrayList) {
            Timber.e("SAVING SERVICE COMMENT INTO ROOM -> $serviceComment")
            serviceCommentDao.upsertServiceComment(serviceComment)
        }
    }

    fun saveServiceCommentFromInputToRoom(serviceCommentEntity: ServiceCommentEntity) {
        postServiceCommentToServer(serviceCommentEntity)
        Timber.e("SAVING SERVICE COMMENT FROM INPUT INTO ROOM -> ${serviceCommentEntity.serviceComment}")
        serviceCommentDao.upsertServiceComment(serviceCommentEntity)
    }

    fun getServiceCommentsFromRoomByServiceId(serviceID: String): ArrayList<ServiceCommentEntity> {
        return serviceCommentDao.getServiceCommentsByServiceId(serviceID) as ArrayList<ServiceCommentEntity>
    }

    fun getServiceCommentsFromRoomByService(serviceID: String) =
        serviceCommentDao.getServiceCommentsByServiceId(serviceID)
}