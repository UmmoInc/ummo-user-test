package xyz.ummo.user.ui.signup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import xyz.ummo.user.data.repo.AppRepository
import xyz.ummo.user.workers.SocketConnectWorker

class SignupViewModel(application: Application) : AndroidViewModel(application) {
    private val appRepository = AppRepository(application)

    private val socketConnectWorkManager = WorkManager.getInstance(application)

    internal fun socketConnect() {
        socketConnectWorkManager.enqueue(OneTimeWorkRequest.from(SocketConnectWorker::class.java))
    }
}