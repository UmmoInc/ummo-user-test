package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.PagesViewPagerAdapter
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentPagesBinding
import xyz.ummo.user.delegate.GetServiceProvider
import xyz.ummo.user.delegate.GetServices
import xyz.ummo.user.models.ServiceProviderData
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel

class PagesFragment : Fragment() {

    private lateinit var pagesFragmentBinding: FragmentPagesBinding
    private var serviceProviderData: ArrayList<ServiceProviderData> = ArrayList()
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceProviderEntity = ServiceProviderEntity()
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceEntity = ServiceEntity()

    /** Shared Preferences **/
    /*private lateinit var pagesPrefs: SharedPreferences
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences: String = "UMMO_USER_PREFERENCES"*/

    companion object {
        fun newInstance() = PagesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("SERVICE-PROVIDER-DATA {onCreate} [1] -> $serviceProviderData")

        /** Initializing ServiceProviderViewModel **/
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        /** Initializing ServiceViewModel **/
        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

//        pagesPrefs = requireActivity().getSharedPreferences(ummoUserPreferences, mode)
        Timber.e("SERVICE-PROVIDER-DATA {onCreate} [2] -> $serviceProviderData")

//        sortServiceProvidersByCategoryAndSave()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        pagesFragmentBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_pages,
                container, false)

        val view = pagesFragmentBinding.root

        //getServiceProviderData()
        setupPagesTabs()

