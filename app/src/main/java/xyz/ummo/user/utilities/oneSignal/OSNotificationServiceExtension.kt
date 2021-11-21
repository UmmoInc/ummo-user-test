package xyz.ummo.user.utilities.oneSignal

import android.content.Context
import com.onesignal.OSMutableNotification
import com.onesignal.OSNotification
import com.onesignal.OSNotificationReceivedEvent
import com.onesignal.OneSignal
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

class OSNotificationServiceExtension : OneSignal.OSRemoteNotificationReceivedHandler {
    override fun remoteNotificationReceived(
        context: Context?,
        notificationReceivedEvent: OSNotificationReceivedEvent?
    ) {
        val notification: OSNotification? = notificationReceivedEvent?.notification

        //Modifying the notification's accent color
        val mutableNotification: OSMutableNotification = notification!!.mutableCopy()
        mutableNotification.setExtender { builder ->
            builder.setColor(context!!.resources.getColor(R.color.colorPrimary))
        }

        val data: JSONObject = notification.additionalData
        Timber.e("OneSignalData received -> $data")

        notificationReceivedEvent.complete(mutableNotification)

        /**Method runs before displaying a notification while the app is in focus
         * Use this handler to read notif. & decide if the notification should show or not. **/
        OneSignal.setNotificationWillShowInForegroundHandler { handler ->

        }

        /** Method runs upon opening the app after a notification is clicked **/
        OneSignal.setNotificationOpenedHandler { result ->
            val actionId = result.action.actionId
            val type: String = result.action.type.toString() // "ActionTaken" | "Opened"
            val title = result.notification.title

            Timber.e("OS NOTIFICATION ACTION-ID -> $actionId")
            Timber.e("OS NOTIFICATION TYPE -> $type")
            Timber.e("OS NOTIFICATION TITLE -> $title")
        }

    }
}