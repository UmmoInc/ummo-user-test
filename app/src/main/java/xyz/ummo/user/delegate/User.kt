package xyz.ummo.user.delegate

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager

import xyz.ummo.user.R.string.*

//import com.parse.Parse;

class User : Application() {

    override fun onCreate() {
        super.onCreate()
        FuelManager.instance.basePath = getString(serverUrl)

        val jwt:String = PreferenceManager.getDefaultSharedPreferences(this).getString("jwt","")

        if(jwt!=""){
            FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)
        }

        Log.e("App", "Applicaytion created")

        setUser()

        /*Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("ummo-delegate-dev-server")
                .clientKey("")
                .server("https://ummo-dev.herokuapp.com/parse")
                .build()
        );*/
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