        return view
    }

    private fun setupPagesTabs() {
        val pagesAdapter = PagesViewPagerAdapter(childFragmentManager)
        pagesAdapter.addFragment(HomeAffairsFragment(), "Home-Affairs")
        pagesAdapter.addFragment(RevenueFragment(), "Revenue")
        pagesAdapter.addFragment(CommerceFragment(), "Commerce")

        pagesFragmentBinding.pagesViewPager.adapter = pagesAdapter
        pagesFragmentBinding.pagesTabLayout.setupWithViewPager(pagesFragmentBinding.pagesViewPager)

        pagesFragmentBinding.pagesTabLayout.getTabAt(0)!!
                .setIcon(R.drawable.ic_home_affairs_24)
        pagesFragmentBinding.pagesTabLayout.getTabAt(1)!!
                .setIcon(R.drawable.ic_treasury_24)
        pagesFragmentBinding.pagesTabLayout.getTabAt(2)!!
                .setIcon(R.drawable.ic_commerce_24)
    }

    /** This function fetches service-providers && #decomposes them with
     * `decomposeServiceProviderData(arrayList)`
     * TODO: since its a network operation, it needs to be moved away from the UI to another class**/
    /*private fun getServiceProviderData() {

        object : GetServiceProvider(requireActivity()) {

            override fun done(data: List<ServiceProviderData>, code: Number) {

                if (code == 200) {
                    serviceProviderData.addAll(data)
                    Timber.e(" GETTING SERVICE PROVIDER DATA ->%s", serviceProviderData)

                    decomposeServiceProviderData(serviceProviderData)

                } else {
                    Timber.e("No PublicService READY!")
                }
            }
        }
    }*/

    /** The `decomposeServiceProviderData` function takes an arrayList of #ServiceProviderData;
     * then, for each serviceProvider, we store that data with `storeServiceProviderData` **/
    /*private fun decomposeServiceProviderData(mServiceProviderData: ArrayList<ServiceProviderData>) {
        Timber.e("DECOMPOSING SERVICE PROVIDER DATA -> $mServiceProviderData")
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        for (i in 0 until mServiceProviderData.size) {
            Timber.e("SERVICE-PROVIDER-DATA i->[$i] -> ${mServiceProviderData[i]}")
            storeServiceProviderData(mServiceProviderData[i])

            getServicesFromServiceProviders(mServiceProviderData[i].serviceProviderId)
        }
    }*/

    /*private fun storeServiceProviderData(mSingleServiceProviderData: ServiceProviderData) {
        *//*serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)*//*

        serviceProviderEntity.serviceProviderId = mSingleServiceProviderData.serviceProviderId
        serviceProviderEntity.serviceProviderName = mSingleServiceProviderData.serviceProviderName
        serviceProviderEntity.serviceProviderDescription = mSingleServiceProviderData.serviceProviderDescription
        serviceProviderEntity.serviceProviderContact = mSingleServiceProviderData.serviceProviderContact
        serviceProviderEntity.serviceProviderEmail = mSingleServiceProviderData.serviceProviderEmail
        serviceProviderEntity.serviceProviderAddress = mSingleServiceProviderData.serviceProviderAddress

        Timber.e("STORING SERVICE PROVIDER DATA [ID]-> ${serviceProviderEntity.serviceProviderId}")
        Timber.e("STORING SERVICE PROVIDER DATA [NAME] -> ${serviceProviderEntity.serviceProviderName}")
        serviceProviderViewModel?.addServiceProvider(serviceProviderEntity)
    }*/

    /** This function gets services from a given service provider (via serviceProviderID).
     * Likewise, it needs to be moved to a different class that handles network requests **/
    /*private fun getServicesFromServiceProviders(serviceProviderId: String) {
        object : GetServices(requireActivity(), serviceProviderId) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    try {
                        val servicesArray = JSONArray(String(data))
                        var service: JSONObject

                        var serviceId: String //0
                        var serviceName: String //1
                        var serviceDescription: String //2
                        var serviceEligibility: String //3

                        var serviceCentresJSONArray: JSONArray //4
                        val serviceCentresArrayList = ArrayList(listOf<String>())

                        var presenceRequired: Boolean? //5
                        var serviceCost: String//6 //TODO: Add SERVICE_COST

                        var serviceDocumentsJSONArray: JSONArray //7
                        val serviceDocumentsArrayList = ArrayList(listOf<String>())

                        var serviceDuration: String //8
                        var notUsefulCount = 0 //9
                        var usefulCount = 0 //10

                        var commentsJSONArray: JSONArray //11
                        var commentsArrayList = ArrayList(listOf<String>())

                        var serviceShares = 0 //12
                        var serviceViews = 0 //13
                        var serviceProvider: String //14

                        for (i in 0 until servicesArray.length()) {
                            service = servicesArray.getJSONObject(i)

                            Timber.e("TESTING SERVICE-DATA-> $service")
                            serviceId = service.getString("_id")
                            serviceName = service.getString("service_name")

                            serviceDescription = service.getString("service_description")

                            serviceEligibility = service.getString("service_eligibility")

                            serviceCentresJSONArray = service.getJSONArray("service_centres")
                            for (j in 0 until serviceCentresJSONArray.length()) {
                                serviceCentresArrayList.add(serviceCentresJSONArray.getString(j))
                            }

                            serviceCost = "E150"
                            presenceRequired = service.getJSONObject("service_requirements")
                                    .getBoolean("presence_required")

                            //serviceCost = service.getString("service_cost")

                            serviceDocumentsJSONArray = service
                                    .getJSONObject("service_requirements")
                                    .getJSONArray("service_documents")

                            for (k in 0 until serviceDocumentsJSONArray.length()) {
                                serviceCentresArrayList.add(serviceDocumentsJSONArray.getString(k))

                            }

                            serviceDuration = service.getString("service_duration")

                            serviceProvider = service.getString("service_provider")

                            serviceEntity.serviceId = serviceId //0
                            serviceEntity.serviceName = serviceName //1
                            serviceEntity.serviceDescription = serviceDescription //2
                            serviceEntity.serviceEligibility = serviceEligibility //3
                            serviceEntity.serviceCentres = serviceCentresArrayList //4
                            serviceEntity.presenceRequired = presenceRequired //5
                            serviceEntity.serviceCost = serviceCost //6
                            serviceEntity.serviceDocuments = serviceDocumentsArrayList //7
                            serviceEntity.serviceDuration = serviceDuration //8
                            serviceEntity.notUsefulCount = notUsefulCount //9
                            serviceEntity.usefulCount = usefulCount //10
                            serviceEntity.commentCount = commentsArrayList.size //11
                            serviceEntity.serviceViews = serviceViews //12
                            serviceEntity.serviceShares = serviceShares //13
                            serviceEntity.serviceProvider = serviceProvider //14

                            serviceViewModel?.addService(serviceEntity)
                            Timber.e("SAVING SERVICE -> ${serviceEntity.serviceId} FROM -> ${serviceEntity.serviceProvider}")
                        }

                    } catch (jse: JSONException) {
                        Timber.e("FAILED TO GET SERVICES -> $jse")
                    }
                }
            }
        }
    }*/

    /** The function below might be useful, but for now, I'm stashing it #hoarding **/
    /*private fun sortServiceProvidersByCategoryAndSave() {
        val serviceProviders: List<ServiceProviderEntity>? = serviceProviderViewModel?.getServiceProviderList()
        var homeAffairsServiceIdPref: String
        var revenueServiceIdPref: String
        var commerceServiceIdPref: String

        Timber.e("SERVICE-PROVIDERS => ${serviceProviders?.get(0)?.serviceProviderName}")

        for (i in serviceProviders?.indices!!) {
            when {
                serviceProviders[i].serviceProviderName
                        .equals("Ministry Of Home Affairs", true) -> {
                    homeAffairsServiceIdPref = serviceProviders[i].serviceProviderId.toString()
                    pagesPrefs.edit().putString("HOME_AFFAIRS_PROVIDER", homeAffairsServiceIdPref)
                            .apply()
                    Timber.e("SHARED-PREFS [1] => $homeAffairsServiceIdPref")

                }
                serviceProviders[i].serviceProviderName
                        .equals("Ministry of Finance", true) -> {
                    revenueServiceIdPref = serviceProviders[i].serviceProviderId.toString()
                    pagesPrefs.edit().putString("REVENUE_PROVIDER", revenueServiceIdPref).apply()
                    Timber.e("SHARED-PREFS [2] => $revenueServiceIdPref")

                }
                else -> {
                    commerceServiceIdPref = serviceProviders[i].serviceProviderId.toString()
                    pagesPrefs.edit().putString("COMMERCE_PROVIDER", commerceServiceIdPref).apply()
                    Timber.e("SHARED-PREFS [3] => $commerceServiceIdPref")

                }
            }
            pagesPrefs.edit().apply()
        }
    }*/
}