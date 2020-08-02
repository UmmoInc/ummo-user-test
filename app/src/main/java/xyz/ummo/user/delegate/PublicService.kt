package xyz.ummo.user.delegate

import android.app.Activity
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.models.PublicServiceData

const val TAG = "PublicService.kt"

abstract class PublicService(val activity: Activity) {
    init {
        Fuel.get("/public-service")
                .response { request, response, result ->
                    if (response.statusCode != 200) {
                        Timber.e("Retrieving PUBLIC-SERVICE -> ${response.data}")
                        return@response done(fromJSONList(JSONArray("[]")), response.statusCode)
                    }

                    try {
                        activity.runOnUiThread(Runnable {

                            if (response.data.isNotEmpty()) {
//                         ToDo: java.lang.RuntimeException: java.lang.reflect.InvocationTargetException
                                val array = JSONArray(String(response.data))
                                Timber.e("New PublicService Data -> ${response.data}")
                                done(fromJSONList(array), response.statusCode)
                            } else {
                                Timber.e("No value for Product DATA!")
                                done(fromJSONList(JSONArray("[]")), 200)
//                                return@Runnable
                            }
                        })

                    } catch (ex: JSONException) {
                        Timber.e("Response-> ${String(response.data)}")
                        Timber.e("Error-> $ex")
                        done(fromJSONList(JSONArray("[]")), 200)
                    }
                }
    }

    /** Function takes a JSON Array and returns a (Array)List<PublicServiceData> **/
    private fun fromJSONList(array: JSONArray): List<PublicServiceData> {
        val tmp = ArrayList<PublicServiceData>()
        for (i in 0 until array.length()) {
            tmp.add(fromJSONObject(array.getJSONObject(i)))
        }

        return tmp
    }

    /** Function a JSON Object and returns a PublicServiceData **/
    private fun fromJSONObject(obj: JSONObject): PublicServiceData {
        Timber.e("fromJSONOBJECT-> $obj")
        val serviceName = get(obj, "service_name", "serviceName") as String
        val province = get(obj, "location.province", "province") as String
        val municipality = get(obj, "location.municipality", "municipality") as String
        val town = get(obj, "location.town", "town") as String
        val serviceCode = get(obj, "_id", "serviceName") as String

        return PublicServiceData(serviceName, province, municipality, town, serviceCode)
    }


    private fun get(obj: JSONObject, path: String, default: Any): Any? {
        return try {
            if (path.contains("."))
                get(obj.getJSONObject(path.substringBefore(".")), path.substringAfter("."), default)
            else
                obj.get(path)
        } catch (ex: JSONException) {
            default
        }
    }

    abstract fun done(data: List<PublicServiceData>, code: Number)
}