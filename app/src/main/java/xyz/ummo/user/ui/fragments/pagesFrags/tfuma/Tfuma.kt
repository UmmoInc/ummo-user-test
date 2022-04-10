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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.GetAllServices
import xyz.ummo.user.api.Service
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentTfumaBinding
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.fragments.categories.ServiceCategories
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.ReloadingServicesEvent
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import xyz.ummo.user.workers.fromJSONArray
import xyz.ummo.user.workers.fromServiceBenefitsJSONArray
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
    lateinit var serviceCentresJSONArray: JSONArray //5.1
    var delegatable: Boolean = false //6
    lateinit var serviceCost: String //7
    lateinit var serviceCostArrayList: ArrayList<ServiceCostModel> //7.1
    lateinit var serviceCostJSONArray: JSONArray //7.1
    var serviceDocuments = ArrayList<String>() //8
    lateinit var serviceDocumentsJSONArray: JSONArray //8.1
    lateinit var serviceDuration: String //9
    var approvalCount: Int = 0 //10
    var disapprovalCount: Int = 0 //11
    var serviceComments = ArrayList<String>() //12
    lateinit var serviceCommentsJSONArray: JSONArray //12.1
    var commentCount: Int = 0 //13
    var shareCount: Int = 0 //14
    var viewCount: Int = 0 //15
    lateinit var serviceProvider: String //16
    var serviceLink: String = "" //17
    var serviceAttachmentJSONArray = JSONArray() //18
    var serviceAttachmentJSONObject = JSONObject() //18.1
    var serviceAttachmentName = "" //18.2
    var serviceAttachmentSize = "" //18.3
    var serviceAttachmentURL = "" //18.4
    var serviceBenefitJSONArray = JSONArray() //19
    var serviceBenefits = ArrayList<ServiceBenefit>() //19.1
    var serviceCategory = "" //20
    lateinit var category: String
    var countOfServices: Int = 0
    lateinit var mixpanelAPI: MixpanelAPI

    /** The following object will be used to cache offline services**/
    lateinit var offlineServiceObject: ServiceObject

    private var tfumaViewModel: TfumaViewModel? = null

    /** SharedPref Editor **/
    private lateinit var editor: SharedPreferences.Editor

    /** JSON Value for Mixpanel **/
    private var categoryJSONObject: JSONObject = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceViewModel = ViewModelProvider(this)[ServiceViewModel::class.java]

        gAdapter = GroupAdapter()

        /** Instantiating [delegatableServicesArrayList]; filling it with data from
         * [serviceViewModel]'s DelegatableServices **/
        delegatableServicesArrayList = (serviceViewModel?.getDelegatableServices()
                as ArrayList<ServiceEntity>?)!!

        delegatedServicePrefs = this.requireActivity()
            .getSharedPreferences(ummoUserPreferences, mode)

        /** Initing TfumaViewModel **/
        tfumaViewModel = ViewModelProvider(this).get(TfumaViewModel::class.java)

        /** Initing Shared Pref. Editor **/
        editor = delegatedServicePrefs.edit()

        categoryJSONObject.put(
            "CATEGORY",
            parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()
        )

    }

    override fun onStart() {
        super.onStart()
        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()
        mixpanelAPI.timeEvent("Viewing TFUMA")
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()
        mixpanelAPI.track("Viewing TFUMA", categoryJSONObject)
        EventBus.getDefault().unregister(this)
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

        mixpanelAPI = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        reloadServices()

        returnHome()

        /** Refreshing services with [tfuma_swipe_refresher] **/
        tfumaBinding.tfumaSwipeRefresher.setOnRefreshListener {
            gAdapter.clear()
            getDelegatableServicesFromServer()
            pollingAdapterState()
            tfumaBinding.tfumaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)
            mixpanelAPI.track("delegateFragment_manuallyRefreshed")
        }

        return view
    }

    private fun showProgressBar() {
        tfumaBinding.loadProgressBar.visibility = View.VISIBLE
        tfumaBinding.noServicesRelativeLayout.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
        tfumaBinding.offlineLayout.visibility = View.GONE
    }

    @Subscribe
    fun onSocketStateEvent(socketStateEvent: SocketStateEvent) {
        if (!socketStateEvent.socketConnected!!) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {

                    Timber.e("OFFLINE")

                    val timer = object : CountDownTimer(3000, 1000) {
                        override fun onTick(p0: Long) {
                            Timber.e("Countdown to Offline Display")
                            gAdapter.clear()
                            showProgressBar()
                        }

                        override fun onFinish() {
                            loadOfflineServices()

                            /** Registering that we've loaded offline services in Shared Prefs. **/
                            editor.putBoolean("$OFFLINE_LOADED-$category", true).apply()
                        }
                    }

                    if (!delegatedServicePrefs.getBoolean("$OFFLINE_LOADED-$category", false)) {
                        timer.start()
                    } else
                        timer.cancel()
                }
            }

        } else {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    gAdapter.clear()
                    Timber.e("ONLINE")

                    hideOfflineState()

                    val timer = object : CountDownTimer(3000, 1000) {
                        override fun onTick(p0: Long) {
                            Timber.e("Loading fresh data")
                            gAdapter.clear()
                            tfumaBinding.loadProgressBar.visibility = View.VISIBLE
                        }

                        override fun onFinish() {
                            getDelegatableServicesFromServer()
                            showSnackbarBlue("Getting fresh data", -1)
                            /** Clearing the offline service state from Shared Prefs **/
                            editor.putBoolean("$OFFLINE_LOADED-$category", false).apply()
                        }
                    }
                    timer.start()
                }
            }
        }
    }

    private fun loadOfflineServices() {
        /** Hiding load progress bar **/
        tfumaBinding.loadProgressBar.visibility = View.GONE

        /** Placeholder Service Cost **/
        val serviceCostArray = ArrayList<ServiceCostModel>()
        serviceCostArray.add(ServiceCostModel("Cost Unavailable", 0))

        /** Placeholder Service Benefits **/
        val serviceBenefitsArray = ArrayList<ServiceBenefit>()
        serviceBenefitsArray.add(ServiceBenefit("", ""))

        Timber.e("Loading Offline Services")

        if (delegatableServicesArrayList.isNotEmpty()) {
            for (i in 0 until delegatableServicesArrayList.size) {
                val delegatableServiceEntity = delegatableServicesArrayList[i]
                Timber.e("DELEGATABLE SERVICES ARE -> ${delegatableServiceEntity.serviceName}")
                offlineServiceObject = ServiceObject(
                    delegatableServiceEntity.serviceId,
                    delegatableServiceEntity.serviceName!!,
                    delegatableServiceEntity.serviceDescription!!,
                    delegatableServiceEntity.serviceEligibility!!,
                    delegatableServiceEntity.serviceCentres!!,
                    delegatableServiceEntity.delegatable!!,
                    serviceCostArray,
                    delegatableServiceEntity.serviceDocuments!!,
                    delegatableServiceEntity.serviceDuration!!,
                    delegatableServiceEntity.usefulCount!!,
                    delegatableServiceEntity.notUsefulCount!!,
                    delegatableServiceEntity.serviceComments!!,
                    delegatableServiceEntity.commentCount!!,
                    delegatableServiceEntity.serviceShares!!,
                    delegatableServiceEntity.serviceViews!!,
                    delegatableServiceEntity.serviceProvider!!,
                    "No Link", "No Attachments",
                    "0", "", "", serviceBenefits
                )

                getSavedUserActionsFromSharedPrefs(delegatableServiceEntity.serviceId)

                /** 1. Checking if the Fragment has been added to the Activity context.
                 *  2. Checking if the Offline Service's category is the same as the category we're in. **/
                if (isAdded && category == delegatableServiceEntity.serviceCategory) {
                    hideNoServicesLayout()
                    hideOfflineState()

                    gAdapter.add(ServiceItem(offlineServiceObject, context, savedUserActions))
                    Timber.e("Displaying offline services -> ${offlineServiceObject.serviceName}")
                    showSnackbarBlue("Showing offline services", -2)
                } else {
                    showOfflineState()
                    hideNoServicesLayout()
                }
            }
        } else {
            Timber.e("No offline services -> ${delegatableServicesArrayList.size}")
            hideNoServicesLayout()
            showOfflineState()
        }
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offline_layout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        tfumaBinding.reloadTfumaServicesButton.setOnClickListener {

            pollingAdapterState()

            Timber.e("Reloading Services")

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
                    showOfflineState()
                    noServicesInCategory()
                    loadOfflineServices()
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

            mixpanelAPI.track("delegateFragment_noServicesInCategory")
        }
    }

    private fun showOfflineState() {
        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.offlineLayout.visibility = View.VISIBLE
        tfumaBinding.noServicesRelativeLayout.visibility = View.GONE

        if (isAdded) {

            mixpanelAPI.track("delegateFragment_showingOffline")
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

            mixpanelAPI.track("delegateFragment_hidingOffline")
        }
    }

    @AddTrace(name = "get_delegatable_services_from_server")
    fun getDelegatableServicesFromServer() {
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
                                countOfServices++
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

                                /** Checking if [serviceBenefits] exists in the [Service] object,
                                 * since not all services have benefits listed under them. **/
                                serviceBenefitJSONArray = if (service.has(SERVICE_BENEFITS)) {
                                    service.getJSONArray(SERVICE_BENEFITS)
                                } else {
                                    val noServiceBenefit = ServiceBenefit("", "")
                                    Timber.e("NO SERVICE BENEFIT -> $noServiceBenefit")
                                    serviceBenefitJSONArray.put(0, noServiceBenefit)
                                }

                                serviceBenefits =
                                    fromServiceBenefitsJSONArray(serviceBenefitJSONArray)

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
                                    serviceAttachmentSize, serviceAttachmentURL, serviceCategory,
                                    serviceBenefits
                                )

                                getSavedUserActionsFromSharedPrefs(serviceId)

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

                        Timber.e("COUNT OF SERVICES -> $countOfServices")

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

    private fun getSavedUserActionsFromSharedPrefs(serviceID: String) {
        /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
         * 2. wrapping those values in a JSON Object
         * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
        serviceUpVoteBoolean = delegatedServicePrefs
            .getBoolean("UP-VOTE-${serviceID}", false)

        serviceDownVoteBoolean = delegatedServicePrefs
            .getBoolean("DOWN-VOTE-${serviceID}", false)

        serviceCommentBoolean = delegatedServicePrefs
            .getBoolean("COMMENTED-ON-${serviceID}", false)

        serviceBookmarked = delegatedServicePrefs
            .getBoolean("BOOKMARKED-${serviceID}", false)

        savedUserActions
            .put("UP-VOTE", serviceUpVoteBoolean)
            .put("DOWN-VOTE", serviceDownVoteBoolean)
            .put("COMMENTED-ON", serviceCommentBoolean)
            .put("BOOKMARKED", serviceBookmarked)
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