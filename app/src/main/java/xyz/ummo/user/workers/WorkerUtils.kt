@file:JvmName("WorkerUtils")

package xyz.ummo.user.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.utilities.*

/**
 * @author Rego
 * We're creating a notification for every Work done by a particular Worker
 * @param message Message shown on the notification
 * @param context Context needed for a Toast
 * **/

fun makeStatusNotification(message: String, context: Context) {

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
            .setContentTitle(NOTIFICATION_TITLE)
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