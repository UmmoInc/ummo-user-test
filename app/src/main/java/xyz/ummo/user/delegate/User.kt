package xyz.ummo.user.delegate

import android.app.Application
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.github.kittinunf.fuel.core.FuelManager
import com.github.nkzawa.emitter.Emitter
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import org.json.JSONArray
import org.json.JSONObject

import xyz.ummo.user.R.string.*
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import java.net.URISyntaxException

class User : Application() {

    //public var mSocket: Socket? = null
    private var detailedProductViewModel: DetailedProductViewModel? = null

    private fun initializeSocket(_id: String) {
        try {
            Log.e(TAG, "Trying connection...")
            SocketIO.mSocket = IO.socket("${getString(serverUrl)}/user-$_id")
            Log.e(TAG, "${getString(serverUrl)}/user-$_id")
            SocketIO.mSocket?.connect()
            SocketIO.anything = "Hello World"
            if (SocketIO.mSocket == null) {
                Log.e(TAG, "Probably not connected")
            } else {
                Log.e(TAG, "Probably connected")
            }
        } catch (e: URISyntaxException) {
            Log.e(TAG, e.toString())
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
                val intent = Intent(this, MainScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                Log.e(TAG, "Service-Created: IT->"+JSONObject(it[0].toString()))
                intent.putExtra("SERVICE_ID", JSONObject(it[0].toString()).getString("_id"))
                intent.putExtra("SERVICE_AGENT_ID", JSONObject(it[0].toString()).getString("agent"))
                intent.putExtra("DELEGATED_PRODUCT_ID", JSONObject(it[0].toString()).getString("product"))

                intent.putExtra("OPEN_DELEGATED_SERVICE_FRAG",1)
                Log.e(TAG, "service-created with ID->${JSONObject(it[0].toString())}")

                val serviceId: String = JSONObject(it[0].toString()).getString("_id")
                val delegatedProductId: String = JSONObject(it[0].toString()).getString("product")
                val serviceAgentId: String = JSONObject(it[0].toString()).getString("agent")
                val arr = JSONObject(it[0].toString()).getJSONArray("progress")
                intent.putExtra("progress",arr.toString());
//                val serviceName: String = JSONObject(it[0].toString()).getJSONArray("progress")



                startActivity(intent)
            })

            SocketIO.mSocket?.on("connect_error", Emitter.Listener {
                Log.e(TAG, "Socket Connect-ERROR-> ${it[0].toString() + SocketIO.mSocket?.io()}")
            })

            SocketIO.mSocket?.on("error", Emitter.Listener {
                Log.e(TAG, "Socket ERROR-> ${it[0].toString() + SocketIO.mSocket?.io()}")
            })

            /*  SocketIO.mSocket?.on("message", Emitter.Listener {
                  Log.e("Message",it[0].toString())
              })*/
        }

        Log.e(TAG, "Application created - Server URL->${getString(serverUrl)}")
    }
}

object SocketIO {
    var mSocket: Socket? = null
    var anything = ""
}
