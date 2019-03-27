package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.Fuel

abstract class PublicService() {
    init {
        Fuel.get("/public-service")
                .response { request, response, result ->
                    done(response.data,response.statusCode)
                }
    }

    abstract fun done(data:ByteArray,code:Number)
}