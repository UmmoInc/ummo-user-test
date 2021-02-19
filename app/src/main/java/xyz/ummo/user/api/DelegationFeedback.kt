package xyz.ummo.user.api

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import timber.log.Timber

abstract class DelegationFeedback(serviceId: String, rating: Int, review: String) {

    init {
        val obj = JSONObject()
        obj.put("rating", rating).put("review", review).put("status", "RATED")
        Fuel.put("/service/${serviceId}")
                .jsonBody(obj.toString())
                .response { request, response, result ->

                    Timber.e("DELEGATION_FEEDBACK [DATA] -> ${response.data}")
                    Timber.e("DELEGATION_FEEDBACK [CODE] -> ${response.statusCode}")
                    done(response.data, response.statusCode)

                }
    }

    abstract fun done(data: ByteArray, code: Number)
}