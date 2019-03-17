package xyz.ummo.user.delegate

import com.github.kittinunf.fuel.Fuel
import org.json.JSONObject
import xyz.ummo.user.Interfaces.HttpFinished
import xyz.ummo.user.R.string.*


abstract class User {
    abstract var httpFinished: HttpFinished?

    fun login(name: String, email: String, mobile_contact: String): User {

        val _user = JSONObject()
        _user.put("name", name)
        _user.put("email", email)
        _user.put("mobile_contact", mobile_contact)

        Fuel.post("$serverUrl/user/login")
                .jsonBody(_user.toString())
                .response { request, response, result ->
                    httpFinished?.done(response.data,response.statusCode)
                }
        return this
    }

    fun onHttpRequestFinished(_httpFinished:HttpFinished){
        httpFinished = httpFinished
    }

}
