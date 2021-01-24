package xyz.ummo.user.ui.legal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import xyz.ummo.user.R

class TermsAndConditions : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        val webView = findViewById<WebView>(R.id.privacy_policy_web_view)
        webView.loadUrl("file:////android_asset/www/terms_and_conditions.html")
    }
}