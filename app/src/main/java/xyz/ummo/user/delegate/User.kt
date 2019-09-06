package xyz.ummo.user.delegate

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.onesignal.OneSignal

import xyz.ummo.user.R.string.*

class User : Application() {

    override fun onCreate() {
        super.onCreate()
        FuelManager.instance.basePath = getString(serverUrl)

        val jwt:String = PreferenceManager.getDefaultSharedPreferences(this).getString("jwt","")

        if(jwt!=""){
            FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)
        }

        Log.e("App", "Application created - Server URL->${getString(serverUrl)}")

        setUser()

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    fun setUser() {
        user = this
    }

    companion object {
        var user: User? = null
            private set
    }
}
