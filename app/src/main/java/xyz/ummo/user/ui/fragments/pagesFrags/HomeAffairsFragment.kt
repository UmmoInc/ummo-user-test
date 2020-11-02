package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_home_affairs.view.*
import xyz.ummo.user.R
import xyz.ummo.user.Service
import xyz.ummo.user.databinding.FragmentHomeAffairsBinding
import xyz.ummo.user.rvItems.ServiceItem

class HomeAffairsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var homeAffairsBinding: FragmentHomeAffairsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

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

    companion object {

        fun newInstance() = HomeAffairsFragment()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HomeAffairsFragment().apply {

                }
    }
}