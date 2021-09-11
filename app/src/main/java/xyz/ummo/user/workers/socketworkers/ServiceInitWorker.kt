package xyz.ummo.user.workers.socketworkers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import xyz.ummo.user.workers.SocketConnectWorker

class ServiceInitWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        val socket = SocketConnectWorker.SocketIO.mSocket

        return if (socket!!.isActive) {
            Result.success()
        } else
            Result.failure()
    }
}