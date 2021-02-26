package xyz.ummo.user.ui.fragments.pagesFrags

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_home_affairs.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.User.Companion.mode
import xyz.ummo.user.api.User.Companion.ummoUserPreferences
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentHomeAffairsBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.DownvoteServiceEvent
import xyz.ummo.user.utilities.eventBusEvents.ServiceCommentEvent
import xyz.ummo.user.utilities.eventBusEvents.UpvoteServiceEvent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeAffairsFragment : Fragment() {

    private lateinit var homeAffairsBinding: FragmentHomeAffairsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    /** ServiceProvider ViewModel && Entity Declarations **/
    private var serviceProviderViewModel: ServiceProviderViewModel? = null

    /** Service ViewModel && Entity Declarations **/
    private var serviceViewModel: ServiceViewModel? = null

    /** HomeAffairs Service instance && Service ID **/
    private var homeAffairsServiceId: String = ""
    private lateinit var homeAffairsService: ServiceObject

    private lateinit var homeAffairsServiceList: List<ServiceEntity>

    /** Shared Preferences for storing user actions **/
    private lateinit var homeAffairsPrefs: SharedPreferences
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()
    private lateinit var loadProgressBar: ProgressBar

    private var newSession = false

    private lateinit var homeAffairsSwipeRefresher: SwipeRefreshLayout

    /** Date-time values for tracking events **/
    private lateinit var simpleDateFormat: SimpleDateFormat
    private var currentDate: String = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        currentDate = simpleDateFormat.format(Date())

        /** [Service-Actions Event] Register for EventBus events **/
        EventBus.getDefault().register(this)

        gAdapter = GroupAdapter()

        homeAffairsPrefs = this.requireActivity().getSharedPreferences(ummoUserPreferences, mode)

        /** Initializing ViewModels: ServiceProvider && Services **/
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        Timber.e("CREATING HOME-AFFAIRS-FRAGMENT!")

        getHomeAffairsServiceProviderId()

        getHomeAffairsServices(homeAffairsServiceId)

        /*loadProgressBar = requireActivity().findViewById(R.id.load_progress_bar)

        if (homeAffairsServiceList.isNotEmpty())
            loadProgressBar.visibility = View.GONE*/
    }

    @Subscribe
    fun onServiceUpvotedEvent(upvoteServiceEvent: UpvoteServiceEvent) {
        Timber.e("SERVICE-UPVOTED-EVENT -> ${upvoteServiceEvent.serviceId}")
        Timber.e("SERVICE-UPVOTED-EVENT -> ${upvoteServiceEvent.serviceUpvote}")
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
        /** [Service-Actions Event] Register for EventBus events **/
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onServiceDownvotedEvent(downvoteServiceEvent: DownvoteServiceEvent) {
        Timber.e("SERVICE-DOWNVOTED-EVENT -> ${downvoteServiceEvent.serviceId}")
        Timber.e("SERVICE-DOWNVOTED-EVENT -> ${downvoteServiceEvent.serviceDownvote}")
    }

    @Subscribe
    fun onServiceCommentedOnEvent(viewHolder: GroupieViewHolder, serviceCommentEvent: ServiceCommentEvent) {
        Timber.e("SERVICE-COMMENTED-ON-EVENT -> ${serviceCommentEvent.serviceId}")
        Timber.e("SERVICE-COMMENTED-ON-EVENT -> ${serviceCommentEvent.serviceCommentedOn}")

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        homeAffairsBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_home_affairs,
                container, false)

        val view = homeAffairsBinding.root
        val layoutManager = view.services_recycler_view.layoutManager

        recyclerView = view.services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gAdapter

        val homeAffairsEvent = JSONObject()
        homeAffairsEvent.put("EVENT_DATE_TIME", currentDate)
        val mixpanel = MixpanelAPI.getInstance(context,
                resources.getString(R.string.mixpanelToken))
        mixpanel?.track("homeAffairsTab_displayed", homeAffairsEvent)

        /** Refreshing HomeAffairs services with `SwipeRefreshLayout **/
        homeAffairsBinding.homeAffairsSwipeRefresher.setOnRefreshListener {
            Timber.e("REFRESHING VIEW")

            getHomeAffairsServices(homeAffairsServiceId)
            homeAffairsBinding.homeAffairsSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed", -1)
            mixpanel?.track("homeAffairsTab_swipeRefreshed", homeAffairsEvent)
        }

        return view
    }

    private fun getHomeAffairsServiceProviderId() {
        val serviceProviders: List<ServiceProviderEntity>? = serviceProviderViewModel
                ?.getServiceProviderList()

        for (i in serviceProviders?.indices!!) {
            Timber.e("SERVICE-PROVIDERS [2]=> ${serviceProviders[i].serviceProviderId}")
            when {
                serviceProviders[i].serviceProviderName
                        .equals("ministry of home affairs", true) -> {

                    homeAffairsServiceId = serviceProviders[i].serviceProviderId.toString()
                    Timber.e("Home Affairs ID [2] -> $homeAffairsServiceId")
                }
            }
        }
    }

    private fun getHomeAffairsServices(homeAffairsId: String) {
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

        Timber.e("SERVICE-LIST [BEFORE]-> ${servicesList?.size}")
        homeAffairsServiceList = servicesList!!

        for (i in servicesList.indices) {
            if (servicesList[i].serviceProvider == homeAffairsId) {
                Timber.e("SERVICE-LIST [AFTER]-> $servicesList")
                homeAffairsServiceList = servicesList

                Timber.e("HOME AFFAIRS SERVICE [3] -> ${servicesList[i].serviceName}")
                serviceId = homeAffairsServiceList[i].serviceId.toString() //0
                serviceName = homeAffairsServiceList[i].serviceName.toString() //1
                serviceDescription = homeAffairsServiceList[i].serviceDescription.toString() //2
                serviceEligibility = homeAffairsServiceList[i].serviceEligibility.toString() //3
                serviceCentres = homeAffairsServiceList[i].serviceCentres!! //4
                delegatable = homeAffairsServiceList[i].delegatable!! //5
                serviceCost = homeAffairsServiceList[i].serviceCost.toString() //6
                serviceDocuments = homeAffairsServiceList[i].serviceDocuments!!//7
                serviceDuration = homeAffairsServiceList[i].serviceDuration.toString() //8
                approvalCount = homeAffairsServiceList[i].usefulCount!! //9
                disapprovalCount = homeAffairsServiceList[i].notUsefulCount!! //10
                serviceComments = homeAffairsServiceList[i].serviceComments!!
                commentCount = homeAffairsServiceList[i].commentCount!! //11
                shareCount = homeAffairsServiceList[i].serviceShares!! //12
                viewCount = homeAffairsServiceList[i].serviceViews!! //13
                serviceProvider = homeAffairsServiceId //14

                Timber.e("HOME-AFFAIRS-SERVICE-LIST => ${homeAffairsServiceList[i].serviceId}")

                homeAffairsService = ServiceObject(serviceId, serviceName, serviceDescription,
                        serviceEligibility, serviceCentres, delegatable, serviceCost,
                        serviceDocuments, serviceDuration, approvalCount, disapprovalCount,
                        serviceComments, commentCount, shareCount, viewCount, serviceProvider)
                Timber.e("HOME-AFFAIRS-SERVICE-BLOB [1] -> $homeAffairsService")

                /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
                 * 2. wrapping those values in a JSON Object
                 * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
                serviceUpVoteBoolean = homeAffairsPrefs
                        .getBoolean("UP-VOTE-${homeAffairsServiceList[i].serviceId}", false)

                serviceDownVoteBoolean = homeAffairsPrefs
                        .getBoolean("DOWN-VOTE-${homeAffairsServiceList[i].serviceId}", false)

                serviceCommentBoolean = homeAffairsPrefs
                        .getBoolean("COMMENTED-ON-${homeAffairsServiceList[i].serviceId}", false)

                serviceBookmarked = homeAffairsPrefs
                        .getBoolean("BOOKMARKED-${homeAffairsServiceList[i].serviceId}", false)

                Timber.e("HOME-AFFAIRS-UP-VOTE-${homeAffairsServiceList[i].serviceId} -> $serviceUpVoteBoolean")
                Timber.e("HOME-AFFAIRS-DOWN-VOTE-${homeAffairsServiceList[i].serviceId} -> $serviceDownVoteBoolean")

                savedUserActions
                        .put("UP-VOTE", serviceUpVoteBoolean)
                        .put("DOWN-VOTE", serviceDownVoteBoolean)
                        .put("COMMENTED-ON", serviceCommentBoolean)
                        .put("BOOKMARKED", serviceBookmarked)

                Timber.e("SAVED-USER-ACTIONS -> $savedUserActions")

                gAdapter.add(ServiceItem(homeAffairsService, context, savedUserActions))

            } else {
                homeAffairsServiceList = arrayListOf()
            }
        }
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

        fun newInstance() = HomeAffairsFragment()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomeAffairsFragment().apply {

                }
    }
}