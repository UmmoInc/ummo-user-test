package xyz.ummo.user.delegate

import android.app.Activity
import android.content.Context
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import xyz.ummo.user.R
//TODO: Deletable
abstract class ConfirmService(context: Context, service_id:String) {
    init {
        val data = JSONObject();
        data.put("status","DONE")
        Fuel.put("${context.getString(R.string.serverUrl)}/service/${service_id}")
                .jsonBody(data.toString())
                .response { request, response, result ->
                    (context as Activity).runOnUiThread {
                        done(response.data,response.statusCode)
                    }
                }
}                                                           
    abstract fun done(data:ByteArray,code:Int)
}