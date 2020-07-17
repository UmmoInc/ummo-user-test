package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject

abstract class UpdateUserLocation(context:Context, lat:Double, lng:Double, uid:String) {
    init {
        val _user = JSONObject()
                .put("geo_location", JSONObject()
                        .put("lat",lat)
                        .put("lng",lng));

        Fuel.post("/user/$uid")
                .jsonBody(_user.toString())
                .response { request, response, result ->
                    done(response.data, response.statusCode)
                }
    }
    abstract fun done(data:ByteArray,code:Number)
}