package xyz.ummo.user.delegate

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import org.json.JSONObject
import xyz.ummo.user.R

abstract class DelegateService(context: Context, user:String, product:String) {
    init {
        val data = JSONObject();
        data.put("user_id",user)
        data.put("product_id",product)

        Log.e("Delegate.kt",data.toString())

        Fuel.post("${context.getString(R.string.serverUrl)}/api/dispatch")
                .jsonBody(data.toString())
                .response { request, response, result ->
                    (context as Activity).runOnUiThread {
                        done(response.data,response.statusCode)
                        Log.e("Delegate.kt", "Response Code->${response.statusCode}")
                    }
                }
    }
    abstract fun done(data:ByteArray,code:Int)
}