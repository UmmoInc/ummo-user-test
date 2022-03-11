package xyz.ummo.user.api

import android.app.Activity
import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

abstract class GeneralFeedback(var context: Context, feedback: String, userContact: String) {

    init {
        val feedbackObject = JSONObject().put("feedbackText", feedback)
                .put("userContact", userContact)

        Fuel.post("${context.getString(R.string.serverUrl)}/feedback")
                .jsonBody(feedbackObject.toString())
                .response { request, response, result ->
                    (context as Activity).runOnUiThread {

                        done(response.data, response.statusCode)

                        if (response.statusCode == 200) {
                            Timber.e("Responding well| Data -> ${response.data}")
                        } else {
                            Timber.e("Status Code -> ${response.statusCode}")
                        }

                    }

                }
    }

    abstract fun done(data: ByteArray, code: Number)
}