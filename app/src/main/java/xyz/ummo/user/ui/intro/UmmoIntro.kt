package xyz.ummo.user.ui.intro

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mixpanel.android.mpmetrics.MixpanelAPI
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.utilities.UMMO_INTRO_COMPLETE
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences

class UmmoIntro : AppCompatActivity() {

    private lateinit var continueButton: Button
    private lateinit var userDataPromiseText: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val mixpanel = MixpanelAPI.getInstance(
            this.applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        continueButton = findViewById(R.id.continue_button)
        userDataPromiseText = findViewById(R.id.user_agreement_text_view)

        sharedPreferences = getSharedPreferences(ummoUserPreferences, mode)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        val userAgreementText =
            "<div>Ummo takes your privacy very seriously. We will never share your data with Third Parties without consulting you first. Check out our <a href='https://sites.google.com/view/ummo-privacy-policy/home'> Privacy Policy</a> for more info.</div>"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userDataPromiseText.text = Html.fromHtml(userAgreementText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            userDataPromiseText.text = Html.fromHtml(userAgreementText)
        }

        continueButton.setOnClickListener {
            startActivity(Intent(this@UmmoIntro, AppIntro::class.java))
            editor.putBoolean(UMMO_INTRO_COMPLETE, true).apply()
            mixpanel?.track("UmmoIntro: Intro Complete")
        }
    }

    private fun getDisplayMetrics() {

        val wm = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        Timber.e("W -> $width AND H -> $height")

        /** Determining Device Screen Size **/
        when (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) {
            Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                Timber.e("SCREEN SIZE -> ${Configuration.SCREENLAYOUT_LAYOUTDIR_MASK}")
            }/*Toast.makeText(
                this,
                "Large screen",
                Toast.LENGTH_LONG
            ).show()*/
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> Toast.makeText(
                this,
                "Normal screen",
                Toast.LENGTH_LONG
            ).show()
            Configuration.SCREENLAYOUT_SIZE_SMALL -> Toast.makeText(
                this,
                "Small screen",
                Toast.LENGTH_LONG
            ).show()
            else -> Toast.makeText(
                this,
                "Screen size is neither large, normal or small",
                Toast.LENGTH_LONG
            ).show()
        }

        /** Determining Device Screen Density **/
        when (resources.displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> {
                Timber.e("LDPI")
            }//Toast.makeText(applicationContext, "LDPI", Toast.LENGTH_SHORT).show()
            DisplayMetrics.DENSITY_MEDIUM -> {
                Timber.e("MDPI")
            }//Toast.makeText(applicationContext, "MDPI", Toast.LENGTH_SHORT)
            //.show()
            DisplayMetrics.DENSITY_HIGH -> {
                Timber.e("HDPI")
            }//Toast.makeText(applicationContext, "HDPI", Toast.LENGTH_SHORT)
            //.show()
            DisplayMetrics.DENSITY_XHIGH -> {
                Timber.e("XHDPI")
            }//Toast.makeText(applicationContext, "XHDPI", Toast.LENGTH_SHORT)
            //.show()
        }
    }
}

enum class WindowSizeClass { COMPACT, MEDIUM, EXPANDED }