package xyz.ummo.user.ui.fragments.search

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_all_services.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.GetAllServices
import xyz.ummo.user.api.Service
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentAllServicesBinding
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.rvItems.ServiceSliceItem
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.workers.fromJSONArray
import xyz.ummo.user.workers.fromServiceBenefitsJSONArray
import xyz.ummo.user.workers.fromServiceCostJSONArray

class AllServices : Fragment(), SearchView.OnQueryTextListener {
    private lateinit var allServiceBinding: FragmentAllServicesBinding
    private lateinit var rootView: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var serviceArrayList: ArrayList<ServiceEntity>

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

    /** Initializing ServiceViewModel **/
    private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
        .get(ServiceViewModel::class.java)
    lateinit var allServicesViewModel: AllServicesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gAdapter = GroupAdapter()
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
        recyclerView.adapter = gAdapter

        getAllServices()

        return rootView
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

                            Timber.e("MINI SERVICE -> $serviceName")
                            if (isAdded) {
                                allServiceBinding.loadAllServicesProgressBar.visibility = View.GONE
                                gAdapter.add(ServiceSliceItem(serviceMini, context))

                            }
                        }
                    } catch (jse: JSONException) {
                        Timber.e("FAILED TO PARSE DELEGATABLE SERVICES -> $jse")
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.top_app_bar, menu)

        val search: MenuItem = menu.findItem(R.id.service_search)
        val searchView = search.actionView as SearchView
        searchView.queryHint = "Search here... "
        searchView.isSubmitButtonEnabled = true

        searchView.setOnQueryTextListener(this)
        return super.onCreateOptionsMenu(menu, inflater)
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
            searchDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchDatabase(query)
        }
        return true
    }

    private fun searchDatabase(query: String?) {
        val searchQuery = "%$query%"
        serviceViewModel.searchServiceDatabase(query!!)

        Timber.e("SEARCH QUERY -> $query")

        /** 1. With the ViewModel, search database and observe the liveData
         *  2. Using the liveData, populate the adapter **/

        /** viewModel.searchDatabase(query).observe(this, { list -> list.let {gAdapter.setData(it)}}) **/
    }
}