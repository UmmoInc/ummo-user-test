package xyz.ummo.user.ui.fragments.categories

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_service_categories.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.GetCategorySummary
import xyz.ummo.user.data.entity.ServiceCategoryEntity
import xyz.ummo.user.databinding.FragmentServiceCategoriesBinding
import xyz.ummo.user.models.ServiceCategoryModel
import xyz.ummo.user.rvItems.ServiceCategoryItem
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
    private var serviceCategoriesViewModel: ServiceCategoriesViewModel? = null
    private val serviceCategoryEntity = ServiceCategoryEntity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

        getCatSum(requireContext())

        mixpanelAPI = MixpanelAPI
            .getInstance(context, context?.resources?.getString(R.string.mixpanelToken))

        serviceCategoriesViewModel =
            ViewModelProvider(this)[ServiceCategoriesViewModel::class.java]

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

        /** Scaffolding the [recyclerView] **/
        recyclerView = rootView.service_category_recycler_view
        recyclerView.setHasFixedSize(true)
//        recyclerView.layoutManager = rootView.service_category_recycler_view.layoutManager
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = gAdapter

        viewBinding.homeBarTitleTextView.text = "Welcome, $firstName"

        return rootView
    }

    private fun getCatSum(context: Context) {
        object : GetCategorySummary(requireActivity()) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    Timber.e("CATEGORY SERVICE COUNT -> ${String(data)}")

                    gAdapter = GroupAdapter()
                    recyclerView.adapter = gAdapter

                    try {
                        val categorySummary = JSONObject(String(data))
                        val categoryObjects: JSONArray = categorySummary.getJSONArray("payload")
                        var categoryObject: JSONObject

                        for (j in 0 until categoryObjects.length()) {
                            categoryObject = categoryObjects[j] as JSONObject

                            serviceCategoryEntity.serviceCategory = categoryObject.getString("_id")
                            serviceCategoryEntity.serviceCount = categoryObject.getInt("total")
                            serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)
                        }

                        for (i in 0 until categoryObjects.length()) {
                            categoryObject = categoryObjects[i] as JSONObject
                            Timber.e("CATEGORY OBJECT -> $categoryObject")

                            when {
                                /** 1. VEHICLES **/
                                categoryObject.get("_id") == "vehicles" -> {
                                    totalVehicles = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "VEHICLES"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                                /** 2. BUSINESS **/
                                categoryObject.get("_id") == "business" -> {
                                    totalBusiness = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "BUSINESS"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                                /** 3. IDEAS **/
                                categoryObject.get("_id") == "ideas" -> {
                                    totalIdeas = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "IDEAS"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                                /** 4. IDENTITY **/
                                categoryObject.get("_id") == "identity" -> {
                                    totalIdentity = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "IDENTITY"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                                /** 5. EDUCATION **/
                                categoryObject.get("_id") == "education" -> {
                                    totalEducation = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "EDUCATION"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                                /** 6. AGRICULTURE **/
                                categoryObject.get("_id") == "agriculture" -> {
                                    totalAgriculture = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "AGRICULTURE"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                                /** 7. HEALTH **/
                                categoryObject.get("_id") == "health" -> {
                                    totalHealth = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "HEALTH"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                                /** 8. TRAVEL **/
                                categoryObject.get("_id") == "travel" -> {
                                    totalTravel = categoryObject.getInt("total")

                                    /*serviceCategoryEntity.serviceCategory = "TRAVEL"
                                    serviceCategoryEntity.serviceCount =
                                        categoryObject.getInt("total")
                                    serviceCategoriesViewModel?.insertCategory(serviceCategoryEntity)*/
                                }
                            }

                            gAdapter.clear()
                            gAdapter.add(
                                0,
                                ServiceCategoryItem(
                                    ServiceCategoryModel("vehicles", totalVehicles),
                                    context
                                )
                            )
                            gAdapter.add(
                                1,
                                ServiceCategoryItem(
                                    ServiceCategoryModel("business", totalBusiness),
                                    context
                                )
                            )
                            gAdapter.add(
                                2,
                                ServiceCategoryItem(
                                    ServiceCategoryModel("ideas", totalIdeas),
                                    context
                                )
                            )
                            gAdapter.add(
                                3,
                                ServiceCategoryItem(
                                    ServiceCategoryModel("identity", totalIdentity),
                                    context
                                )
                            )
                            gAdapter.add(
                                4,
                                ServiceCategoryItem(
                                    ServiceCategoryModel(
                                        "education",
                                        totalEducation
                                    ), context
                                )
                            )
                            gAdapter.add(
                                5,
                                ServiceCategoryItem(
                                    ServiceCategoryModel(
                                        "agriculture",
                                        totalAgriculture
                                    ), context
                                )
                            )
                            gAdapter.add(
                                6,
                                ServiceCategoryItem(
                                    ServiceCategoryModel("health", totalHealth),
                                    context
                                )
                            )
                            gAdapter.add(
                                7,
                                ServiceCategoryItem(
                                    ServiceCategoryModel("travel", totalTravel),
                                    context
                                )
                            )
                            viewBinding.loadCategoriesProgressBar.visibility = View.GONE

                        }
                    } catch (jse: JSONException) {
                        Timber.e("THROWING JSE -> $jse")
                    }
                }
            }
        }
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