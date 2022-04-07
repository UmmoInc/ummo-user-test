package xyz.ummo.user.utilities.serviceutils

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.*
import java.util.ArrayList

class SaveServiceLocally(mServiceObject: JSONObject, context: Context) {
    private var serviceEntity = ServiceEntity()
    private lateinit var serviceId: String
    private lateinit var serviceName: String
    private lateinit var serviceDescription: String
    private lateinit var serviceEligibility: String
    private lateinit var serviceCentresJSONArray: JSONArray
    private var serviceCentresArrayList = ArrayList(listOf<String>())
    private var delegatable: Boolean? = null
    private lateinit var serviceCostJSONArray: JSONArray
    private lateinit var serviceDocumentsJSONArray: JSONArray
    private val serviceDocumentsArrayList = ArrayList(listOf<String>())
    private lateinit var serviceDuration: String
    private var notUsefulCount:Int = 0
    private var usefulCount: Int = 0
    private lateinit var commentsJSONArray: JSONArray
    private val commentsArrayList = ArrayList(listOf<String>())
    private var serviceCommentCount = 0
    private var serviceShares = 0
    private var serviceViewCount = 0
    private lateinit var serviceProvider: String
    private var serviceBookmarked: Boolean? = null
    private var serviceDelegated: Boolean? = null
    private lateinit var serviceCategory: String

    /** Initializing ServiceViewModel **/
    private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
        .get(ServiceViewModel::class.java)

