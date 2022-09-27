package xyz.ummo.user.api

import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

abstract class ServiceComment(context: Context, serviceComment: JSONObject) {

    init {
        Timber.e("SERVICE-COMMENT -> $serviceComment")
//        Fuel.put("${context.getString(R.string.serverUrl)}/api/service_comment/")
        Fuel.put("${context.getString(R.string.serverUrl)}/service/service_comment/")
            .jsonBody(serviceComment.toString())
            .response { request, response, result ->
                done(response.data, response.statusCode)
            }
    }
    abstract fun done(data: ByteArray, code: Number)
}