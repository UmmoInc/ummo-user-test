package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.Fuel

import xyz.ummo.user.R.string.*

abstract class Service(context: Context) {
    init {
        val jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "jwt")
        Fuel.get("${context.getString(serverUrl)}/service")
                .header("Jwt" to jwt)
                .response { request, response, result ->
                    done(response.data,response.statusCode)
                }
    }

    abstract fun done(data:ByteArray,code:Number)
}
