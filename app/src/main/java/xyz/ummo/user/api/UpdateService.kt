package xyz.ummo.user.api

import android.app.Activity
import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

abstract class UpdateService(context: Context, serviceUpdate: JSONObject) {
    init {
        Timber.e("SERVICE-UPDATE -> $serviceUpdate")
        Fuel.put("${context.getString(R.string.serverUrl)}/api/update_service")
//        Fuel.put("${context.getString(R.string.serverUrl)}/product/${serviceUpdate.getString("_id")}/")
                .jsonBody(serviceUpdate.toString())
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