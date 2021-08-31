package xyz.ummo.user.utilities.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class UmmoFirebaseService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.e("Refreshed Token -> $token")
        //TODO: sendRegistrationToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.e("Message -> $message")
    }
}