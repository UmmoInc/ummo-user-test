package xyz.ummo.user.rvItems

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_centre_card.view.*
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.delegate.RequestService
import xyz.ummo.user.delegate.User
import xyz.ummo.user.models.ServiceCentre
import xyz.ummo.user.ui.detailedService.DetailedProduct
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import java.util.ArrayList

class ServiceCentreItem(private val serviceCentre: ServiceCentre, val context: Context?) : Item<GroupieViewHolder>() {

    /** SharedPref values **/
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private val mode = Activity.MODE_PRIVATE

    /**  **/
    private val detailedProduct: DetailedProduct = DetailedProduct()
    private val productId: String
    private val agentRequestDialog = MaterialAlertDialogBuilder(context!!)
    private val agentNotFoundDialog = AlertDialog.Builder(context)
    private var agentRequestStatus = "Requesting agent..."
    private var progress: ProgressDialog? = null
    private val serviceCentreItemPrefs: SharedPreferences

    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")
    //TODO: Update preferences

    init {
        progress = ProgressDialog(context)
        serviceCentreItemPrefs = context!!.getSharedPreferences(ummoUserPreferences, mode)
        productId = serviceCentreItemPrefs.getString("PRODUCT_ID", "")!!
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

        viewHolder.itemView.product_info_relative_layout.setOnClickListener {
            makeRequest()
        }

        viewHolder.itemView.request_agent_image_view.setOnClickListener {
            makeRequest()
        }

        viewHolder.itemView.product_image.setOnClickListener {
            makeRequest()
        }

        viewHolder.itemView.product_title.setOnClickListener {
            makeRequest()
        }
    }

    private fun makeRequest() {
        val alertDialogBuilder =  MaterialAlertDialogBuilder(context!!)

        val alertDialogView = LayoutInflater.from(context)
                .inflate(R.layout.delegate_agent_dialog, null)

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

        alertDialogBuilder.show() //TODO: BIG BUG!!!
//            alertDialog.dismiss()
        //TODO: attend to bug with alertDialog
    }

    private fun requestAgentDelegate(mProductId: String) {

        val editor: SharedPreferences.Editor = serviceCentreItemPrefs.edit()

        Timber.e("PRODUCT_ID->%s", mProductId)

        if (jwt != null) {
            object : RequestService(context, User.getUserId(jwt!!), mProductId) {
                override fun done(data: ByteArray, code: Int) {
                    Timber.e("delegatedService: Done->%s", String(data))
                    Timber.e("delegatedService: Status Code->%s", code)

                    when (code) {
                        200 -> {
//                            alertDialogBuilder.dismiss()

                            Timber.e("CODE IS $code")

                            val service = JSONObject(String(data))
                            Timber.e("SERVICE OBJ -> $service")
                            val serviceId = service.getString("_id")
                            val serviceProduct = service.getString("product")
                            val serviceAgent = service.getString("agent")

                            editor.putString("SERVICE_ID", serviceId)
                            //TODO: remove after service is done
                            editor.putString("DELEGATED_PRODUCT_ID", productId)
                            editor.putString("SERVICE_AGENT_ID", serviceAgent)
                            editor.apply()

                            launchDelegatedService(context, serviceId, serviceAgent, serviceProduct)

                        }
                        404 -> {
                            Timber.e("CODE IS $code")

                            agentRequestDialog.setTitle("Meh")
                            agentRequestDialog.setMessage("Blah Blah Blah")
                            agentRequestDialog.setPositiveButton("Continue...")
                            { dialogInterface: DialogInterface?, i: Int ->
                                Timber.e("GOING OFF!")

                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchDelegatedService(context: Context?, serviceId: String, agentId: String, productId: String) {
        val bundle = Bundle()
        bundle.putString("SERVICE_ID", serviceId)
        bundle.putString("DELEGATED_PRODUCT_ID", productId)
        bundle.putString("SERVICE_AGENT_ID", agentId)

        val progress = ArrayList<String>()
        val delegatedServiceEntity = DelegatedServiceEntity()
        val delegatedServiceViewModel = ViewModelProvider((context as FragmentActivity?)!!)
                .get(DelegatedServiceViewModel::class.java)

        delegatedServiceEntity.serviceId = serviceId
        delegatedServiceEntity.delegatedProductId = productId
        delegatedServiceEntity.serviceAgentId = agentId
        delegatedServiceEntity.serviceProgress = progress
        delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

        val fragmentActivity = context as FragmentActivity
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val delegatedServiceFragment = DelegatedServiceFragment()
        delegatedServiceFragment.arguments = bundle
//        fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
        fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
        fragmentTransaction.commit()
    }
}