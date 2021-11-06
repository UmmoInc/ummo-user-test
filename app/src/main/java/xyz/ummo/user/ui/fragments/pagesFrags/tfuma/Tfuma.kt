package xyz.ummo.user.ui.fragments.pagesFrags.tfuma

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_tfuma.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.GetAllServices
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentTfumaBinding
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.fragments.categories.ServiceCategories
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.ReloadingServicesEvent
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import xyz.ummo.user.workers.fromJSONArray
import xyz.ummo.user.workers.fromServiceCostJSONArray


class Tfuma : Fragment() {
    private lateinit var tfumaBinding: FragmentTfumaBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    //    private lateinit var delegatableServicesArrayList: List<ServiceEntity>
    private lateinit var delegatableServicesArrayList: ArrayList<ServiceEntity>
    private lateinit var delegatableService: ServiceObject
    private lateinit var delegatedServicePrefs: SharedPreferences
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()
    private var reloadingServicesEvent = ReloadingServicesEvent()
    private var allDelegatableServicesJSONArray = JSONArray()

    lateinit var serviceId: String //1
    lateinit var serviceName: String //2
    lateinit var serviceDescription: String //3
    lateinit var serviceEligibility: String //4
    var serviceCentres = ArrayList<String>() //5
    lateinit var serviceCentresJSONArray: JSONArray //5
    var delegatable: Boolean = false //6

    lateinit var serviceCost: String //7
    lateinit var serviceCostArrayList: ArrayList<ServiceCostModel>
    lateinit var serviceCostJSONArray: JSONArray

    var serviceDocuments = ArrayList<String>() //8
    lateinit var serviceDocumentsJSONArray: JSONArray //8
    lateinit var serviceDuration: String //9
    var approvalCount: Int = 0 //10
    var disapprovalCount: Int = 0 //11
    var serviceComments = ArrayList<String>() //12
    lateinit var serviceCommentsJSONArray: JSONArray //12
    var commentCount: Int = 0 //13
    var shareCount: Int = 0 //14
    var viewCount: Int = 0 //15
    lateinit var serviceProvider: String //16
    var serviceLink: String = "" //17
    var serviceAttachmentJSONArray = JSONArray()
    var serviceAttachmentJSONObject = JSONObject()
    var serviceAttachmentName = ""
    var serviceAttachmentSize = ""
    var serviceAttachmentURL = ""
    var serviceCategory = ""
    lateinit var category: String

    private var tfumaViewModel: TfumaViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceViewModel = ViewModelProvider(this)
            .get(ServiceViewModel::class.java)

        gAdapter = GroupAdapter()

        /** Instantiating [delegatableServicesArrayList]; filling it with data from
         * [serviceViewModel]'s DelegatableServices **/
        delegatableServicesArrayList = (serviceViewModel?.getDelegatableServices()
                as ArrayList<ServiceEntity>?)!!

        delegatedServicePrefs = this.requireActivity()
            .getSharedPreferences(ummoUserPreferences, mode)

        /** Initing TfumaViewModel **/
        tfumaViewModel = ViewModelProvider(this).get(TfumaViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        tfumaBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tfuma,
            container,
            false
        )

        val view = tfumaBinding.root

        /** Scaffolding the [recyclerView] **/
        recyclerView = view.tfuma_services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = view.tfuma_services_recycler_view.layoutManager
        recyclerView.adapter = gAdapter
//        Timber.e("GROUPIE-ADAPTER [1]-> ${gAdapter.itemCount}")

        Timber.e("SERVICE CATEGORY -> ${parentFragment?.arguments?.getString(SERVICE_CATEGORY)}")
        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()
        pollingAdapterState()

        getDelegatableServicesFromServer()

        Timber.e("DELEGATED SERVICES LIST [0] -> ${delegatableServicesArrayList.size}")

        val mixpanel = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        reloadServices()

        returnHome()

//        checkServicesAfterSomeTime()

