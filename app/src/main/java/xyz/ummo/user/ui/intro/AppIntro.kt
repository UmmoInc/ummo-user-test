package xyz.ummo.user.ui.intro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType
import com.mixpanel.android.mpmetrics.MixpanelAPI
import xyz.ummo.user.R
import xyz.ummo.user.ui.signup.RegisterActivity
import xyz.ummo.user.utilities.APP_INTRO_COMPLETE
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences

class AppIntro : AppIntro() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var mixpanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mixpanel = MixpanelAPI.getInstance(
            this.applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        sharedPreferences = getSharedPreferences(ummoUserPreferences, mode)
        editor = sharedPreferences.edit()

        addSlide(
            AppIntroFragment.createInstance(
                title = "Find your service",
                description = "By either searching for it or choosing a service-filter.",
                imageDrawable = R.drawable.find_your_service,
                backgroundColorRes = R.color.screenWhite,
                titleColorRes = R.color.ummo_2,
                descriptionColorRes = R.color.ummo_3,
                titleTypefaceFontRes = R.font.rubik
            )
        )
        addSlide(
            AppIntroFragment.createInstance(
                title = "Help improve your service",
                description = "By leaving a comment, give a thumbs-up or thumbs-down.",
                imageDrawable = R.drawable.engage_your_service,
                backgroundColorRes = R.color.screenWhite,
                titleColorRes = R.color.ummo_2,
                descriptionColorRes = R.color.ummo_3,
                titleTypefaceFontRes = R.font.rubik
            )
        )
        addSlide(
            AppIntroFragment.createInstance(
                title = "Out of time?",
                description = "Request an Agent for the services you don't have time for.",
                imageDrawable = R.drawable.request_an_agent,
                backgroundColorRes = R.color.screenWhite,
                titleColorRes = R.color.ummo_2,
                descriptionColorRes = R.color.ummo_3,
                titleTypefaceFontRes = R.font.rubik
            )
        )
        addSlide(
            AppIntroFragment.createInstance(
                title = "Sharing is caring",
                description = "Send a service to your friends and family and help them save time.",
                imageDrawable = R.drawable.share_your_service,
                backgroundColorRes = R.color.screenWhite,
                titleColorRes = R.color.ummo_2,
                descriptionColorRes = R.color.ummo_3,
                titleTypefaceFontRes = R.font.rubik
            )
        )
        addSlide(
            AppIntroFragment.createInstance(
                title = "Found something wrong?",
                description = "Leave your feedback to help improve Ummo & we will attend to it ASAP.",
                imageDrawable = R.drawable.share_your_feedback,
                backgroundColorRes = R.color.screenWhite,
                titleColorRes = R.color.ummo_2,
                descriptionColorRes = R.color.ummo_3,
                titleTypefaceFontRes = R.font.rubik
            )
        )

        setTransformer(AppIntroPageTransformerType.Depth)
        isColorTransitionsEnabled = true
        setIndicatorColor(
            selectedIndicatorColor = R.color.ummo_1,
            unselectedIndicatorColor = R.color.White
        )

        isVibrate = true
        vibrateDuration = 50L
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        mixpanel.track("AppIntro: App Tour Skipped")
        startActivity(Intent(this, RegisterActivity::class.java))
        editor.putBoolean(APP_INTRO_COMPLETE, true)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        startActivity(Intent(this, RegisterActivity::class.java))
        editor.putBoolean(APP_INTRO_COMPLETE, true)
        mixpanel.track("AppIntro: App Tour Done")

    }
}