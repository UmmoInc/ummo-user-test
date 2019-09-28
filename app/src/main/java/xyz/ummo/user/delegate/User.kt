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

    //public var mSocket: Socket? = null

    private fun initializeSocket(_id:String){
        try {
            Log.e("User","Trying connection")
            SocketIO.mSocket = IO.socket("${getString(serverUrl)}/user-$_id")
            Log.e("NMSP","${getString(serverUrl)}/user-$_id")
            SocketIO.mSocket?.connect()
            SocketIO.anything = "Hello World"
            if(SocketIO.mSocket==null){
                Log.e("AGeNT","Probably not connected");
            }else{
                Log.e("Agent","Probably connected")
            }
        } catch (e: URISyntaxException) {
            Log.e("User",e.toString())
        }
    }


        companion object{
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
                Log.e("Socket","Connected to ")
            })
            SocketIO.mSocket?.on("message1", Emitter.Listener {
                Log.e("Message","it[0].toString()")
            })
            SocketIO.mSocket?.on("service-created", Emitter.Listener {
                val intent = Intent(this, DelegationChat::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("service-created", it[0].toString())
                startActivity(intent)
            })

         /*   mSocket?.on("message", Emitter.Listener {
                Log.e("Socket","Got message")
            })*/

            SocketIO.mSocket?.on("connect_error", Emitter.Listener {
                Log.e("COERR",it[0].toString()+SocketIO.mSocket?.io().toString())
            })

            SocketIO.mSocket?.on("error", Emitter.Listener {
                Log.e("COERR",it[0].toString()+SocketIO.mSocket?.io().toString())
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

object SocketIO{
    var mSocket: Socket? = null
    var anything = ""

}
