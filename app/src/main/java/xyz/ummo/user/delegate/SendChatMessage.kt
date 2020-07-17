package xyz.ummo.user.delegate

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject

abstract class SendChatMessage(message:String, serviceId:String) {
    init {
        val obj = JSONObject()
        obj.put("message",message)
        obj.put("service",serviceId)
        obj.put("from","user")
        Fuel.post("/chat/message")
                .jsonBody(obj.toString())
                .response { request, response, result ->
                    done(response.data,response.statusCode)
                }
    }
    abstract fun done(data:ByteArray,code:Number)
}