package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.PagesViewPagerAdapter
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.FragmentPagesBinding
import xyz.ummo.user.delegate.GetServiceProvider
import xyz.ummo.user.models.ServiceProviderData
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel

class PagesFragment : Fragment() {

    private lateinit var pagesFragmentBinding: FragmentPagesBinding
    private var serviceProviderData: ArrayList<ServiceProviderData> = ArrayList()
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceProviderEntity = ServiceProviderEntity()

    companion object {
        fun newInstance() = PagesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("SERVICE-PROVIDER-DATA -> $serviceProviderData")

        getServiceProviderData()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        pagesFragmentBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_pages,
                container, false)

        val view = pagesFragmentBinding.root
        setupPagesTabs()

        return view
    }

    private fun setupPagesTabs() {
        val pagesAdapter = PagesViewPagerAdapter(childFragmentManager)
        pagesAdapter.addFragment(HomeAffairsFragment(), "Home Affairs")
        pagesAdapter.addFragment(TreasuryFragment(), "Revenue")
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

    private fun getServiceProviderData() {

        object : GetServiceProvider(requireActivity()) {

            override fun done(data: List<ServiceProviderData>, code: Number) {

                Timber.e("LENGTH -> ${data.size}")

                if (code == 200) {
                    serviceProviderData.addAll(data)
                    Timber.e(" GETTING SERVICE PROVIDER DATA ->%s", serviceProviderData)

                    decomposeServiceProviderData(serviceProviderData)

                } else {
                    Timber.e("No PublicService READY!")
                }
            }
        }
    }

    private fun decomposeServiceProviderData(mServiceProviderData: ArrayList<ServiceProviderData>) {
        Timber.e("DECOMPOSING SERVICE PROVIDER DATA -> $mServiceProviderData")
        serviceProviderViewModel = ViewModelProvider(this).get(ServiceProviderViewModel::class.java)

        for (i in 0 until mServiceProviderData.size ) {
            Timber.e("SERVICE-PROVIDER-DATA [$i] -> ${mServiceProviderData[i]}")
            storeServiceProviderData(mServiceProviderData[i])
        }
    }

    private fun storeServiceProviderData(mSingleServiceProviderData: ServiceProviderData) {

        serviceProviderViewModel = ViewModelProvider(this).get(ServiceProviderViewModel::class.java)

        serviceProviderEntity.serviceProviderId = mSingleServiceProviderData.serviceProviderId
        serviceProviderEntity.serviceProviderName = mSingleServiceProviderData.serviceProviderName
        serviceProviderEntity.serviceProviderDescription = mSingleServiceProviderData.serviceProviderDescription
        serviceProviderEntity.serviceProviderContact = mSingleServiceProviderData.serviceProviderContact
        serviceProviderEntity.serviceProviderEmail = mSingleServiceProviderData.serviceProviderEmail
        serviceProviderEntity.serviceProviderAddress = mSingleServiceProviderData.serviceProviderAddress

        Timber.e("STORING SERVICE PROVIDER DATA -> ${serviceProviderEntity.serviceProviderName}")
        serviceProviderViewModel?.addServiceProvider(serviceProviderEntity)
    }
}