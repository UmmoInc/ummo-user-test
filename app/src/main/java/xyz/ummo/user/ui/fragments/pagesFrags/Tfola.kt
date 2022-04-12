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
import com.google.android.material.snackbar.Snackbar
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_tfola.view.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentTfolaBinding
import xyz.ummo.user.databinding.ServiceFilterChipLayoutBinding
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.fragments.categories.ServiceCategories
import xyz.ummo.user.ui.fragments.search.AllServicesViewModel
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.SERVICE_CATEGORY
import xyz.ummo.user.utilities.eventBusEvents.ReloadingServicesEvent
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences

class Tfola : Fragment() {
    private lateinit var nonDelegatableServicesViewModel: AllServicesViewModel
    private lateinit var allServices: JSONArray

    private var serviceViewModel: ServiceViewModel? = null
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()
    private var reloadingServicesEvent = ReloadingServicesEvent()
    private lateinit var nonDelegatedServicePrefs: SharedPreferences
    private lateinit var nonDelegatableServicesArrayList: ArrayList<ServiceEntity>
    private lateinit var tfolaBinding: FragmentTfolaBinding
    private lateinit var serviceFilterChipBinding: ServiceFilterChipLayoutBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    lateinit var category: String

    lateinit var mixpanelAPI: MixpanelAPI

    /** SharedPref Editor **/
    private lateinit var editor: SharedPreferences.Editor

