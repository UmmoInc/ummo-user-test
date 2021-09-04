package xyz.ummo.user.ui.fragments.pagesFrags.tfuma

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.data.repo.AppRepository
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.utilities.*
import xyz.ummo.user.workers.SocketConnectWorker
import xyz.ummo.user.workers.fromJSONArray
import xyz.ummo.user.workers.fromServiceCostJSONArray

class TfumaViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)
    private lateinit var delegatableService: ServiceObject

    private val socketConnectWorkManager = WorkManager.getInstance(application)

    internal fun socketConnect() {
        socketConnectWorkManager.enqueue(OneTimeWorkRequest.from(SocketConnectWorker::class.java))
    }

    internal fun parseAndDisplayServices(
        serviceJSONArray: JSONArray,
        serviceCondition: String
    ): ServiceObject? {
        var service: JSONObject
        var conditionMet: Boolean
        var serviceId: String
        var serviceName: String
        var serviceDescription: String
        var serviceEligibility: String

        var serviceCentresJSONArray: JSONArray
        var serviceCentres: ArrayList<String>

        var serviceCostJSONArray: JSONArray
        var serviceCosts: ArrayList<ServiceCostModel>

        var serviceDocsJSONArray: JSONArray
        var serviceDocs: ArrayList<String>

        var serviceCommentsJSONArray: JSONArray
        var serviceComments: ArrayList<String>

        var serviceAttachmentJSONArray: JSONArray
        var serviceAttachmentJSONObject: JSONObject
        var serviceAttachmentName = ""
        var serviceAttachmentSize = ""
        var serviceAttachmentURL = ""

        var serviceDuration: String
        var upvoteCount: Int
        var downvoteCount: Int
        var serviceProvider: String
        var serviceLink: String
        var commentCount: Int
        var shareCount: Int
        var viewCount: Int

        return try {
            for (i in 0 until serviceJSONArray.length()) {
                service = serviceJSONArray[i] as JSONObject
                conditionMet = service.getBoolean(serviceCondition)

                if (conditionMet) {
                    serviceId = service.getString(SERV_NAME) //1
                    serviceName = service.getString(SERV_NAME) //2
                    serviceDescription = service.getString(SERV_DESCR) //3
                    serviceEligibility = service.getString(SERV_ELIG) //4
                    serviceCentresJSONArray = service.getJSONArray(SERV_CENTRES) //5
                    serviceCentres = fromJSONArray(serviceCentresJSONArray) //5
                    conditionMet = service.getBoolean(DELEGATABLE) //6
                    serviceCostJSONArray = service.getJSONArray(SERV_COST) //7
                    serviceCosts = fromServiceCostJSONArray(serviceCostJSONArray) //7
                    serviceDocsJSONArray = service.getJSONArray(SERV_DOCS) //8
                    serviceDocs = fromJSONArray(serviceDocsJSONArray) //8
                    serviceDuration = service.getString(SERV_DURATION) //9
                    upvoteCount = service.getInt(UPVOTE_COUNT) //10
                    downvoteCount = service.getInt(DOWNVOTE_COUNT) //11
                    serviceCommentsJSONArray = service.getJSONArray(SERV_COMMENTS) //12
                    serviceComments = fromJSONArray(serviceCommentsJSONArray) //12
                    commentCount = service.getInt(SERV_COMMENT_COUNT) //13
                    shareCount = service.getInt(SERV_SHARE_COUNT) //14
                    viewCount = service.getInt(SERV_VIEW_COUNT) //15
                    serviceProvider = service.getString(SERV_PROVIDER) //16

                    serviceLink = if (service.getString(SERV_LINK).isNotEmpty())
                        service.getString(SERV_LINK)
                    else
                        ""

                    try {
                        serviceAttachmentJSONArray = service.getJSONArray(SERV_ATTACH_OBJS)

                        for (x in 0 until serviceAttachmentJSONArray.length()) {
                            serviceAttachmentJSONObject =
                                serviceAttachmentJSONArray.getJSONObject(x)
                            serviceAttachmentName = serviceAttachmentJSONObject.getString(FILE_NAME)
                            serviceAttachmentSize = serviceAttachmentJSONObject.getString(FILE_NAME)
                            serviceAttachmentURL = serviceAttachmentJSONObject.getString(FILE_NAME)
                        }
                    } catch (jse: JSONException) {
                        Timber.e("ISSUE PARSING SERVICE ATTACHMENT")
                    }

                    delegatableService = ServiceObject(
                        serviceId, serviceName, serviceDescription,
                        serviceEligibility, serviceCentres, conditionMet, serviceCosts, serviceDocs,
                        serviceDuration, upvoteCount, downvoteCount, serviceComments, commentCount,
                        shareCount, viewCount, serviceProvider, serviceLink, serviceAttachmentName,
                        serviceAttachmentSize, serviceAttachmentURL
                    )

//                    return delegatableService

                }
            }
            return delegatableService
        } catch (jse: JSONException) {
            Timber.e("FAILED TO PARSE JSON-ARRAY")
            return null
        }
//        Timber.e("RETURNING DELEGATABLE SERVICE")
//        return delegatableService
    }
}