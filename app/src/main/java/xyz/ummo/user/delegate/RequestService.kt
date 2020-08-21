package xyz.ummo.user.delegate

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

abstract class RequestService(context: Context?, user: String, product: String) {

    private lateinit var alertDialog: AlertDialog

    init {
        val alertDialogBuilder = AlertDialog.Builder(context)

        val alertDialogView = LayoutInflater.from(context)
                .inflate(R.layout.completing_request_dialog, null)
        val data = JSONObject()
        data.put("user_id", user)
        data.put("product_id", product)

        Fuel.post("${context?.getString(R.string.serverUrl)}/api/service_request")
                .jsonBody(data.toString())
                .response { request, response, result ->
                    (context as Activity).runOnUiThread {
                        done(response.data, response.statusCode)

                        Timber.e("Request Body->${request.body}")
                        Timber.e("Response Code->${response.statusCode}")
                        Timber.e("Result Body->${result}")

                        alertDialogBuilder.setTitle("Making Request")
                                .setIcon(R.drawable.logo)
                                .setView(alertDialogView)

                        alertDialog = alertDialogBuilder.show()

                        Thread {
                            try {
                                Thread.sleep(1000)

                                alertDialog.dismiss()

                            } catch (ie: InterruptedException) {
                                Timber.e("Thread exception -> $ie")
                            }
                        }.start()

                    }
                }
    }

    abstract fun done(data: ByteArray, code: Int)
}