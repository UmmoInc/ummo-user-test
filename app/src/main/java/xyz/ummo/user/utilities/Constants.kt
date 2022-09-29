@file:JvmName("Constants")

package xyz.ummo.user.utilities

import android.app.Activity
import org.json.JSONArray
import org.json.JSONObject
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel

/** Notification Channel constants
 * Name of Notification Channel for verbose notifications of background work **/
@JvmField
val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
        "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION = "Shows notifications whenever work starts"

@JvmField
val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1
const val DELAY_TIME_MILLIS: Long = 3000

/** OneSignal Notification Constants **/
const val ACTION_ID = "ACTION_ID"
const val ACTION_TAKEN = "ACTION_TAKEN"
const val LAUNCH_URL = "LAUNCH_URL"
const val NOTIFICATION_BODY = "NOTIFICATION_BODY"

const val ummoUserPreferences = "UMMO_USER_PREFERENCES"
const val mode = Activity.MODE_PRIVATE
const val SERVICE_STATE = "SERVICE_STATE"
const val DELEGATION_STATE = "DELEGATION_STATE"
const val PENDING = "PENDING"
const val STARTED = "STARTED"
const val DELAYED = "DELAYED"
const val DONE = "DONE"
const val DELIVERED = "DELIVERED"
const val RATED = "RATED"

const val SERVICE_PENDING = "SERVICE_PENDING"
const val CURRENT_SERVICE_PENDING = "CURRENT_SERVICE_PENDING"
const val SERVICE_ID = "SERVICE_ID"
const val DELEGATION_ID = "DELEGATION_ID"
const val SERVICE_OBJECT = "SERVICE_OBJECT"
const val SERVICE_ENTITY = "SERVICE_ENTITY"
const val SERVICE_IMAGE = "SERVICE_IMAGE"
const val SERVICE_COMMENTS = "SERVICE_COMMENTS"
const val DELEGATION_FEE = "DELEGATION_FEE"
const val DELEGATION_SPEC = "DELEGATION_SPEC"
const val SERVICE_AGENT_ID = "SERVICE_AGENT_ID"
const val DELEGATED_SERVICE_ID = "DELEGATED_SERVICE_ID"
const val AGENT_ID = "AGENT_ID"

const val SERVICE_NAME = "SERVICE_NAME"
const val SPEC_FEE = "SPEC_FEE"
const val SERVICE_SPEC = "SERVICE_SPEC"
const val SERVICE_DATE = "SERVICE_DATE"

const val CHOSEN_SERVICE_SPEC = "chosen_service_spec"
const val TOTAL_DELEGATION_FEE = "total_delegation_fee"
const val OPEN_DELEGATION = "OPEN_DELEGATION"

const val SERVICE_DELEGATED = "SERVICE_DELEGATED"
const val WHATSAPP_LAUNCHED = "WHATSAPP_LAUNCHED"

/** The Constants below will be used to parse services based on the ServiceObject schema **/
const val SERV_NAME = "service_name"
const val SERV_DESCR = "service_description"
const val SERV_ELIG = "service_eligibility"
const val SERV_CENTRES = "service_centres"
const val DELEGATABLE = "delegatable"
const val SERV_COST = "service_cost"
const val SERV_DOCS = "service_documents"
const val SERV_DURATION = "service_duration"
const val UPVOTE_COUNT = "useful_count"
const val DOWNVOTE_COUNT = "not_useful_count"
const val SERV_COMMENTS = "service_comments"
const val SERV_STEPS = "service_steps"
const val SERV_COMMENT_COUNT = "service_comment_count"
const val SERV_SHARE_COUNT = "service_share_count"
const val SERV_VIEW_COUNT = "service_view_count"
const val SERV_PROVIDER = "service_provider"
const val SERV_LINK = "service_link"
const val SERVICE_CATEGORY = "service_category"
const val SERVICE_BENEFITS = "service_benefits"
const val REASON_TITLE = "reason_title"
const val REASON_BODY = "reason_body"
const val SERV_ATTACH_OBJS = "service_attachment_objects"
const val FILE_NAME = "file_name"
const val FILE_SIZE = "file_size"
const val FILE_URI = "file_uri"

