package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

val TAG = "PublicService"

abstract class PublicService() {
    init {
        Fuel.get("/public-service")
                .response { request, response, result ->
                    try {
                        var array = JSONArray(String(response.data))
                        done(fromJSONList(array), response.statusCode)
                    } catch (ex: JSONException) {
                        Log.e(TAG, ex.toString())
                    }

                }
    }

    private fun fromJSONList(array: JSONArray): List<PublicServiceData> {
        val tmp = ArrayList<PublicServiceData>()
        for (i in 0..array.length() - 1) {
            tmp.add(fromJSONObject(array.getJSONObject(i)))
        }
        return tmp
    }

    private fun fromJSONObject(obj: JSONObject): PublicServiceData {
        Log.e(TAG,obj.toString())
        val serviceName = get(obj, "service_name", "serviceName") as String
        val province = get(obj, "location.province", "province") as String
        val municipality = get(obj, "location.municipality", "municipality") as String
        val town = get(obj, "location.town", "town") as String
        val serviceCode = get(obj, "_id", "serviceName") as String
        return PublicServiceData(serviceName, province, municipality, town, serviceCode)
    }

    private fun get(obj: JSONObject, path: String, default: Any): Any? {
        try {
            return if (path.contains("."))
                get(obj.getJSONObject(path.substringBefore(".")), path.substringAfter("."), default)
            else
                obj.get(path)
        } catch (ex: JSONException) {
            return default
        }

    }

    abstract fun done(data: List<PublicServiceData>, code: Number)
}