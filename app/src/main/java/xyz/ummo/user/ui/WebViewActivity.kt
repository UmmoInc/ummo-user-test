package xyz.ummo.user.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.Toolbar
import kotlinx.android.synthetic.main.activity_web_view.*
import timber.log.Timber
import xyz.ummo.user.R

class WebViewActivity : AppCompatActivity() {

    private var webViewToolbar: androidx.appcompat.widget.Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        val link = intent.getStringExtra("LINK")

        webViewToolbar = findViewById(R.id.web_view_toolbar)

        setSupportActionBar(webViewToolbar)
        supportActionBar?.title = "Verify Service"
        supportActionBar?.subtitle = "Official Website"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        web_view.webViewClient = WebViewClient()

        web_view.settings.javaScriptEnabled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            web_view.settings.safeBrowsingEnabled = true
        }
        web_view.settings.setSupportZoom(true)
        web_view.settings.cacheMode = WebSettings.LOAD_DEFAULT

        web_view.loadUrl(link!!)
    }

    override fun onBackPressed() {

        if (web_view.canGoBack())
            web_view.goBack()
        else
            super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainScreen::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}