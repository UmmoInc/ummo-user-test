package xyz.ummo.user.ui.detailedService

//import kotlinx.android.synthetic.main.service_card.view.*
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.content_detailed_service.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.RequestService
import xyz.ummo.user.api.User
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProductEntity
import xyz.ummo.user.databinding.ActivityDetailedServiceBinding
import xyz.ummo.user.databinding.ContentDetailedServiceBinding
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.ui.MainScreen.Companion.DELEGATION_FEE
import xyz.ummo.user.ui.MainScreen.Companion.SERVICE_OBJECT
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceFeeQuery
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.ConfirmPaymentTermsEvent
import java.util.*
import kotlin.collections.ArrayList

class DetailedServiceActivity : AppCompatActivity() {
    var nestedScrollView: NestedScrollView? = null
    var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    var serviceDescriptionTextView: TextView? = null
    var serviceNameTextView: TextView? = null
    var serviceCostTextView: TextView? = null
    var serviceDurationTextView: TextView? = null
    var serviceDocsTextView: TextView? = null
    var serviceStepsTextView: TextView? = null
    var serviceDocsChipGroup: ChipGroup? = null
    var serviceCentresChipGroup: ChipGroup? = null
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
    var serviceCentresList: ArrayList<String>? = null
    private var agentRequestStatus = "Requesting agent..."
    private var agentDelegate = JSONObject()
    private var agentName: String? = null
    private var serviceId: String? = null
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
    private lateinit var detailedServiceBinding: ActivityDetailedServiceBinding
    private lateinit var detailedServiceContentBinding: ContentDetailedServiceBinding
    private lateinit var serviceObject: ServiceObject
    private val paymentTermsEvent = ConfirmPaymentTermsEvent()
    private lateinit var detailedServicePrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var mixpanel: MixpanelAPI

    private var serviceCostAdapter: ArrayAdapter<ServiceCostModel>? = null
    private var serviceCostSpinner: Spinner? = null
    private var serviceCostArrayList = ArrayList<ServiceCostModel>()
    private lateinit var serviceCostItem: ServiceCostModel
    private var serviceSpec = ""
    private var specCost = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mixpanel = MixpanelAPI.getInstance(this,
                resources.getString(R.string.mixpanelToken))

        /** Binding Layout Views **/
        detailedServiceBinding = ActivityDetailedServiceBinding.inflate(layoutInflater)
        detailedServiceContentBinding = ContentDetailedServiceBinding.inflate(layoutInflater)

        val view = detailedServiceBinding.root

        setContentView(view)
        toolbar = findViewById(R.id.toolbar_detailed_service)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        progress = ProgressDialog(this)
        agentRequestDialog = AlertDialog.Builder(this@DetailedServiceActivity)
        agentNotFoundDialog = AlertDialog.Builder(this@DetailedServiceActivity)

        nestedScrollView = findViewById(R.id.nested_scrollview)
        requestAgentBtn = findViewById(R.id.request_agent_btn)
        mCollapsingToolbarLayout = findViewById(R.id.toolbar_collapsing_layout)
        val appBar = findViewById<AppBarLayout>(R.id.app_bar_layout)
        serviceNameTextView = findViewById(R.id.detailed_service_name_text_view)
        serviceDescriptionTextView = findViewById(R.id.detailed_description_text_view)
//        serviceCostTextView = findViewById(R.id.detailed_service_cost_text_view)
        serviceDurationTextView = findViewById(R.id.detailed_service_duration_text_view)
        serviceDocsChipGroup = findViewById(R.id.detailed_service_docs_chip_group)
        serviceCentresChipGroup = findViewById(R.id.detailed_service_centres_chip_group)

