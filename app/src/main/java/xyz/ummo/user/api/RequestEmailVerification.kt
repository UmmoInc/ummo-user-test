package xyz.ummo.user.api

import android.content.Context
import com.github.kittinunf.fuel.Fuel

abstract class RequestEmailVerification(context: Context, userId: String) {
    init {
        Fuel.get("/user/request_verification?_id=$userId")
            .response { request, response, result ->
                done(response.data, response.statusCode)
            }
    }

    abstract fun done(data: ByteArray, code: Number)
}