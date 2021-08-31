package xyz.ummo.user.workers

import android.content.Context
import android.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.socket.client.IO
import io.socket.client.Socket
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.User.Companion.getUserId
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import java.net.URISyntaxException

class SocketConnectWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val socketStateEvent = SocketStateEvent()
    val jwt: String = PreferenceManager.getDefaultSharedPreferences(context)
        .getString("jwt", "").toString()

    object SocketIO {
        var mSocket: Socket? = null
    }

    init {
        val options = IO.Options()
        xyz.ummo.user.api.SocketIO.mSocket = IO
            .socket(context.getString(R.string.serverUrl), options)

        SocketIO.mSocket?.connect()

    }

    override fun doWork(): Result {
        val appContext = applicationContext

        return try {

            /** This method will only execute when the User has signed up && has a JWT assigned **/
            if (jwt.isNotEmpty()) {

                /** Getting the User ID from the JWT **/
                val userId = getUserId(jwt)
                initializeSocketWithId(userId)

                if (SocketIO.mSocket != null) {
                    Timber.e("DOING WORK${SocketIO.mSocket}")

                    /** Checking if our Socket event is on "connect" **/
                    SocketIO.mSocket?.on("connect") {
                        makeStatusNotification("We're LIVE!", appContext)
                        socketStateEvent.socketConnected = true
                        EventBus.getDefault().post(socketStateEvent)
                    }

                    /** Checking if our Socket instance has an error connecting **/
                    SocketIO.mSocket?.on("connect_error") {
                        socketStateEvent.socketConnected = false
                        makeStatusNotification("MAYDAY!", appContext)
                        EventBus.getDefault().post(socketStateEvent)
                    }
                } else {
                    makeStatusNotification("Failed to connect", appContext)
                    Timber.e("NOT DOING WORK")
                }
            }
            Result.success()

        } catch (throwable: Throwable) {
            Timber.e("Error connecting Worker -> $throwable")
            Result.failure()
        }
    }

    fun initializeSocketWithId(userId: String) {
        try {
            val options: IO.Options = IO.Options()
            options.query = "token=$userId"

            SocketIO.mSocket = IO.socket(applicationContext.getString(R.string.serverUrl), options)
            SocketIO.mSocket?.connect()

            if (SocketIO.mSocket == null) {
                Timber.e("Probably NOT connected")
            } else {
                Timber.e("Probably connected")
            }
        } catch (e: URISyntaxException) {
            Timber.e(e.toString())
        }
    }
}