        appBar.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (mCollapsingToolbarLayout!!.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(mCollapsingToolbarLayout!!)) {
                toolbar!!.navigationIcon!!.setColorFilter(resources.getColor(R.color.black), PorterDuff.Mode.SRC_ATOP)
            } else {
                toolbar!!.navigationIcon!!.setColorFilter(resources.getColor(R.color.UmmoPurple), PorterDuff.Mode.SRC_ATOP)
            }
        })

        /** Assigning [serviceObject] with the [serviceObject] we receive from ServiceItem **/
        serviceObject = intent.extras!!.get(SERVICE_OBJECT) as ServiceObject
        serviceId = serviceObject.serviceId

        populateDetailedServiceElements(serviceObject)

        detailedServicePrefs = getSharedPreferences(ummoUserPreferences, mode)

        editor = detailedServicePrefs.edit()

        detailedProductViewModel = ViewModelProvider(this).get(DetailedProductViewModel::class.java)
        delegatedServiceViewModel = ViewModelProvider(this).get(DelegatedServiceViewModel::class.java)

        /** Instantiating Service Cost Spinner **/
        addListenerOnSpinnerItemSelected()
        serviceCostSpinner = findViewById(R.id.detailed_service_cost_spinner)
        serviceCostSpinner!!.prompt = "Choose your Service Cost"
        serviceCostAdapter = ArrayAdapter(this,
                R.layout.support_simple_spinner_dropdown_item, serviceCostArrayList)
        serviceCostSpinner?.adapter = serviceCostAdapter

        checkForDelegatedServiceAndCompare()

        requestAgentBtn!!.setOnClickListener {

            mixpanel.track("detailedServiceAct_requestAgentButtonTapped")
            makeRequest()
        }

        mCollapsingToolbarLayout!!.title = _serviceName
        mCollapsingToolbarLayout!!.setExpandedTitleTextAppearance(R.style.ExpandedAppBar)
        mCollapsingToolbarLayout!!.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar)

        detailedServiceFeeQuery()
    }

    private fun checkForDelegatedServiceAndCompare() {
        delegatedServiceViewModel = ViewModelProvider(this).get(DelegatedServiceViewModel::class.java)
        val countOfDelegatedServices = delegatedServiceViewModel!!.getCountOfDelegatedServices()

        if (countOfDelegatedServices > 0) {
            delegatedServiceViewModel!!.delegatedServiceEntityLiveData.observe(this, { delegatedServiceEntity ->
                val delegatedServiceId = delegatedServiceEntity.delegatedProductId
                val serviceAgent = detailedServicePrefs.getString(SERVICE_AGENT_ID, SERVICE_AGENT_ID)!!
                val delegationId = detailedServicePrefs.getString(DELEGATED_SERVICE_ID, DELEGATED_SERVICE_ID)!!
                if (serviceObject.serviceId == delegatedServiceId) {
                    requestAgentBtn?.text = "VIEW PROGRESS"
                    requestAgentBtn?.isActivated = false
                    requestAgentBtn?.setBackgroundColor(this.resources.getColor(R.color.ummo_3))

                    requestAgentBtn?.setOnClickListener {
                        launchDelegatedService(this@DetailedServiceActivity,
                                delegatedServiceId, serviceAgent, delegationId)
                    }
                }
            })

        } else {
            Timber.e("NO DELEGATED SERVICES")
        }
    }

    private fun detailedServiceFeeQuery() {
        val detailedServiceFeeQueryLayout = findViewById<RelativeLayout>(R.id.detailed_service_query_icon_relative_layout)
        val detailedServiceFeeQueryIcon = findViewById<ImageView>(R.id.detailed_query_image_view)

        detailedServiceFeeQueryLayout.setOnClickListener { openServiceFeeSelfSupport() }
        detailedServiceFeeQueryIcon.setOnClickListener { openServiceFeeSelfSupport() }
    }


    private fun openServiceFeeSelfSupport() {
        val serviceFeeQuery = ServiceFeeQuery()
        serviceFeeQuery.show(this.supportFragmentManager, ServiceFeeQuery.TAG)
        mixpanel.track("detailedService_serviceFeeSelfSupport")

    }
    private fun addListenerOnSpinnerItemSelected() {

        serviceCostSpinner = findViewById(R.id.detailed_service_cost_spinner)
        serviceCostSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                serviceCostItem = ServiceCostModel(serviceCostArrayList[position].serviceSpec,
                        serviceCostArrayList[position].specCost)

                Timber.e("SELECTED SERVICE-COST -> $serviceCostItem")
                serviceSpec = serviceCostItem.serviceSpec
                specCost = serviceCostItem.specCost.toString()

                val serviceSpecCost = JSONObject()
                serviceSpecCost
                        .put("SERVICE_SPEC", serviceSpec)
                        .put("SPEC_COST", specCost)
                mixpanel.track("detailedService_serviceSpecSelected", serviceSpecCost)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                Timber.e("NOTHING SELECTED!")
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

    private fun populateDetailedServiceElements(mService: ServiceObject) {
        Timber.e("UNPACKING SERVICE INTO UI ELEMENTS $mService")

        toolbar!!.title = mService.serviceName
        mCollapsingToolbarLayout!!.title = mService.serviceName
        serviceNameTextView!!.text = mService.serviceName
        serviceDescriptionTextView!!.text = mService.serviceDescription

        /** Filling up [serviceCostArrayList] with [mService]'s serviceCost **/

        serviceCostArrayList = mService.serviceCost
        serviceDurationTextView!!.text = mService.serviceDuration

        if (mService.delegatable) {
            requestAgentBtn!!.visibility = View.VISIBLE
        } else {
            requestAgentBtn!!.visibility = View.GONE
        }

        docsList = ArrayList(serviceObject.serviceDocuments)
        Timber.e("DOCS-LIST -> $docsList")

        /** Parsing Service Docs into Chip-items **/
        if (docsList!!.isNotEmpty()) {

            serviceDocsChipGroup!!.removeAllViews()
            for (i in docsList!!.indices) {
                val serviceDocsChipItem = LayoutInflater.from(this)
                        .inflate(R.layout.service_centre_chip_item, null, false) as Chip

                serviceDocsChipItem.text = docsList!![i].replace("\"\"", "")
                serviceDocsChipItem.textSize = 12F
                serviceDocsChipGroup!!.addView(serviceDocsChipItem)
            }
        } else {
            Timber.e("onCreate: docsList is EMPTY!")
        }

        /** Parsing Service Centres into Chip-items **/
        serviceCentresList = ArrayList(serviceObject.serviceCentres)
        Timber.e("CENTRES-LIST -> $serviceCentresList")

        if (serviceCentresList!!.isNotEmpty()) {

            serviceCentresChipGroup!!.removeAllViews()
            for (i in serviceCentresList!!.indices) {
                val serviceCentreChipItem = LayoutInflater.from(this)
                        .inflate(R.layout.service_centre_chip_item, null, false) as Chip

                serviceCentreChipItem.text = serviceCentresList!![i].replace("\"\"", "")
                serviceCentreChipItem.textSize = 12F
                serviceCentresChipGroup!!.addView(serviceCentreChipItem)
            }
        } else {
            Timber.e("onCreate: docsList is EMPTY!")
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

    @SuppressLint("SetTextI18n")
    private fun makeRequest() {
        val mixpanel = MixpanelAPI.getInstance(this,
                resources.getString(R.string.mixpanelToken))

        val alertDialogBuilder = MaterialAlertDialogBuilder(this)

        val alertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.delegate_agent_dialog, null)

        val delegationFee = JSONObject()

        /** Formatting and appending service texts to alertDialog **/
        val requestAgentText = String.format(this.resources.getString(R.string.request_ummo_agent), serviceObject.serviceName)
        val requestAgentTextView = alertDialogView.findViewById<TextView>(R.id.request_description_text_view)
        val serviceCostTextView = alertDialogView.findViewById<TextView>(R.id.service_cost_text_view)
        val delegationCostTextView = alertDialogView.findViewById<TextView>(R.id.delegation_cost_text_view)
        val totalCostTextView = alertDialogView.findViewById<TextView>(R.id.total_cost_text_view)
        val confirmPaymentCheckBox = alertDialogView.findViewById<CheckBox>(R.id.confirm_payment_check_box)

        requestAgentTextView.text = requestAgentText

        serviceCostTextView.text = "E$specCost"
        /** Hard coding Delegation Cost (temporarily) **/
        delegationCostTextView.text = this.getString(R.string.delegation_fee)

        /** 1) Removing the currency from the fee
         *  2) Converting fee string to int
         *  3) Adding [Delegation Fee] to get Total Cost (int)
         *  4) Displaying Total Cost**/

        val serviceCost: String = specCost
        val formattedServiceCost: String

        formattedServiceCost = if (serviceCost.contains(",")) {
            serviceCost.replace(",", "")
        } else {
            serviceCost
        }

        val serviceCostInt = Integer.parseInt(formattedServiceCost)
        val totalCostInt = serviceCostInt + 100
        totalCostTextView.text = "E$totalCostInt"

        delegationFee.put("chosen_service_spec", serviceSpec)
                .put("total_delegation_fee", totalCostInt)

        alertDialogBuilder.setTitle("Request Agent")
                .setIcon(R.drawable.logo)
                .setView(alertDialogView)

        confirmPaymentCheckBox.setOnClickListener {
            if (confirmPaymentCheckBox.isChecked) {
                paymentTermsEvent.paymentTermsConfirmed = true
                EventBus.getDefault().post(paymentTermsEvent)

            } else {
                alertDialogBuilder.setPositiveButton("Req") { dialogInterface, i ->
//                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.YELLOW)
                }
            }
        }

        alertDialogBuilder.setPositiveButton("Request") { dialogInterface, i ->
            Timber.e("Clicked Confirm!")
//                detailedProduct.requestAgentDelegate(productId)

            if (!confirmPaymentCheckBox.isChecked) {
                showSnackbarYellow("Please confirm Payment Ts & Cs", -1)
                mixpanel?.track("requestAgentDialog_unconfirmedCheckBox")
            } else {
                requestAgentDelegate(serviceId!!, delegationFee)
                mixpanel?.track("requestAgentDialog_confirmedCheckBox")
            }
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
            Timber.e("Clicked Cancel!")
        }

        alertDialogBuilder.show() //TODO: BIG BUG!!!
//            alertDialog.dismiss()
    }

    /** Requesting Agent Delegate **/
    private fun requestAgentDelegate(mServiceId: String, mDelegationFee: JSONObject) {
        val jwt = PreferenceManager.getDefaultSharedPreferences(this).getString("jwt", "")

        Timber.e("SERVICE_ID REQUEST->%s", mServiceId)

        if (jwt != null) {
            object : RequestService(this, User.getUserId(jwt), mServiceId, mDelegationFee) {
                override fun done(data: ByteArray, code: Int) {
                    Timber.e("delegatedService: Done->%s", String(data))
                    Timber.e("delegatedService: Status Code->%s", code)

                    when (code) {
                        200 -> {
//                            alertDialogBuilder.dismiss()

                            Timber.e("CODE IS $code")

                            val delegation = JSONObject(String(data))
                            Timber.e("SERVICE OBJ -> $delegation")
                            val delegatedServiceId = delegation.getString("product")
                            val delegationId = delegation.getString("_id")
                            val serviceAgent = delegation.getString("agent")

                            editor.putString("DELEGATION_ID", delegationId)
                            //TODO: remove after service is done
                            editor.putString(DELEGATED_SERVICE_ID, serviceId)
                            editor.putString(SERVICE_AGENT_ID, serviceAgent)
                            editor.putString(DELEGATION_FEE, mDelegationFee.getString("total_delegation_fee"))
                            editor.apply()

                            launchDelegatedService(this@DetailedServiceActivity,
                                    delegatedServiceId, serviceAgent, delegationId)

                        }
                        404 -> {
                            Timber.e("CODE IS $code")

                            agentRequestDialog!!.setTitle("Meh")
                            agentRequestDialog!!.setMessage("Blah Blah Blah")
                            agentRequestDialog!!.setPositiveButton("Continue...")
                            { dialogInterface: DialogInterface?, i: Int ->
                                Timber.e("GOING OFF!")

                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchDelegatedService(context: Context?,
                                       delegatedServiceId: String,
                                       agentId: String,
                                       delegationId: String) {

        val bundle = Bundle()
        bundle.putString(DELEGATED_SERVICE_ID, delegatedServiceId)
        bundle.putString(SERVICE_AGENT_ID, agentId)
        bundle.putString(DELEGATION_ID, delegationId)

        Timber.e("DELEGATION_ID -> $delegationId")
        Timber.e("DELEGATED_SERVICE_ID -> $delegatedServiceId")
        Timber.e("SERVICE_AGENT_ID -> $agentId")

        val progress = java.util.ArrayList<String>()
        val delegatedServiceEntity = DelegatedServiceEntity()
        val delegatedServiceViewModel = ViewModelProvider((context as FragmentActivity?)!!)
                .get(DelegatedServiceViewModel::class.java)

        /** Setting Service as Delegated **/
        delegatedServiceEntity.delegationId = delegationId
        delegatedServiceEntity.delegatedProductId = delegatedServiceId
        delegatedServiceEntity.serviceAgentId = agentId
        delegatedServiceEntity.serviceProgress = progress
        delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

        startActivity(Intent(this, MainScreen::class.java).putExtras(bundle))
    }

    private fun showSnackbarYellow(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val snackbar = Snackbar.make(this@DetailedServiceActivity.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.gold))

        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.show()
    }

    companion object {
        const val DELEGATED_SERVICE_ID = "DELEGATED_SERVICE_ID"
        const val SERVICE_AGENT_ID = "SERVICE_AGENT_ID"
        const val DELEGATION_ID = "DELEGATION_ID"
    }
}