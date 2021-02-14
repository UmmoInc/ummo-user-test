package xyz.ummo.user.utilities.oneSignal

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import com.onesignal.OSNotificationAction.ActionType
import com.onesignal.OSNotificationOpenResult
import com.onesignal.OneSignal.NotificationOpenedHandler
import timber.log.Timber
import xyz.ummo.user.delegate.User.Companion.SERVICE_STATE
import xyz.ummo.user.delegate.User.Companion.mode
import xyz.ummo.user.delegate.User.Companion.ummoUserPreferences
import xyz.ummo.user.ui.MainScreen

class UmmoNotificationOpenedHandler(private val application: Application) : NotificationOpenedHandler {


    override fun notificationOpened(result: OSNotificationOpenResult) {
        val editor: SharedPreferences.Editor
        val sharedPreferences = (application).getSharedPreferences(ummoUserPreferences, mode)
        editor = sharedPreferences!!.edit()
        // Get custom data from notification
        val data = result.notification.payload.additionalData
        if (data != null) {
            val delegationStatus = data.optString("STATUS", null)
            Timber.e("CUSTOM DATA -> $delegationStatus")

            when (delegationStatus) {
                "PENDING" -> {
//                    editor.putInt(SERVICE_STATE, 0).apply()
                }
                "STARTED" -> {
//                    editor.putInt(SERVICE_STATE, 1).apply()
                }
                "DELAYED" -> {
//                    editor.putInt(SERVICE_STATE, -1).apply()

                }
                "DONE" -> {
//                    editor.putInt(SERVICE_STATE, 2).apply()

                }
                "DELIVERED" -> {
//                    editor.putInt(SERVICE_STATE, 3).apply()

                }
            }
        }

        // React to button pressed
        val actionType = result.action.type
        if (actionType == ActionType.ActionTaken)
            Timber.e("Button pressed with id: %s", result.action.actionID)

        // Launch new activity using Application object
        startApp()
    }

    private fun startApp() {
        val intent: Intent = Intent(application, MainScreen::class.java)
                .putExtra(OPEN_DELEGATION, 1)
                .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }

    companion object {
        const val OPEN_DELEGATION = "OPEN_DELEGATION"
    }
}