/** The constants below are mostly used in DetailedServiceActivity.kt **/
const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
const val ATTACHMENT_DOWNLOADED = "ATTACHMENT_DOWNLOADED"
const val REQUESTED_SERVICE = "REQUESTED_SERVICE"

/** User Profile Constants**/
const val USER_CONTACT = "USER_CONTACT"
const val USER_NAME = "USER_NAME"
const val USER_EMAIL = "USER_EMAIL"
const val USER_PID = "USER_PID"
const val NEW_SESSION = "NEW_SESSION"
const val SIGN_UP_DATE = "SIGN_UP_DATE"
const val MIXPANEL_NAME = "\$name"
const val EMAIL_VERIFIED = "EMAIL_VERIFIED"

/** App Startup Constants **/
const val UMMO_INTRO_COMPLETE = "UMMO_INTRO_COMPLETE"
const val SIGNED_UP = "SIGNED_UP"
const val FIRST_LAUNCH = "FIRST_LAUNCH"
const val APP_INTRO_COMPLETE = "APP_INTRO_COMPLETE"

const val DELEGATION_INTRO_IS_CONFIRMED = "DELEGATION_INTRO_IS_CONFIRMED"
const val CATEGORY = "CATEGORY"
const val EMAIL_REMINDER_SENT = "EMAIL_REMINDER_SENT"

/** View Sources **/
const val VIEW_SOURCE = "VIEW_SOURCE"
const val DETAILED_SERVICE = "DETAILED_SERVICE"

const val TAKE_ME_TO = "TAKE_ME_TO"
const val DELEGATED_SERVICE_FRAGMENT = "DELEGATED_SERVICE_FRAGMENT"

const val OFFLINE_LOADED = "OFFLINE_LOADED"

/** Service Variables **/
lateinit var serviceId: String //1
lateinit var serviceName: String //2
lateinit var serviceDescription: String //3
lateinit var serviceEligibility: String //4
var serviceCentres = ArrayList<String>() //5
lateinit var serviceCentresJSONArray: JSONArray //5.1
var delegatable: Boolean = false //6
lateinit var serviceCost: String //7
lateinit var serviceCostArrayList: ArrayList<ServiceCostModel> //7.1
lateinit var serviceCostJSONArray: JSONArray //7.1
var serviceDocuments = ArrayList<String>() //8
lateinit var serviceDocumentsJSONArray: JSONArray //8.1
lateinit var serviceDuration: String //9
var approvalCount: Int = 0 //10
var disapprovalCount: Int = 0 //11
var serviceComments = ArrayList<String>() //12
lateinit var serviceCommentsJSONArray: JSONArray //12.1
var serviceSteps = ArrayList<String>()
lateinit var serviceStepsJSONArray: JSONArray
var commentCount: Int = 0 //13
var shareCount: Int = 0 //14
var viewCount: Int = 0 //15
lateinit var serviceProvider: String //16
var serviceLink: String = "" //17
var serviceAttachmentJSONArray = JSONArray() //18
var serviceAttachmentJSONObject = JSONObject() //18.1
var serviceAttachmentName = "" //18.2
var serviceAttachmentSize = "" //18.3
var serviceAttachmentURL = "" //18.4
var serviceBenefitJSONArray = JSONArray() //19
var serviceBenefits = ArrayList<ServiceBenefit>() //19.1
var serviceCategory = "" //20

var serviceCategoryName = ""
var serviceCategoryTotal = 0

const val PARENT = "PARENT"
const val FRAGMENT_DESTINATION = "FRAG_DEN"
const val OPEN_DELEGATED_SERVICE_FRAG = "OPEN_DELEGATED_SERVICE_FRAG"

/** Service Provider Constants **/
const val EDUCATION = "61827579ab47d4cbaf5a3615"
const val HEALTH = "61825c2dab47d4cbaf5a360a"
const val AGRICULTURE = "61827559ab47d4cbaf5a3614"
const val REVENUE = "601268ff5ad77100154da835"
const val COMMERCE = "601266be5ad77100154da833"
const val HOME_AFFAIRS = "601268725ad77100154da834"