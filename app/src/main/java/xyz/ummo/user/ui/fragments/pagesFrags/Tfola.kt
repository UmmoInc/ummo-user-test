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
import kotlinx.android.synthetic.main.fragment_tfola.view.*
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
import xyz.ummo.user.databinding.FragmentTfolaBinding
import xyz.ummo.user.models.Service
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.ReloadingServicesEvent

class Tfola : Fragment() {
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
    private lateinit var nonDelegatedServicePrefs: SharedPreferences
    private lateinit var nonDelegatableServicesArrayList: ArrayList<ServiceEntity>
    private lateinit var nonDelegatedService: Service
    private lateinit var tfolaBinding: FragmentTfolaBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        gAdapter = GroupAdapter()

        nonDelegatedServicePrefs = this.requireActivity()
                .getSharedPreferences(ummoUserPreferences, mode)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        tfolaBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_tfola,
                container,
                false)

        val view = tfolaBinding.root
        recyclerView = view.tfola_services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = view.tfola_services_recycler_view.layoutManager
        recyclerView.adapter = gAdapter

        getNonDelegatableServices()

        Timber.e("NON-DELEGATABLE SERVICES LIST [0] -> ${nonDelegatableServicesArrayList.size}")

        reloadServices()

        /** Refreshing services with [tfola_swipe_refresher] **/
        tfolaBinding.tfolaSwipeRefresher.setOnRefreshListener {
            getNonDelegatableServices()
            tfolaBinding.tfolaSwipeRefresher.isRefreshing = false
            showSnackbarBlue("Services refreshed...", -1)

        }

        return view
    }

    /** Below: we're checking if there are any services to be displayed. If not, then we show
     * the User the [offlineLayout] and allowing them to reload the services manually **/
    private fun reloadServices() {
        if (nonDelegatableServicesArrayList.isEmpty()) {
            showOfflineView()
            tfolaBinding.reloadTfolaServicesButton.setOnClickListener {

                GlobalScope.launch {
                    /** Running this process on UI thread because the coroutine cannot "touch" the
                     * UI & we're trying to show the loader **/
                    requireActivity().runOnUiThread {
                        tfolaBinding.loadProgressBar.visibility = View.VISIBLE
                        tfolaBinding.offlineLayout.visibility = View.GONE
                        tfolaBinding.tfolaSwipeRefresher.visibility = View.GONE
                    }

                    /** Posting this EventBus in order to display the Snackbar **/
                    reloadingServicesEvent.reloadingServices = true
                    EventBus.getDefault().post(reloadingServicesEvent)

                    /** Delaying for 3 seconds for UX-sake **/
                    delay(3000L)

                    /** Running this process on UI thread again for the same reason as above **/
                    requireActivity().runOnUiThread {
                        getNonDelegatableServices()
                    }
                    Timber.e("RELOADING SERVICES")
                }
            }
        } else {
            hideOfflineView()
        }
    }

    private fun showOfflineView() {
        tfolaBinding.offlineLayout.visibility = View.VISIBLE
        tfolaBinding.loadProgressBar.visibility = View.GONE
        tfolaBinding.tfolaSwipeRefresher.visibility = View.INVISIBLE
    }

    private fun hideOfflineView() {
        tfolaBinding.offlineLayout.visibility = View.GONE
        tfolaBinding.loadProgressBar.visibility = View.GONE
        tfolaBinding.tfolaSwipeRefresher.visibility = View.VISIBLE
    }

    private fun getNonDelegatableServices() {
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

        nonDelegatableServicesArrayList = (serviceViewModel?.getNonDelegatableServices() as ArrayList<ServiceEntity>?)!!

        Timber.e("NON-DELEGATABLE SERVICES [1] -> ${nonDelegatableServicesArrayList.size}")

        if (nonDelegatableServicesArrayList.isEmpty()) {
            showOfflineView()
            reloadServices()
        } else {
            hideOfflineView()
        }

        for (i in nonDelegatableServicesArrayList.indices) {
            Timber.e("SERVICE-LIST [AFTER]-> $nonDelegatableServicesArrayList")

            serviceId = nonDelegatableServicesArrayList[i].serviceId.toString() //0
            serviceName = nonDelegatableServicesArrayList[i].serviceName.toString() //1
            serviceDescription = nonDelegatableServicesArrayList[i].serviceDescription.toString() //2
            serviceEligibility = nonDelegatableServicesArrayList[i].serviceEligibility.toString() //3
            serviceCentres = nonDelegatableServicesArrayList[i].serviceCentres!! //4
            delegatable = nonDelegatableServicesArrayList[i].delegatable!! //5
            serviceCost = nonDelegatableServicesArrayList[i].serviceCost.toString() //6
            serviceDocuments = nonDelegatableServicesArrayList[i].serviceDocuments!!//7
            serviceDuration = nonDelegatableServicesArrayList[i].serviceDuration.toString() //8
            approvalCount = nonDelegatableServicesArrayList[i].usefulCount!! //9
            disapprovalCount = nonDelegatableServicesArrayList[i].notUsefulCount!! //10
            serviceComments = nonDelegatableServicesArrayList[i].serviceComments!!
            commentCount = nonDelegatableServicesArrayList[i].commentCount!! //11
            shareCount = nonDelegatableServicesArrayList[i].serviceShares!! //12
            viewCount = nonDelegatableServicesArrayList[i].serviceViews!! //13
            serviceProvider = nonDelegatableServicesArrayList[i].serviceProvider!!

            nonDelegatedService = Service(serviceId, serviceName, serviceDescription,
                    serviceEligibility, serviceCentres, delegatable, serviceCost,
                    serviceDocuments, serviceDuration, approvalCount, disapprovalCount,
                    serviceComments, commentCount, shareCount, viewCount, serviceProvider)

            /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB, using the $serviceId
             * 2. wrapping those values in a JSON Object
             * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
            serviceUpVoteBoolean = nonDelegatedServicePrefs
                    .getBoolean("UP-VOTE-${nonDelegatableServicesArrayList[i].serviceId}", false)

            serviceDownVoteBoolean = nonDelegatedServicePrefs
                    .getBoolean("DOWN-VOTE-${nonDelegatableServicesArrayList[i].serviceId}", false)

            serviceCommentBoolean = nonDelegatedServicePrefs
                    .getBoolean("COMMENTED-ON-${nonDelegatableServicesArrayList[i].serviceId}", false)

            serviceBookmarked = nonDelegatedServicePrefs
                    .getBoolean("BOOKMARKED-${nonDelegatableServicesArrayList[i].serviceId}", false)

            savedUserActions
                    .put("UP-VOTE", serviceUpVoteBoolean)
                    .put("DOWN-VOTE", serviceDownVoteBoolean)
                    .put("COMMENTED-ON", serviceCommentBoolean)
                    .put("BOOKMARKED", serviceBookmarked)

            gAdapter.add(ServiceItem(nonDelegatedService, context, savedUserActions))

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
         * @return A new instance of fragment Tfola.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                Tfola().apply {

                }
    }
}