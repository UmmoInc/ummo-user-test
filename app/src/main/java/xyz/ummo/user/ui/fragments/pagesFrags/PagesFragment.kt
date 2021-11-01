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

class PagesFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var pagesFragmentBinding: FragmentPagesBinding
    private var serviceProviderData: ArrayList<ServiceProviderData> = ArrayList()
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceProviderEntity = ServiceProviderEntity()
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceEntity = ServiceEntity()
    private var category = ""

    companion object {
        fun newInstance() = PagesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("SERVICE-CATEGORY -> ${arguments?.get(SERVICE_CATEGORY)}")
        category = arguments?.getString(SERVICE_CATEGORY).toString()

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
        pagesFragmentBinding.pagesAppbar.title = "Services under $category"
        pagesFragmentBinding.pagesAppbar.setNavigationOnClickListener {
            openFragment(ServiceCategories())
        }

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

        //getServiceProviderData()
        setupPagesTabs()

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
        /*pagesFragmentBinding.pagesTabLayout.getTabAt(2)!!
                .setIcon(R.drawable.ic_coronavirus_24)*/
    }
}