    init {
        try {
            /** [SERVICE-ASSIGNMENT: 0]
             * 1. Declaring $serviceID value
             * 2. Assigning $serviceID value from service JSON value **/
            serviceId= mServiceObject.getString("_id") //0

            /** [SERVICE-ASSIGNMENT: 1]
             * 1. Declaring $serviceName value
             * 2. Assigning $serviceName value from service JSON value **/
            serviceName= mServiceObject.getString(SERV_NAME) //1

            /** [SERVICE-ASSIGNMENT: 2]
             * 1. Declaring $serviceDescription value
             * 2. Assigning $serviceDescription value from service JSON value **/
            serviceDescription = mServiceObject.getString(SERV_DESCR) //2

            /** [SERVICE-ASSIGNMENT: 3]
             * 1. Declaring $serviceEligibility value
             * 2. Assigning $serviceEligibility value from service JSON value **/
            serviceEligibility = mServiceObject.getString(SERV_ELIG) //3

            /** [SERVICE-ASSIGNMENT: 4]
             * 1. Declaring $serviceCentres value
             * 2. Assigning $serviceCentres value to service JSON value **/
            serviceCentresJSONArray = mServiceObject.getJSONArray(SERV_CENTRES)

            for (j in 0 until serviceCentresJSONArray.length()) {
                serviceCentresArrayList.add(serviceCentresJSONArray.getString(j))
            }

            /** [SERVICE-ASSIGNMENT: 5]
             * 1. Declaring $presenceRequired value
             * 2. Assigning $presenceRequired value from service JSON value **/
            delegatable = mServiceObject.getBoolean(DELEGATABLE)

            /** [SERVICE-ASSIGNMENT: 6]
             * 1. Declaring $serviceCost value
             * 2. TODO: Assigning $serviceCost value to service JSON value **/
            serviceCostJSONArray = mServiceObject.getJSONArray(SERV_COST)

            /** [SERVICE-ASSIGNMENT: 7]
             * 1. Declaring $serviceDocuments values
             * 2. TODO: Assigning $serviceDocuments value from service JSON value **/
            serviceDocumentsJSONArray = mServiceObject.getJSONArray(SERV_DOCS)

            for (k in 0 until serviceDocumentsJSONArray.length()) {
                serviceDocumentsArrayList.add(serviceDocumentsJSONArray.getString(k))
            }

            /** [SERVICE-ASSIGNMENT: 8]
             * 1. Declaring $serviceDuration value
             * 2. Assigning $serviceDuration value to service JSON value **/
            serviceDuration = mServiceObject.getString(SERV_DURATION)

            /** [SERVICE-ASSIGNMENT: 9]
             * 1. Declaring $downVote value
             * 2. TODO: Assigning $downVote value from service JSON value **/
            notUsefulCount = mServiceObject.getInt(DOWNVOTE_COUNT)

            /** [SERVICE-ASSIGNMENT: 10]
             * 1. Declaring $upVote value
             * 2. TODO: Assigning $upVote value from service JSON value **/
            usefulCount = mServiceObject.getInt(UPVOTE_COUNT)

            /** [SERVICE-ASSIGNMENT: 11]
             * 1. Declaring $serviceComments values
             * 2. TODO: Assigning $serviceComments value from service JSON value **/
            commentsJSONArray = mServiceObject.getJSONArray(SERV_COMMENTS)
            for (k in 0 until commentsJSONArray.length()) {
                commentsArrayList.add(commentsJSONArray.getString(k))
            }

            /** [SERVICE-ASSIGNMENT: 12]
             * 1. Declaring $serviceCommentCount value **/
            serviceCommentCount = commentsArrayList.size

            /** [SERVICE-ASSIGNMENT: 13]
             * 1. Declaring $serviceShares value
             * 2. TODO: Stash $serviceShares value; replace with SAVE **/
            serviceShares = 0

            /** [SERVICE-ASSIGNMENT: 14]
             * 1. Declaring $serviceViewCount value **/
            serviceViewCount = 0

            /** [SERVICE-ASSIGNMENT: 15]
             * 1. Declaring $serviceProvider value
             * 2. Assigning $serviceProvider value to service JSON value **/
            serviceProvider = mServiceObject.getString(SERV_PROVIDER)

            /** [SERVICE-ASSIGNMENT: 16]
             * 1. Declaring $serviceBookmarked value
             * 2. Assigning $serviceBookmarked value to service JSON value **/
            serviceBookmarked = true

            /** [SERVICE-ASSIGNMENT: 17]
             * 1. Declaring $serviceDelegated value
             * 2. Assigning $serviceDelegated value to service JSON value **/
            serviceDelegated = true

            /** [SERVICE-ASSIGNMENT: 18]
             * 1. Declaring $serviceCategory value
             * 2. Assigning $serviceCategory value to service JSON value **/
            serviceCategory = mServiceObject.getString(SERVICE_CATEGORY)
        } catch (jse: JSONException) {
            Timber.e("JSE - > $jse")
        }
    }

    private fun assignServiceEntity(mServiceEntity: ServiceEntity) {
        mServiceEntity.serviceId = serviceId //0
        mServiceEntity.serviceName = serviceName //1
        mServiceEntity.serviceDescription = serviceDescription //2
        mServiceEntity.serviceEligibility = serviceEligibility //3
        mServiceEntity.serviceCentres = serviceCentresArrayList //4
        mServiceEntity.delegatable = delegatable //5
//        mServiceEntity.serviceCost = service.serviceCost //6
        mServiceEntity.serviceDocuments = serviceDocumentsArrayList //7
        mServiceEntity.serviceDuration = serviceDuration //8
        mServiceEntity.usefulCount = usefulCount //9
        mServiceEntity.notUsefulCount = notUsefulCount //10
        mServiceEntity.serviceComments = commentsArrayList //11
        mServiceEntity.commentCount = serviceCommentCount //12
        mServiceEntity.serviceShares = serviceShares //13
        mServiceEntity.serviceViews = serviceViewCount //14
        mServiceEntity.serviceProvider = serviceProvider //15
        mServiceEntity.serviceCategory = serviceCategory //16
    }

    fun savingService() {
        assignServiceEntity(serviceEntity)
        serviceViewModel.addService(serviceEntity)
        Timber.e("Saving Service Locally -> ${serviceEntity.serviceName}")
    }
}