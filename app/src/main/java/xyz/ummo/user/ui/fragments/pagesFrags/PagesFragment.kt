package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.PagesViewPagerAdapter
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentPagesBinding
import xyz.ummo.user.models.ServiceProviderData
import xyz.ummo.user.ui.fragments.categories.ServiceCategories
import xyz.ummo.user.ui.fragments.pagesFrags.tfuma.Tfuma
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.SERVICE_CATEGORY
import java.util.*
import kotlin.collections.ArrayList


class PagesFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var pagesFragmentBinding: FragmentPagesBinding
    private var serviceProviderData: ArrayList<ServiceProviderData> = ArrayList()
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceProviderEntity = ServiceProviderEntity()
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceEntity = ServiceEntity()
    private var category = ""
    private lateinit var mixpanel: MixpanelAPI

    companion object {
        fun newInstance() = PagesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("SERVICE-CATEGORY -> ${arguments?.get(SERVICE_CATEGORY)}")
        category = arguments?.getString(SERVICE_CATEGORY).toString()

        /** Initializing Mixpanel**/
        mixpanel = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        /** Initializing ServiceProviderViewModel **/
        serviceProviderViewModel = ViewModelProvider(this)
            .get(ServiceProviderViewModel::class.java)

        /** Initializing ServiceViewModel **/
        serviceViewModel = ViewModelProvider(this)
            .get(ServiceViewModel::class.java)

//        pagesPrefs = requireActivity().getSharedPreferences(ummoUserPreferences, mode)
        Timber.e("SERVICE-PROVIDER-DATA {onCreate} [2] -> $serviceProviderData")

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.visibility = View.GONE

        pagesFragmentBinding.pagesAppbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        pagesFragmentBinding.pagesAppbar.title =
            "Services under ${category.capitalize(Locale.ROOT)}"
        pagesFragmentBinding.pagesAppbar.setNavigationOnClickListener {
            openFragment(ServiceCategories())
        }

        checkTabAndClearBadge()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        toolbar.visibility = View.VISIBLE
    }

    private fun openFragment(fragment: Fragment) {

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager
            .beginTransaction()

        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        pagesFragmentBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pages,
            container, false
        )

        val view = pagesFragmentBinding.root

        pagesFragmentBinding.pagesTabLayout.getTabAt(1)?.apply {
            orCreateBadge
            badge!!.isVisible = true
        }

        pagesFragmentBinding.pagesTabLayout.getTabAt(1)?.orCreateBadge

        //getServiceProviderData()
        setupPagesTabs()

        checkTabAndClearBadge()

        return view
    }

    private fun setupPagesTabs() {
        val pagesAdapter = PagesViewPagerAdapter(childFragmentManager)

        pagesAdapter.addFragment(Tfuma(), "Tfola Lusito")
        pagesAdapter.addFragment(Tfola(), "Tfola Lwati")

//        pagesAdapter.addFragment(Phepha(), "Phepha")

        pagesFragmentBinding.pagesViewPager.adapter = pagesAdapter
        pagesFragmentBinding.pagesTabLayout.setupWithViewPager(pagesFragmentBinding.pagesViewPager)

        pagesFragmentBinding.pagesTabLayout.getTabAt(0)!!
            .setIcon(R.drawable.ic_tfuma_24)
        pagesFragmentBinding.pagesTabLayout.getTabAt(1)!!
            .setIcon(R.drawable.ic_tfola_24)

        pagesFragmentBinding.pagesTabLayout.getTabAt(1)!!.orCreateBadge

//        checkTabAndClearBadge()
    }

    /** We're checking if the selected tab is [Tfola], then we disable the badge.
     * Then we track each tab event accordingly **/
    private fun checkTabAndClearBadge() {
        var position: Int
        val pagesTabObject = JSONObject()

        pagesFragmentBinding.pagesTabLayout.addOnTabSelectedListener(object :
            OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                position = tab.position
                if (position == 1) {
                    pagesFragmentBinding.pagesTabLayout.getTabAt(position)?.badge?.isVisible = false
                }

                pagesTabObject.put("TAB_POSITION", position)
                mixpanel.track("pagesFragment_selectedTab", pagesTabObject)

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                position = tab!!.position
                pagesTabObject.put("TAB_POSITION", position)
                mixpanel.track("pagesFragment_unSelectedTab", pagesTabObject)

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                position = tab!!.position
                pagesTabObject.put("TAB_POSITION", position)
                mixpanel.track("pagesFragment_reSelectedTab", pagesTabObject)
            }
        })
    }
}