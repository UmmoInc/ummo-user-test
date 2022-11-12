package xyz.ummo.user.api

import android.app.Activity
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Base64
import androidx.multidex.MultiDexApplication
import com.github.kittinunf.fuel.core.FuelManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.onesignal.OneSignal
import io.socket.client.IO
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.BuildConfig
import xyz.ummo.user.R
import xyz.ummo.user.R.string.onesignal_app_id
import xyz.ummo.user.R.string.serverUrl
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import java.net.URISyntaxException

class User : MultiDexApplication() {

    private var fcmToken: String = ""

    private lateinit var mixpanelAPI: MixpanelAPI

    //public var mSocket: Socket? = null
    private var detailedProductViewModel: DetailedProductViewModel? = null
    private val MIXPANEL_TOKEN = "d787d12259b1db03ada420ec6bb9e5af"
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private val mode = Activity.MODE_PRIVATE

    //    private val socketReceiver = SocketReceiver()
    private val socketState: Boolean = false
    private val socketStateEvent = SocketStateEvent()

    private fun initializeSocketWithId(_id: String) {
        try {
            Timber.e("user _id: $_id")
            val options: IO.Options = IO.Options()
            options.query = "token=$_id"

            SocketIO.mSocket = IO.socket(getString(serverUrl), options)
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
            return JSONObject(
                String(
                    Base64.decode(
                        _jwt.split(".")[1],
                        Base64.DEFAULT
                    )
                )
            ).getString("_id")
        }
    }

