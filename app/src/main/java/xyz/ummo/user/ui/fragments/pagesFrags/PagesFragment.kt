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
import com.google.firebase.perf.metrics.AddTrace
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.PagesViewPagerAdapter
import xyz.ummo.user.api.GetAllServices
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentPagesBinding
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.models.ServiceProviderData
import xyz.ummo.user.ui.fragments.categories.ServiceCategories
import xyz.ummo.user.ui.fragments.pagesFrags.tfuma.Tfuma
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.SERVICE_CATEGORY
import java.util.*

class PagesFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var pagesFragmentBinding: FragmentPagesBinding
    private var serviceProviderData: ArrayList<ServiceProviderData> = ArrayList()
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceProviderEntity = ServiceProviderEntity()
    private var serviceViewModel: ServiceViewModel? = null
    private lateinit var serviceEntity: ServiceEntity
    private var category = ""
    private lateinit var mixpanel: MixpanelAPI
    private val tfumaObject = Tfuma()

    /** Delegatable Service Variables **/
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
    var serviceBenefitJSONArray = JSONArray()
    var serviceBenefits = ArrayList<ServiceBenefit>()
    var serviceAttachmentName = ""
    var serviceAttachmentSize = ""
    var serviceAttachmentURL = ""
    var serviceCategory = ""
    var countOfDelegatableServices: Int = 0
    var countOfNonDelegatableServices: Int = 0
    private lateinit var delegatableService: ServiceObject

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
        setupPagesTabs(0, 0)

        getDelegatableServicesFromServer()

        checkTabAndClearBadge()

        return view
    }

    @AddTrace(name = "get_delegatable_services_from_server")
    fun getDelegatableServicesFromServer() {
        object : GetAllServices(requireActivity()) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    val allServices = JSONObject(String(data)).getJSONArray("payload")
                    var service: JSONObject

                    try {
                        for (i in 0 until allServices.length()) {
                            service = allServices[i] as JSONObject
                            delegatable = service.getBoolean("delegatable")

                            if (delegatable && category == service.getString(SERVICE_CATEGORY)) {
                                countOfDelegatableServices++
                            } else if (!delegatable && category == service.getString(
                                            SERVICE_CATEGORY
                                    )
                            ) {
                                countOfNonDelegatableServices++
                            }
                        }
                        if (isAdded)
                            setupPagesTabs(
                                    countOfDelegatableServices,
                                    countOfNonDelegatableServices
                            )

                    } catch (jse: JSONException) {
                        Timber.e("FAILED TO PARSE DELEGATABLE SERVICES -> $jse")
                    }

                } else {
                    Timber.e("FAILED TO GET SERVICES : $code")
                }
            }
        }
    }

    private fun setupPagesTabs(delegatable: Int, nonDelegatable: Int) {
        val pagesAdapter = PagesViewPagerAdapter(childFragmentManager)

        pagesAdapter.addFragment(Tfuma(), "Tfola Lusito")
        pagesFragmentBinding.pagesTabLayout.getTabAt(0)?.orCreateBadge?.number = delegatable

        pagesAdapter.addFragment(Tfola(), "Tfola Lwati")
        pagesFragmentBinding.pagesTabLayout.getTabAt(1)?.orCreateBadge?.number = nonDelegatable

//        pagesAdapter.addFragment(Phepha(), "Phepha")

        pagesFragmentBinding.pagesViewPager.adapter = pagesAdapter
        pagesFragmentBinding.pagesTabLayout.setupWithViewPager(pagesFragmentBinding.pagesViewPager)

        pagesFragmentBinding.pagesTabLayout.getTabAt(0)!!
            .setIcon(R.drawable.ic_tfuma_24)
        pagesFragmentBinding.pagesTabLayout.getTabAt(1)!!
            .setIcon(R.drawable.ic_tfola_24)

//        pagesFragmentBinding.pagesTabLayout.getTabAt(1)!!.orCreateBadge

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
                if (position == 0) {
                    pagesFragmentBinding.pagesTabLayout.getTabAt(position)?.badge?.isVisible = false
                }
                pagesTabObject.put("TAB_POSITION", position)
                mixpanel.track("pagesFragment_unSelectedTab", pagesTabObject)

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                position = tab!!.position

                if (position == 0) {
                    pagesFragmentBinding.pagesTabLayout.getTabAt(position)?.badge?.isVisible = false
                } else if (position == 1) {
                    pagesFragmentBinding.pagesTabLayout.getTabAt(position)?.badge?.isVisible = false
                }

                pagesTabObject.put("TAB_POSITION", position)
                mixpanel.track("pagesFragment_reSelectedTab", pagesTabObject)
            }
        })
    }
}