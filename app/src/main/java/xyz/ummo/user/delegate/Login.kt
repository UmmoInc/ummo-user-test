package xyz.ummo.user.delegate

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import org.json.JSONObject
import xyz.ummo.user.R.string.*

abstract class Login(context: Context, name: String, email: String, mobile_contact: String) {
    init {
        val _user = JSONObject()
                .put("name", name)
                .put("email", email)
                .put("mobile_contact", mobile_contact)

        Fuel.post("/user/login")
                .jsonBody(_user.toString())
                .response { request, response, result ->
                    if (response.statusCode==200){
                        val jwt = response.headers.get("Jwt")?.get(0).toString()
                        Log.e("jwt",jwt)
                        FuelManager.instance.baseHeaders = mapOf("jwt" to jwt)

                        PreferenceManager
                                .getDefaultSharedPreferences(context)
                                .edit()
                                .putString("jwt",jwt)
                                .putString("user",String(response.data))
                                .apply()
                    }
                    done(response.data, response.statusCode)
                }
    }

    abstract fun done(data:ByteArray,code:Number)

}