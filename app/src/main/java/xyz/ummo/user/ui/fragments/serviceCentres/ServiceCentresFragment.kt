package xyz.ummo.user.ui.fragments.serviceCentres

import android.app.Activity
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_service_centres_rv.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ProductEntity
import xyz.ummo.user.databinding.FragmentServiceCentresRvBinding
import xyz.ummo.user.delegate.GetProducts
import xyz.ummo.user.delegate.PublicService
import xyz.ummo.user.models.PublicServiceData
import xyz.ummo.user.models.ServiceCentre
import xyz.ummo.user.rvItems.ServiceCentreItem
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel
import kotlin.collections.ArrayList

class ServiceCentresFragment : Fragment() {

    /** ServiceCentresFragment View Binder **/
    private lateinit var serviceCentresRvBinding: FragmentServiceCentresRvBinding

    /** Groupie Adapter - for quickly rendering recycler-views **/
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var recyclerView: RecyclerView
    private var publicServiceData: ArrayList<PublicServiceData> = ArrayList()
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private val mode = Activity.MODE_PRIVATE
    private lateinit var productData: JSONObject
    private lateinit var productArray: JSONArray
    private var progress: ProgressDialog? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"

    private val productEntity = ProductEntity()
    private var detailedProductViewModel: DetailedProductViewModel? = null

    companion object {
        fun newInstance() = ServiceCentresFragment()
    }

    fun newInstance(param2: String?): ServiceCentresFragment? {
        val fragment = ServiceCentresFragment()
        val args = Bundle()
        args.putString(ARG_PARAM1, "param1")
        args.putString(ARG_PARAM2, param2)
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progress = ProgressDialog(requireContext())

        getServiceCentreData()
        Timber.e("GOT SERVICE CENTRE DATA ->%s", publicServiceData)
        detailedProductViewModel = ViewModelProvider((context as FragmentActivity?)!!).get(DetailedProductViewModel::class.java)

        //Init GroupAdapter
        gAdapter = GroupAdapter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        serviceCentresRvBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_service_centres_rv,
                container, false)

        val view = serviceCentresRvBinding.root
        val layoutManager = view.service_centre_recycler_view.layoutManager

        recyclerView = view.service_centre_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = gAdapter
        //TODO: handle empty adapter instances (i.e., no views available)

        return view
    }

    override fun onPause() {
        super.onPause()
        Timber.e("PAUSING...")
    }

    private fun getServiceCentreData() {

        object : PublicService(requireActivity()) {
            override fun done(data: List<PublicServiceData>, code: Number) {

                Timber.e("LENGTH -> ${data.size}")

                if (code == 200) {
                    publicServiceData.addAll(data)
                    Timber.e(" GETTING SERVICE CENTRE DATA ->%s", publicServiceData)

                    getProductData(requireActivity(), publicServiceData[0].serviceCode)

                    serviceCentresRvBinding.loadProgressBar.visibility = View.GONE
                }
                //TODO: handle incident when response code is not 200
            }
        }
    }

    private fun getProductData(mActivity: Activity, mServiceCentreId: String) {
        object : GetProducts(mActivity, mServiceCentreId) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    try {
                        val sharedPreferences = requireContext()
                                .getSharedPreferences(ummoUserPreferences, mode)
                        val editor: SharedPreferences.Editor
                        editor = sharedPreferences.edit()

                        var productId = ""
                        var productName = ""
                        var productDescription = ""
                        var productCost = ""
                        var productDuration = ""
                        var productStepsJSONArray: JSONArray
                        val productStepsArrayList = java.util.ArrayList(listOf<String>())
                        var productDocsJSONArray: JSONArray
                        val productDocsArrayList = java.util.ArrayList(listOf<String>())

                        /**Assigning the DATA we receive from the server to #productArray (JSON Array)**/
                        productArray = JSONArray(String(data))

                        for (i in 0 until publicServiceData.size) {

                            val singleServiceCentreName = publicServiceData[i].serviceName
                            val singleServiceCentreLocation = publicServiceData[i].town

                            for (j in 0 until productArray.length()) {
                                /** Pulling apart productArray and inserting it into productData **/
                                productData = productArray.getJSONObject(j)
                            }

                            /** For convenience, we're storing the data units in the #productData
                             * JSON Object in unit variables **/
                            productId = productData.getString("_id")
                            productName = productData.getString("product_name")
                            productDescription = productData.getString("product_description")
                            productCost = productData.getJSONObject("requirements")
                                    .getString("procurement_cost")
                            productDuration = productData.getString("duration")
                            productStepsJSONArray = productData.getJSONArray("procurement_process")

                            /** Storing the #productId into a shared preference **/
                            editor.putString("PRODUCT_ID", productId)
                            editor.apply()

                            /** Assigning JSONArray to ArrayList **/
                            for (k in 0 until productStepsJSONArray.length()) {
                                productStepsArrayList.add(productStepsJSONArray.getString(k))
                            }

                            /** Assigning DOCUMENTS from #productData to #productDocsJSONArray **/
                            productDocsJSONArray = productData.getJSONObject("requirements")
                                    .getJSONArray("documents")

                            /** Converting #productDocsJSONArray to #productDocsArrayList - to store
                             *  into RoomDB via #productEntity **/
                            for (m in 0 until productDocsJSONArray.length()) {
                                productDocsArrayList.add(productDocsJSONArray.getString(m))
                            }

                            /** Creating a blob of #ServiceCentre to add into #gAdapter **/
                            val singleServiceCentre = ServiceCentre(singleServiceCentreName,
                                    singleServiceCentreLocation, productName,
                                    productDescription)

//                            Timber.e(" Single Service Centre -> $singleServiceCentre")
                            /** Adding ServiceCentreItem into #gAdapter **/
                            gAdapter.add(ServiceCentreItem(singleServiceCentre, context))
                        }
                        recyclerView.adapter = gAdapter

                        Timber.e("Product Data->%s", productData)

                        /** Inserting Product into Room **/
                        Timber.e("Product Steps -> $productStepsArrayList")
                        Timber.e("Product DOCS -> $productDocsArrayList")
                        Timber.e("Product Duration -> $productDuration")
                        Timber.e("Product Docs -> ${
                            productData
                                    .getJSONObject("requirements").getJSONArray("documents")
                        }")
                        productEntity.productId = productId
                        productEntity.productName = productName
                        productEntity.productDescription = productDescription
                        productEntity.productCost = productCost
                        productEntity.productSteps = productStepsArrayList
                        productEntity.productDocuments = productDocsArrayList
                        productEntity.productDuration = productDuration
                        productEntity.isDelegated = false
                        detailedProductViewModel!!.insertProduct(productEntity)

                    } catch (jse: JSONException) {
                        Timber.e("JSE -> $jse")
                    }
                }
            }
        }
    }
}