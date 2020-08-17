package xyz.ummo.user.delegate

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.View
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

abstract class RequestService(context: Context?, user: String, product: String) {
    init {
        val data = JSONObject()
        data.put("user_id", user)
        data.put("product_id", product)

        Fuel.post("${context?.getString(R.string.serverUrl)}/api/service_request")
                .jsonBody(data.toString())
                .response{request, response, result ->
                    (context as Activity).runOnUiThread {
                        done(response.data, response.statusCode)

                        Timber.e("Request Body->${request.body}")
                        Timber.e("Response Code->${response.statusCode}")
                        Timber.e("Result Body->${result}")

                        val snackbar = Snackbar.make(context.findViewById(android.R.id.content), "Request made...",
                                Snackbar.LENGTH_LONG)
                        snackbar.anchorView = context.findViewById<BottomNavigationMenuView>(R.id.bottom_nav)
                        snackbar.setAction("OPEN") {
                            //TODO: open delegatedServiceFragment
                            Timber.e("OPENING...")
                        }
                        snackbar.setActionTextColor(Color.WHITE)
                        val snackbarView = snackbar.view
                        snackbarView.setBackgroundColor(context.resources.getColor(R.color.White))
                        snackbar.show()
                    }
                }
    }

    abstract fun done(data: ByteArray, code: Int)
}