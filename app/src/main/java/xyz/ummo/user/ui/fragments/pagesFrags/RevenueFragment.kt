package xyz.ummo.user.ui.fragments.pagesFrags

import android.annotation.SuppressLint
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
import com.google.android.material.snackbar.Snackbar
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_commerce.view.*
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentRevenueBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RevenueFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var revenueBinding: FragmentRevenueBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    /** ServiceProvider ViewModel && Entity Declarations **/
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceViewModel: ServiceViewModel? = null

    private var financeServiceId: String = ""
    private lateinit var revenueService: ServiceObject
    private lateinit var revenueServiceList: List<ServiceEntity>

    /** Shared Preferences for storing user actions **/
    private lateinit var revenuePrefs: SharedPreferences
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences: String = "UMMO_USER_PREFERENCES"
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()

    /** Date-time values for tracking events **/
    private lateinit var simpleDateFormat: SimpleDateFormat
    private var currentDate: String = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        currentDate = simpleDateFormat.format(Date())

        gAdapter = GroupAdapter()

        revenuePrefs = this.requireActivity().getSharedPreferences(ummoUserPreferences, mode)

        /** Initializing ViewModels: ServiceProvider && Services **/
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        getRevenueServiceProviderId()
        getRevenueServices(financeServiceId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        revenueBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_revenue,
                container, false)

        val view = revenueBinding.root
        val layoutManager = view.services_recycler_view.layoutManager

        recyclerView = view.services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gAdapter

        Timber.e("CREATING REVENUE-VIEW!")

        /*if (revenueServiceList.isNotEmpty()) {
            revenueBinding.loadProgressBar.visibility = View.GONE
        }*/

        val mixpanel = MixpanelAPI.getInstance(context,
                resources.getString(R.string.mixpanelToken))
        val revenueEventObject = JSONObject()
        revenueEventObject.put("EVENT_DATE_TIME", currentDate)
        mixpanel?.track("revenueTab_displayed", revenueEventObject)

        /** Refreshing HomeAffairs services with `SwipeRefreshLayout **/
        revenueBinding.revenueSwipeRefresher.setOnRefreshListener {
            Timber.e("REFRESHING VIEW")

//            getRevenueServices(financeServiceId)
            revenueBinding.revenueSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services reloaded", -1)
            mixpanel?.track("revenueTab_swipeRefreshed", revenueEventObject)
        }

        return view
    }

    private fun getRevenueServiceProviderId() {
        val serviceProviders: List<ServiceProviderEntity>? = serviceProviderViewModel
                ?.getServiceProviderList()

        for (i in serviceProviders?.indices!!) {
            Timber.e("SERVICE-PROVIDERS [2]=> ${serviceProviders[i].serviceProviderId}")
            when {
                serviceProviders[i].serviceProviderName
                        .equals("ministry of finance", true) -> {

                    financeServiceId = serviceProviders[i].serviceProviderId.toString()
                    Timber.e("Revenue ID [2] -> $financeServiceId")
                }
            }
        }
    }

    private fun getRevenueServices(revenueId: String) {
        var serviceId: String
        var serviceName: String
        var serviceDescription: String
        var serviceEligibility: String
        var serviceCentres: ArrayList<String>
        var delegatable: Boolean
        var serviceCost: String
        var serviceDocuments: ArrayList<String>
        var serviceDuration: String
        var approvalCount: Int
        var disapprovalCount: Int
        var serviceComments: ArrayList<String>
        var commentCount: Int
        var shareCount: Int
        var viewCount: Int
        var serviceProvider: String

        val servicesList = serviceViewModel?.getServicesList()
        for (i in servicesList?.indices!!) {
            if (servicesList[i].serviceProvider == revenueId) {
                revenueServiceList = servicesList
                Timber.e("REVENUE SERVICE [3] -> ${servicesList[i].serviceName}")
                serviceId = revenueServiceList[i].serviceId.toString() //0
                serviceName = revenueServiceList[i].serviceName.toString() //1
                serviceDescription = revenueServiceList[i].serviceDescription.toString() //2
                serviceEligibility = revenueServiceList[i].serviceEligibility.toString() //3
                serviceCentres = revenueServiceList[i].serviceCentres!! //4
                delegatable = revenueServiceList[i].delegatable!! //5
//                serviceCost = revenueServiceList[i].serviceCost.toString() //6
                serviceDocuments = revenueServiceList[i].serviceDocuments!! //7
                serviceDuration = revenueServiceList[i].serviceDuration.toString() //8
                approvalCount = revenueServiceList[i].usefulCount!! //9
                disapprovalCount = revenueServiceList[i].notUsefulCount!! //10
                serviceComments = revenueServiceList[i].serviceComments!!
                commentCount = revenueServiceList[i].commentCount!! //11
                shareCount = revenueServiceList[i].serviceShares!! //12
                viewCount = revenueServiceList[i].serviceViews!! //13
                serviceProvider = revenueId

                /*revenueService = ServiceObject(serviceId, serviceName, serviceDescription,
                        serviceEligibility, serviceCentres, delegatable, serviceCost,
                        serviceDocuments, serviceDuration, approvalCount, disapprovalCount,
                        serviceComments, commentCount, shareCount, viewCount, serviceProvider)
                Timber.e("REVENUE-SERVICE-BLOB [1] -> $revenueService")*/

                /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
                 * 2. wrapping those values in a JSON Object
                 * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
                serviceUpVoteBoolean = revenuePrefs
                        .getBoolean("UP-VOTE-${revenueServiceList[i].serviceId}", false)

                serviceDownVoteBoolean = revenuePrefs
                        .getBoolean("DOWN-VOTE-${revenueServiceList[i].serviceId}", false)

                serviceCommentBoolean = revenuePrefs
                        .getBoolean("COMMENTED-ON-${revenueServiceList[i].serviceId}", false)

                serviceBookmarked = revenuePrefs
                        .getBoolean("BOOKMARKED-${revenueServiceList[i].serviceId}", false)

                Timber.e("HOME-AFFAIRS-UP-VOTE-${revenueServiceList[i].serviceId} -> $serviceUpVoteBoolean")
                Timber.e("HOME-AFFAIRS-DOWN-VOTE-${revenueServiceList[i].serviceId} -> $serviceDownVoteBoolean")

                savedUserActions
                        .put("UP-VOTE", serviceUpVoteBoolean)
                        .put("DOWN-VOTE", serviceDownVoteBoolean)
                        .put("COMMENTED-ON", serviceCommentBoolean)
                        .put("BOOKMARKED", serviceBookmarked)

                Timber.e("SAVED-USER-ACTIONS -> $savedUserActions")

                gAdapter.add(ServiceItem(revenueService, context, savedUserActions))

            } else {
                revenueServiceList = arrayListOf()
            }
        }
    }

    private fun displayRevenueServices() {
        //gAdapter.add(ServiceItem(revenueService, context))

        revenueBinding.loadProgressBar.visibility = View.GONE

        recyclerView.adapter = gAdapter
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        /** Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE**/
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    companion object {

        fun newInstance() = RevenueFragment()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                RevenueFragment().apply {

                }
    }
}