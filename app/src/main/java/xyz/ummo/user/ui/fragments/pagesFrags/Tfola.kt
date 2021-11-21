package xyz.ummo.user.ui.fragments.pagesFrags

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
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.perf.metrics.AddTrace
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_tfola.view.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.GetAllServices
import xyz.ummo.user.api.GetServiceProvider
import xyz.ummo.user.api.Service
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentTfolaBinding
import xyz.ummo.user.databinding.ServiceFilterChipLayoutBinding
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.models.ServiceProviderData
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.fragments.categories.ServiceCategories
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.SERVICE_BENEFITS
import xyz.ummo.user.utilities.SERVICE_CATEGORY
import xyz.ummo.user.utilities.eventBusEvents.LoadingCategoryServicesEvent
import xyz.ummo.user.utilities.eventBusEvents.ReloadingServicesEvent
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences
import xyz.ummo.user.workers.fromServiceBenefitsJSONArray

class Tfola : Fragment() {
    private lateinit var allServices: JSONArray

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var serviceObject = JSONObject()
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()
    private var reloadingServicesEvent = ReloadingServicesEvent()
    private var loadingCategoryServicesEvent = LoadingCategoryServicesEvent()
    private lateinit var nonDelegatedServicePrefs: SharedPreferences
    private lateinit var nonDelegatableServicesArrayList: ArrayList<ServiceEntity>
    private lateinit var nonDelegatedService: ServiceObject
    private lateinit var tfolaBinding: FragmentTfolaBinding
    private lateinit var serviceFilterChipBinding: ServiceFilterChipLayoutBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var nonDelegatableService: ServiceObject

    lateinit var serviceId: String //1
    lateinit var serviceName: String //2
    lateinit var serviceDescription: String //3
    lateinit var serviceEligibility: String //4
    var serviceCentres = ArrayList<String>() //5
    lateinit var serviceCentresJSONArray: JSONArray //5
    var delegatable: Boolean = false //6

    lateinit var serviceCost: ArrayList<ServiceCostModel> //7
    lateinit var serviceCostArrayList: ArrayList<ServiceCostModel> //7
    lateinit var serviceCostJSONArray: JSONArray //7

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
    lateinit var serviceProviderData: ServiceProviderData
    lateinit var serviceProviderList: List<ServiceProviderData>
    lateinit var serviceProvider: String //16
    var serviceLink = "" //17
    var serviceAttachmentJSONArray = JSONArray()
    var serviceAttachmentJSONObject = JSONObject() //18

    var serviceBenefitJSONArray = JSONArray()
    var serviceBenefits = ArrayList<ServiceBenefit>()

    var serviceAttachmentName = ""
    var serviceAttachmentSize = ""
    var serviceAttachmentURL = ""
    var serviceCategory = ""
    lateinit var category: String

    lateinit var mixpanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceViewModel = ViewModelProvider(this)
            .get(ServiceViewModel::class.java)

        gAdapter = GroupAdapter()

