package xyz.ummo.user.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.mixpanel.android.mpmetrics.MixpanelAPI
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.ui.signup.RegisterActivity

class Splash : Activity() {
    private val splashPrefs = "UMMO_USER_PREFERENCES"
    private val mode = MODE_PRIVATE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val context = this.applicationContext
        val mixpanel = MixpanelAPI.getInstance(context,
                resources.getString(R.string.mixpanelToken))
        mixpanel?.track("appLaunched")
        Thread {
            try {
                Thread.sleep(1000)
            } catch (ie: InterruptedException) {
                Timber.e(" onCreate-> $ie")
            }
            finish()

            val splashPreferences = getSharedPreferences(splashPrefs, mode)
            /*val signedUp = splashPreferences.getBoolean("SIGNED_UP", false)
            if (signedUp) {
                Timber.e("onCreate - User has already signed up")
                startActivity(Intent(this@Splash, MainScreen::class.java))
            } else {
                Timber.e("onCreate - User has not signed up yet!")
                startActivity(Intent(this@Splash, RegisterActivity::class.java))
            }*/

            val firstTimeLaunch = splashPreferences.getBoolean("IsFirstTimeLaunch", true)
            if (!firstTimeLaunch) {
                Timber.e("onCreate - User has already signed up")
                startActivity(Intent(this@Splash, MainScreen::class.java))
            } else {
                Timber.e("onCreate - User has not signed up yet!")
                startActivity(Intent(this@Splash, RegisterActivity::class.java))
            }
            finish()
        }.start()
    }

    override fun onDestroy() {
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))
        mixpanel.flush()
        super.onDestroy()
    }

}