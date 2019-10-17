package xyz.ummo.user.delegate

import android.app.Application
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModelProviders
import com.github.kittinunf.fuel.core.FuelManager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONObject

import xyz.ummo.user.R.string.*
import xyz.ummo.user.ui.detailedService.DetailedProduct
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import java.net.URISyntaxException


class User : Application() {

    //public var mSocket: Socket? = null
    private var detailedProductViewModel: DetailedProductViewModel? = null


    private fun initializeSocket(_id: String) {
        try {
            Log.e("User", "Trying connection")
            SocketIO.mSocket = IO.socket("${getString(serverUrl)}/user-$_id")
            Log.e("NMSP", "${getString(serverUrl)}/user-$_id")
            SocketIO.mSocket?.connect()
            SocketIO.anything = "Hello World"
            if (SocketIO.mSocket == null) {
                Log.e("User.kt", "Probably not connected")
            } else {
                Log.e("User.kt", "Probably connected")
            }
        } catch (e: URISyntaxException) {
            Log.e("User", e.toString())
        }
    }


    companion object {
        fun getUserId(_jwt: String): String { //Remember, it takes a jwt string
            return JSONObject(String(Base64.decode(_jwt.split(".")[1], Base64.DEFAULT))).getString("_id")
        }
    }


    override fun onCreate() {
        super.onCreate()
        FuelManager.instance.basePath = getString(serverUrl)

        val jwt: String = PreferenceManager.getDefaultSharedPreferences(this).getString("jwt", "")

        if (jwt != "") {
            FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)
            initializeSocket(getUserId(jwt))
            //SocketIO.mSocket?.connect()
            SocketIO.mSocket?.on("connect", Emitter.Listener {
                Log.e("Socket", "Connected to ")
            })
            SocketIO.mSocket?.on("message1", Emitter.Listener {
                Log.e("Message", "it[0].toString()")
            })
            SocketIO.mSocket?.on("service-created", Emitter.Listener {
                val intent = Intent(this, DetailedProduct::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                Log.e(TAG, "Service-Created: IT->"+JSONObject(it[0].toString()))
                intent.putExtra("SERVICE_ID", JSONObject(it[0].toString()).getString("_id"))
                Log.e(TAG, "service-created with ID->${JSONObject(it[0].toString()).getString("_id")}")

                startActivity(intent)
            })

            SocketIO.mSocket?.on("connect_error", Emitter.Listener {
                Log.e("COERR", it[0].toString() + SocketIO.mSocket?.io().toString())
            })

            SocketIO.mSocket?.on("error", Emitter.Listener {
                Log.e("COERR", it[0].toString() + SocketIO.mSocket?.io().toString())
            })

            /*  SocketIO.mSocket?.on("message", Emitter.Listener {
                  Log.e("Message",it[0].toString())
              })*/
        }

        Log.e("App", "Application created - Server URL->${getString(serverUrl)}")

        // OneSignal Initialization
        /* OneSignal.startInit(this)
                 .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                 .unsubscribeWhenNotificationsAreDisabled(true)
                 .init()*/
    }
}

object SocketIO {
    var mSocket: Socket? = null
    var anything = ""

}
