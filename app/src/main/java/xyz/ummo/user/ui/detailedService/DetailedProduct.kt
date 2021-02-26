package xyz.ummo.user.ui.detailedService

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.DelegateService
import xyz.ummo.user.api.User.Companion.getUserId
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProductEntity
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import java.lang.ref.WeakReference
import java.util.*

class DetailedProduct : AppCompatActivity() {
    var nestedScrollView: NestedScrollView? = null
    var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    var serviceDescriptionTextView: TextView? = null
    var serviceCostTextView: TextView? = null
    var serviceDurationTextView: TextView? = null
    var serviceDocsTextView: TextView? = null
    var serviceStepsTextView: TextView? = null
    var serviceDocsLayout: LinearLayout? = null
    var serviceStepsLayout: LinearLayout? = null
    var toolbar: Toolbar? = null
    var requestAgentBtn: Button? = null

    private val greenResponse = false
    private var detailedProductViewModel: DetailedProductViewModel? = null
    private var delegatedServiceViewModel: DelegatedServiceViewModel? = null
    private val productEntity = ProductEntity()
    private val delegatedServiceEntity = DelegatedServiceEntity()
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private val mode = MODE_PRIVATE
    var stepsList: ArrayList<String>? = null
    var docsList: ArrayList<String>? = null
    private var agentRequestStatus = "Requesting agent..."
    private var agentDelegate = JSONObject()
    private var agentName: String? = null
    private val serviceId: String? = null
    private val delegatedProductId: String? = null
    private val serviceProgress: String? = null
    private var _serviceName: String? = null
    private var _description: String? = null
    private var _cost: String? = null
    private val _duration: String? = null
    private val _steps: String? = null
    private val _docs: String? = null
    private var progress: ProgressDialog? = null
    var agentRequestDialog: AlertDialog.Builder? = null
    var agentNotFoundDialog: AlertDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_service)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        val mixpanel = MixpanelAPI.getInstance(this,
                resources.getString(R.string.mixpanelToken))

        progress = ProgressDialog(this)
        agentRequestDialog = AlertDialog.Builder(this@DetailedProduct)
        agentNotFoundDialog = AlertDialog.Builder(this@DetailedProduct)
        nestedScrollView = findViewById(R.id.nested_scrollview)
        requestAgentBtn = findViewById(R.id.request_agent_btn)
        mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout)
        val appBar = findViewById<AppBarLayout>(R.id.app_bar)
        serviceDescriptionTextView = findViewById(R.id.description_text_view)
        serviceCostTextView = findViewById(R.id.service_cost_text_view)
        serviceDurationTextView = findViewById(R.id.service_duration_text_view)
        serviceDocsLayout = findViewById(R.id.service_docs_linear_layout)
        serviceStepsLayout = findViewById(R.id.service_steps_linear_layout)

        appBar.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (mCollapsingToolbarLayout!!.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(mCollapsingToolbarLayout!!)) {
                toolbar!!.navigationIcon!!.setColorFilter(resources.getColor(R.color.black), PorterDuff.Mode.SRC_ATOP)
            } else {
                toolbar!!.navigationIcon!!.setColorFilter(resources.getColor(R.color.UmmoPurple), PorterDuff.Mode.SRC_ATOP)
            }
        })
        val detailedProductPrefs = getSharedPreferences(ummoUserPreferences, mode)
        val editor: SharedPreferences.Editor
        editor = detailedProductPrefs.edit()

        detailedProductViewModel = ViewModelProviders.of(this)
                .get(DetailedProductViewModel::class.java)
        delegatedServiceViewModel = ViewModelProviders.of(this).get(DelegatedServiceViewModel::class.java)
        val _productId = intent.getStringExtra("product_id")
        val _serviceId = intent.getStringExtra("_id")
        if (_productId != null) {
            detailedProductViewModel!!.getProductEntityLiveDataById(_productId).observe(this, { productEntity1: ProductEntity ->
                _serviceName = productEntity1.productName
                Timber.e("onCreate: Within ProductVM: ProductModel ID->%s", _productId)
                _description = productEntity1.productDescription
                _cost = productEntity1.productCost
                //                _duration = productEntity1.getProductDuration();
                stepsList = ArrayList(productEntity1.productSteps)

//                docsList = new ArrayList<>(productEntity1.getProductDocuments());

                //Filling in UI components
                toolbar!!.title = _serviceName
                serviceDescriptionTextView!!.text = _description
                serviceCostTextView!!.text = _cost
                serviceDurationTextView!!.text = _duration
                if (stepsList!!.isNotEmpty()) {
                    serviceStepsLayout!!.removeAllViews()
                    for (i in stepsList!!.indices) {
                        Timber.e("onCreate: stepsList->%s", stepsList)
                        serviceStepsTextView = TextView(applicationContext)
                        serviceStepsTextView!!.id = i
                        serviceStepsTextView!!.text = stepsList!![i]
                        serviceStepsTextView!!.textSize = 14f
                        serviceStepsLayout!!.addView(serviceStepsTextView)
                    }
                } else {
                    Timber.e("onCreate: stepsList is EMPTY!")
                }
                if (docsList!!.isNotEmpty()) {
                    serviceDocsLayout!!.removeAllViews()
                    for (i in docsList!!.indices) {
                        Timber.e("onCreate: docsList->%s", docsList)
                        serviceDocsTextView = TextView(applicationContext)
                        serviceDocsTextView!!.id = i
                        serviceDocsTextView!!.text = docsList!![i].replace("\"\"", "")
                        serviceDocsTextView!!.textSize = 14f
                        serviceDocsLayout!!.addView(serviceDocsTextView)
                    }
                } else {
                    Timber.e("onCreate: docsList is EMPTY!")
                }
                Timber.e("onCreate: isDelegated%s", productEntity1.isDelegated)
            })
        } else if (_serviceId != null) {
            Timber.e("onCreate: SERVICE-ID->%s", _serviceId)
        }
        val arrayAdapter = ArrayAdapter(this, R.layout.steps_list, R.id.step, stepsList!!)
        requestAgentBtn!!.setOnClickListener(View.OnClickListener { v: View? -> requestAgentDelegate(_productId) })
        mCollapsingToolbarLayout!!.title = _serviceName
        mCollapsingToolbarLayout!!.setExpandedTitleTextAppearance(R.style.ExpandedAppBar)
        mCollapsingToolbarLayout!!.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar)
    }

    private fun requestAgentDelegate(mProductId: String?) {
        val detailedProductPrefs = getSharedPreferences(ummoUserPreferences, mode)
        val editor: SharedPreferences.Editor
        editor = detailedProductPrefs.edit()
        /*if (mixpanel != null) {
                mixpanel.track("requestAgentTapped");
            }*/progress!!.setTitle("Agent Request")
        progress!!.setMessage(agentRequestStatus)
        progress!!.show()
        val jwt = PreferenceManager.getDefaultSharedPreferences(this@DetailedProduct).getString("jwt", "")
        if (jwt != null) {
            assert(mProductId != null)
            object : DelegateService(this@DetailedProduct, getUserId(jwt), mProductId!!) {
                override fun done(data: ByteArray, code: Int) {
                    Timber.e("delegatedService: Done->%s", String(data))
                    Timber.e("delegatedService: Status Code->%s", code)
                    progress!!.dismiss()
                    if (code == 200) {
                        try {
                            agentDelegate = JSONObject(String(data))
                            agentName = agentDelegate.getString("name")
                            Timber.e("done: agentName->%s", agentName)
                            agentRequestDialog!!.setTitle("Agent Delegate")
                            //                                agentRequestDialog.setIcon()
                            agentRequestDialog!!.setMessage("$agentName is available...")
                            agentRequestDialog!!.setPositiveButton("Continue") { dialog: DialogInterface?, which: Int ->

                                /*if (mixpanel != null) {
                                        mixpanel.track("requestAgentContinue");
                                    }*/agentRequestStatus = "Waiting for a response from $agentName..."
                                val progress = ProgressDialog(this@DetailedProduct)
                                progress.setTitle("Agent Request")
                                progress.setMessage(agentRequestStatus)
                                progress.show() // TODO: 10/22/19 -> handle leaking window
                                editor.clear() //Removing old key-values from a previous session
                                editor.putString("DELEGATED_AGENT", agentName)
                                editor.putString("DELEGATED_PRODUCT", mProductId)
                                editor.apply()
                            }
                            agentRequestDialog!!.show()
                            detailedProductViewModel!!.getProductEntityLiveDataById(mProductId)
                                    .observe(this@DetailedProduct, { productEntity1: ProductEntity ->
                                        productEntity1.isDelegated = true
                                        Timber.e("done: isDelegated-> TRUE")
                                    })
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else if (code == 404) {
                        Timber.e("done: Status Code 500!!!")
                        agentNotFoundDialog!!.setTitle("Agent Delegate")
                        agentNotFoundDialog!!.setMessage("No Agent currently available.")
                        agentNotFoundDialog!!.setPositiveButton("Dismiss") { dialog: DialogInterface?, which: Int ->

                            /*if (mixpanel != null) {
                                    mixpanel.track("requestAgentDismiss");
                                }*/Timber.e("done: Dismissed!")
                            requestAgentBtn!!.text = resources.getString(R.string.retry_agent_request)
                        }
                        agentNotFoundDialog!!.show()
                    } else {
                        Timber.e("done: Status Code 500!!!")
                        Toast.makeText(this@DetailedProduct, "BOMDAS!", Toast.LENGTH_LONG).show()
                        agentNotFoundDialog!!.setTitle("Agent Delegate")
                        agentNotFoundDialog!!.setMessage("We honestly don't know what happened, please check if there is internet")
                        agentNotFoundDialog!!.setPositiveButton("Dismiss") { dialog: DialogInterface?, which: Int ->
                            Timber.e("done: Dismissed!")
                            requestAgentBtn!!.text = "RETRY AGENT REQUEST"
                        }
                        agentNotFoundDialog!!.show()
                    }
                    //                            progress.setMessage(getResources().getString(R.string.loading_agent_message));
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onPause() {
        super.onPause()
        progress!!.dismiss()
        agentRequestDialog!!.setOnDismissListener { Timber.e("onPause: onDialogDismiss!") }
        agentNotFoundDialog!!.setOnDismissListener { Timber.e("onPause: onDialogDismiss!") }
        finish()
    }

    private fun launchDelegatedService() {
        val serviceId = intent.getStringExtra("SERVICE_ID")!!
        if (!serviceId.isEmpty()) {
            Timber.e("newDelegatedService Bundle->%s", serviceId)
        } else {
            Timber.e("newDelegatedService NO Bundle!")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(this, MainScreen::class.java)
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private open class DelegateAsyncTask(activity: DetailedProduct) : AsyncTask<Int?, Void?, String>() {
        private val detailedProductWeakReference: WeakReference<DetailedProduct> = WeakReference(activity)
        var newAgentRequestStatus = ""

        override fun doInBackground(vararg integers: Int?): String {
            for (i in 0 until integers[0]!!) {
                Timber.e("doInBackGround: %s", i)
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Timber.e(e, "doInBackGroundException")
                    e.printStackTrace()
                }
            }
            return newAgentRequestStatus
        }

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            val detailedProduct = detailedProductWeakReference.get()
            if (detailedProduct == null || detailedProduct.isFinishing) {
                return
            }
            detailedProduct.agentRequestStatus = newAgentRequestStatus
        }


    }

    companion object {
        private const val TAG = "DetailedProduct"
    }
}