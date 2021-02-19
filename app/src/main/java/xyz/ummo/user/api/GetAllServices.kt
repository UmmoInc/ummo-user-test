package xyz.ummo.user.api

import android.app.Activity
import com.github.kittinunf.fuel.Fuel

//TODO: Deletable
abstract class GetAllServices(activity: Activity) {
    init {
        Fuel.get("/api/get_all_services")
                .response { request, response, result ->
                    activity.runOnUiThread {
                        done(response.data, response.statusCode)
                    }
                }
    }

    abstract fun done(data: ByteArray, code:Number)
}