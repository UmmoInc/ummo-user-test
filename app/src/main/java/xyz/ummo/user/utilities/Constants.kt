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