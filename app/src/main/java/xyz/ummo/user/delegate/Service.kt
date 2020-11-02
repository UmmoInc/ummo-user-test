package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import timber.log.Timber

import xyz.ummo.user.R.string.*

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
