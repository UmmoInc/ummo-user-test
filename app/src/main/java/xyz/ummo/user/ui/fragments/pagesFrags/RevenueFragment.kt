package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_commerce.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentRevenueBinding
import xyz.ummo.user.models.Service
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel

class RevenueFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var revenueBinding: FragmentRevenueBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    /** ServiceProvider ViewModel && Entity Declarations **/
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceViewModel: ServiceViewModel? = null

    private lateinit var financeServiceId: String
    private lateinit var revenueService: Service
    private lateinit var revenueServiceList: List<ServiceEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

        /** Initializing ViewModels: ServiceProvider && Services **/
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        Timber.e("CREATING REVENUE-FRAGMENT!")
        getRevenueServiceProviderId()
        getRevenueServices(financeServiceId)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        revenueBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_revenue,
                container, false)

        val view = revenueBinding.root
        val layoutManager = view.services_recycler_view.layoutManager

        recyclerView = view.services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gAdapter

        Timber.e("CREATING REVENUE-VIEW!")

        if (revenueServiceList.isNotEmpty()) {
            revenueBinding.loadProgressBar.visibility = View.GONE
        }

        return view
    }

    private fun getRevenueServiceProviderId() {
        val serviceProviders: List<ServiceProviderEntity>? = serviceProviderViewModel
                ?.getServiceProviderList()

        for (i in serviceProviders?.indices!!) {
            Timber.e("SERVICE-PROVIDERS [2]=> ${serviceProviders[i].serviceProviderId}")
            when {
                serviceProviders[i].serviceProviderName
                        .equals("ministry of finance", true) -> {

                    financeServiceId = serviceProviders[i].serviceProviderId.toString()
                    Timber.e("Revenue ID [2] -> $financeServiceId")
                }
            }
        }
    }

    private fun getRevenueServices(revenueId: String) {
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
        var disApprovalCount: Int
        var commentCount: Int
        var shareCount: Int
        var viewCount: Int

        val servicesList = serviceViewModel?.getServicesList()
        for (i in servicesList?.indices!!) {
            if (servicesList[i].serviceProvider == revenueId) {
                revenueServiceList = servicesList
                Timber.e("REVENUE SERVICE [3] -> ${servicesList[i].serviceName}")
                serviceId = revenueServiceList[i].serviceId.toString() //0
                serviceName = revenueServiceList[i].serviceName.toString() //1
                serviceDescription = revenueServiceList[i].serviceDescription.toString() //2
                serviceEligibility = revenueServiceList[i].serviceEligibility.toString() //3
                serviceCentre = revenueServiceList[i].serviceCentres.toString() //4
                presenceRequired = revenueServiceList[i].presenceRequired!! //5
                serviceCost = revenueServiceList[i].serviceCost.toString() //6
                serviceRequirements = revenueServiceList[i].serviceDocuments.toString() //7
                serviceDuration = revenueServiceList[i].serviceDuration.toString() //8
                approvalCount = revenueServiceList[i].approvalCount!! //9
                disApprovalCount = revenueServiceList[i].disapprovalCount!! //10
                commentCount = revenueServiceList[i].comments?.size!! //11
                shareCount = revenueServiceList[i].serviceShares!! //12
                viewCount = revenueServiceList[i].serviceViews!! //13

                revenueService = Service(serviceId, serviceName, serviceDescription,
                        serviceEligibility, serviceCentre, presenceRequired, serviceCost,
                        serviceRequirements, serviceDuration, approvalCount, disApprovalCount,
                        commentCount, shareCount, viewCount)
                Timber.e("REVENUE-SERVICE-BLOB [1] -> $revenueService")

                gAdapter.add(ServiceItem(revenueService, context))

            }
        }
    }

    private fun displayRevenueServices() {
        gAdapter.add(ServiceItem(revenueService, context))

        revenueBinding.loadProgressBar.visibility = View.GONE

        recyclerView.adapter = gAdapter
    }

    companion object {

        fun newInstance() = RevenueFragment()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                RevenueFragment().apply {

                }
    }
}