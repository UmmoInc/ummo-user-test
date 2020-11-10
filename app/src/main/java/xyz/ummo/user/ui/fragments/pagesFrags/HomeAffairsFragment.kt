package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_home_affairs.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.Service
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentHomeAffairsBinding
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel

class HomeAffairsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var homeAffairsBinding: FragmentHomeAffairsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    /** ServiceProvider ViewModel && Entity Declarations **/
    private val serviceProviderEntity = ServiceProviderEntity()
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    var serviceProviderName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

        unpackAndGrabHomeAffairsServiceProvider()

        Timber.e("SERVICE-PROVIDER-NAME [1]-> $serviceProviderName")
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

        generateHomeAffairsServices()

        return view
    }

    /** TODO:
     * 1) Unzip ServiceProviderViewModel;
     * 2) Inspect ServiceProviderEntities;
     * 3) Check for all ServiceProviderEntity names & grab all that have "*Home Affairs*" **/
    private fun unpackAndGrabHomeAffairsServiceProvider() {

        serviceProviderViewModel?.serviceProviderEntityLiveData
                ?.observe(viewLifecycleOwner, Observer { serviceProviderEntity: ServiceProviderEntity ->
                    serviceProviderName = serviceProviderEntity.serviceProviderName
                    Timber.e("SERVICE-PROVIDER-NAME [2]-> $serviceProviderName")
                    showSnackbarBlue(serviceProviderName.toString(), 0)
                })
        Timber.e("SERVICE-PROVIDER-NAME [3]-> $serviceProviderName")

    }

    private fun generateHomeAffairsServices() {
        val mServiceName = "Renew Passport"
        val mServiceDescription = "Every 10 years, we need to renew our passports"
        val mServiceEligibility = "Current Passport Owners"
        val mServiceCentre = "Ministry of Home Affairs (HQ)"
        val mServiceCost = "150"
        val mServiceRequirements = "ID, Old Passport & Fee"
        val mServiceDuration = "1 Day"
        val mApproveCount = 5
        val mDisapproveCount = 1
        val mComments = 2
        val mShares = 3
        val mViews = 4

        val singleService = xyz.ummo.user.models.Service(mServiceName,
                mServiceDescription,
                mServiceEligibility,
                mServiceCentre,
                mServiceCost,
                mServiceRequirements,
                mServiceDuration,
                mApproveCount,
                mDisapproveCount,
                mComments,
                mShares,
                mViews)

        gAdapter.add(ServiceItem(singleService, context))

        homeAffairsBinding.loadProgressBar.visibility = View.GONE

        recyclerView.adapter = gAdapter
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        /** Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE**/
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(requireActivity().findViewById(android.R.id.content), message, length)
        snackbar.setTextColor( resources.getColor(R.color.ummo_4))
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