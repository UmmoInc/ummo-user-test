package xyz.ummo.user.ui.intro

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import xyz.ummo.user.R
import xyz.ummo.user.ui.signup.RegisterActivity
import xyz.ummo.user.utilities.CONTINUED
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences

class Intro : AppCompatActivity() {

    private lateinit var continueButton: Button
    private lateinit var userDataPromiseText: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        continueButton = findViewById(R.id.continue_button)
        userDataPromiseText = findViewById(R.id.user_agreement_text_view)

        sharedPreferences = getSharedPreferences(ummoUserPreferences, mode)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        val userAgreementText =
            "<div>Ummo takes your privacy very seriously. We will never share your data with government without consulting you first. Check out our <a href='https://sites.google.com/view/ummo-privacy-policy/home'> Privacy Policy</a> for more info.</div>"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            userDataPromiseText.text = Html.fromHtml(userAgreementText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            userDataPromiseText.text = Html.fromHtml(userAgreementText)
        }

        continueButton.setOnClickListener {
            startActivity(Intent(this@Intro, RegisterActivity::class.java))

            editor.putBoolean(CONTINUED, true).apply()
        }
    }
}