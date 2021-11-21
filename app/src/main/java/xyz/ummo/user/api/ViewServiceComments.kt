package xyz.ummo.user.api

import com.github.kittinunf.fuel.Fuel
import org.json.JSONException
import timber.log.Timber

abstract class ViewServiceComments(serviceId: String) {

    init {
        Fuel.get("product/comments?_id=$serviceId").response { request, response, result ->
            try {
//                (context as Activity).runOnUiThread {
                done(response.data, response.statusCode)

                if (response.statusCode == 200) {
                    Timber.e("Service Comments are -> ${String(response.data)}")
                } else {
                    Timber.e("Status Code -> ${String(response.data)}")
                }
//                }
            } catch (jse: JSONException) {
                Timber.e("JSON Exception -> $jse")
            }
        }
    }

    abstract fun done(data: ByteArray, code: Number)
}