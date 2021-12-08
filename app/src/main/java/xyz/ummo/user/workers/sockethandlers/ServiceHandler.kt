package xyz.ummo.user.workers.sockethandlers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.socket.emitter.Emitter
import org.json.JSONObject
import xyz.ummo.user.workers.SocketConnectWorker

class ServiceHandler(serviceJSON: JSONObject, context: Context, params: WorkerParameters) :
    Worker(context, params) {

    val socket = SocketConnectWorker.SocketIO.mSocket

    var serviceScheduleCallback = Emitter.Listener {
        socket!!.emit("service/schedule", serviceJSON)
    }

    var serviceReadyCallback = Emitter.Listener {
        socket!!.emit("service/ready", serviceJSON)
    }

    override fun doWork(): Result {

        return try {
            if (SocketConnectWorker.SocketIO.mSocket != null) {
                SocketConnectWorker.SocketIO.mSocket?.emit(
                    "service/schedule",
                    serviceScheduleCallback
                )

                SocketConnectWorker.SocketIO.mSocket?.emit(
                    "service/update",
                    serviceReadyCallback
                )
            }

            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }
}