        /** Refreshing services with [tfuma_swipe_refresher] **/
        tfumaBinding.tfumaSwipeRefresher.setOnRefreshListener {
            delegatableServicesArrayList.clear()
            gAdapter.clear()
            getDelegatableServicesFromServer()
            tfumaBinding.tfumaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)
            mixpanel?.track("delegateFragment_refreshed")
        }

        return view
    }

    @Subscribe
    fun checkSocketConnectionState(socketStateEvent: SocketStateEvent) {
        if (!socketStateEvent.socketConnected!!) {
            loadOfflineServices()
        }
    }

    //TODO: Attend to
    private fun loadOfflineServices() {
        /** Setting up Socket Worker from TfumaViewModel **/
//        tfumaViewModel!!.socketConnect()
        Timber.e("OFFLINE SERVICES -> $delegatableServicesArrayList")
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offline_layout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        tfumaBinding.reloadTfumaServicesButton.setOnClickListener {

            pollingAdapterState()

            loadOfflineServices()

            /** Posting this EventBus in order to display the Snackbar **/
            reloadingServicesEvent.reloadingServices = true
            EventBus.getDefault().post(reloadingServicesEvent)
        }
    }

    private fun returnHome() {
        tfumaBinding.goHomeButton.setOnClickListener {
            openFragment(ServiceCategories())
        }
    }

    private fun openFragment(fragment: Fragment) {

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager
            .beginTransaction()

        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    private fun pollingAdapterState() {
        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(p0: Long) {
                checkingAdapterState()
            }

            override fun onFinish() {
                if (gAdapter.itemCount == 0) {
//                    showOfflineState()
                    noServicesInCategory()
                } else {
                    hideOfflineState()
                    hideNoServicesLayout()
                }
            }
        }

        timer.start()
    }

    private fun checkingAdapterState() {
        if (gAdapter.itemCount == 0) {
            tfumaBinding.tfumaSwipeRefresher.visibility = View.INVISIBLE
            tfumaBinding.loadProgressBar.visibility = View.VISIBLE
            tfumaBinding.offlineLayout.visibility = View.GONE
        } else {
            tfumaBinding.tfumaSwipeRefresher.visibility = View.VISIBLE
            tfumaBinding.loadProgressBar.visibility = View.GONE
            tfumaBinding.offlineLayout.visibility = View.GONE

        }
    }

    private fun noServicesInCategory() {
        tfumaBinding.noServicesRelativeLayout.visibility = View.VISIBLE
        tfumaBinding.offlineLayout.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE

        if (isAdded) {
            val mixpanel = MixpanelAPI.getInstance(
                requireContext(),
                resources.getString(R.string.mixpanelToken)
            )
            mixpanel?.track("delegateFragment_noServicesInCategory")
        }
    }

    private fun showOfflineState() {
        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.offlineLayout.visibility = View.VISIBLE

        if (isAdded) {
            val mixpanel = MixpanelAPI.getInstance(
                requireContext(),
                resources.getString(R.string.mixpanelToken)
            )
            mixpanel?.track("delegateFragment_showingOffline")
        }
    }

    private fun hideNoServicesLayout() {
        tfumaBinding.tfumaSwipeRefresher.visibility = View.VISIBLE
        tfumaBinding.noServicesRelativeLayout.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.offlineLayout.visibility = View.GONE
    }

    private fun hideOfflineState() {
        tfumaBinding.offlineLayout.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.VISIBLE

        if (isAdded) {
            val mixpanel = MixpanelAPI.getInstance(
                requireContext(),
                resources.getString(R.string.mixpanelToken)
            )
            mixpanel?.track("delegateFragment_hidingOffline")
        }
    }

    @AddTrace(name = "get_delegatable_services_from_server")
    private fun getDelegatableServicesFromServer() {
        object : GetAllServices(requireActivity()) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    val allServices = JSONObject(String(data)).getJSONArray("payload")
                    var service: JSONObject

//                    tfumaViewModel!!.parseAndDisplayServices(allServices, "delegatable")

                    try {
                        for (i in 0 until allServices.length()) {
                            service = allServices[i] as JSONObject
                            delegatable = service.getBoolean("delegatable")

                            if (delegatable && category == service.getString(SERVICE_CATEGORY)) {
                                serviceId = service.getString("_id") //1
                                serviceName = service.getString(SERV_NAME) //2
                                serviceDescription = service.getString(SERV_DESCR) //3
                                serviceEligibility = service.getString(SERV_ELIG) //4
//                                serviceCentres //5
                                serviceCentresJSONArray = service.getJSONArray(SERV_CENTRES)
                                serviceCentres = fromJSONArray(serviceCentresJSONArray)

                                delegatable = service.getBoolean(DELEGATABLE) //6
                                serviceCostJSONArray = service.getJSONArray(SERV_COST)
                                serviceCostArrayList =
                                    fromServiceCostJSONArray(serviceCostJSONArray)
//                                serviceCost = service.getString("service_cost") //7
//                                serviceDocuments = //8
                                serviceDocumentsJSONArray = service.getJSONArray(SERV_DOCS)
                                serviceDocuments = fromJSONArray(serviceDocumentsJSONArray)

                                serviceDuration = service.getString(SERV_DURATION) //9
                                approvalCount = service.getInt(UPVOTE_COUNT) //10
                                disapprovalCount = service.getInt(DOWNVOTE_COUNT) //11
//                                serviceComments = s //12
                                serviceCommentsJSONArray = service.getJSONArray(SERV_COMMENTS)
                                serviceComments = fromJSONArray(serviceCommentsJSONArray)

                                commentCount = service.getInt(SERV_COMMENT_COUNT) //13
                                shareCount = service.getInt(SERV_SHARE_COUNT) //14
                                viewCount = service.getInt(SERV_VIEW_COUNT) //15
                                serviceProvider = service.getString(SERV_PROVIDER) //16

                                serviceLink = if (service.getString(SERV_LINK).isNotEmpty())
                                    service.getString(SERV_LINK) //17
                                else
                                    ""

                                serviceCategory = service.getString(SERVICE_CATEGORY)

                                try {
                                    serviceAttachmentJSONArray = service.getJSONArray(
                                        SERV_ATTACH_OBJS
                                    )

                                    for (x in 0 until serviceAttachmentJSONArray.length()) {
                                        serviceAttachmentJSONObject =
                                            serviceAttachmentJSONArray.getJSONObject(x)
                                        serviceAttachmentName =
                                            serviceAttachmentJSONObject.getString(
                                                FILE_NAME
                                            )
                                        serviceAttachmentSize =
                                            serviceAttachmentJSONObject.getString(
                                                FILE_SIZE
                                            )
                                        serviceAttachmentURL =
                                            serviceAttachmentJSONObject.getString(
                                                FILE_URI
                                            )
                                    }
                                } catch (jse: JSONException) {
                                    Timber.e("ISSUE PARSING SERVICE ATTACHMENT -> $jse")
                                }

                                delegatableService = ServiceObject(
                                    serviceId, serviceName,
                                    serviceDescription, serviceEligibility, serviceCentres,
                                    delegatable, serviceCostArrayList, serviceDocuments,
                                    serviceDuration, approvalCount, disapprovalCount,
                                    serviceComments, commentCount, shareCount, viewCount,
                                    serviceProvider, serviceLink, serviceAttachmentName,
                                    serviceAttachmentSize, serviceAttachmentURL, serviceCategory
                                )

                                /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
                                 * 2. wrapping those values in a JSON Object
                                 * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
                                serviceUpVoteBoolean = delegatedServicePrefs
                                    .getBoolean("UP-VOTE-${serviceId}", false)

                                serviceDownVoteBoolean = delegatedServicePrefs
                                    .getBoolean("DOWN-VOTE-${serviceId}", false)

                                serviceCommentBoolean = delegatedServicePrefs
                                    .getBoolean("COMMENTED-ON-${serviceId}", false)

                                serviceBookmarked = delegatedServicePrefs
                                    .getBoolean("BOOKMARKED-${serviceId}", false)

                                savedUserActions
                                    .put("UP-VOTE", serviceUpVoteBoolean)
                                    .put("DOWN-VOTE", serviceDownVoteBoolean)
                                    .put("COMMENTED-ON", serviceCommentBoolean)
                                    .put("BOOKMARKED", serviceBookmarked)

                                if (isAdded) {
                                    gAdapter
                                        .add(
                                            ServiceItem(
                                                delegatableService,
                                                context,
                                                savedUserActions
                                            )
                                        )
                                    Timber.e("GROUPIE-ADAPTER [2] -> ${gAdapter.itemCount}")
                                    checkingAdapterState()
                                }
                            }
                        }

                    } catch (jse: JSONException) {
                        Timber.e("FAILED TO PARSE DELEGATABLE SERVICES -> $jse")
                    }

                } else {
                    Timber.e("FAILED TO GET SERVICES : $code")
                    /** If we fail to load services, we should:
                     * 1. poll for a connection;
                     * 2. display the Offline services view; which will allow the User to retry **/
                    pollingAdapterState()

                    //TODO: Let's tell the User what to do here
                }
            }
        }
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        /** Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE**/
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(requireActivity().findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tfuma().apply {

            }
    }
}