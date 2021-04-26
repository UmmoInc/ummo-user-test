package xyz.ummo.user.ui.fragments.pagesFrags

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
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
import xyz.ummo.user.api.User.Companion.mode
import xyz.ummo.user.api.User.Companion.ummoUserPreferences
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentTfumaBinding
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.ReloadingServicesEvent
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent


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

    val delegatableServiceJSONObject = JSONObject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        gAdapter = GroupAdapter()

        delegatableServicesArrayList = (serviceViewModel?.getDelegatableServices() as ArrayList<ServiceEntity>?)!!

        delegatedServicePrefs = this.requireActivity()
                .getSharedPreferences(ummoUserPreferences, mode)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        tfumaBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_tfuma,
                container,
                false)

        val view = tfumaBinding.root
        recyclerView = view.tfuma_services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = view.tfuma_services_recycler_view.layoutManager
        recyclerView.adapter = gAdapter
        Timber.e("GROUPIE-ADAPTER [1]-> ${gAdapter.itemCount}")

        pollingAdapterState()
//        getDelegatableServicesLocally()

        getDelegatableServicesFromServer()

        Timber.e("DELEGATED SERVICES LIST [0] -> ${delegatableServicesArrayList.size}")

        reloadServices()

        /** Refreshing services with [tfuma_swipe_refresher] **/
        tfumaBinding.tfumaSwipeRefresher.setOnRefreshListener {
            delegatableServicesArrayList.clear()
            gAdapter.clear()
            getDelegatableServicesFromServer()
            tfumaBinding.tfumaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)
        }

        return view
    }

    @Subscribe
    fun checkSocketConnectionState(socketStateEvent: SocketStateEvent) {
        if (!socketStateEvent.socketConnected!!) {
            loadOfflineServices()
        }
    }

    private fun loadOfflineServices() {
        Timber.e("OFFLINE SERVICES -> $delegatableServicesArrayList")
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offlineLayout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        tfumaBinding.reloadTfumaServicesButton.setOnClickListener {

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
                    showOfflineState()
                } else {
                    hideOfflineState()
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

    private fun showOfflineState() {
        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.offlineLayout.visibility = View.VISIBLE

    }

    private fun hideOfflineState() {
        tfumaBinding.offlineLayout.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.VISIBLE
    }

    private fun getDelegatableServicesFromServer() {
        object : GetAllServices(requireActivity()) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    val allServices = JSONArray(String(data))
                    var service: JSONObject

                    try {
                        for (i in 0 until allServices.length()) {
                            Timber.e("ALL SERVICES [$i]-> ${allServices[i]}")
                            service = allServices[i] as JSONObject
                            delegatable = service.getBoolean("delegatable")

                            if (delegatable) {
                                Timber.e("DELEGATABLE -> $service")

                                serviceId = service.getString("_id") //1
                                serviceName = service.getString("service_name") //2
                                serviceDescription = service.getString("service_description") //3
                                serviceEligibility = service.getString("service_eligibility") //4
//                                serviceCentres //5
                                serviceCentresJSONArray = service.getJSONArray("service_centres")
                                serviceCentres = fromJSONArray(serviceCentresJSONArray)

                                delegatable = service.getBoolean("delegatable") //6
                                serviceCostJSONArray = service.getJSONArray("service_cost")
                                serviceCostArrayList = fromServiceCostJSONArray(serviceCostJSONArray)
//                                serviceCost = service.getString("service_cost") //7
                                Timber.e("SERVICE COST ARRAY LIST -> $serviceCostArrayList")

//                                serviceDocuments = //8
                                serviceDocumentsJSONArray = service.getJSONArray("service_documents")
                                serviceDocuments = fromJSONArray(serviceDocumentsJSONArray)

                                serviceDuration = service.getString("service_duration") //9
                                approvalCount = service.getInt("useful_count") //10
                                disapprovalCount = service.getInt("not_useful_count") //11
//                                serviceComments = s //12
                                serviceCommentsJSONArray = service.getJSONArray("service_comments")
                                serviceComments = fromJSONArray(serviceCommentsJSONArray)

                                commentCount = service.getInt("service_comment_count") //13
                                shareCount = service.getInt("service_share_count") //14
                                viewCount = service.getInt("service_view_count") //15
                                serviceProvider = service.getString("service_provider") //16

                                serviceLink = if (service.getString("service_link").isNotEmpty())
                                    service.getString("service_link") //17
                                else
                                    ""

                                try {
                                    serviceAttachmentJSONArray = service.getJSONArray("service_attachment_objects")

                                    for (x in 0 until serviceAttachmentJSONArray.length()) {
                                        serviceAttachmentJSONObject = serviceAttachmentJSONArray.getJSONObject(x)
                                        serviceAttachmentName = serviceAttachmentJSONObject.getString("file_name")
                                        serviceAttachmentSize = serviceAttachmentJSONObject.getString("file_size")
                                        serviceAttachmentURL = serviceAttachmentJSONObject.getString("file_uri")
                                    }
                                } catch (jse: JSONException) {
                                    Timber.e("ISSUE PARSING SERVICE ATTACHMENT -> $jse")
                                }

                                delegatableService = ServiceObject(serviceId, serviceName,
                                        serviceDescription, serviceEligibility, serviceCentres,
                                        delegatable, serviceCostArrayList, serviceDocuments,
                                        serviceDuration, approvalCount, disapprovalCount,
                                        serviceComments, commentCount, shareCount, viewCount,
                                        serviceProvider, serviceLink, serviceAttachmentName,
                                        serviceAttachmentSize, serviceAttachmentURL)

                                Timber.e("DELEGATED---SERVICE -> $delegatableService")

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
                                    gAdapter.add(ServiceItem(delegatableService, context, savedUserActions))
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
                }
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

    /** Function a JSON Object and returns a PublicServiceData **/
    private fun fromJSONObject(obj: JSONObject): String {
        Timber.e("fromJSONOBJECT-> $obj")

        return obj.toString()
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Tfuma.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                Tfuma().apply {

                }
    }
}