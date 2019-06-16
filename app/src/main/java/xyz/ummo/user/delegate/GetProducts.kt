package xyz.ummo.user.delegate

import com.github.kittinunf.fuel.Fuel

abstract class GetProducts {
    init {
        Fuel.get("/product")
                .response { request, response, result ->
                    done(response.data, response.statusCode)
                }
    }

    abstract fun done(data:ByteArray,code:Number)
}