package xyz.ummo.user.api

import android.app.Activity
import android.preference.PreferenceManager
import com.github.kittinunf.fuel.Fuel
import com.google.firebase.perf.metrics.AddTrace
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber


abstract class GetAllServices(private val activity: Activity) {

    var client = OkHttpClient()
    val jwt: String = PreferenceManager.getDefaultSharedPreferences(activity)
        .getString("jwt", "").toString()

    init {
        getAllServices()
        /*getAllServicesOK("${activity.getString(R.string.serverUrl)}/product", object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Timber.e("OK HTTP FAILED -> $e")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body.toString()

                activity.runOnUiThread {
                    try {
                        val json = JSONObject(responseData)
                        Timber.e("OK HTTP RESPONSE -> $json")
                    } catch (e: JSONException) {
                        Timber.e("OK HTTP JSE -> $e")
                        e.printStackTrace()
                    }
                }
            }
        })*/
    }

    @AddTrace(name = "get_all_services")
    private fun getAllServices() {
        Fuel.get("/product")
            .response { request, response, result ->
                activity.runOnUiThread {
                    if (response.data.isNotEmpty()) {
                        done(response.data, response.statusCode)
                        Timber.e("SERVICES FOUND -> ${String(response.data)}")
                    } else
                        Timber.e("RESPONSE IS EMPTY!")
                }
            }
    }

    private fun getAllServicesOK(url: String, callback: Callback): Call {
        val request = Request.Builder().url(url).header("jwt", jwt).build()
        val call = client.newCall(request)
        call.enqueue(callback)
        return call
    }

    /*private fun getAllServicesOk() {
        val okHttp = OkHttpClient()

        val requestBuilder =
            Request.Builder()
                .url("${activity.getString(R.string.serverUrl)}/product")
                .build()

        activity.runOnUiThread {


            if (response.isSuccessful) {
                Timber.e("OK HTTP RESPONSE -> ${response.body}")
            }
        }
    }*/

    abstract fun done(data: ByteArray, code: Number)
}