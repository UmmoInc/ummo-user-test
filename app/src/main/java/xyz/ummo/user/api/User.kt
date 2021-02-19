package xyz.ummo.user.api

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Base64
import com.github.kittinunf.fuel.core.FuelManager
import com.github.nkzawa.socketio.client.IO
import com.github.nkzawa.socketio.client.Socket
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.onesignal.OneSignal
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.BuildConfig
import xyz.ummo.user.R.string.serverUrl
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel
import xyz.ummo.user.utilities.eventBusEvents.ServiceUpdateEvents
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import xyz.ummo.user.utilities.oneSignal.UmmoNotificationOpenedHandler
import java.net.URISyntaxException

class User : Application() {

    //public var mSocket: Socket? = null
    private var detailedProductViewModel: DetailedProductViewModel? = null
    val MIXPANEL_TOKEN = "d787d12259b1db03ada420ec6bb9e5af"
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private val mode = Activity.MODE_PRIVATE

    //    private val socketReceiver = SocketReceiver()
    private val socketState: Boolean = false
    private val socketStateEvent = SocketStateEvent()
    private val serviceUpdateEvents = ServiceUpdateEvents()

    private fun initializeSocketWithId(_id: String) {
        try {
            SocketIO.mSocket = IO.socket("${getString(serverUrl)}/user-$_id")
            Timber.e("${getString(serverUrl)}/user-$_id")
            SocketIO.mSocket?.connect()
            SocketIO.anything = "Hello World"
            if (SocketIO.mSocket == null) {
                Timber.e("Probably not connected")
            } else {
                Timber.e("Probably connected")
            }
        } catch (e: URISyntaxException) {
            Timber.e(e.toString())
        }
    }

    private fun initializeSocketWithOutId() {
        try {
            SocketIO.mSocket = IO.socket(getString(serverUrl))
            SocketIO.mSocket?.connect()
            SocketIO.anything = "Hello World"
            if (SocketIO.mSocket == null) {
                Timber.e("Probably not connected")
            } else {
                Timber.e("Probably connected")
            }
        } catch (e: URISyntaxException) {
            Timber.e(e.toString())
        }
    }

    companion object {
        fun getUserId(_jwt: String): String { //Remember, it takes a jwt string
            return JSONObject(String(Base64.decode(_jwt.split(".")[1], Base64.DEFAULT))).getString("_id")
        }
        const val ummoUserPreferences = "UMMO_USER_PREFERENCES"
        const val mode = Activity.MODE_PRIVATE
        const val SERVICE_STATE = "SERVICE_STATE"
        const val DELEGATION_STATE = "DELEGATION_STATE"
        const val PENDING = "PENDING"
        const val STARTED = "STARTED"
        const val DELAYED = "DELAYED"
        const val DONE = "DONE"
        const val DELIVERED = "DELIVERED"
        const val RATED = "RATED"
     }

    init {
        //Planting tree!
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        //Initializing OneSignal
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(UmmoNotificationOpenedHandler(this))
//                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
//                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()

//        OneSignal.handleNotificationOpen(applicationContext, )

        OneSignal.idsAvailable { userId: String?, registrationId: String? ->
            Timber.e("IDs Available: USER -> $userId; REG -> $registrationId")

            if (userId == null) {
                val sharedPreferences = getSharedPreferences(ummoUserPreferences, mode)
                val editor: SharedPreferences.Editor
                editor = sharedPreferences.edit()
                editor.putString("USER_PID", userId)
                editor.apply()
            }
        }
    }

    /*override fun notificationOpened(result: OSNotificationOpenResult?) {
        val actionType: OSNotificationAction.ActionType = result!!.action.type
        val data: JSONObject = result.notification.payload.additionalData

        if (data != null) {
            Timber.e("NOTIFICATION OPENED [DATA] -> $data")
        } else
            Timber.e("NOTIFICATION DATA IS NULL!")

        if (actionType == OSNotificationAction.ActionType.ActionTaken)
            Timber.e("BUTTOn PRESSED WITH ID -> ${result.action.actionID}")

        Timber.e("NOTIFICATION OPENED [actionType] -> $actionType")
    }*/

    override fun onCreate() {
        super.onCreate()

        val jwt: String = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("jwt", "").toString()

        FuelManager.instance.basePath = getString(serverUrl)
        val mixpanel = MixpanelAPI.getInstance(applicationContext, MIXPANEL_TOKEN)

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)


        if (jwt != "") {
            FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)
            initializeSocketWithId(getUserId(jwt))

            Timber.e("Application created - Server URL [1]->${getString(serverUrl)}")

            //SocketIO.mSocket?.connect()
            SocketIO.mSocket?.on("connect") {
                Timber.e("Connected")
                socketStateEvent.socketConnected = true
                EventBus.getDefault().post(socketStateEvent)
            }

