package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_home_affairs.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentHomeAffairsBinding
import xyz.ummo.user.models.Service
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel

class HomeAffairsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var homeAffairsBinding: FragmentHomeAffairsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    /** ServiceProvider ViewModel && Entity Declarations **/
    private var serviceProviderViewModel: ServiceProviderViewModel? = null

    /** Service ViewModel && Entity Declarations **/
    private var serviceViewModel: ServiceViewModel? = null

    /** HomeAffairs Service instance && Service ID **/
    private lateinit var homeAffairsServiceId: String
    private lateinit var homeAffairsService: Service

    private lateinit var homeAffairsServiceList: List<ServiceEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

        /** Initializing ViewModels: ServiceProvider && Services **/
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        Timber.e("CREATING HOME-AFFAIRS-FRAGMENT!")
        getHomeAffairsServiceProviderId()
        getHomeAffairsServices(homeAffairsServiceId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        homeAffairsBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_home_affairs,
                container, false)

        val view = homeAffairsBinding.root
        val layoutManager = view.services_recycler_view.layoutManager

        recyclerView = view.services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gAdapter

        if (homeAffairsServiceList.isNotEmpty()) {
            homeAffairsBinding.loadProgressBar.visibility = View.GONE
        }

        Timber.e("CREATING HOME-AFFAIRS-VIEW!")
        return view
    }

    private fun getHomeAffairsServiceProviderId() {
        val serviceProviders: List<ServiceProviderEntity>? = serviceProviderViewModel
                ?.getServiceProviderList()

        for (i in serviceProviders?.indices!!) {
            Timber.e("SERVICE-PROVIDERS [2]=> ${serviceProviders[i].serviceProviderId}")
            when {
                serviceProviders[i].serviceProviderName
                        .equals("ministry of home affairs", true) -> {

                    homeAffairsServiceId = serviceProviders[i].serviceProviderId.toString()
                    Timber.e("Home Affairs ID [2] -> $homeAffairsServiceId")
                }
            }
        }
    }

    private fun getHomeAffairsServices(homeAffairsId: String) {
        var serviceId: String
        var serviceName: String
        var serviceDescription: String
        var serviceEligibility: String
        var serviceCentre: String
        var presenceRequired: Boolean
        var serviceCost: String
        var serviceRequirements: String
        var serviceDuration: String
        var approvalCount: Int
        var disapprovalCount: Int
        var commentCount: Int
        var shareCount: Int
        var viewCount: Int

        val servicesList = serviceViewModel?.getServicesList()

        Timber.e("SERVICE-LIST [BEFORE]-> ${servicesList?.size}")
        for (i in servicesList?.indices!!) {
            if (servicesList[i].serviceProvider == homeAffairsId) {
                Timber.e("SERVICE-LIST [AFTER]-> $servicesList")
                homeAffairsServiceList = servicesList

                Timber.e("HOME AFFAIRS SERVICE [3] -> ${servicesList[i].serviceName}")
                serviceId = homeAffairsServiceList[i].serviceId.toString() //0
                serviceName = homeAffairsServiceList[i].serviceName.toString() //1
                serviceDescription = homeAffairsServiceList[i].serviceDescription.toString() //2
                serviceEligibility = homeAffairsServiceList[i].serviceEligibility.toString() //3
                serviceCentre = homeAffairsServiceList[i].serviceCentres.toString() //4
                presenceRequired = homeAffairsServiceList[i].presenceRequired!! //5
                serviceCost = homeAffairsServiceList[i].serviceCost.toString() //6
                serviceRequirements = homeAffairsServiceList[i].serviceDocuments.toString() //7
                serviceDuration = homeAffairsServiceList[i].serviceDuration.toString() //8
                approvalCount = homeAffairsServiceList[i].approvalCount!! //9
                disapprovalCount = homeAffairsServiceList[i].disapprovalCount!! //10
                commentCount = homeAffairsServiceList[i].comments?.size!! //11
                shareCount = homeAffairsServiceList[i].serviceShares!! //12
                viewCount = homeAffairsServiceList[i].serviceViews!! //13

                Timber.e("HOME-AFFAIRS-SERVICE-LIST => ${homeAffairsServiceList[i].serviceId}")

                homeAffairsService = Service(serviceId, serviceName, serviceDescription,
                        serviceEligibility, serviceCentre, presenceRequired, serviceCost,
                        serviceRequirements, serviceDuration, approvalCount, disapprovalCount,
                        commentCount, shareCount, viewCount)
                Timber.e("HOME-AFFAIRS-SERVICE-BLOB [1] -> $homeAffairsService")

                gAdapter.add(ServiceItem(homeAffairsService, context))

            }
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

        fun newInstance() = HomeAffairsFragment()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomeAffairsFragment().apply {

                }
    }
}