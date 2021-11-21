package xyz.ummo.user.workers.sockethandlers

import android.content.SharedPreferences
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.utilities.eventBusEvents.UserVerificationEvent

class UserHandler(socket: Socket) {

    val name = "user"
    private var dataObject = JSONObject()
    private var userObject = JSONObject()
    private var userConfirmed: Boolean? = null
    private var userVerificationEvent = UserVerificationEvent()
    private lateinit var sharedPreferences: SharedPreferences

    init {
        socket.on("$name/notify") {
            this.notify(it.toString())
            Timber.e("USER MESSAGE -> ${it[0]}")

            try {
                dataObject = it[0] as JSONObject
                userObject = dataObject.getJSONObject("data")
                Timber.e("USER HANDLER [CONFIRMED] -> ${userObject.getBoolean("confirmed")}")

                userConfirmed = userObject.getBoolean("confirmed")
                userVerificationEvent.userVerified = userConfirmed

//                EventBus.getDefault().register(this)
                EventBus.getDefault().post(userVerificationEvent)
                Timber.e("EMITTING EVENT -> ${userVerificationEvent.userVerified}")


            } catch (jse: JSONException) {
                Timber.e("USER HANDLER JSE -> $jse")
            }
        }

        socket.on("/user/verified") {
            this.notify(it.toString())
            Timber.e("EMAIL VERIFIER -> ${it[0]}")
        }

    }

    private fun notify(message: String) {
        Timber.e("USER MESSAGE -> $message")
    }

    @Subscribe
    fun onVerifiedStateEvent(userVerificationEvent: UserVerificationEvent) {
        userVerificationEvent.userVerified = userConfirmed
        EventBus.getDefault().post(userVerificationEvent)
    }

}