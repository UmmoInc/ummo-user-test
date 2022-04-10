package xyz.ummo.user.data.repo

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
import xyz.ummo.user.api.Service
import xyz.ummo.user.data.dao.ServiceDao
import xyz.ummo.user.data.db.AllServicesDatabase
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.fragments.search.AllServicesViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.workers.fromJSONArray
import xyz.ummo.user.workers.fromServiceCostJSONArray

//TODO: Saved for [UMMO-75]
/** This repo will get data from DB and/or our API and propagate it to the viewModel associated **/
class AllServicesRepository(
    val db: AllServicesDatabase,
    private val activity: Activity
) {
    private lateinit var serviceEntity: ServiceEntity
    private var serviceDao: ServiceDao
    private var allServicesDatabase = AllServicesDatabase.invoke(activity.applicationContext)

    private var client = OkHttpClient()
    private var servicesArrayList = ArrayList<ServiceEntity>()

    init {
        serviceDao = allServicesDatabase.serviceDao()!!
    }

    private lateinit var allServicesViewModel: AllServicesViewModel

    /** Auth JWT **/
    val jwt: String = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
        .getString("jwt", "").toString()

    private fun fetchAllServicesFromServer(): String {
        val request = Request.Builder()
            .url("${activity.resources.getString(R.string.serverUrl)}/product")
            .header("jwt", jwt)
            .build()

        return client.newCall(request).execute().use { response ->
            response.body!!.string()
        }
    }

    private suspend fun parseServicesStringReturnServiceEntityArrayList(): ArrayList<ServiceEntity> {
        val serviceResponse = withContext(Dispatchers.IO) {
            fetchAllServicesFromServer()
        }

        if (serviceResponse.isNotEmpty()) {
            val allServices = JSONObject(serviceResponse).getJSONArray("payload")
            var service: JSONObject
            try {
                for (i in 0 until allServices.length()) {
                    service = allServices[i] as JSONObject

                    serviceId = service.getString("_id") //1
                    serviceName = service.getString(SERV_NAME) //2
                    serviceDescription = service.getString(SERV_DESCR) //3
                    serviceEligibility = service.getString(SERV_ELIG) //4
//                                serviceCentres //5
                    serviceCentresJSONArray = service.getJSONArray(SERV_CENTRES)
                    serviceCentres = fromJSONArray(serviceCentresJSONArray)

                    delegatable = service.getBoolean(DELEGATABLE) //6
                    serviceCostJSONArray = service.getJSONArray(SERV_COST)
                    serviceCostArrayList = fromServiceCostJSONArray(serviceCostJSONArray)
//                                serviceCost = service.getString("service_cost") //7
//                                serviceDocuments = //8
                    serviceDocumentsJSONArray = service.getJSONArray(SERV_DOCS)
                    serviceDocuments = fromJSONArray(serviceDocumentsJSONArray)

                    serviceDuration = service.getString(SERV_DURATION) //9
                    approvalCount = service.getInt(UPVOTE_COUNT) //10
                    disapprovalCount = service.getInt(DOWNVOTE_COUNT) //11
//                                serviceComments = s //12
                    serviceCommentsJSONArray = service.getJSONArray(SERV_COMMENTS)
                    serviceComments = fromJSONArray(serviceCommentsJSONArray)

                    commentCount = service.getInt(SERV_COMMENT_COUNT) //13
                    shareCount = service.getInt(SERV_SHARE_COUNT) //14
                    viewCount = service.getInt(SERV_VIEW_COUNT) //15
                    serviceProvider = service.getString(SERV_PROVIDER) //16

                    /** Checking if [serviceBenefits] exists in the [Service] object,
                     * since not all services have benefits listed under them. **/
                    serviceBenefitJSONArray = if (service.has(SERVICE_BENEFITS)) {
                        service.getJSONArray(SERVICE_BENEFITS)
                    } else {
                        val noServiceBenefit = ServiceBenefit("", "")
                        Timber.e("NO SERVICE BENEFIT -> $noServiceBenefit")
                        serviceBenefitJSONArray.put(0, noServiceBenefit)
                    }

//                    serviceBenefits = fromServiceBenefitsJSONArray(serviceBenefitJSONArray)
                    serviceBenefits = fromJSONArray(serviceBenefitJSONArray)
                    serviceLink = service.getString(SERV_LINK).ifEmpty { "" }

                    serviceCategory = service.getString(SERVICE_CATEGORY)

                    try {
                        serviceAttachmentJSONArray = service.getJSONArray(SERV_ATTACH_OBJS)

                        for (x in 0 until serviceAttachmentJSONArray.length()) {
                            serviceAttachmentJSONObject =
                                serviceAttachmentJSONArray.getJSONObject(x)
                            serviceAttachmentName =
                                serviceAttachmentJSONObject.getString(FILE_NAME)
                            serviceAttachmentSize =
                                serviceAttachmentJSONObject.getString(FILE_SIZE)
                            serviceAttachmentURL =
                                serviceAttachmentJSONObject.getString(FILE_URI)
                        }
                    } catch (jse: JSONException) {
                        Timber.e("ISSUE PARSING SERVICE ATTACHMENT -> $jse")
                    }

                    serviceEntity = ServiceEntity(
                        serviceId, serviceName,
                        serviceDescription, serviceEligibility, serviceCentres,
                        delegatable/*, serviceCostArrayList*/, serviceDocuments,
                        serviceDuration, approvalCount, disapprovalCount,
                        serviceComments, commentCount, shareCount, viewCount,
                        serviceProvider,
                        true, false, // TODO: Attend to these special bools
                        serviceCategory, serviceLink, serviceBenefits
                    )

                    servicesArrayList.add(serviceEntity)
                    Timber.e("MINI SERVICES -> $serviceEntity")

                }
            } catch (jse: JSONException) {
                Timber.e("FAILED TO PARSE DELEGATABLE SERVICES -> $jse")
            }
        }
        return servicesArrayList
    }

    suspend fun saveServicesInRoom() {
        val mServicesArrayList = parseServicesStringReturnServiceEntityArrayList()
        for (serviceEntity in mServicesArrayList) {
            Timber.e("SAVING SERVICE IN ROOM ->  ${serviceEntity.serviceName}")
            serviceDao.upsertService(serviceEntity)
        }
    }

    fun searchServices(searchQuery: String) = serviceDao.searchRoomDB(searchQuery)

    fun getLocallyStoredServices(): ArrayList<ServiceEntity> {
        return serviceDao.serviceListData as ArrayList<ServiceEntity>
    }

    fun saveSingleServiceInRoom(mService: ServiceObject) {
        serviceEntity.serviceId = mService.serviceId
        serviceEntity.serviceName = mService.serviceName
        serviceEntity.serviceDescription = mService.serviceDescription
        serviceEntity.serviceEligibility = mService.serviceEligibility
        serviceEntity.serviceCentres = mService.serviceCentres
        serviceEntity.delegatable = mService.delegatable
        //TODO: Save service costs here!
        serviceEntity.serviceDocuments = mService.serviceDocuments
        serviceEntity.serviceDuration = mService.serviceDuration
        serviceEntity.usefulCount = mService.usefulCount
        serviceEntity.notUsefulCount = mService.notUsefulCount
        serviceEntity.serviceComments = mService.serviceComments
        serviceEntity.commentCount = mService.serviceCommentCount
        serviceEntity.serviceViews = mService.serviceViewCount
        serviceEntity.serviceProvider = mService.serviceProvider
        serviceEntity.serviceCategory = mService.serviceCategory
        Timber.e("SAVING SERVICE IN ROOM ->  ${serviceEntity.serviceName}")
        serviceDao.upsertService(serviceEntity)
    }
}