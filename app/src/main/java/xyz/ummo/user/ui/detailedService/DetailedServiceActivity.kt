package xyz.ummo.user.ui.detailedService

//import kotlinx.android.synthetic.main.service_card.view.*
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.content_delegation_progress.*
import kotlinx.android.synthetic.main.content_detailed_service.*
import kotlinx.android.synthetic.main.service_card.view.*
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProductEntity
import xyz.ummo.user.databinding.ActivityDetailedServiceBinding
import xyz.ummo.user.databinding.ContentDetailedServiceBinding
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.ui.MainScreen.Companion.SERVICE_OBJECT
import xyz.ummo.user.ui.WebViewActivity
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceFeeQuery
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceRequestBottomSheet
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.ConfirmPaymentTermsEvent
import xyz.ummo.user.utilities.eventBusEvents.ServiceSpecifiedEvent
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
    var serviceCentresTextView: TextView? = null
    var serviceStepsTextView: TextView? = null

    //    var serviceDocsChipGroup: ChipGroup? = null
    var serviceDocsLayout: LinearLayout? = null

    //    var serviceCentresChipGroup: ChipGroup? = null
    var serviceCentresLinearLayout: LinearLayout? = null
    var serviceCentreRadioButton: RadioButton? = null
    var toolbar: Toolbar? = null
    var requestAgentBtn: Button? = null
    var webViewLink: TextView? = null

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
    private val serviceSpecifiedEvent = ServiceSpecifiedEvent()

    private lateinit var detailedServicePrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var mixpanel: MixpanelAPI

    private var serviceCostAdapter: ArrayAdapter<ServiceCostModel>? = null
    private var serviceCostSpinner: Spinner? = null
    private var serviceCostTextInputLayout: TextInputLayout? = null
    private var serviceCostArrayList = ArrayList<ServiceCostModel>()
    private lateinit var serviceCostItem: ServiceCostModel
    private var serviceSpec = ""
    private var specCost = ""
    private var chosenServiceCentre = ""

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
        webViewLink = findViewById(R.id.link_source_text_view)
        mCollapsingToolbarLayout = findViewById(R.id.toolbar_collapsing_layout)
        val appBar = findViewById<AppBarLayout>(R.id.app_bar_layout)
        serviceNameTextView = findViewById(R.id.detailed_service_name_text_view)
        serviceDescriptionTextView = findViewById(R.id.detailed_description_text_view)
//        serviceCostTextView = findViewById(R.id.detailed_service_cost_text_view)
        serviceDurationTextView = findViewById(R.id.detailed_service_duration_text_view)
        serviceDocsLayout = findViewById(R.id.service_requirements_linear_layout)
//        serviceCentresChipGroup = findViewById(R.id.detailed_service_centres_chip_group)
        serviceCentresLinearLayout = findViewById(R.id.service_centre_linear_layout)

        appBar.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (mCollapsingToolbarLayout!!.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(mCollapsingToolbarLayout!!)) {
                toolbar!!.navigationIcon!!.setColorFilter(resources.getColor(R.color.black), PorterDuff.Mode.SRC_ATOP)
            } else {
                toolbar!!.navigationIcon!!.setColorFilter(resources.getColor(R.color.UmmoPurple), PorterDuff.Mode.SRC_ATOP)
            }
        })

        /** Assigning [serviceObject] with the [serviceObject] we receive from ServiceItem **/
        serviceObject = intent.extras!!.get(SERVICE_OBJECT) as ServiceObject
        Timber.e("SERVICE OBJECT -> ${serviceObject.serviceLink}")
        serviceId = serviceObject.serviceId

        populateDetailedServiceElements(serviceObject)

        detailedServicePrefs = getSharedPreferences(ummoUserPreferences, mode)

        editor = detailedServicePrefs.edit()

        detailedProductViewModel = ViewModelProvider(this).get(DetailedProductViewModel::class.java)
        delegatedServiceViewModel = ViewModelProvider(this).get(DelegatedServiceViewModel::class.java)

        /** Instantiating Service Cost Spinner **/
