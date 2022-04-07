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
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.fragment_all_services.view.*
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.SearchServicesAdapter
import xyz.ummo.user.api.GetAllServices
import xyz.ummo.user.api.Service
import xyz.ummo.user.databinding.FragmentAllServicesBinding
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.SearchResultsEvent
import xyz.ummo.user.utilities.serviceutils.SaveServiceLocally
import xyz.ummo.user.workers.fromJSONArray
import xyz.ummo.user.workers.fromServiceBenefitsJSONArray
import xyz.ummo.user.workers.fromServiceCostJSONArray
import xyz.ummo.user.workers.makeStatusNotification


class AllServices : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var allServiceBinding: FragmentAllServicesBinding
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView

    //    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var searchServiceAdapter: SearchServicesAdapter
    private lateinit var serviceArrayList: ArrayList<ServiceObject>

    private lateinit var serviceMini: ServiceObject
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

    private lateinit var loadServicesProgressBar: ProgressBar
    private lateinit var mixpanel: MixpanelAPI

    /** Initializing ServiceViewModel **/
    /*private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
        .get(ServiceViewModel::class.java)*/
    private lateinit var allServicesViewModel: AllServicesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceArrayList = ArrayList(listOf<ServiceObject>())

        getAllServices()

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

        /** Instantiating [allServicesViewModel] **/
        allServicesViewModel = (activity as MainScreen).allServicesViewModel

        rootView = allServiceBinding.root

        /** Scaffolding the [recyclerView] **/
        recyclerView = rootView.all_services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = rootView.all_services_recycler_view?.layoutManager

        setupSearchView()

        getAllServices()

        checkForServices()

        /** Checking if SearchView is expanded **/
        allServiceBinding.serviceSearchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus || !allServiceBinding.serviceSearchView.isIconified) {
                /** If focused, hide title text **/
                allServiceBinding.allServicesIntroTitleTextView.visibility = View.GONE
            } else if (!hasFocus || allServiceBinding.serviceSearchView.isIconified) {
                allServiceBinding.allServicesIntroTitleTextView.visibility = View.VISIBLE
            }
        }

        /** Reloading all services on either Refresh or Thank You events **/
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

    private fun reloadAllServices() {
        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                allServiceBinding.missingServiceCapturedLayout.visibility = View.GONE
                allServiceBinding.loadAllServicesProgressBar.visibility = View.VISIBLE
                allServiceBinding.allServicesSwipeRefresher.visibility = View.GONE
                searchServiceAdapter.clearAdapter()
            }

            override fun onFinish() {
                MainScope().launch {
                    withContext(Dispatchers.Default) {

                    }
                    allServiceBinding.noResultsLayout.visibility = View.GONE
                    allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
                    allServiceBinding.missingServiceCapturedLayout.visibility = View.GONE
                    allServiceBinding.allServicesSwipeRefresher.visibility = View.VISIBLE
                    allServiceBinding.allServicesIntroTitleTextView.visibility = View.VISIBLE
                    allServiceBinding.serviceSearchView.isIconified = true
                    allServiceBinding.serviceSearchView.clearFocus()
                    getAllServices()
                }
            }
        }
        timer.start()
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

    private fun checkForServices() {
        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                if (serviceArrayList.isEmpty()) {
                    displayNoServicesFound()
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

    private fun getAllServices() {
        object : GetAllServices(requireActivity()) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    val allServices = JSONObject(String(data)).getJSONArray("payload")
                    var service: JSONObject

                    try {
                        for (i in 0 until allServices.length()) {
                            service = allServices[i] as JSONObject

                            /** Saving services locally **/
                            val saveServiceLocally = SaveServiceLocally(service, context!!)
                            saveServiceLocally.savingService()

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

                            serviceLink = service.getString(SERV_LINK).ifEmpty { "" }

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

                            serviceMini = ServiceObject(
                                serviceId, serviceName,
                                serviceDescription, serviceEligibility, serviceCentres,
                                delegatable, serviceCostArrayList, serviceDocuments,
                                serviceDuration, approvalCount, disapprovalCount,
                                serviceComments, commentCount, shareCount, viewCount,
                                serviceProvider, serviceLink, serviceAttachmentName,
                                serviceAttachmentSize, serviceAttachmentURL, serviceCategory,
                                serviceBenefits
                            )

                            serviceArrayList.add(i, serviceMini)

//                            Timber.e("SERVICE Array List -> $serviceArrayList")
//                            Timber.e("MINI SERVICE -> $serviceName")
                            if (isAdded) {
                                allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
                                /*gAdapter.add(ServiceSliceItem(serviceMini, context))*/
                                searchServiceAdapter = SearchServicesAdapter(serviceArrayList)
                                recyclerView.adapter = searchServiceAdapter
                            }
                        }
                    } catch (jse: JSONException) {
                        Timber.e("FAILED TO PARSE DELEGATABLE SERVICES -> $jse")
                    }
                }
            }
        }
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AllServices().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
//            searchServiceArrayList(query)
        }
        return false
    }

    override fun onQueryTextChange(query: String?): Boolean {

        var job: Job? = null

        job?.cancel()
        job = MainScope().launch {
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


    private fun searchServiceArrayList(query: String?) {
        val searchQuery = "%$query%"
        searchServiceAdapter.filter.filter(query)
        /** 1. With the ViewModel, search database and observe the liveData
         *  2. Using the liveData, populate the adapter **/

        /** viewModel.searchDatabase(query).observe(this, { list -> list.let {gAdapter.setData(it)}}) **/
    }
}