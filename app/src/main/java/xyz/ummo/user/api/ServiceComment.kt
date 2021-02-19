package xyz.ummo.user.api

import android.app.Activity
import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

abstract class ServiceComment(context: Context, serviceComment: JSONObject) {
    init {
        Timber.e("SERVICE-COMMENT -> $serviceComment")
        Fuel.put("${context.getString(R.string.serverUrl)}/api/service_comment/")
//        Fuel.put("${context.getString(R.string.serverUrl)}/product/${serviceUpdate.getString("_id")}/")
                .jsonBody(serviceComment.toString())
                .response { request, response, result ->
                    (context as Activity).runOnUiThread {
                        done(response.data, response.statusCode)

                        if (response.statusCode == 200) {
                            Timber.e("Responding well | Data -> ${response.data}")
                        } else {
                            Timber.e("Status Code -> ${String(response.data)}")
                        }
                    }
                }
    }

    abstract fun done(data: ByteArray, code: Number)

}