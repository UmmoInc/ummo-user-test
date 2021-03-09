package xyz.ummo.user.api

import android.content.Context
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.Fuel

abstract class Service(context: Context) {
    init {
        val jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "jwt")
        Fuel.get("/service?user=${User.getUserId(jwt!!)}&status=IN-PROGRESS")
                .response { request, response, result ->
                    //Timber.e("Services->%s",String(response.data))
                    done(response.data,response.statusCode)
                }
    }

    abstract fun done(data:ByteArray,code:Number)
}