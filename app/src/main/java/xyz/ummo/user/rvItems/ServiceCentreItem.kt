package xyz.ummo.user.rvItems

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_centre_card.view.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.delegate.DelegateService
import xyz.ummo.user.delegate.RequestService
import xyz.ummo.user.delegate.User
import xyz.ummo.user.models.ServiceCentre
import xyz.ummo.user.ui.detailedService.DetailedProduct

class ServiceCentreItem(private val serviceCentre: ServiceCentre, val context: Context?) : Item<GroupieViewHolder>() {

    /** SharedPref values **/
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private val mode = Activity.MODE_PRIVATE

    /**  **/
    private val detailedProduct: DetailedProduct = DetailedProduct()
    private val productId: String
    private val alertDialogBuilder = AlertDialog.Builder(context)
    private val agentRequestDialog = AlertDialog.Builder(context)
    private val agentNotFoundDialog = AlertDialog.Builder(context)
    private var agentRequestStatus = "Requesting agent..."
    private var progress: ProgressDialog? = null
    private val serviceCentreItemPrefs: SharedPreferences


    private lateinit var alertDialog: AlertDialog
    private val alertDialogView = LayoutInflater.from(context).inflate(R.layout.delegate_agent_dialog, null)
    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")

    init {
        progress = ProgressDialog(context)
        serviceCentreItemPrefs = context!!.getSharedPreferences(ummoUserPreferences, mode)
        productId = serviceCentreItemPrefs.getString("PRODUCT_ID","")!!
    }

    override fun getLayout(): Int {
        return R.layout.service_centre_card
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        Timber.e("JWT -> $jwt")

        /** Inflating the Service Provider fields with data from `serviceCentre` **/
        viewHolder.itemView.service_centre_title_text_view.text = serviceCentre.serviceCentreName
        viewHolder.itemView.service_centre_location_text_view.text = serviceCentre.serviceCentreLocation
        viewHolder.itemView.product_title.text = serviceCentre.productName
        viewHolder.itemView.product_description.text = serviceCentre.productDescription
        //TODO: handle image rendering with Picasso

        /** Inflating the Product offerings from the Service Centre **/
        //TODO: inflate product offering -> Product Name + Product Description

        viewHolder.itemView.setOnClickListener {
            //TODO: handle intent transaction to ProductDetail
        }

        viewHolder.itemView.request_agent_image_view.setOnClickListener {

            alertDialogBuilder.setTitle("Request Agent")
                    .setIcon(R.drawable.logo)
                    .setView(alertDialogView)

            alertDialogBuilder.setPositiveButton("Request") { dialogInterface, i ->
                Timber.e("Clicked Confirm!")
//                detailedProduct.requestAgentDelegate(productId)

                requestAgentDelegate(productId)
            }

            alertDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
                Timber.e("Clicked Cancel!")
            }

            alertDialogBuilder.setOnDismissListener {
                alertDialogBuilder.setView(null)
            }

            alertDialog = alertDialogBuilder.show()
//            alertDialog.dismiss()
            //TODO: attend to bug with alertDialog
        }
    }

    private fun requestAgentDelegate(mProductId: String) {

        val editor: SharedPreferences.Editor = serviceCentreItemPrefs.edit()

        var agentDelegate: JSONObject
        var agentName: String?

        Timber.e("PRODUCT_ID->%s", mProductId)

        if (jwt != null) {
            object : RequestService(context, User.getUserId(jwt!!), mProductId) {
                override fun done(data: ByteArray, code: Int) {
                    Timber.e("delegatedService: Done->%s", String(data))
                    Timber.e("delegatedService: Status Code->%s", code)

                    when (code) {
                        200 -> {
                            agentRequestDialog.setTitle("Bleh Bleh")
                            agentRequestDialog.setMessage("Blah Blah Blah")
                            agentRequestDialog.setPositiveButton("Continue...") { dialogInterface: DialogInterface?, i: Int ->
                                Timber.e("GOING ON!")

                            }
                        }
                        404 -> {
                            agentRequestDialog.setTitle("Meh")
                            agentRequestDialog.setMessage("Blah Blah Blah")
                            agentRequestDialog.setPositiveButton("Continue...") { dialogInterface: DialogInterface?, i: Int ->
                                Timber.e("GOING OFF!")

                            }
                        }
                    }
                }
            }
        }

        if (jwt != null) {
            object : DelegateService(context, User.getUserId(jwt!!), mProductId) {
                override fun done(data: ByteArray, code: Int) {
                    Timber.e("delegatedService: Done->%s", String(data))
                    Timber.e("delegatedService: Status Code->%s", code)
                    progress?.dismiss()

                    when (code) {
                        200 -> {
                            try {
                                agentDelegate = JSONObject(String(data))
                                agentName = agentDelegate.getString("name")
                                Timber.e("done: agentName->%s", agentName)
                                agentRequestDialog.setTitle("Agent Delegate")
                                //                                agentRequestDialog.setIcon()
                                agentRequestDialog.setMessage("$agentName is available...")
                                agentRequestDialog.setPositiveButton("Continue") { dialog: DialogInterface?, which: Int ->
                                    agentRequestStatus = "Waiting for a response from $agentName..."

                                    val progress = ProgressDialog(context)
                                    progress.setTitle("Agent Request")
                                    progress.setMessage(agentRequestStatus)
                                    progress.show() // TODO: 10/22/19 -> handle leaking window

                                    editor.putString("DELEGATED_AGENT", agentName)
                                    editor.putString("DELEGATED_PRODUCT", mProductId)
                                    editor.apply()
                                }
                                agentRequestDialog.show()

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        }
                        404 -> {
                            Timber.e("done: Status Code 500!!!")
                            agentNotFoundDialog.setTitle("Agent Delegate")
                            agentNotFoundDialog.setMessage("No Agent currently available.")
                            agentNotFoundDialog.setPositiveButton("Dismiss") { dialog: DialogInterface?, which: Int ->
                                Toast.makeText(context, "OOPS!", Toast.LENGTH_LONG).show()
                            }
                            agentNotFoundDialog.show()
                        }
                        else -> {
                            Timber.e("done: Status Code 500!!!")
                            Toast.makeText(context, "OOPS!", Toast.LENGTH_LONG).show()
                            agentNotFoundDialog.setTitle("Agent Delegate")
                            agentNotFoundDialog.setMessage("We honestly don't know what happened, please check if there is internet")
                            agentNotFoundDialog.setPositiveButton("Dismiss") { dialog: DialogInterface?, which: Int ->
                                Timber.e("done: Dismissed!")
                                //                                requestAgentBtn.setText("RETRY AGENT REQUEST")
                            }
                            agentNotFoundDialog.show()
                        }
                    }
                }
            }
        }
    }
}