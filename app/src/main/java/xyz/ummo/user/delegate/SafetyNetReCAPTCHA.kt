package xyz.ummo.user.delegate

import android.app.Activity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import timber.log.Timber

abstract class SafetyNetReCAPTCHA(var activity: Activity, reCAPTCHASiteKey: String) {
    init {
        Fuel.post("/security/reCAPTCHAVerification")
                .jsonBody(reCAPTCHASiteKey)
                .response { request, response, result ->

                    Thread {
                        done(response.data, response.statusCode)

                        if (response.statusCode == 200) {
                            Timber.e("Responding well| Data -> ${response.data}")
                        } else {
                            Timber.e("Status Code -> ${response.statusCode}")
                        }
                    }.start()
                }
    }

    abstract fun done(data: ByteArray, code: Number)
}