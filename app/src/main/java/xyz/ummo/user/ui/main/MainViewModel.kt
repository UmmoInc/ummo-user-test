package xyz.ummo.user.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import timber.log.Timber
import xyz.ummo.user.data.repo.AppRepository
import xyz.ummo.user.workers.SocketConnectWorker
import xyz.ummo.user.workers.sockethandlers.ServiceHandler

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository: AppRepository = AppRepository(application)

    private val socketConnectWorkManager = WorkManager.getInstance(application)
    private val serviceWorkManager = WorkManager.getInstance(application)
    private val userWorkManager = WorkManager.getInstance(application)
    private val agentWorkManager = WorkManager.getInstance(application)

    internal fun socketConnect() {
        socketConnectWorkManager.enqueue(OneTimeWorkRequest.from(SocketConnectWorker::class.java))
        Timber.e("Socket connecting from MainViewModel's Work Manager")
    }

    internal fun serviceHandler() {
        serviceWorkManager.enqueue(OneTimeWorkRequest.from(ServiceHandler::class.java))
        Timber.e("Service Handler connecting from MainViewModel's Work Manager")
    }

    internal fun userHandler() {
//        serviceWorkManager.enqueue(OneTimeWorkRequest.from(UserHandler::class.java))
        Timber.e("User Handler connecting from MainViewModel's Work Manager")
    }

    internal fun agentHandler() {
//        serviceWorkManager.enqueue(OneTimeWorkRequest.from(AgentHandler::class.java))
        Timber.e("Socket connecting from MainViewModel's Work Manager")
    }
}