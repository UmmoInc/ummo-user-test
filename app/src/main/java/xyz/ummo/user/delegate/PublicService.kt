package xyz.ummo.user.delegate

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.google.gson.JsonArray
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

const val TAG = "PublicService.kt"

abstract class PublicService(val activity:Activity) {
    init {
        Fuel.get("/public-service")
                .response { request, response, result ->
                    if(response.statusCode!=200){
                        return@response done(fromJSONList(JSONArray("[]")),response.statusCode)
                    }
                    try {
                        activity.runOnUiThread(Runnable {

                            if (response.data.isNotEmpty()){
//                         ToDo: java.lang.RuntimeException: java.lang.reflect.InvocationTargetException
                                val array = JSONArray(String(response.data))
                                Log.e(TAG, "Got new service data"+String(response.data))
                                done(fromJSONList(array), response.statusCode)
                            } else{
                                Log.e(TAG, "No value for Product DATA!")
                                done(fromJSONList(JSONArray("[]")),200)
//                                return@Runnable
                            }
                        })

                    } catch (ex: JSONException) {
                        Log.e(TAG, "Response-> ${String(response.data)}")
                        Log.e(TAG, "Here  error $ex")
                        done(fromJSONList(JSONArray("[]")),200)
                    }

                }
    }

    private fun fromJSONList(array: JSONArray): List<PublicServiceData> {
        val tmp = ArrayList<PublicServiceData>()
        for (i in 0 until array.length()) {
            tmp.add(fromJSONObject(array.getJSONObject(i)))
        }
        return tmp
    }

    private fun fromJSONObject(obj: JSONObject): PublicServiceData {
        Log.e(TAG,"fromJSONOBJECT-> $obj")
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