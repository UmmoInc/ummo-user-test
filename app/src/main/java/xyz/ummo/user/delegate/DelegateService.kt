package xyz.ummo.user.delegate

import android.app.Activity
import android.content.Context
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

abstract class DelegateService(context: Context?, user: String, product: String) {
    init {
        val data = JSONObject();
        data.put("user_id", user)
        data.put("product_id", product)

        Timber.e("DATA -> $data")

        Fuel.post("${context!!.getString(R.string.serverUrl)}/api/dispatch")
                .jsonBody(data.toString())
                .response { request, response, result ->
                    (context as Activity).runOnUiThread {
                        done(response.data, response.statusCode)

                        Timber.e("Request Body->${request.body}")
                        Timber.e("Response Code->${response.statusCode}")
                        Timber.e("Result Body->${result}")
                    }
                }
    }

    abstract fun done(data: ByteArray, code: Int)
}