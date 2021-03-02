package xyz.ummo.user.api

import android.app.Activity
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class SafetyNetReCAPTCHA(var activity: Activity, reCAPTCHASiteKey: String) {
    init {

        Fuel.get("/security/captcha_verification", listOf("user_token" to reCAPTCHASiteKey))
                .responseString { request, response, result ->
                    Timber.e("REQUEST -> $request")
                    Timber.e("RESULT -> $result")
                    Timber.e("RESPONSE -> $response")

                    GlobalScope.launch {
                        done(response.data, response.statusCode)

                        result.fold({data ->
                            if (data.contains("PASSED"))
                                Timber.e("CAPTCHA PASSED!")
                            else
                                Timber.e("CAPTCHA FAILED!")
                        }, { error ->
                            Timber.e("ERROR CONNECTING TO SERVER")
                        })

                    }
                }

    }

    abstract fun done(data: ByteArray, code: Number)
}