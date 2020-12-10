package xyz.ummo.user.ui.fragments.pagesFrags

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_commerce.view.*
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentCommerceBinding
import xyz.ummo.user.models.Service
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel

class CommerceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var commerceBinding: FragmentCommerceBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    /** ServiceProvider ViewModel && Entity Declarations **/
    private var serviceProviderViewModel: ServiceProviderViewModel? = null

    /** Service ViewModel && Entity Declarations **/
    private var serviceViewModel: ServiceViewModel? = null

    /** Commerce Service instance && Service ID **/
    private var commerceServiceId: String = ""
    private lateinit var commerceService: Service
    private lateinit var commerceServiceList: List<ServiceEntity>

    /** Shared Preferences for storing user actions **/
    private lateinit var commercePrefs: SharedPreferences
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences: String = "UMMO_USER_PREFERENCES"
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

        commercePrefs = this.requireActivity().getSharedPreferences(ummoUserPreferences, mode)

        /** Initializing ViewModels: ServiceProvider && Services **/
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        Timber.e("CREATING COMMERCE-FRAGMENT!")
        getCommerceServiceProviderId()
        getCommerceServices(commerceServiceId)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        commerceBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_commerce,
                container, false)

        val view = commerceBinding.root
        val layoutManager = view.services_recycler_view.layoutManager
        recyclerView = view.services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gAdapter

        Timber.e("CREATING COMMERCE-VIEW!")

        if (commerceServiceList.isNotEmpty()) {
            commerceBinding.loadProgressBar.visibility = View.GONE
        }

        return view
    }

    private fun getCommerceServiceProviderId() {
        val serviceProviders: List<ServiceProviderEntity>? = serviceProviderViewModel
                ?.getServiceProviderList()

        for (i in serviceProviders?.indices!!) {
            Timber.e("SERVICE-PROVIDERS [2]=> ${serviceProviders[i].serviceProviderId}")
            when {
                serviceProviders[i].serviceProviderName
                        .equals("ministry of commerce", true) -> {

                    commerceServiceId = serviceProviders[i].serviceProviderId.toString()
                    Timber.e("Commerce ID [2] -> $commerceServiceId")
                }
            }
        }
    }

    private fun getCommerceServices(commerceId: String) {
        var serviceId: String
        var serviceName: String
        var serviceDescription: String
        var serviceEligibility: String
        var serviceCentres: ArrayList<String>
        var presenceRequired: Boolean
        var serviceCost: String
        var serviceDocuments: ArrayList<String>
        var serviceDuration: String
        var approvalCount: Int
        var disApprovalCount: Int
        var serviceComments: ArrayList<String>
        var commentCount: Int
        var shareCount: Int
        var viewCount: Int
        var serviceProvider: String

        val servicesList = serviceViewModel?.getServicesList()
        for (i in servicesList?.indices!!) {
            if (servicesList[i].serviceProvider == commerceId) {
                commerceServiceList = servicesList
                Timber.e("COMMERCE SERVICE [3] -> ${servicesList[i].serviceName}")
                serviceId = commerceServiceList[i].serviceId.toString() //0
                serviceName = commerceServiceList[i].serviceName.toString() //1
                serviceDescription = commerceServiceList[i].serviceDescription.toString() //2
                serviceEligibility = commerceServiceList[i].serviceEligibility.toString() //3
                serviceCentres = commerceServiceList[i].serviceCentres!! //4
                presenceRequired = commerceServiceList[i].presenceRequired!! //5
                serviceCost = commerceServiceList[i].serviceCost.toString() //6
                serviceDocuments = commerceServiceList[i].serviceDocuments!! //7
                serviceDuration = commerceServiceList[i].serviceDuration.toString() //8
                approvalCount = commerceServiceList[i].usefulCount!! //9
                disApprovalCount = commerceServiceList[i].notUsefulCount!! //10
                serviceComments = commerceServiceList[i].serviceComments!!
                commentCount = commerceServiceList[i].commentCount!! //11
                shareCount = commerceServiceList[i].serviceShares!! //12
                viewCount = commerceServiceList[i].serviceViews!! //13
                serviceProvider = commerceServiceId //14

                commerceService = Service(serviceId, serviceName, serviceDescription,
                        serviceEligibility, serviceCentres, presenceRequired, serviceCost,
                        serviceDocuments, serviceDuration, approvalCount, disApprovalCount,
                        serviceComments, commentCount, shareCount, viewCount, serviceProvider)
                Timber.e("COMMERCE-SERVICE-BLOB [1] -> $commerceService")

                /** 1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
                 *  2. wrapping those values in a JSON Object
                 *  3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
                serviceUpVoteBoolean = commercePrefs
                        .getBoolean("UP-VOTE-${commerceServiceList[i].serviceId}", false)

                serviceDownVoteBoolean = commercePrefs
                        .getBoolean("DOWN-VOTE-${commerceServiceList[i].serviceId}", false)

                serviceCommentBoolean = commercePrefs
                        .getBoolean("COMMENTED-ON-${commerceServiceList[i].serviceId}", false)

                serviceBookmarked = commercePrefs
                        .getBoolean("BOOKMARKED-${commerceServiceList[i].serviceId}", false)

                Timber.e("HOME-AFFAIRS-UP-VOTE-${commerceServiceList[i].serviceId} -> $serviceUpVoteBoolean")
                Timber.e("HOME-AFFAIRS-DOWN-VOTE-${commerceServiceList[i].serviceId} -> $serviceDownVoteBoolean")

                savedUserActions
                        .put("UP-VOTE", serviceUpVoteBoolean)
                        .put("DOWN-VOTE", serviceDownVoteBoolean)
                        .put("COMMENTED-ON", serviceCommentBoolean)
                        .put("BOOKMARKED", serviceBookmarked)

                Timber.e("SAVED-USER-ACTIONS -> $savedUserActions")

                gAdapter.add(ServiceItem(commerceService, context, savedUserActions))

            } else {
                commerceServiceList = arrayListOf()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                CommerceFragment().apply {

                }
    }
}