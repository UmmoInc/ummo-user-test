package xyz.ummo.user.api

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.workers.SocketConnectWorker

/** This abstract function takes a User (contact) && Product (ID); then a request
 *  is made with Fuel (HTTP POST) **/
abstract class RequestService(
    context: Context?, user: String, product: String,
    /*delegationFee: JSONObject, chosenServiceCentre: String,*/ serviceDate: String?
) {

    init {
        val alertDialogBuilder = MaterialAlertDialogBuilder(context!!)
        val socket = SocketConnectWorker.SocketIO.mSocket

        val errorAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.request_error_dialog, null)

        val data = JSONObject()
        data.put("user", user)
        data.put("product", product)
        /*data.put("delegation_fee", delegationFee)
        data.put("chosen_service_centre", chosenServiceCentre)*/
        data.put("service_date", serviceDate)

//        Fuel.post("${context.getString(R.string.serverUrl)}/api/service_request")
        Fuel.post("${context.getString(R.string.serverUrl)}/service/service_request")
            .jsonBody(data.toString())
            .response { request, response, result ->
                (context as Activity).runOnUiThread {
                    done(response.data, response.statusCode)

                    if (response.statusCode == 200) {
                        Timber.e("REQUESTING SERVICE -> $product")
                    } else if (response.statusCode == 500) {
                        alertDialogBuilder.setTitle("Something's Wrong")
                            .setIcon(R.drawable.logo)
                                    .setView(errorAlertDialogView)

                            alertDialogBuilder.setPositiveButton("Feedback") { dialogInterface, i ->
                                //TODO: add feedback functionality
                                val feedbackFromMainScreen = MainScreen()
                                feedbackFromMainScreen.feedback()
                            }

                            alertDialogBuilder.setNegativeButton("Later") { dialogInterface, i ->
                                //TODO: track `Later` event
                            }

                            alertDialogBuilder.show()
                        }
                    }
                }

    }

    abstract fun done(data: ByteArray, code: Int)

}