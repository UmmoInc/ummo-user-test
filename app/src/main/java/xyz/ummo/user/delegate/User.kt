package xyz.ummo.user.delegate

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.preference.PreferenceManager
import android.support.v4.media.MediaBrowserCompat
import android.util.Base64
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject
import xyz.ummo.user.DelegationChat
import com.onesignal.OneSignal

import xyz.ummo.user.R.string.*
import java.net.URISyntaxException

class User : Application() {

    private var mSocket: Socket? = null

    private fun initalizeSocket(_id: String) {
        try {
            Log.e("User", "Trying connection")
            mSocket = IO.socket("${getString(serverUrl)}/user-$_id")
        } catch (e: URISyntaxException) {
            Log.e("User", e.toString())
        }
    }



        fun getUserId(_jwt: String): String { //Remember, it takes a jwt string
            return JSONObject(String(Base64.decode(_jwt.split(".")[1], Base64.DEFAULT))).getString("_id")
        }



    override fun onCreate() {
        super.onCreate()
        FuelManager.instance.basePath = getString(serverUrl)

        val jwt: String = PreferenceManager.getDefaultSharedPreferences(this).getString("jwt", "")

        if (jwt != "") {
            FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)
            initalizeSocket(getUserId(jwt))
            mSocket?.connect()
            mSocket?.on("service-created", Emitter.Listener {
                val intent = Intent(this, DelegationChat::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("service-created", it[0].toString())
                startActivity(intent)
            })

        }

        Log.e("App", "Application created - Server URL->${getString(serverUrl)}")

        setUser()

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
    }

    fun setUser() {
        user = this
    }

    companion object {
        var user: User? = null
            private set
    }
}
