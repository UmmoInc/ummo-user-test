@file:JvmName("WorkerUtils")

package xyz.ummo.user.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.utilities.*

/**
 * @author Rego
 * We're creating a notification for every Work done by a particular Worker
 * @param message Message shown on the notification
 * @param context Context needed for a Toast
 * **/

fun makeStatusNotification(title: String, message: String, context: Context) {

    /** Making a channel if necessary **/
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        /** Since the NotificationChannel is relatively new and not in the support library,
         * we are creating the NotificationChannel, but only on API 26+ **/
        val name = VERBOSE_NOTIFICATION_CHANNEL_NAME
        val description = VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description

        /** Add the channel **/
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)

        /** Create the notification **/
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(LongArray(0))

        /** Show the Notification **/
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }
}

/**
 * Method for sleeping for a fixed amount of time to emulate slower work
 */
fun sleep() {
    try {
        Thread.sleep(DELAY_TIME_MILLIS, 0)
    } catch (e: InterruptedException) {
        Timber.e("Sleeper held -> ${e.message}")
    }

}

/** Function takes a JSON Array and returns a (Array)List<PublicServiceData> **/
fun fromJSONArray(array: JSONArray): ArrayList<String> {
    val tmp = ArrayList<String>()
    for (i in 0 until array.length()) {
        tmp.add((array.getString(i)))
    }

    return tmp
}

/** Function takes a JSON Array and returns a (Array)List<PublicServiceData> **/
fun fromServiceCostJSONArray(array: JSONArray): ArrayList<ServiceCostModel> {
    val tmp = ArrayList<ServiceCostModel>()
    var serviceCostObject: JSONObject
    var spec: String
    var cost: Int
    var serviceCostModel: ServiceCostModel
    for (i in 0 until array.length()) {
        /*serviceCostModel = array.get(i) as ServiceCostModel
        tmp.add(serviceCostModel)*/

        try {
            serviceCostObject = array.getJSONObject(i)
            spec = serviceCostObject.getString("service_spec")
            cost = serviceCostObject.getInt("spec_cost")
            serviceCostModel = ServiceCostModel(spec, cost)
            tmp.add(serviceCostModel)
        } catch (jse: JSONException) {
            Timber.e("CONVERTING SERVICE COST JSE -> $jse")
        }
    }

    Timber.e("SERVICE COST from FUN -> $tmp")
    return tmp
}

fun fromServiceBenefitsJSONArray(array: JSONArray): ArrayList<ServiceBenefit> {
    val temp = ArrayList<ServiceBenefit>()
    var serviceBenefitObject: JSONObject
    var serviceBenefitTitle: String
    var serviceBenefitBody: String
    var serviceBenefit: ServiceBenefit

    for (i in 0 until array.length()) {
        try {
            serviceBenefitObject = array.getJSONObject(i)
            serviceBenefitTitle = serviceBenefitObject.getString("reason_title")
            serviceBenefitBody = serviceBenefitObject.getString("reason_body")
            serviceBenefit = ServiceBenefit(serviceBenefitTitle, serviceBenefitBody)
            temp.add(serviceBenefit)
        } catch (jse: JSONException) {
            Timber.e("SERVICE BENEFIT JSE -> $jse")
        }
    }
    Timber.e("SERVICE BENEFIT FROM FUN -> $temp")
    return temp
}