    /** JSON Value for Mixpanel **/
    private var categoryJSONObject: JSONObject = JSONObject()

    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)

    override fun onStart() {
        super.onStart()
        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()
        mixpanelAPI.timeEvent("Viewing TFOLA")
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()
        mixpanelAPI.track("Viewing TFOLA")
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceViewModel = ViewModelProvider(this)
            .get(ServiceViewModel::class.java)

        gAdapter = GroupAdapter()

        nonDelegatedServicePrefs = this.requireActivity()
            .getSharedPreferences(ummoUserPreferences, mode)
        /** Instantiating [nonDelegatableServicesArrayList]; filling it with data from
         * [serviceViewModel]'s DelegatableServices **/
        nonDelegatableServicesArrayList = (serviceViewModel?.getNonDelegatableServices()
                as ArrayList<ServiceEntity>?)!!

        /** Initing Shared Pref. Editor **/
        editor = nonDelegatedServicePrefs.edit()

        categoryJSONObject.put(
            "CATEGORY",
            parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()
        )

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

        reloadServices()

        returnHome()

        mixpanelAPI = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()

        /** Refreshing services with [tfola_swipe_refresher] **/
        tfolaBinding.tfolaSwipeRefresher.setOnRefreshListener {
            gAdapter.clear()
            getAllServicesFromRoomAndDisplay()
            tfolaBinding.tfolaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)
            mixpanelAPI.track("discoverFragment_manuallyRefreshed")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Instantiating [nonDelegatableServicesViewModel] **/
        nonDelegatableServicesViewModel = (activity as MainScreen).allServicesViewModel

        showProgressBar()
        /** Within this [coroutineScope], we're fetching locally stored services **/
        coroutineScope.launch(Dispatchers.IO) {
            nonDelegatableServicesViewModel.getLocallyStoredServices()
        }

        getAllServicesFromRoomAndDisplay()
    }

    private fun getAllServicesFromRoomAndDisplay() {
        nonDelegatableServicesViewModel.servicesLiveDataList
            .observe(viewLifecycleOwner) { nonDelegatableServices ->
                Timber.e("SERVICES RETURNED FROM OBSERVER -> $nonDelegatableServices")
                checkForServices(nonDelegatableServices)

                coroutineScope.launch(Dispatchers.IO) {
                    if (nonDelegatableServices.isEmpty()) {
                        nonDelegatableServicesViewModel.getAllServicesFromServer()
                    }
                }
            }
    }

    private fun checkForServices(serviceEntityArrayList: ArrayList<ServiceEntity>) {
        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                showProgressBar()
            }

            override fun onFinish() {
                hideProgressBar()
                if (serviceEntityArrayList.isNotEmpty()) {
                    processAndDisplayDelegatableServices(serviceEntityArrayList)
                }
            }
        }

        timer.start()
    }

    private fun processAndDisplayDelegatableServices(allServices: ArrayList<ServiceEntity>) {
        val nonDelegatableServicesInCat = ArrayList<ServiceEntity>()

        for (service in allServices) {
            if (service.delegatable == false && service.serviceCategory == category) {
                nonDelegatableServicesInCat.add(service)
            }
        }
        /** Filtering through delegatableServices for categorical matching **/

        if (nonDelegatableServicesInCat.isNotEmpty()) {
            showServicesLayout()

            for (nonDelegatableServiceInCat in nonDelegatableServicesInCat) {
                getSavedUserActionsFromSharedPrefs(nonDelegatableServiceInCat.serviceId)
                gAdapter.add(ServiceItem(nonDelegatableServiceInCat, context, savedUserActions))
                Timber.e("SERVICES IN CAT -> $nonDelegatableServiceInCat")
            }
        } else {
            Timber.e("NO SERVICES IN CAT")
            showNoServicesInCategory()
        }
    }

    private fun showServicesLayout() {
        hideProgressBar()
        tfolaBinding.noServicesRelativeLayout.visibility = View.GONE
        tfolaBinding.tfolaSwipeRefresher.visibility = View.VISIBLE
        tfolaBinding.offlineLayout.visibility = View.GONE
    }

    private fun showNoServicesInCategory() {
        tfolaBinding.noServicesRelativeLayout.visibility = View.VISIBLE
        tfolaBinding.offlineLayout.visibility = View.GONE
        tfolaBinding.tfolaServicesRecyclerView.visibility = View.GONE
        tfolaBinding.loadProgressBar.visibility = View.GONE

        if (isAdded) {
            val mixpanel = MixpanelAPI.getInstance(
                requireContext(),
                resources.getString(R.string.mixpanelToken)
            )
            mixpanel?.track("Discover Fragment - No Services In Category")
        }
    }

    /*private fun filterServicesByCategory() {

        tfolaBinding.serviceCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
            Timber.e("CHECKED GROUP -> $group")
            Timber.e("CHECKED CHIP -> $checkedId")

            val titleOrNull = group.findViewById<Chip>(checkedId)?.text
            Timber.e("CHECKED CHIP ID ->>> $titleOrNull")

            when (titleOrNull) {

                "All Services" -> {
                    Timber.e("CHECKED ALL")
                    Timber.e("SERVICE PROVIDER -> $serviceProviderList")
//                    getNonDelegatableServicesFromServer()

                }
                "Home Affairs" -> {
                    Timber.e("CHECKED HOME AFFAIRS")
                    displayServiceByCategory("601268725ad77100154da834")
                    mixpanelAPI.track("discoverFragment_homeAffairs_selected")
                    loadingCategoryServicesEvent.categoryLoading = "Home-Affairs"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                }
                "Commerce" -> {
                    Timber.e("CHECKED COMMERCE")
                    displayServiceByCategory("601266be5ad77100154da833")
                    mixpanelAPI.track("discoverFragment_commerce_selected")
                    loadingCategoryServicesEvent.categoryLoading = "Commerce"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                }
                "Revenue" -> {
                    Timber.e("CHECKED REVENUE")
                    displayServiceByCategory("601268ff5ad77100154da835")
                    mixpanelAPI.track("discoverFragment_revenue_selected")
                    loadingCategoryServicesEvent.categoryLoading = "Revenue"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                }
            }
        }

        Timber.e("CHIP GROUP -> ${tfolaBinding.serviceCategoryChipGroup.checkedChipId}")
        Timber.e("CHIP GROUP -> ${tfolaBinding.serviceCategoryChipGroup}")
    }*/

    private fun showProgressBar() {
        tfolaBinding.loadProgressBar.visibility = View.VISIBLE
        tfolaBinding.noServicesRelativeLayout.visibility = View.GONE
        tfolaBinding.tfolaSwipeRefresher.visibility = View.GONE
        tfolaBinding.offlineLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        tfolaBinding.loadProgressBar.visibility = View.GONE
    }

    @Subscribe
    fun onSocketStateEvent(socketStateEvent: SocketStateEvent) {
        if (!socketStateEvent.socketConnected!!) {
            Timber.e("SOCKET DISCONNECTED")
        } else {
            Timber.e("SOCKET RECONNECTED")
        }
    }

    private fun getSavedUserActionsFromSharedPrefs(serviceID: String) {
        /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
         * 2. wrapping those values in a JSON Object
         * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
        serviceUpVoteBoolean = nonDelegatedServicePrefs
            .getBoolean("UP-VOTE-${serviceID}", false)

        serviceDownVoteBoolean = nonDelegatedServicePrefs
            .getBoolean("DOWN-VOTE-${serviceID}", false)

        serviceCommentBoolean = nonDelegatedServicePrefs
            .getBoolean("COMMENTED-ON-${serviceID}", false)

        serviceBookmarked = nonDelegatedServicePrefs
            .getBoolean("BOOKMARKED-${serviceID}", false)

        savedUserActions
            .put("UP-VOTE", serviceUpVoteBoolean)
            .put("DOWN-VOTE", serviceDownVoteBoolean)
            .put("COMMENTED-ON", serviceCommentBoolean)
            .put("BOOKMARKED", serviceBookmarked)
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offline_layout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        tfolaBinding.reloadTfolaServicesButton.setOnClickListener {

            /** Posting this EventBus in order to display the Snackbar **/
            reloadingServicesEvent.reloadingServices = true
            EventBus.getDefault().post(reloadingServicesEvent)
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

    private fun displayServiceByCategory(categoryId: String) {
        gAdapter.clear()
        gAdapter.notifyDataSetChanged()

        var categoryService: JSONObject

        for (i in 0 until allServices.length()) {
            categoryService = allServices[i] as JSONObject
            Timber.e("CATEGORY SERVICES -> $categoryService")
            if (categoryService.getString("service_provider") == categoryId) {
//                parseSingleService(categoryService)
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
        private val parentJob = Job()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tfola().apply {

            }
    }
}