    init {
        //Planting tree!
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onCreate() {
        super.onCreate()

        /** Init Mixpanel **/
        mixpanelAPI = MixpanelAPI.getInstance(
            applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        /** Auth JWT **/
        val jwt: String = PreferenceManager.getDefaultSharedPreferences(this)
            .getString("jwt", "").toString()

        /** Init MainViewModel **/
//        mainViewModel = ViewModelProvider().get(MainViewModel::class.java)

        FuelManager.instance.basePath = getString(serverUrl)

        //Initializing OneSignal
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        OneSignal.initWithContext(this)
        OneSignal.setAppId(getString(onesignal_app_id))

        /** The below method helps us retrieve the Firebase Cloud Messages token **/
        getFCMToken()
        /** The below method helps us handle a notification from OneSignal whenever
         * the User opens it or taps on an action from the notif.**/
        notificationOpenedHandler()

        if (jwt != "") {

            /** Identifying User in Mixpanel's Profile properties **/
            /*mixpanelAPI.people.identify(getUserId(jwt))
            mixpanelAPI.identify(getUserId(jwt))*/

            FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)
            initializeSocketWithId(getUserId(jwt))

            Timber.e("{JWT} No Socket Event - Server URL [1]->${getString(serverUrl)}")

            SocketIO.mSocket?.on("message1") {
                Timber.e("it[0].toString()")
            }

            SocketIO.mSocket?.on("service-done") {
                Timber.e("SERVICE-DONE SOCKET")
            }

            SocketIO.mSocket?.on("updated-service") {
                try {
                    val sharedPreferences = (this.applicationContext)
                        .getSharedPreferences(ummoUserPreferences, mode)

                    val statusEditor = sharedPreferences!!.edit()

                    val doc = JSONObject(it[0].toString())
                    val status = doc.getString("status")
                    Timber.e("SERVICE IS UPDATING - DOC -> $doc!")

                    //TODO: revive with FCM
                    val intent: Intent = Intent(this, MainScreen::class.java)
                    /*.putExtra(UmmoNotificationOpenedHandler.OPEN_DELEGATION, 1)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)*/

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

                intent.putExtra(
                    "SERVICE_ID", JSONObject(it[0].toString())
                        .getString("_id")
                )

                intent.putExtra(
                    "SERVICE_AGENT_ID", JSONObject(it[0].toString())
                        .getString("agent")
                )

                intent.putExtra(
                    "DELEGATED_PRODUCT_ID", JSONObject(it[0].toString())
                        .getString("product")
                )

                intent.putExtra("OPEN_DELEGATED_SERVICE_FRAG", 1)

                Timber.e("Service-created with ID->${JSONObject(it[0].toString())}")

                val serviceId: String = JSONObject(it[0].toString()).getString("_id")
                val delegatedProductId: String = JSONObject(it[0].toString()).getString("product")
                val serviceAgentId: String = JSONObject(it[0].toString()).getString("agent")
                val arr = JSONObject(it[0].toString()).getJSONArray("progress")
                intent.putExtra("progress", arr.toString())
                //                val serviceName: String = JSONObject(it[0].toString()).getJSONArray("progress")

                startActivity(intent)
            }

            SocketIO.mSocket?.on("error") {
                Timber.e("Socket ERROR-> ${it[0].toString() + SocketIO.mSocket?.io()}")
            }
        } else { //TODO: ONE
            Timber.e("{No JWT} - Socket About to Connect - Server URL[3]->${getString(serverUrl)}")

            initializeSocketWithOutId()

            SocketIO.mSocket?.on("connect") {
                Timber.e("Connected!")
                Timber.e("{No JWT} Socket Connected - Server URL[4]->${getString(serverUrl)}")

//                sendSocketStateBroadcast(true)
                socketStateEvent.socketConnected = true
                EventBus.getDefault().post(socketStateEvent)
            }

            SocketIO.mSocket?.on("connect_error") {
                Timber.e("{No JWT} Socket Connection Error - Server URL[5]->${getString(serverUrl)}")
                //TODO: Display a warning
//                sendSocketStateBroadcast(false)
                socketStateEvent.socketConnected = false
                EventBus.getDefault().post(socketStateEvent)
            }
        }
    }

    private fun getFCMToken() {

        /** Instantiating Firebase Instance **/
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e("Fetching FCM registration token failed -> ${task.exception}")
                return@OnCompleteListener
            }

            fcmToken = task.result
            Timber.e("FCM TOKEN -> $fcmToken")
        })
    }

    private fun notificationOpenedHandler() {
        /** Method runs upon opening the app after a notification is clicked **/
        OneSignal.setNotificationOpenedHandler { result ->
            val actionId = result.action.actionId
            val type: String = result.action.type.toString() // "ActionTaken" | "Opened"
            val title = result.notification.title
            val body = result.notification.body
            val data = result.notification.rawPayload
            val dataObject = JSONObject(data)
            var customObject = JSONObject()
            var a = JSONObject()
            var openURL = ""

            if (dataObject.has("custom")) {
                customObject = JSONObject(dataObject.getString("custom"))
            }

            if (customObject.has("a")) {
                a = customObject.getJSONObject("a")
            }

            if (customObject.has("OPEN_URL")) {
                openURL = customObject.getString("OPEN_URL")

            }
            val url = result.notification.launchURL

            val notificationJSONObject = JSONObject()

            /** Checking if URL is not empty, then launching an in-app browser window **/
            if (openURL.isNotEmpty()) {
                /** Open the link **/
                val intent = Intent(applicationContext, MainScreen::class.java)
                intent.putExtra(LAUNCH_URL, openURL)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                Timber.e("LAUNCH URL -> $openURL")

                notificationJSONObject.put(ACTION_ID, actionId)
                notificationJSONObject.put(ACTION_TAKEN, type)
                notificationJSONObject.put(LAUNCH_URL, openURL)
                notificationJSONObject.put(NOTIFICATION_BODY, body)
                mixpanelAPI.track("notificationAction", notificationJSONObject)

                startActivity(intent)
            } else
                return@setNotificationOpenedHandler

            Timber.e("OS NOTIFICATION ACTION-ID -> $actionId")
            Timber.e("OS NOTIFICATION TYPE -> $type")
            Timber.e("OS NOTIFICATION TITLE -> $title")
            Timber.e("OS NOTIFICATION BODY -> $body")
            Timber.e("OS NOTIFICATION DATA -> $dataObject")
            Timber.e("OS NOTIFICATION CUSTOM DATA -> $a")
            Timber.e("OS NOTIFICATION OPEN URL -> $openURL")
        }
    }
}

object SocketIO {
    var mSocket: Socket? = null
    var anything = ""
}