        nonDelegatedServicePrefs = this.requireActivity()
            .getSharedPreferences(ummoUserPreferences, mode)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        tfolaBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tfola,
            container,
            false
        )

        serviceFilterChipBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.service_filter_chip_layout,
            container,
            false
        )

        val view = tfolaBinding.root
        recyclerView = view.tfola_services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = view.tfola_services_recycler_view.layoutManager
        recyclerView.adapter = gAdapter

        getNonDelegatableServicesFromServer()

        pollingAdapterState()

        reloadServices()

        returnHome()

        if (isAdded) {
            getServiceProviderData()
        }

        filterServicesByCategory()

        mixpanel = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()

        /** Refreshing services with [tfola_swipe_refresher] **/
        tfolaBinding.tfolaSwipeRefresher.setOnRefreshListener {
            getNonDelegatableServicesFromServer()
            tfolaBinding.tfolaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)

            mixpanel.track("discoverFragment_refreshed")
        }

        return view
    }

    private fun noServicesInCategory() {
        tfolaBinding.noServicesRelativeLayout.visibility = View.VISIBLE
        tfolaBinding.offlineLayout.visibility = View.GONE
        tfolaBinding.tfolaServicesRecyclerView.visibility = View.GONE
        tfolaBinding.loadProgressBar.visibility = View.GONE

        if (isAdded) {
            val mixpanel = MixpanelAPI.getInstance(
                requireContext(),
                resources.getString(R.string.mixpanelToken)
            )
            mixpanel?.track("discoverFragment_noServicesInCategory")
        }
    }

    private fun hideNoServicesLayout() {
        tfolaBinding.noServicesRelativeLayout.visibility = View.GONE
        tfolaBinding.tfolaSwipeRefresher.visibility = View.VISIBLE
        tfolaBinding.loadProgressBar.visibility = View.GONE
        tfolaBinding.offlineLayout.visibility = View.GONE
    }

    private fun filterServicesByCategory() {

        tfolaBinding.serviceCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
            Timber.e("CHECKED GROUP -> $group")
            Timber.e("CHECKED CHIP -> $checkedId")

            val titleOrNull = group.findViewById<Chip>(checkedId)?.text
            Timber.e("CHECKED CHIP ID ->>> $titleOrNull")

            when (titleOrNull) {

                "All Services" -> {
                    Timber.e("CHECKED ALL")
                    Timber.e("SERVICE PROVIDER -> $serviceProviderList")
                    getNonDelegatableServicesFromServer()

                }
                "Home Affairs" -> {
                    Timber.e("CHECKED HOME AFFAIRS")
                    displayServiceByCategory("601268725ad77100154da834")
                    mixpanel.track("discoverFragment_homeAffairs_selected")
                    loadingCategoryServicesEvent.categoryLoading = "Home-Affairs"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                }
                "Commerce" -> {
                    Timber.e("CHECKED COMMERCE")
                    displayServiceByCategory("601266be5ad77100154da833")
                    mixpanel.track("discoverFragment_commerce_selected")
                    loadingCategoryServicesEvent.categoryLoading = "Commerce"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                }
                "Revenue" -> {
                    Timber.e("CHECKED REVENUE")
                    displayServiceByCategory("601268ff5ad77100154da835")
                    mixpanel.track("discoverFragment_revenue_selected")
                    loadingCategoryServicesEvent.categoryLoading = "Revenue"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                }
            }
        }

        Timber.e("CHIP GROUP -> ${tfolaBinding.serviceCategoryChipGroup.checkedChipId}")
        Timber.e("CHIP GROUP -> ${tfolaBinding.serviceCategoryChipGroup}")
    }

    private fun getServiceProviderData() {
        object : GetServiceProvider(requireActivity()) {
            override fun done(data: List<ServiceProviderData>, code: Number) {
                if (code == 200) {
                    serviceProviderList = data

                    for (i in data.indices) {
                        serviceProviderData = data[i]
                    }
                } else {
                    Timber.e("NO SERVICE PROVIDERS FOUND -> $code")
                }
            }
        }
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offline_layout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        tfolaBinding.reloadTfolaServicesButton.setOnClickListener {

            pollingAdapterState()
            /** Posting this EventBus in order to display the Snackbar **/
            reloadingServicesEvent.reloadingServices = true
            EventBus.getDefault().post(reloadingServicesEvent)
        }
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
            tfolaBinding.tfolaSwipeRefresher.visibility = View.INVISIBLE
            tfolaBinding.loadProgressBar.visibility = View.VISIBLE
            tfolaBinding.offlineLayout.visibility = View.GONE
        } else {
            tfolaBinding.tfolaSwipeRefresher.visibility = View.VISIBLE
            tfolaBinding.loadProgressBar.visibility = View.GONE
            tfolaBinding.offlineLayout.visibility = View.GONE

        }
    }

    private fun showOfflineState() {
        tfolaBinding.tfolaSwipeRefresher.visibility = View.GONE
        tfolaBinding.loadProgressBar.visibility = View.GONE
        tfolaBinding.offlineLayout.visibility = View.VISIBLE

        if (isAdded) {
            val mixpanel = MixpanelAPI.getInstance(
                requireContext(),
                resources.getString(R.string.mixpanelToken)
            )
            mixpanel?.track("discoverFragment_showingOffline")
        }

    }

    private fun hideOfflineState() {
        tfolaBinding.offlineLayout.visibility = View.GONE
        tfolaBinding.loadProgressBar.visibility = View.GONE
        tfolaBinding.tfolaSwipeRefresher.visibility = View.VISIBLE

        if (isAdded) {
            val mixpanel = MixpanelAPI.getInstance(
                requireContext(),
                resources.getString(R.string.mixpanelToken)
            )
            mixpanel?.track("discoverFragment_hidingOffline")
        }
    }

    private fun returnHome() {
        tfolaBinding.goHomeButton.setOnClickListener {
            openFragment(ServiceCategories())
        }
    }

    private fun openFragment(fragment: Fragment) {

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager
            .beginTransaction()

        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    @AddTrace(name = "get_non_delegatable_services_from_server")
    private fun getNonDelegatableServicesFromServer() {
        object : GetAllServices(requireActivity()) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    allServices = JSONObject(String(data)).getJSONArray("payload")

                    gAdapter.clear()

                    try {
                        parseAllServices(allServices)

                    } catch (jse: JSONException) {
                        Timber.e("FAILED TO PARSE NON-DELEGATABLE SERVICES -> $jse")
                    }

                } else {
                    Timber.e("FAILED TO GET SERVICES : $code")
                }
            }
        }
    }

    private fun parseAllServices(allServices: JSONArray) {
        var service: JSONObject

        for (i in 0 until allServices.length()) {
            service = allServices[i] as JSONObject
            delegatable = service.getBoolean("delegatable")

            if (!delegatable && category == service.getString(SERVICE_CATEGORY)) {
                parseSingleService(service)
            }
        }
    }

    private fun parseSingleService(serviceJSONObject: JSONObject) {

        serviceId = serviceJSONObject.getString("_id") //1
        serviceName = serviceJSONObject.getString("service_name") //2
        serviceDescription = serviceJSONObject.getString("service_description") //3
        serviceEligibility = serviceJSONObject.getString("service_eligibility") //4
//                                serviceCentres //5
        serviceCentresJSONArray = serviceJSONObject.getJSONArray("service_centres")
        serviceCentres = fromJSONArray(serviceCentresJSONArray)

        delegatable = serviceJSONObject.getBoolean("delegatable") //6
        //TODO: ATTEND TO ASAP
//                serviceCost = service.getJSONArray("service_cost") //7
        serviceCostJSONArray = serviceJSONObject.getJSONArray("service_cost")
        serviceCostArrayList = fromServiceCostJSONArray(serviceCostJSONArray)
        Timber.e("SERVICE COST ARRAY LIST -> $serviceCostArrayList")
//                                serviceDocuments = //8
        serviceDocumentsJSONArray = serviceJSONObject.getJSONArray("service_documents")
        serviceDocuments = fromJSONArray(serviceDocumentsJSONArray)

        serviceDuration = serviceJSONObject.getString("service_duration") //9
        approvalCount = serviceJSONObject.getInt("useful_count") //10
        disapprovalCount = serviceJSONObject.getInt("not_useful_count") //11
//                                serviceComments = s //12
        serviceCommentsJSONArray = serviceJSONObject.getJSONArray("service_comments")
        serviceComments = fromJSONArray(serviceCommentsJSONArray)

        commentCount = serviceJSONObject.getInt("service_comment_count") //13
        shareCount = serviceJSONObject.getInt("service_share_count") //14
        viewCount = serviceJSONObject.getInt("service_view_count") //15
        serviceProvider = serviceJSONObject.getString("service_provider") //16

        /** Checking if [serviceBenefits] exists in the [Service] object,
         * since not all services have benefits listed under them. **/
        serviceBenefitJSONArray = if (serviceJSONObject.has(SERVICE_BENEFITS)) {
            Timber.e("SERVICE BENEFIT FOUND")
            serviceJSONObject.getJSONArray(SERVICE_BENEFITS)
        } else {
            val noServiceBenefit = ServiceBenefit("", "")
            Timber.e("NO SERVICE BENEFIT -> $noServiceBenefit")
            serviceBenefitJSONArray.put(0, noServiceBenefit)
        }

        serviceBenefits = fromServiceBenefitsJSONArray(serviceBenefitJSONArray)

        serviceLink = if (serviceJSONObject.getString("service_link").isNotEmpty())
            serviceJSONObject.getString("service_link") //17
        else
            ""

        serviceCategory = serviceJSONObject.getString(SERVICE_CATEGORY)

        try {
            serviceAttachmentJSONArray =
                serviceJSONObject.getJSONArray("service_attachment_objects")

            for (x in 0 until serviceDocumentsJSONArray.length()) {
                serviceAttachmentJSONObject = serviceAttachmentJSONArray.getJSONObject(x)
                serviceAttachmentName = serviceAttachmentJSONObject.getString("file_name")
                serviceAttachmentSize = serviceAttachmentJSONObject.getString("file_size")
                serviceAttachmentURL = serviceAttachmentJSONObject.getString("file_uri")
            }

        } catch (jse: JSONException) {
            //TODO: handle missing [serviceAttachment]
            Timber.e("ISSUE PARSING SERVICE ATTACHMENT -> $jse")
        }

        nonDelegatableService = ServiceObject(
            serviceId, serviceName,
            serviceDescription, serviceEligibility, serviceCentres,
            delegatable, serviceCostArrayList, serviceDocuments, serviceDuration,
            approvalCount, disapprovalCount, serviceComments,
            commentCount, shareCount, viewCount, serviceProvider, serviceLink,
            serviceAttachmentName, serviceAttachmentSize, serviceAttachmentURL, serviceCategory,
            serviceBenefits
        )

        Timber.e("NON-DELEGATED---SERVICE -> $nonDelegatableService")

        /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
         * 2. wrapping those values in a JSON Object
         * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
        serviceUpVoteBoolean = nonDelegatedServicePrefs
            .getBoolean("UP-VOTE-${serviceId}", false)

        serviceDownVoteBoolean = nonDelegatedServicePrefs
            .getBoolean("DOWN-VOTE-${serviceId}", false)

        serviceCommentBoolean = nonDelegatedServicePrefs
            .getBoolean("COMMENTED-ON-${serviceId}", false)

        serviceBookmarked = nonDelegatedServicePrefs
            .getBoolean("BOOKMARKED-${serviceId}", false)

        savedUserActions
            .put("UP-VOTE", serviceUpVoteBoolean)
            .put("DOWN-VOTE", serviceDownVoteBoolean)
            .put("COMMENTED-ON", serviceCommentBoolean)
            .put("BOOKMARKED", serviceBookmarked)

        if (isAdded) {
            gAdapter.add(ServiceItem(nonDelegatableService, context, savedUserActions))
            Timber.e("GROUPIE-ADAPTER [2] -> ${gAdapter.itemCount}")
            checkingAdapterState()
        }
    }

    private fun displayServiceByCategory(categoryId: String) {
        gAdapter.clear()
        gAdapter.notifyDataSetChanged()

        var categoryService: JSONObject

        pollingAdapterState()

        for (i in 0 until allServices.length()) {
            categoryService = allServices[i] as JSONObject
            Timber.e("CATEGORY SERVICES -> $categoryService")
            if (categoryService.getString("service_provider") == categoryId) {
                parseSingleService(categoryService)
            }
        }
    }

    /** Function takes a JSON Array and returns a (Array)List<PublicServiceData> **/
    private fun fromJSONArray(array: JSONArray): ArrayList<String> {
        val tmp = ArrayList<String>()
        for (i in 0 until array.length()) {
            tmp.add((array.getString(i)))
        }

        return tmp
    }

    /** Function takes a JSON Array and returns a (Array)List<PublicServiceData> **/
    private fun fromServiceCostJSONArray(array: JSONArray): ArrayList<ServiceCostModel> {
        val tmp = ArrayList<ServiceCostModel>()
        var serviceCostObject: JSONObject
        var spec: String
        var cost: Int
        var serviceCostModel: ServiceCostModel
        for (i in 0 until array.length()) {
            /*serviceCostModel = array.get(i) as ServiceCostModel
            tmp.add(serviceCostModel)*/

            try {
                serviceCostObject = array.getJSONObject(i)
                spec = serviceCostObject.getString("service_spec")
                cost = serviceCostObject.getInt("spec_cost")
                serviceCostModel = ServiceCostModel(spec, cost)
                tmp.add(serviceCostModel)
            } catch (jse: JSONException) {
                Timber.e("CONVERTING SERVICE COST JSE -> $jse")
            }
        }

        Timber.e("SERVICE COST from FUN -> $tmp")
        return tmp
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tfola.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tfola().apply {

            }
    }
}