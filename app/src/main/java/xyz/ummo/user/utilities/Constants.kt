@file:JvmName("Constants")

package xyz.ummo.user.utilities

import android.app.Activity

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
const val SERV_COMMENT_COUNT = "service_comment_count"
const val SERV_SHARE_COUNT = "service_share_count"
const val SERV_VIEW_COUNT = "service_view_count"
const val SERV_PROVIDER = "service_provider"
const val SERV_LINK = "service_link"
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

/** App Startup Constants **/
const val CONTINUED = "CONTINUED"
const val SIGNED_UP = "SIGNED_UP"
const val FIRST_LAUNCH = "FIRST_LAUNCH"

const val DELEGATION_INTRO_IS_CONFIRMED = "DELEGATION_INTRO_IS_CONFIRMED"