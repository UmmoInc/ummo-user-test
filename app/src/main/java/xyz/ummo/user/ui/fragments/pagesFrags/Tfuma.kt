package xyz.ummo.user.ui.fragments.pagesFrags

import android.content.SharedPreferences
import android.os.Bundle
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.User.Companion.mode
import xyz.ummo.user.api.User.Companion.ummoUserPreferences
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentTfumaBinding
import xyz.ummo.user.models.Service
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.ReloadingServicesEvent

class Tfuma : Fragment() {
    private lateinit var tfumaBinding: FragmentTfumaBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    //    private lateinit var delegatableServicesArrayList: List<ServiceEntity>
    private lateinit var delegatableServicesArrayList: ArrayList<ServiceEntity>
    private lateinit var delegatedService: Service
    private lateinit var delegatedServicePrefs: SharedPreferences
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()
    private var reloadingServicesEvent = ReloadingServicesEvent()

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

        getDelegatableServices()

        Timber.e("DELEGATED SERVICES LIST [0] -> ${delegatableServicesArrayList.size}")

        reloadServices()

        /** Refreshing services with [tfuma_swipe_refresher] **/
        tfumaBinding.tfumaSwipeRefresher.setOnRefreshListener {
            delegatableServicesArrayList.clear()
            getDelegatableServices()
            tfumaBinding.tfumaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)
        }

        return view
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offlineLayout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        if (delegatableServicesArrayList.isEmpty()) {
            showOfflineView()
            tfumaBinding.reloadTfumaServicesButton.setOnClickListener {

                GlobalScope.launch {
                    /** Running this process on UI thread because the coroutine cannot "touch" the
                     * UI & we're trying to show the loader **/
                    requireActivity().runOnUiThread {
                        tfumaBinding.loadProgressBar.visibility = View.VISIBLE
                        tfumaBinding.offlineLayout.visibility = View.GONE
                        tfumaBinding.tfumaSwipeRefresher.visibility = View.GONE
                    }

                    /** Posting this EventBus in order to display the Snackbar **/
                    reloadingServicesEvent.reloadingServices = true
                    EventBus.getDefault().post(reloadingServicesEvent)

                    /** Delaying for 3 seconds for UX-sake **/
                    delay(3000L)

                    /** Running this process on UI thread again for the same reason as above **/
                    requireActivity().runOnUiThread {
                        getDelegatableServices()
                    }
                    Timber.e("RELOADING SERVICES")
                }
            }
        } else {
            hideOfflineView()
        }
    }

    private fun showOfflineView() {
        tfumaBinding.offlineLayout.visibility = View.VISIBLE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.INVISIBLE
    }

    private fun hideOfflineView() {
        tfumaBinding.offlineLayout.visibility = View.GONE
        tfumaBinding.loadProgressBar.visibility = View.GONE
        tfumaBinding.tfumaSwipeRefresher.visibility = View.VISIBLE
    }

    private fun getDelegatableServices() {
        var serviceId: String
        var serviceName: String
        var serviceDescription: String
        var serviceEligibility: String
        var serviceCentres: ArrayList<String>
        var delegatable: Boolean
        var serviceCost: String
        var serviceDocuments: ArrayList<String>
        var serviceDuration: String
        var approvalCount: Int
        var disapprovalCount: Int
        var serviceComments: ArrayList<String>
        var commentCount: Int
        var shareCount: Int
        var viewCount: Int
        var serviceProvider: String

        delegatableServicesArrayList.clear()

        delegatableServicesArrayList = (serviceViewModel?.getDelegatableServices() as ArrayList<ServiceEntity>?)!!

        Timber.e("DELEGATABLE SERVICES [1] -> ${delegatableServicesArrayList.size}")

        if (delegatableServicesArrayList.isEmpty()) {
            showOfflineView()
            reloadServices()
        } else {
            hideOfflineView()
        }

        for (i in delegatableServicesArrayList.indices) {
            Timber.e("SERVICE-LIST [AFTER]-> $delegatableServicesArrayList")

            serviceId = delegatableServicesArrayList[i].serviceId.toString() //0
            serviceName = delegatableServicesArrayList[i].serviceName.toString() //1
            serviceDescription = delegatableServicesArrayList[i].serviceDescription.toString() //2
            serviceEligibility = delegatableServicesArrayList[i].serviceEligibility.toString() //3
            serviceCentres = delegatableServicesArrayList[i].serviceCentres!! //4
            delegatable = delegatableServicesArrayList[i].delegatable!! //5
            serviceCost = delegatableServicesArrayList[i].serviceCost.toString() //6
            serviceDocuments = delegatableServicesArrayList[i].serviceDocuments!!//7
            serviceDuration = delegatableServicesArrayList[i].serviceDuration.toString() //8
            approvalCount = delegatableServicesArrayList[i].usefulCount!! //9
            disapprovalCount = delegatableServicesArrayList[i].notUsefulCount!! //10
            serviceComments = delegatableServicesArrayList[i].serviceComments!!
            commentCount = delegatableServicesArrayList[i].commentCount!! //11
            shareCount = delegatableServicesArrayList[i].serviceShares!! //12
            viewCount = delegatableServicesArrayList[i].serviceViews!! //13
            serviceProvider = delegatableServicesArrayList[i].serviceProvider!!

            Timber.e("DELEGATABLE-SERVICE-LIST => ${delegatableServicesArrayList[i].serviceId}")

            delegatedService = Service(serviceId, serviceName, serviceDescription,
                    serviceEligibility, serviceCentres, delegatable, serviceCost,
                    serviceDocuments, serviceDuration, approvalCount, disapprovalCount,
                    serviceComments, commentCount, shareCount, viewCount, serviceProvider)

            /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
             * 2. wrapping those values in a JSON Object
             * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
            serviceUpVoteBoolean = delegatedServicePrefs
                    .getBoolean("UP-VOTE-${delegatableServicesArrayList[i].serviceId}", false)

            serviceDownVoteBoolean = delegatedServicePrefs
                    .getBoolean("DOWN-VOTE-${delegatableServicesArrayList[i].serviceId}", false)

            serviceCommentBoolean = delegatedServicePrefs
                    .getBoolean("COMMENTED-ON-${delegatableServicesArrayList[i].serviceId}", false)

            serviceBookmarked = delegatedServicePrefs
                    .getBoolean("BOOKMARKED-${delegatableServicesArrayList[i].serviceId}", false)

            savedUserActions
                    .put("UP-VOTE", serviceUpVoteBoolean)
                    .put("DOWN-VOTE", serviceDownVoteBoolean)
                    .put("COMMENTED-ON", serviceCommentBoolean)
                    .put("BOOKMARKED", serviceBookmarked)

            gAdapter.add(ServiceItem(delegatedService, context, savedUserActions))

        }

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