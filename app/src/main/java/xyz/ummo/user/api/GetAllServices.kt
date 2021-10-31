package xyz.ummo.user.api

import android.app.Activity
import com.github.kittinunf.fuel.Fuel
import timber.log.Timber

abstract class GetAllServices(activity: Activity) {
    init {
//        Fuel.get("/api/get_all_services")
        Fuel.get("/product")
            .response { request, response, result ->
                activity.runOnUiThread {
                    if (response.data.isNotEmpty()) {
                        done(response.data, response.statusCode)
                        Timber.e("SERVICES FOUND -> ${String(response.data)}")
                    } else
                        Timber.e("RESPONSE IS EMPTY!")
                }
            }
    }

    abstract fun done(data: ByteArray, code:Number)
}