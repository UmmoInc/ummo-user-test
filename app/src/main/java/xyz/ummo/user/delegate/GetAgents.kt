package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.Fuel
import xyz.ummo.user.R

abstract class GetAgents(context:Context) {
    init {
        val jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "jwt")
        Fuel.get("${context.getString(R.string.serverUrl)}/agent")
                .header("Jwt" to jwt)
                .response { request, response, result ->
                    done(response.data,response.statusCode)
                }
    }

    abstract fun done(data:ByteArray,code:Number)
}