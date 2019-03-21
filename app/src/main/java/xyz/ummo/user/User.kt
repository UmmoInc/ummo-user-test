package xyz.ummo.user

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log

//import com.parse.Parse;

class User : Application() {

    override fun onCreate() {
        super.onCreate()



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
