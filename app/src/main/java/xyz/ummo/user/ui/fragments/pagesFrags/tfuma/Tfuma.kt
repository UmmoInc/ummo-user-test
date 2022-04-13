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
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_tfuma.view.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentTfumaBinding
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

class Tfuma : Fragment() {
    private lateinit var tfumaBinding: FragmentTfumaBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    private lateinit var delegatableServicesArrayList: ArrayList<ServiceEntity>
    private lateinit var delegatableService: ServiceEntity
    private lateinit var delegatedServicePrefs: SharedPreferences
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()
    private var reloadingServicesEvent = ReloadingServicesEvent()

    lateinit var category: String
    var countOfServices: Int = 0
    lateinit var mixpanelAPI: MixpanelAPI

    private var tfumaViewModel: TfumaViewModel? = null
    private lateinit var delegatableServicesViewModel: AllServicesViewModel

    /** SharedPref Editor **/
    private lateinit var editor: SharedPreferences.Editor

    /** JSON Value for Mixpanel **/
    private var categoryJSONObject: JSONObject = JSONObject()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)

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
//        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = gAdapter

        category = parentFragment?.arguments?.getString(SERVICE_CATEGORY).toString()

        mixpanelAPI = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        reloadServices()

        returnHome()

        /** Refreshing services with [tfuma_swipe_refresher] **/
        tfumaBinding.tfumaSwipeRefresher.setOnRefreshListener {
            gAdapter.clear()
            getAllServicesFromRoomAndDisplay()
            tfumaBinding.tfumaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)
            mixpanelAPI.track("delegateFragment_manuallyRefreshed")
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Instantiating [delegatableServicesViewModel] **/
        delegatableServicesViewModel = (activity as MainScreen).allServicesViewModel

        showProgressBar()
        /** Within this [coroutineScope], we're fetching locally stored services **/
        coroutineScope.launch(Dispatchers.IO) {
            delegatableServicesViewModel.getLocallyStoredServices()
        }

        getAllServicesFromRoomAndDisplay()
    }

    private fun getAllServicesFromRoomAndDisplay() {
        delegatableServicesViewModel.servicesLiveDataList
            .observe(viewLifecycleOwner) { delegatableServices ->
                Timber.e("SERVICES RETURNED FROM OBSERVER -> $delegatableServices")
                checkForServices(delegatableServices)

                coroutineScope.launch(Dispatchers.IO) {
                    if (delegatableServices.isEmpty()) {
                        delegatableServicesViewModel.getAllServicesFromServer()
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
        val delegatableServicesInCat = ArrayList<ServiceEntity>()

        for (service in allServices) {
            if (service.delegatable == true && service.serviceCategory == category) {
                delegatableServicesInCat.add(service)
            }
        }
        /** Filtering through delegatableServices for categorical matching **/

        if (delegatableServicesInCat.isNotEmpty()) {
            showServicesLayout()

            for (delegatableServiceInCat in delegatableServicesInCat) {
                getSavedUserActionsFromSharedPrefs(delegatableServiceInCat.serviceId)
                gAdapter.add(ServiceItem(delegatableServiceInCat, context, savedUserActions))
                Timber.e("SERVICES IN CAT -> $delegatableServiceInCat")
            }
        } else {
            Timber.e("NO SERVICES IN CAT")
            showNoServicesInCategory()
        }
    }

    private fun showProgressBar() {
        tfumaBinding.loadProgressBar.visibility = View.VISIBLE
        tfumaBinding.noServicesRelativeLayout.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
        tfumaBinding.offlineLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        tfumaBinding.loadProgressBar.visibility = View.GONE
    }

    private fun showServicesLayout() {
        hideProgressBar()
        tfumaBinding.noServicesRelativeLayout.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.VISIBLE
        tfumaBinding.offlineLayout.visibility = View.GONE
    }

    private fun showNoServicesInCategory() {
        tfumaBinding.noServicesRelativeLayout.visibility = View.VISIBLE
        tfumaBinding.offlineLayout.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
        hideProgressBar()

        mixpanelAPI.track("Delegate Fragment - No Services In Category")
    }

    @Subscribe
    fun onSocketStateEvent(socketStateEvent: SocketStateEvent) {
        if (!socketStateEvent.socketConnected!!) {
            Timber.e("SOCKET CONNECTION LOST")
        } else {
            Timber.e("SOCKET CONNECTED")

        }
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offline_layout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        tfumaBinding.reloadTfumaServicesButton.setOnClickListener {

//            pollingAdapterState()

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

    private fun getSavedUserActionsFromSharedPrefs(serviceID: String) {
        /** 1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
         *  2. wrapping those values in a JSON Object
         *  3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
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
        private val parentJob = Job()

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Tfuma().apply {

            }
    }
}