            SocketIO.mSocket?.on("connect_error") {
                Timber.e("Socket Connect-ERROR-> ${it[0].toString() + SocketIO.mSocket?.io()}")
                socketStateEvent.socketConnected = false
                EventBus.getDefault().post(socketStateEvent)
            }

            SocketIO.mSocket?.on("message1") {
                Timber.e("it[0].toString()")
            }

            SocketIO.mSocket?.on("service-done") {
                Timber.e("SERVICE-DONE SOCKET")
            }

            SocketIO.mSocket?.on("updated-service") {
                try {
                    val sharedPreferences = (this.applicationContext).getSharedPreferences(ummoUserPreferences, mode)
                    val statusEditor = sharedPreferences!!.edit()

                    val doc = JSONObject(it[0].toString())
                    val status = doc.getString("status")
                    Timber.e("SERVICE IS UPDATING - DOC -> $doc!")

                    val intent: Intent = Intent(this, MainScreen::class.java)
                            .putExtra(UmmoNotificationOpenedHandler.OPEN_DELEGATION, 1)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    when (status) {
                        PENDING -> {
                            statusEditor.putInt(SERVICE_STATE, 0).apply()
                            Timber.e("WHEN PENDING ")

                            intent.putExtra(DELEGATION_STATE, PENDING)
                            startActivity(intent)
                        }
                        STARTED -> {
                            statusEditor.putInt(SERVICE_STATE, 1).apply()
                            Timber.e("WHEN STARTED ")
                            intent.putExtra(DELEGATION_STATE, STARTED)

                            startActivity(intent)

                        }
                        DELAYED -> {
                            statusEditor.putInt(SERVICE_STATE, -1).apply()
                            Timber.e("WHEN DELAYED")
                            intent.putExtra(DELEGATION_STATE, DELAYED)

                            startActivity(intent)

                        }
                        DONE -> {
                            statusEditor.putInt(SERVICE_STATE, 2).apply()
                            Timber.e("WHEN DONE ")
                            intent.putExtra(DELEGATION_STATE, DONE)
                            startActivity(intent)
                        }
                        DELIVERED -> {
                            statusEditor.putInt(SERVICE_STATE, 3).apply()
                            Timber.e("WHEN DELIVERED")
                            intent.putExtra(DELEGATION_STATE, DELIVERED)
                            startActivity(intent)

                        }
                        RATED -> {
                            statusEditor.remove(SERVICE_STATE).apply()
                            Timber.e("WHEN RATED")
                            intent.putExtra(DELEGATION_STATE, RATED)
                            startActivity(intent)
                        }
                    }
                } catch (jse: JSONException) {
                    Timber.e("JSE -> $jse")
                    throw jse
                }
            }

            SocketIO.mSocket?.on("service-created") {
                Timber.e("service-created!")
                val intent = Intent(this, MainScreen::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                intent.putExtra("SERVICE_ID", JSONObject(it[0].toString())
                        .getString("_id"))

                intent.putExtra("SERVICE_AGENT_ID", JSONObject(it[0].toString())
                        .getString("agent"))

                intent.putExtra("DELEGATED_PRODUCT_ID", JSONObject(it[0].toString())
                        .getString("product"))

                intent.putExtra("OPEN_DELEGATED_SERVICE_FRAG", 1)

                Timber.e("Service-created with ID->${JSONObject(it[0].toString())}")

                val serviceId: String = JSONObject(it[0].toString()).getString("_id")
                val delegatedProductId: String = JSONObject(it[0].toString()).getString("product")
                val serviceAgentId: String = JSONObject(it[0].toString()).getString("agent")
                val arr = JSONObject(it[0].toString()).getJSONArray("progress")
                intent.putExtra("progress", arr.toString());
                //                val serviceName: String = JSONObject(it[0].toString()).getJSONArray("progress")

                startActivity(intent)
            }

            SocketIO.mSocket?.on("error") {
                Timber.e("Socket ERROR-> ${it[0].toString() + SocketIO.mSocket?.io()}")
            }
        } else { //TODO: ONE
            Timber.e("Application created - Server URL[3]->${getString(serverUrl)}")

            initializeSocketWithOutId()

            SocketIO.mSocket?.on("connect") {
                Timber.e("Connected!")
                Timber.e("Application created - Server URL[4]->${getString(serverUrl)}")

//                sendSocketStateBroadcast(true)
                socketStateEvent.socketConnected = true
                EventBus.getDefault().post(socketStateEvent)
            }

            SocketIO.mSocket?.on("connect_error") {
                Timber.e("Socket Connect-ERROR-> ${it[0].toString() + SocketIO.mSocket?.io()}")
                Timber.e("Application created - Server URL[5]->${getString(serverUrl)}")
                //TODO: Display a warning
//                sendSocketStateBroadcast(false)
                socketStateEvent.socketConnected = false
                EventBus.getDefault().post(socketStateEvent)
            }
        }

        Timber.e("Application created - Server URL [2]->${getString(serverUrl)}")
    }
}

object SocketIO {
    var mSocket: Socket? = null
    var anything = ""
}
