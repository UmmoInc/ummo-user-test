package xyz.ummo.user.ui.fragments.categories

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_service_categories.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.ServiceCategoriesAdapter
import xyz.ummo.user.databinding.FragmentServiceCategoriesBinding
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.USER_NAME
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences

class ServiceCategories : Fragment() {
    private lateinit var viewBinding: FragmentServiceCategoriesBinding
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var rootView: View
    private lateinit var mixpanelAPI: MixpanelAPI
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userName: String
    private var totalIdeas = 0
    private var totalVehicles = 0
    private var totalBusiness = 0
    private var totalIdentity = 0
    private var totalHealth = 0
    private var totalTravel = 0
    private var totalAgriculture = 0
    private var totalEducation = 0
    private lateinit var serviceCategoriesViewModel: ServiceCategoriesViewModel
    private val coroutineScope = CoroutineScope(Dispatchers.Main + parentJob)
    private lateinit var serviceCategoryAdapter: ServiceCategoriesAdapter
//    private val serviceCategoryEntity = ServiceCategoryEntity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

//        getCatSum(requireContext())

        mixpanelAPI = MixpanelAPI
            .getInstance(context, context?.resources?.getString(R.string.mixpanelToken))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_service_categories,
            container,
            false
        )
        rootView = viewBinding.root

        sharedPreferences = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        userName = sharedPreferences.getString(USER_NAME, "").toString()
        val endOfFirstName = userName.indexOf(" ", 0, true)
        val firstName = userName.substring(0, endOfFirstName)

        viewBinding.homeBarTitleTextView.text = "Welcome, $firstName"

        return rootView
    }

    private fun setupRecyclerView() {
        /** Scaffolding the [recyclerView] **/
        /*recyclerView = rootView.service_category_recycler_view
        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = rootView.service_category_recycler_view.layoutManager
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = gAdapter*/

        serviceCategoryAdapter = ServiceCategoriesAdapter()
        service_category_recycler_view.apply {
            adapter = serviceCategoryAdapter
            layoutManager = GridLayoutManager(context, 2)
            hasFixedSize()
        }
    }

    private fun getAllServiceCategoriesFromRoomAndDisplay() {

        serviceCategoriesViewModel.serviceCategoriesLiveData.observe(viewLifecycleOwner) { response ->
            serviceCategoryAdapter.differ.submitList(response)
            if (activity != null && isAdded) {
                coroutineScope.launch(Dispatchers.IO) {

                    if (response.isNotEmpty()) {
                        Timber.e("RESPONSE IS EMPTY")

                        serviceCategoriesViewModel.saveAllServiceCategoriesFromServer()

                        requireActivity().runOnUiThread {
                            hideProgressBar()
                        }

                        Handler(Looper.getMainLooper()).post {
                            // TODO: Check for categories
                        }
                    } else {
                        Timber.e("RESPONSE IS NOT EMPTY")

                    }
                }
            }
        }
    }

    private fun reloadServiceCategories() {
        reload_service_categories_button.setOnClickListener {
            showProgressBar()
            getAllServiceCategoriesFromRoomAndDisplay()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        showProgressBar()

        serviceCategoriesViewModel = (activity as MainScreen).serviceCategoriesViewModel

        coroutineScope.launch(Dispatchers.IO) {
            serviceCategoriesViewModel.getLocallyStoredServiceCategories()
        }.invokeOnCompletion {
            Timber.e("FETCHED ALL SERVICE CATEGORIES")
        }

        getAllServiceCategoriesFromRoomAndDisplay()

    }

    private fun showProgressBar() {
        load_categories_progress_bar.visibility = View.VISIBLE
        service_category_nested_scroll_view.visibility = View.GONE
        no_service_categories_layout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        load_categories_progress_bar.visibility = View.GONE
        service_category_nested_scroll_view.visibility = View.VISIBLE
        no_service_categories_layout.visibility = View.GONE
    }

    private fun noServiceCategoriesFound() {
        load_categories_progress_bar.visibility = View.GONE
        service_category_nested_scroll_view.visibility = View.GONE
        no_service_categories_layout.visibility = View.VISIBLE
    }

    companion object {

        private val parentJob = Job()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ServiceCategories().apply {
                arguments = Bundle().apply {

                }
            }
    }
}