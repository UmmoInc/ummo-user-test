package xyz.ummo.user.ui.fragments.search

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
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
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.LoadingCategoryServicesEvent
import xyz.ummo.user.utilities.eventBusEvents.SearchResultsEvent


class AllServicesFragment : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var allServiceBinding: FragmentAllServicesBinding
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView

    private lateinit var allServicesAdapter: ServicesAdapter
    private lateinit var serviceObjectArrayList: ArrayList<ServiceObject>
    private lateinit var serviceEntityArrayList: ArrayList<ServiceEntity>
    private var loadingCategoryServicesEvent = LoadingCategoryServicesEvent()

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

        /** Checking if SearchView is expanded to decide how to handle its appearance **/
        allServiceBinding.serviceSearchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus || !allServiceBinding.serviceSearchView.isIconified) {
                /** If focused, hide title text **/
                allServiceBinding.allServicesIntroTitleTextView.visibility = View.GONE
            } else if (!hasFocus || allServiceBinding.serviceSearchView.isIconified) {
                /** Otherwise, leave it as is **/
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

        /** Within this [coroutineScope], we're fetching locally stored services **/
        coroutineScope.launch(Dispatchers.IO) {
            allServicesViewModel.getLocallyStoredServices()
        }

        getAllServicesFromRoomAndDisplay()

        filterServicesByCategory()
    }

    /** 1. In getting all services from Room, we're actually observing the [allServicesViewModel]'s
     *    livedata returned from [AllServicesViewModel.getLocallyStoredServices] suspend function;
     *    this function runs a call on AllServicesRepository to getLocallyStoredServices for us.
     *
     *   2. With that result, we pass it to our diffUtil operation in [allServicesAdapter] whose
     *     callback function compares if the adapter elements or contents are similar;
     *     this is useful in ensuring that we have a clean list of info that doesn't repeat.
     *
     *   3. We then [checkForServices] and follow its UX functionality, accordingly.
     *
     *   4. But because the contents of the livedata may be empty, we then run another
     *      [coroutineScope] on an IO dispatcher (b/c we're reading from the network) to actually
     *      fetch service data from the server and store them in Room, before attempting to display
     *      them again. **/
    private fun getAllServicesFromRoomAndDisplay() {
        allServicesViewModel.servicesLiveDataList.observe(viewLifecycleOwner) { response ->
            allServicesAdapter.differ.submitList(response)
            showServicesViewAndHideEverythingElse()
            checkForServices(response)

            if (isAdded) {

                coroutineScope.launch(Dispatchers.IO) {
                    if (response.isEmpty()) {
                        allServicesViewModel.getAllServicesFromServer()

                        requireActivity().runOnUiThread {
                            showServicesViewAndHideEverythingElse()
                        }

                        Handler(Looper.getMainLooper()).post {
                            checkForServices(response)
                        }

                    }
                }
            }
        }
    }

    /** This will run everytime the UI is refreshed or when the User doesn't find a service &
     * has to reload the UI accordingly. **/
    private fun reloadAllServices() {
        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                showProgressBar()
            }

            override fun onFinish() {
                getAllServicesFromRoomAndDisplay()
            }
        }
        timer.start()
    }

    private fun showProgressBar() {
        allServiceBinding.missingServiceCapturedLayout.visibility = View.GONE
        allServiceBinding.loadAllServicesProgressBar.visibility = View.VISIBLE
        allServiceBinding.allServicesSwipeRefresher.visibility = View.GONE
    }

    private fun showServicesViewAndHideEverythingElse() {
        allServiceBinding.noResultsLayout.visibility = View.GONE
        allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
        allServiceBinding.missingServiceCapturedLayout.visibility = View.GONE
        allServiceBinding.allServicesSwipeRefresher.visibility = View.VISIBLE
        allServiceBinding.allServicesIntroTitleTextView.visibility = View.VISIBLE
        allServiceBinding.serviceSearchView.isIconified = true
        allServiceBinding.serviceSearchView.clearFocus()
    }


    private fun justShowAllServicesReturnedFromAdapter() {
        allServiceBinding.noResultsLayout.visibility = View.GONE
        allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
        allServiceBinding.missingServiceCapturedLayout.visibility = View.GONE
        allServiceBinding.allServicesSwipeRefresher.visibility = View.VISIBLE
    }

    @Subscribe
    fun onSearchResultsEvent(searchResultsEvent: SearchResultsEvent) {

        if (searchResultsEvent.searchResultsFound!!) {
            Timber.e("SEARCH RESULTS FOUND -> ${searchResultsEvent.searchedService}!")

        } else {
            Timber.e("SEARCH RESULTS NOT FOUND -> ${searchResultsEvent.searchedService}!")
            displayNoServicesFound()

            letMeKnowClicker(searchResultsEvent.searchedService)
        }
    }

    private fun letMeKnowClicker(serviceString: String) {
        allServiceBinding.letMeKnowButton.setOnClickListener {
            capturingMissingService(serviceString)
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

        val timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(p0: Long) {
                allServiceBinding.loadAllServicesProgressBar.visibility = View.VISIBLE
            }

            override fun onFinish() {

                coroutineScope.launch(Dispatchers.Main) {
                    allServiceBinding.noResultsLayout.visibility = View.GONE
                    allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
                    allServiceBinding.allServicesSwipeRefresher.visibility = View.GONE
                    allServiceBinding.missingServiceCapturedLayout.visibility = View.VISIBLE
                }
            }
        }
        timer.start()
    }

    /** This function is meant to give the "We're working on it, quickly!" UX. We show the progress
     *  bar for 2 seconds and then check if the [serviceEntityArrayList] is empty or not.
     *  If it IS empty, we [displayNoServicesFound] to the User;
     *  Else, we [showServicesViewAndHideEverythingElse] **/
    private fun checkForServices(serviceEntityArrayList: ArrayList<ServiceEntity>) {

        val timer = object : CountDownTimer(2000, 1000) {

            override fun onTick(p0: Long) {
                showProgressBar()
            }

            override fun onFinish() {
                if (serviceEntityArrayList.isEmpty()) {
                    displayNoServicesFound()
                    letMeKnowClicker("")
                } else {
                    showServicesViewAndHideEverythingElse()
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
        MainScope().launch {
            delay(1000L)
            query?.let {
                if (query.isNotEmpty()) {
                    searchServiceArrayList(query)
                } else
                    getAllServicesFromRoomAndDisplay()
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
                } else
                    getAllServicesFromRoomAndDisplay()
            }
        }
        return false
    }

    private fun filterServicesByCategory() {

        allServiceBinding.serviceCategoryChipGroup.setOnCheckedChangeListener { group, checkedId ->
            Timber.e("CHECKED GROUP -> $group")
            Timber.e("CHECKED CHIP -> $checkedId")

            val titleOrNull = group.findViewById<Chip>(checkedId)?.text
            Timber.e("CHECKED CHIP ID ->>> $titleOrNull")

            when (titleOrNull) {

                "All Services" -> {
                    mixpanel.track("All Services Filtered - All Services")
                    getAllServicesFromRoomAndDisplay()
                    allServiceBinding.serviceSearchView.isIconified
                }
                "Home Affairs" -> {
                    searchServiceArrayList(HOME_AFFAIRS)
                    mixpanel.track("All Services Filtered - Home-Affairs")
                    loadingCategoryServicesEvent.categoryLoading = "Home-Affairs"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                    allServiceBinding.serviceSearchView.isIconified
                }
                "Commerce" -> {
                    searchServiceArrayList(COMMERCE)
                    mixpanel.track("All Services Filtered - Commerce")
                    loadingCategoryServicesEvent.categoryLoading = "Commerce"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                    allServiceBinding.serviceSearchView.isIconified
                }
                "Revenue" -> {
                    searchServiceArrayList(REVENUE)
                    mixpanel.track("All Services Filtered - Revenue")
                    loadingCategoryServicesEvent.categoryLoading = "Revenue"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                    allServiceBinding.serviceSearchView.isIconified
                }
                "Agriculture" -> {
                    searchServiceArrayList(AGRICULTURE)
                    mixpanel.track("All Services Filtered - Agriculture")
                    loadingCategoryServicesEvent.categoryLoading = "Agriculture"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                    allServiceBinding.serviceSearchView.isIconified
                }
                "Health" -> {
                    searchServiceArrayList(HEALTH)
                    mixpanel.track("All Services Filtered - Health")
                    loadingCategoryServicesEvent.categoryLoading = "Health"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                    allServiceBinding.serviceSearchView.isIconified
                }
                "Education" -> {
                    searchServiceArrayList(EDUCATION)
                    mixpanel.track("All Services Filtered - Home-Affairs")
                    loadingCategoryServicesEvent.categoryLoading = "Education"
                    loadingCategoryServicesEvent.loadingService = true
                    EventBus.getDefault().post(loadingCategoryServicesEvent)
                    allServiceBinding.serviceSearchView.isIconified
                }
            }
        }
    }

    /** This is our MVP function!
     *  It searches for services represented by [searchQueryString] using
     *  [AllServicesViewModel.searchedServicesLiveDataList].
     *  We observe for any response and alter the UI accordingly.**/
    private fun searchServiceArrayList(searchQueryString: String?) {
        if (searchQueryString!!.isNotEmpty()) {
            allServicesViewModel.searchForServices(searchQueryString)
            allServicesViewModel.searchedServicesLiveDataList
                .observe(viewLifecycleOwner) { searchResults ->
                    Timber.e("SEARCHING FOR -> $searchResults")

                    if (searchResults.isNullOrEmpty()) {
                        displayNoServicesFound()
                        letMeKnowClicker(searchQueryString)
                    } else {
                        allServicesAdapter.differ.submitList(searchResults)
                        justShowAllServicesReturnedFromAdapter()
                    }
                }
        }
    }
}