package xyz.ummo.user.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import timber.log.Timber
import xyz.ummo.user.data.repo.AppRepository
import xyz.ummo.user.workers.SocketConnectWorker

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository: AppRepository = AppRepository(application)

    private val socketConnectWorkManager = WorkManager.getInstance(application)

    internal fun socketConnect() {
        socketConnectWorkManager.enqueue(OneTimeWorkRequest.from(SocketConnectWorker::class.java))
        Timber.e("Socket connecting from MainViewModel's Work Manager")
    }
}