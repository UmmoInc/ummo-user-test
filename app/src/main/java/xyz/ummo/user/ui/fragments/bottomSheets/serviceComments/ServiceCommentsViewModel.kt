package xyz.ummo.user.ui.fragments.bottomSheets.serviceComments

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.api.ViewServiceComments
import xyz.ummo.user.models.ServiceCommentObject

class ServiceCommentsViewModel(application: Application) :
    AndroidViewModel(application) {

    private var serviceCommentsArrayList = ArrayList<ServiceCommentObject>()

    fun getServiceComments(serviceId: String): ArrayList<ServiceCommentObject> {

        object : ViewServiceComments(serviceId) {
            var internalServiceCommentsArrayList = ArrayList<ServiceCommentObject>()
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    internalServiceCommentsArrayList = parseServiceCommentsPayload(data)
                    Timber.e("INTERNAL SERVICE COMMENTS -> $internalServiceCommentsArrayList")
                }
            }
        }
        Timber.e("SERVICE COMMENTS ARRAY LIST -> $serviceCommentsArrayList")

        return serviceCommentsArrayList
    }

    private fun parseServiceCommentsPayload(data: ByteArray): ArrayList<ServiceCommentObject> {
        val serviceCommentObjects = ArrayList<ServiceCommentObject>()
        val serviceCommentsJSONArray: JSONArray
        var serviceCommentJSONObject: JSONObject
        var serviceCommentObject: ServiceCommentObject

        try {
            serviceCommentsJSONArray = JSONObject(String(data)).getJSONArray("payload")

            for (i in 0 until serviceCommentsJSONArray.length()) {
                serviceCommentJSONObject = serviceCommentsJSONArray[i] as JSONObject
//                Timber.e("SERVICE COMMENT JSON OBJ -> $serviceCommentJSONObject")

                serviceCommentObject = if (serviceCommentJSONObject.has("user_contact")) {
                    ServiceCommentObject(
                        serviceCommentJSONObject.getString("_id"),
                        serviceCommentJSONObject.getString("service_comment"),
                        serviceCommentJSONObject.getString("comment_date"),
                        serviceCommentJSONObject
                            .getJSONObject("user_contact").getString("name")
                    )
                } else {
//                    Timber.e("NO USER OBJECT FOUND!")
                    ServiceCommentObject(
                        serviceCommentJSONObject.getString("_id"),
                        serviceCommentJSONObject.getString("service_comment"),
                        serviceCommentJSONObject
                            .getString("comment_date"), "Anonymous"
                    )
                }
//                Timber.e("SERVICE COMMENT OBJECT -> $serviceCommentObject")

                serviceCommentObjects.add(serviceCommentObject)
            }
//            Timber.e("SERVICE COMMENT OBJECT [2]-> $serviceCommentObjects")

            return serviceCommentObjects
        } catch (jsonArrayException: JSONException) {
            Timber.e("Unable to parse Service Comment Payload data")
            throw jsonArrayException
        }

    }
}