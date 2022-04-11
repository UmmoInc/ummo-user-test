package xyz.ummo.user.ui.fragments.search

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.fragment_all_services.*
import kotlinx.android.synthetic.main.fragment_all_services.view.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.ServicesAdapter
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentAllServicesBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.eventBusEvents.SearchResultsEvent
import xyz.ummo.user.workers.makeStatusNotification

class AllServicesFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var allServiceBinding: FragmentAllServicesBinding
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView

    private lateinit var allServicesAdapter: ServicesAdapter
    private lateinit var serviceObjectArrayList: ArrayList<ServiceObject>
    private lateinit var serviceEntityArrayList: ArrayList<ServiceEntity>

    private lateinit var serviceMini: ServiceObject

    private lateinit var loadServicesProgressBar: ProgressBar
    private lateinit var mixpanel: MixpanelAPI

    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)

    /** Initializing ServiceViewModel **/
    /*private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
        .get(ServiceViewModel::class.java)*/
    private lateinit var allServicesViewModel: AllServicesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceObjectArrayList = ArrayList(listOf<ServiceObject>())
        serviceEntityArrayList = ArrayList(listOf<ServiceEntity>())

        mixpanel = MixpanelAPI.getInstance(
            context,
            resources.getString(R.string.mixpanelToken)
        )
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        allServiceBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_all_services,
            container,
            false
        )

        rootView = allServiceBinding.root

        /** Scaffolding the [recyclerView] **/
        recyclerView = rootView.all_services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = rootView.all_services_recycler_view?.layoutManager

        setupSearchView()

        /** Checking if SearchView is expanded **/
        allServiceBinding.serviceSearchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus || !allServiceBinding.serviceSearchView.isIconified) {
                /** If focused, hide title text **/
                allServiceBinding.allServicesIntroTitleTextView.visibility = View.GONE
            } else if (!hasFocus || allServiceBinding.serviceSearchView.isIconified) {
                allServiceBinding.allServicesIntroTitleTextView.visibility = View.VISIBLE
            }
        }

        /** Reloading all services on either Refresh or "Thank You" events **/
        allServiceBinding.thankYouButton.setOnClickListener {
            reloadAllServices()
        }
        allServiceBinding.missingServiceCapturedImageView.setOnClickListener {
            reloadAllServices()
        }

        allServiceBinding.allServicesSwipeRefresher.setOnRefreshListener {
            reloadAllServices()
            allServiceBinding.allServicesSwipeRefresher.isRefreshing = false
            mixpanel.track("All Services View Refreshed")
        }

        return rootView
    }

    private fun setupRecyclerView() {
        allServicesAdapter = ServicesAdapter()
        all_services_recycler_view.apply {
            adapter = allServicesAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        /** Instantiating [allServicesViewModel] **/
        allServicesViewModel = (activity as MainScreen).allServicesViewModel

        coroutineScope.launch(Dispatchers.IO) {
            allServicesViewModel.getLocallyStoredServices()
        }

        allServicesViewModel.servicesLiveDataList.observe(viewLifecycleOwner) { response ->
            allServicesAdapter.differ.submitList(response)
            Timber.e("ALL SERVICES ADAPTER -> $response")
            showServicesViewHideEverythingElse()
            checkForServices(response)

            coroutineScope.launch(Dispatchers.IO) {
                if (response.isEmpty()) {
                    allServicesViewModel.getAllServicesFromServer()
                    showServicesViewHideEverythingElse()
                    checkForServices(response)
                }
            }
        }
    }

    private fun reloadAllServices() {
        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                showProgressBar()
//                searchServiceAdapter.clearAdapter()

            }

            override fun onFinish() {
                showServicesViewHideEverythingElse()
            }
        }
        timer.start()
    }

    private fun showProgressBar() {
        allServiceBinding.missingServiceCapturedLayout.visibility = View.GONE
        allServiceBinding.loadAllServicesProgressBar.visibility = View.VISIBLE
        allServiceBinding.allServicesSwipeRefresher.visibility = View.GONE
    }

    private fun showServicesViewHideEverythingElse() {
        allServiceBinding.noResultsLayout.visibility = View.GONE
        allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
        allServiceBinding.missingServiceCapturedLayout.visibility = View.GONE
        allServiceBinding.allServicesSwipeRefresher.visibility = View.VISIBLE
        allServiceBinding.allServicesIntroTitleTextView.visibility = View.VISIBLE
        allServiceBinding.serviceSearchView.isIconified = true
        allServiceBinding.serviceSearchView.clearFocus()
    }

    @Subscribe
    fun onSearchResultsEvent(searchResultsEvent: SearchResultsEvent) {

        if (searchResultsEvent.searchResultsFound!!) {
            Timber.e("SEARCH RESULTS FOUND -> ${searchResultsEvent.searchedService}!")

        } else {
            Timber.e("SEARCH RESULTS NOT FOUND -> ${searchResultsEvent.searchedService}!")
            displayNoServicesFound()

            allServiceBinding.letMeKnowButton.setOnClickListener {
                Timber.e("CLICKING BUTTON -> ${searchResultsEvent.searchedService}")
                capturingMissingService(searchResultsEvent.searchedService)
            }
        }
    }

    /** First, we want to capture the value of the missing service and then send it over to
     *  Mixpanel for record keeping. In doing so, our UX needs to make it appealing to the
     *  User. It needs to take care of the following:
     *  1. Inform them that their query's been sent back for review;
     *  2. Let them know that we'll reach out to them as soon as we find more info on their service
     *  3. Tie ourselves to an SLA of no more than 2 days.
     *  Let's begin! **/
    private fun capturingMissingService(missingService: String) {
        val missingServiceObject = JSONObject()
        missingServiceObject.put("Missing Service", missingService)
        mixpanel.track("Service Not Found", missingServiceObject)
        allServiceBinding.noResultsLayout.visibility = View.GONE
        displayMissingServiceCapturedUI()

    }

    private fun displayMissingServiceCapturedUI() {

        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                allServiceBinding.loadAllServicesProgressBar.visibility = View.VISIBLE
            }

            override fun onFinish() {

                //TODO: Improve Coroutine use
                MainScope().launch {
                    withContext(Dispatchers.Default) {
                        Timber.e("BACKGROUND WORK - 1")
                    }
                    allServiceBinding.noResultsLayout.visibility = View.GONE
                    allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
                    allServiceBinding.missingServiceCapturedLayout.visibility = View.VISIBLE
                    makeStatusNotification(
                        "Service Sent",
                        "We'll let you know by SMS when we find your service",
                        context!!
                    )
                }
            }
        }
        timer.start()
    }

    private fun checkForServices(serviceEntityArrayList: ArrayList<ServiceEntity>) {
        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(p0: Long) {
                showProgressBar()
            }

            override fun onFinish() {
                if (serviceEntityArrayList.isEmpty()) {
                    displayNoServicesFound()
                } else {
                    showServicesViewHideEverythingElse()
                }
            }
        }

        timer.start()
    }

    /** This function displays a loader for 2 seconds && displays that no service was found from
     *  the search **/
    private fun displayNoServicesFound() {
        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                allServiceBinding.allServicesSwipeRefresher.visibility = View.GONE
                allServiceBinding.loadAllServicesProgressBar.visibility = View.VISIBLE
            }

            override fun onFinish() {
                allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
                allServiceBinding.allServicesSwipeRefresher.visibility = View.GONE

                allServiceBinding.noResultsLayout.visibility = View.VISIBLE
            }
        }
        timer.start()
    }

    /*override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)

        val search: MenuItem = menu.findItem(R.id.service_search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search here... "
        searchView.isSubmitButtonEnabled = true

        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu, inflater)
    }*/

    private fun setupSearchView() {
        allServiceBinding.serviceSearchView.setOnQueryTextListener(this)
        Timber.e("Search View Setup")
    }

    companion object {

        private val parentJob = Job()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AllServicesFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            coroutineScope.launch(Dispatchers.IO) {
                searchServiceArrayList(query)
            }
        }
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {

        MainScope().launch {
            delay(1000L)
            query?.let {
                if (query.isNotEmpty()) {
                    searchServiceArrayList(query)
                }
            }
        }
        /*if (query != null) {

            searchServiceArrayList(query)
        }*/
        return false
    }

    private fun searchServiceArrayList(searchQueryString: String?) {
        if (searchQueryString!!.isNotEmpty()) {
            allServicesViewModel.searchForServices(searchQueryString)
            allServicesViewModel.searchedServicesLiveDataList
                .observe(viewLifecycleOwner) { searchResults ->
                    Timber.e("SEARCHING FOR -> $searchResults")
                    allServicesAdapter.differ.submitList(searchResults)
                }
        }
        /** 1. With the ViewModel, search database and observe the liveData
         *  2. Using the liveData, populate the adapter **/

        /** viewModel.searchDatabase(query).observe(this, { list -> list.let {gAdapter.setData(it)}}) **/
    }
}