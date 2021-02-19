package xyz.ummo.user.api

import android.app.Activity
import com.github.kittinunf.fuel.Fuel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.models.ServiceProviderData

abstract class GetServiceProvider(val activity: Activity) {
    init {
        Fuel.get("/public-service").response { request, response, result ->
            try {
                activity.runOnUiThread {
                    if (response.data.isNotEmpty()) {
                        val array = JSONArray(String(response.data))
//                        Timber.e("SERVICE-PROVIDER-DATA -> ${response.data}")
                        done(fromJSONList(array), response.statusCode)
                    } else {
//                        Timber.e("NO SERVICE-PROVIDER DATA")
                        done(fromJSONList(JSONArray("[]")),200)
                    }
                }
            } catch (jse: JSONException) {
                Timber.e("Response-> ${String(response.data)}")
                Timber.e("Error-> $jse")
                done(fromJSONList(JSONArray("[]")), 404)
            }
        }
    }

    /** Function takes a JSON Array and returns a (Array)List<PublicServiceData> **/
    private fun fromJSONList(array: JSONArray): List<ServiceProviderData> {
        val tmp = ArrayList<ServiceProviderData>()
        for (i in 0 until array.length()) {
            tmp.add(fromJSONObject(array.getJSONObject(i)))
        }

        return tmp
    }

    /** Function a JSON Object and returns a PublicServiceData **/
    private fun fromJSONObject(obj: JSONObject): ServiceProviderData {
        Timber.e("fromJSONOBJECT-> $obj")
        val serviceProviderId = get(obj, "_id", "serviceProviderId") as String
        val serviceProviderName = get(obj, "service_provider_name", "serviceProviderName") as String
        val serviceProviderDescription = get(obj, "service_provider_description", "serviceProviderDescription") as String
        val serviceProviderContact = get(obj, "service_provider_contact", "serviceProviderContact") as String
        val serviceProviderEmail = get(obj, "service_provider_email", "serviceProviderEmail") as String
        val serviceProviderAddress = get(obj, "service_provider_address", "serviceProviderAddress") as String

        return ServiceProviderData(serviceProviderId, serviceProviderName, serviceProviderDescription, serviceProviderContact, serviceProviderEmail, serviceProviderAddress)
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

    abstract fun done(data: List<ServiceProviderData>, code: Number)
}