//TODO: undo        addListenerOnSpinnerItemSelected()
//        serviceCostSpinner = findViewById(R.id.detailed_service_cost_dropdown)

        serviceCostTextInputLayout = findViewById(R.id.detailed_service_cost_dropdown)

        selectingServiceSpec()

        checkForDelegatedServiceAndCompare()

        mCollapsingToolbarLayout!!.title = _serviceName
        mCollapsingToolbarLayout!!.setExpandedTitleTextAppearance(R.style.ExpandedAppBar)
        mCollapsingToolbarLayout!!.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar)

        detailedServiceFeeQuery()

        /** Launching Service Web Links from User tapping on either the link or the surrounding
         * environment.**/
        webViewLink!!.setOnClickListener { openWebLink() }
        service_link_relative_layout.setOnClickListener { openWebLink() }
    }

    private fun openWebLink() {
        val webViewIntent = Intent(this, WebViewActivity::class.java)
        webViewIntent.putExtra("LINK", serviceObject.serviceLink)
        Timber.e("WEB LINK -> ${serviceObject.serviceLink}")
        if (serviceObject.serviceLink.isNotEmpty()) {
            showSnackbarWhite("Opening link...", -1)
            startActivity(webViewIntent)
            mixpanel.track("detailedService_serviceLinkOpened")
        } else {
            showSnackbarYellow("No link for '${serviceObject.serviceName}' found", -1)
        }
    }

    private fun selectingServiceSpec() {
        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.detailed_service_cost_text_View)

        serviceCostAdapter = ArrayAdapter(this,
                R.layout.list_item, serviceCostArrayList)

        autoCompleteTextView?.setAdapter(serviceCostAdapter)

        autoCompleteTextView?.setOnItemClickListener { adapterView, autoView, i, l ->
            val selectedText = autoCompleteTextView.text.toString()
            var currencyIndex = 0

            /** Parsing through the selectedText to pull out the [specCost] **/
            for (j in selectedText.indices) {
                val char = selectedText[j]
                if (char == 'E')
                    currencyIndex = j
            }

//            Timber.e("CURRENCY INDEX -> $currencyIndex")
            serviceSpec = selectedText.substring(0, currencyIndex - 2)
            specCost = selectedText.substring(currencyIndex + 1)

//            Timber.e("SPEC-COST -> $specCost")
//            Timber.e("SERVICE-SPEC -> $serviceSpec")

            val serviceSpecCost = JSONObject()
            serviceSpecCost
                    .put("SERVICE_SPEC", serviceSpec)
                    .put("SPEC_COST", specCost)
            mixpanel.track("detailed_serviceSpecSelected", serviceSpecCost)

            checkForDelegatedServiceAndCompare()
        }
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
            requestAgentBtn!!.setOnClickListener {
                mixpanel.track("detailedServiceAct_requestAgentButtonTapped")
                /** Creating bottomSheet service request **/
                val requestBundle = Bundle()
                requestBundle.putSerializable(SERVICE_OBJECT, serviceObject)
                val serviceRequestBottomSheetDialog = ServiceRequestBottomSheet()
                serviceRequestBottomSheetDialog.arguments = requestBundle
                serviceRequestBottomSheetDialog
                        .show(this.supportFragmentManager,
                                ServiceRequestBottomSheet.TAG)
            }
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

        /** Checking if [mService] has ServiceLink **/
        if (mService.serviceLink.isNotEmpty())
            webViewLink!!.text = mService.serviceLink
        else
            webViewLink!!.text = "No link found for this service..."

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
            serviceDocsLayout!!.removeAllViews()
            for (i in docsList!!.indices) {
                serviceDocsTextView = TextView(applicationContext)
                serviceDocsTextView!!.id = i
                serviceDocsTextView!!.text = "\u25CF " + docsList!![i].replace("\"\"", "")
                serviceDocsTextView!!.textSize = 14F
                serviceDocsLayout!!.addView(serviceDocsTextView)
            }
        } else {
            Timber.e("onCreate: docsList is EMPTY!")
        }

        /** Parsing Service Centres into Chip-items **/
        serviceCentresList = ArrayList(serviceObject.serviceCentres)
        Timber.e("CENTRES-LIST -> $serviceCentresList")

        if (serviceCentresList!!.isNotEmpty()) {

            serviceCentresLinearLayout!!.removeAllViews()
            for (i in serviceCentresList!!.indices) {
                serviceCentresTextView = TextView(applicationContext)
                serviceCentresTextView!!.id = i

                serviceCentresTextView!!.text = "\u25CB " + serviceCentresList!![i].replace("\"\"", "")
                serviceCentresTextView!!.textSize = 14F
                serviceCentresTextView!!.setTextColor(resources.getColor(R.color.ummo_2))
                serviceCentresLinearLayout!!.addView(serviceCentresTextView)
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

    private fun showSnackbarWhite(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val snackbar = Snackbar.make(this@DetailedServiceActivity.findViewById(android.R.id.content), message, length)
        val requestAgentButton = findViewById<Button>(R.id.request_agent_btn)
        snackbar.setTextColor(resources.getColor(R.color.appleWhite))
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = requestAgentButton
        snackbar.show()
    }

    private fun showSnackbarYellow(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val snackbar = Snackbar.make(this@DetailedServiceActivity.findViewById(android.R.id.content), message, length)
        val requestAgentButton = findViewById<Button>(R.id.request_agent_btn)
        snackbar.setTextColor(resources.getColor(R.color.gold))
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = requestAgentButton
        snackbar.show()
    }

    companion object {
        const val DELEGATED_SERVICE_ID = "DELEGATED_SERVICE_ID"
        const val SERVICE_AGENT_ID = "SERVICE_AGENT_ID"
        const val DELEGATION_ID = "DELEGATION_ID"
    }
}