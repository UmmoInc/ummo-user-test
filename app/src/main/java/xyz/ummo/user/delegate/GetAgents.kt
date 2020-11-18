package xyz.ummo.user.delegate

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.Fuel
import xyz.ummo.user.R

abstract class GetAgents(context: Context) {
    init {
        val jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "jwt")
        Fuel.get("${context.getString(R.string.serverUrl)}/agent")
                //.header("Jwt" to jwt) //TODO: replace .header with an equivalent
                .response { request, response, result ->
                    if(response.statusCode == 200){
                        ( context as Activity).runOnUiThread {
                            done(response.data,response.statusCode)
                        }
                    }
                }
    }

    abstract fun done(data:ByteArray,code:Number)
}