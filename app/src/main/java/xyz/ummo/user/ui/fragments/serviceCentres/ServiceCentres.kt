package xyz.ummo.user.ui.fragments.serviceCentres

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_service_centres_rv.view.*
import kotlinx.android.synthetic.main.service_centre_card.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentServiceCentresRvBinding
import xyz.ummo.user.delegate.PublicService
import xyz.ummo.user.models.PublicServiceData

class ServiceCentres : Fragment() {

    private lateinit var serviceCentresRvBinding: FragmentServiceCentresRvBinding
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var recyclerView: RecyclerView
    private var publicServiceData: ArrayList<PublicServiceData> = ArrayList()
    private var progress: ProgressDialog? = null


    companion object {
        fun newInstance() = ServiceCentres()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progress = ProgressDialog(requireContext())

        getServiceCentreData()
        Timber.e("GOT SERVICE CENTRE DATA ->%s", publicServiceData)

        //Init GroupAdapter
        gAdapter = GroupAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        serviceCentresRvBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_centres_rv,
                container, false)

        val view = serviceCentresRvBinding.root
        val layoutManager = view.service_centre_recycler_view.layoutManager

        recyclerView = view.service_centre_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gAdapter

        return view
    }

    private fun getServiceCentreData() {

        object : PublicService(requireActivity()) {
            override fun done(data: List<PublicServiceData>, code: Number) {
                if (code == 200) {
                    publicServiceData.addAll(data)
                    Timber.e( "GETTING SERVICE CENTRE DATA ->%s", publicServiceData)

                    for (i in data.indices) {
                        val singleServiceCentre = data[i]
                        Timber.e( "Single Service Centre -> $singleServiceCentre")
                        gAdapter.add(ServiceCentreItem(singleServiceCentre, context))
                    }
                    recyclerView.adapter = gAdapter
                }
                //TODO: handle incident when response code is not 200
            }
        }
    }
}

class ServiceCentreItem(private val serviceCentre: PublicServiceData, val context: Context?) : Item<GroupieViewHolder>() {

    private val alertDialogBuilder = AlertDialog.Builder(context)
    private lateinit var alertDialog: AlertDialog
    private val alertDialogView = LayoutInflater.from(context).inflate(R.layout.delegate_agent_dialog, null)
    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")


    override fun getLayout(): Int {
        return R.layout.service_centre_card
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.service_centre_title_text_view.text = serviceCentre.serviceName
        viewHolder.itemView.service_centre_location_text_view.text = serviceCentre.town
        //TODO: handle image rendering with Picasso

        viewHolder.itemView.setOnClickListener {
            //TODO: handle intent transaction to ProductDetail
        }

        viewHolder.itemView.request_agent_image_view.setOnClickListener {

            alertDialogBuilder.setTitle("Request Agent")
                    .setIcon(R.drawable.logo)
                    .setView(alertDialogView)

            alertDialogBuilder.setPositiveButton("Request") { dialogInterface, i ->
                Timber.e( "Clicked Confirm!")
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
}