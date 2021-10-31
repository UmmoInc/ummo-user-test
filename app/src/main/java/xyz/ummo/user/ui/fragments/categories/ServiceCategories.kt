package xyz.ummo.user.ui.fragments.categories

import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_service_categories.view.*
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentServiceCategoriesBinding
import xyz.ummo.user.models.ServiceCategoryModel
import xyz.ummo.user.rvItems.ServiceCategoryItem

class ServiceCategories : Fragment() {
    private lateinit var viewBinding: FragmentServiceCategoriesBinding
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var rootView: View
    private lateinit var mixpanelAPI: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

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

        /** Scaffolding the [recyclerView] **/
        recyclerView = rootView.service_category_recycler_view
        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = rootView.service_category_recycler_view.layoutManager
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = gAdapter

        gAdapter.add(0, ServiceCategoryItem(ServiceCategoryModel("business", 17), requireContext()))
        gAdapter.add(1, ServiceCategoryItem(ServiceCategoryModel("travel", 7), requireContext()))
        gAdapter.add(
            2,
            ServiceCategoryItem(ServiceCategoryModel("education", 21), requireContext())
        )
        gAdapter.add(3, ServiceCategoryItem(ServiceCategoryModel("health", 9), requireContext()))
        gAdapter.add(4, ServiceCategoryItem(ServiceCategoryModel("identity", 15), requireContext()))
        gAdapter.add(
            5,
            ServiceCategoryItem(ServiceCategoryModel("agriculture", 8), requireContext())
        )
        gAdapter.add(6, ServiceCategoryItem(ServiceCategoryModel("ideas", 3), requireContext()))
        gAdapter.add(7, ServiceCategoryItem(ServiceCategoryModel("vehicles", 8), requireContext()))

        return rootView
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ServiceCategories().apply {
                arguments = Bundle().apply {

                }
            }
    }
}