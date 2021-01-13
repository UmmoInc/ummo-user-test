package xyz.ummo.user.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.gms.safetynet.SafetyNetApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.mixpanel.android.mpmetrics.MixpanelAPI
import timber.log.Timber
import xyz.ummo.user.BuildConfig
import xyz.ummo.user.R
import xyz.ummo.user.ui.signup.RegisterActivity
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.security.SecureRandom
import java.util.*

class Splash : Activity() {
    private val splashPrefs = "UMMO_USER_PREFERENCES"
    private val mode = MODE_PRIVATE
    private val mRandom: Random = SecureRandom()
    private var mResult: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        sendSafetyNetRequest()

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

        /*val mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(SafetyNet.API)
                .addConnectionCallbacks(this)*/

        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
                == ConnectionResult.SUCCESS) {
            Timber.e("SafetyNet Attestation API is available")
        } else {
            Timber.e("WE NEED TO UPDATE Google Play services")
        }
    }

    /**
     * Generates a 16-byte nonce with additional data.
     * The nonce should also include additional information, such as a user id or any other details
     * you wish to bind to this attestation. Here you can provide a String that is included in the
     * nonce after 24 random bytes. During verification, extract this data again and check it
     * against the request that was made with this nonce.
     */
    private fun getRequestNonce(data: String): ByteArray? {
        val byteStream = ByteArrayOutputStream()
        val bytes = ByteArray(24)
        mRandom.nextBytes(bytes)
        try {
            byteStream.write(bytes)
            byteStream.write(data.toByteArray())
        } catch (e: IOException) {
            return null
        }
        return byteStream.toByteArray()
    }

    private fun sendSafetyNetRequest() {
        Timber.e("Sending SafetyNet API request")

        //TODO: Use a dynamic nonce generator to include unique data
        val nonceData = "Safety Net: " + System.currentTimeMillis()
        val nonce = getRequestNonce(nonceData)

        /** Call the SafetyNet API asynchronously.
         * The result is returned through the success/failure listeners.
         * First, get a SafetyNetClient for the foreground Activity.
         * Next, make the call to the attestation API.**/
        val client = SafetyNet.getClient(this)
        val task = client.attest(nonce!!, R.string.safety_net_api_key.toString())

        task.addOnSuccessListener (this, mSuccessListener)
                .addOnFailureListener(this, mFailureListener)
    }

    /**
     * Called after successfully communicating with the SafetyNet API.
     * The #onSuccess callback receives an
     * [com.google.android.gms.safetynet.SafetyNetApi.AttestationResponse] that contains a
     * JwsResult with the attestation result.
     */
    private val mSuccessListener = OnSuccessListener<SafetyNetApi.AttestationResponse> { attestationResponse ->
        /** Successfully communicated with the SafetyNet API.
         * Use result.getJwsResult() to get the signed result data.**/

        mResult = attestationResponse.jwsResult
        Timber.e("Success! SafetyNet Result -> \n$mResult\n")

        //TODO: forward this result to Ummo server together with the nonce
    }

    /** Called when an error occurred when communicating with SafetyNet API */
    private val mFailureListener = OnFailureListener { e ->
        mResult = null

        if (e is ApiException) {
            //An error with the Google Play services contains some additional details
            val apiException = e

            Timber.e("Attestation Error: -> ${CommonStatusCodes.getStatusCodeString(apiException.statusCode)} : ${apiException.statusMessage}")
        }
    }

    override fun onDestroy() {
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))
        mixpanel.flush()
        super.onDestroy()
    }

}