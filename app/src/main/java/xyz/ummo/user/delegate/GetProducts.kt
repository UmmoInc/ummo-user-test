package xyz.ummo.user.delegate

import android.app.Activity
import com.github.kittinunf.fuel.Fuel

abstract class GetProducts(activity:Activity, public_service:String) {
    init {
        Fuel.get("/product?public_service=$public_service")
                .response { request, response, result ->
                    activity.runOnUiThread(Runnable {
                        done(response.data, response.statusCode)
                    })
                }
    }

    abstract fun done(data:ByteArray,code:Number)
}