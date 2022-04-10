package xyz.ummo.user.ui.detailedService

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.TargetApi
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlinx.android.synthetic.main.content_detailed_service.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProductEntity
import xyz.ummo.user.databinding.ActivityDetailedServiceBinding
import xyz.ummo.user.databinding.ContentDetailedServiceBinding
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.WebViewActivity
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceFeeQuery
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceRequestBottomSheet
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.ConfirmPaymentTermsEvent
import xyz.ummo.user.utilities.eventBusEvents.ServiceSpecifiedEvent
import java.net.MalformedURLException

class DetailedServiceActivity : AppCompatActivity() {
    private lateinit var serviceBenefitsRelativeLayout: RelativeLayout
    private lateinit var dividerView: View
    private val WRITE_PERMISSION: Int = 1001
    var attachmentDownloadId: Long = 0
    var nestedScrollView: NestedScrollView? = null
    var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    var serviceImageView: ImageView? = null
    var serviceDescriptionTextView: TextView? = null
    var serviceNameTextView: TextView? = null
    var serviceBenefitsHeaderTextView: TextView? = null
    var serviceBenefitOneTitle: TextView? = null
    var serviceBenefitOneBody: TextView? = null
    var serviceBenefitTwoTitle: TextView? = null
    var serviceBenefitTwoBody: TextView? = null
    var serviceBenefitThreeTitle: TextView? = null
    var serviceBenefitThreeBody: TextView? = null
    var expandCollapseTextView: TextView? = null
    var serviceCostTextView: TextView? = null
    var serviceDurationTextView: TextView? = null
    var serviceDocsTextView: TextView? = null
    var serviceCentresTextView: TextView? = null
    var serviceStepsTextView: TextView? = null

    var serviceDocsLayout: LinearLayout? = null
    var expandCollapseAction: RelativeLayout? = null

    var serviceCentresLinearLayout: LinearLayout? = null
    var serviceAttachmentLayout: LinearLayout? = null
    var serviceBenefitsLayout: RelativeLayout? = null
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
    var serviceBenefitsList: ArrayList<ServiceBenefit>? = null
    private var agentRequestStatus = "Requesting agent..."
    private var agentDelegate = JSONObject()
    private var agentName: String? = null
    private var serviceId: String? = null
    private var _serviceName: String? = null
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
    private lateinit var mixpanelAPI: MixpanelAPI

    private var serviceCostAdapter: ArrayAdapter<ServiceCostModel>? = null
    private var serviceCostSpinner: Spinner? = null
    private var serviceCostTextInputLayout: TextInputLayout? = null
    private var serviceCostArrayList = ArrayList<ServiceCostModel>()
    private lateinit var serviceCostItem: ServiceCostModel
    private var serviceSpec = ""
    private var specCost = ""
    private var chosenServiceCentre = ""

    override fun onStart() {
        super.onStart()
        mixpanelAPI.timeEvent("Viewing DETAILED SERVICE (${serviceObject.serviceName})")
    }

    override fun onStop() {
        super.onStop()
        mixpanelAPI.track("Viewing DETAILED SERVICE (${serviceObject.serviceName})")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mixpanelAPI = MixpanelAPI.getInstance(
                this,
                resources.getString(R.string.mixpanelToken)
        )

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
        serviceImageView = findViewById(R.id.service_image_view)
        val appBar = findViewById<AppBarLayout>(R.id.app_bar_layout)
        serviceNameTextView = findViewById(R.id.detailed_service_name_text_view)
        serviceBenefitsHeaderTextView = findViewById(R.id.service_benefits_header_text_view)
        serviceBenefitsRelativeLayout =
            findViewById<RelativeLayout>(R.id.service_benefits_main_relative_layout)
        dividerView = findViewById(R.id.divider_2)

        serviceBenefitOneTitle = findViewById(R.id.benefit_one_title_text_view)
        serviceBenefitOneBody = findViewById(R.id.benefit_one_body_text_view)

        serviceBenefitTwoTitle = findViewById(R.id.benefit_two_title_text_view)
        serviceBenefitTwoBody = findViewById(R.id.benefit_two_body_text_view)

        serviceBenefitThreeTitle = findViewById(R.id.benefit_three_title_text_view)
        serviceBenefitThreeBody = findViewById(R.id.benefit_three_body_text_view)

        expandCollapseTextView = findViewById(R.id.action_text_view)
        serviceDescriptionTextView = findViewById(R.id.detailed_description_text_view)
//        serviceCostTextView = findViewById(R.id.detailed_service_cost_text_view)
        serviceDurationTextView = findViewById(R.id.detailed_service_duration_text_view)
        serviceDocsLayout = findViewById(R.id.service_requirements_linear_layout)
//        serviceCentresChipGroup = findViewById(R.id.detailed_service_centres_chip_group)
        serviceCentresLinearLayout = findViewById(R.id.service_centre_linear_layout)
        serviceAttachmentLayout = findViewById(R.id.service_attachments_linear_layout)
        serviceBenefitsLayout = findViewById(R.id.service_benefits_holder_relative_layout)

        appBar.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (mCollapsingToolbarLayout!!.height + verticalOffset < 2 * ViewCompat.getMinimumHeight(
                    mCollapsingToolbarLayout!!
                )
            ) {
                toolbar!!.navigationIcon!!.setColorFilter(
                    resources.getColor(R.color.black),
                    PorterDuff.Mode.SRC_ATOP
                )
            } else {
                toolbar!!.navigationIcon!!.setColorFilter(
                    resources.getColor(R.color.UmmoPurple),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        })

        /** Assigning [serviceObject] with the [serviceObject] we receive from ServiceItem **/
        serviceObject = intent.extras!!.get(SERVICE_OBJECT) as ServiceObject
        Timber.e("SERVICE OBJECT -> $serviceObject")
        serviceId = serviceObject.serviceId

        populateDetailedServiceElements(serviceObject)

        detailedServicePrefs = getSharedPreferences(ummoUserPreferences, mode)

        editor = detailedServicePrefs.edit()

        detailedProductViewModel = ViewModelProvider(this)
            .get(DetailedProductViewModel::class.java)

        delegatedServiceViewModel = ViewModelProvider(this)
            .get(DelegatedServiceViewModel::class.java)

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

        showServiceBenefits(serviceObject)
    }

    /** With the function below, we're ...**/
    private fun showServiceBenefits(mService: ServiceObject) {
        val serviceName = mService.serviceName
        val serviceBenefitTitle =
            String.format(resources.getString(R.string.service_benefits_title_text), serviceName)
        serviceBenefitsHeaderTextView!!.text = serviceBenefitTitle
    }

    private fun openWebLink() {
        val webViewIntent = Intent(this, WebViewActivity::class.java)
        val webLinkJSON = JSONObject()
        webViewIntent.putExtra("LINK", serviceObject.serviceLink)
        Timber.e("WEB LINK -> ${serviceObject.serviceLink}")
        if (serviceObject.serviceLink.isNotEmpty()) {
            showSnackbarWhite("Opening link...", 0)
            startActivity(webViewIntent)
            webLinkJSON.put("LINK_OPENED", serviceObject.serviceLink)
            mixpanelAPI.track("detailedService_serviceLinkOpened", webLinkJSON)
        } else {
            showSnackbarYellow("No link for '${serviceObject.serviceName}' found", -1)
        }
    }

    private fun selectingServiceSpec() {
        val autoCompleteTextView =
            findViewById<AutoCompleteTextView>(R.id.detailed_service_cost_text_View)

        serviceCostAdapter = ArrayAdapter(
            this,
            R.layout.list_item, serviceCostArrayList
        )

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
            mixpanelAPI.track("detailedService_serviceSpecSelected", serviceSpecCost)

            checkForDelegatedServiceAndCompare()
        }
    }

    private fun checkForDelegatedServiceAndCompare() {
        delegatedServiceViewModel = ViewModelProvider(this)
            .get(DelegatedServiceViewModel::class.java)

        val countOfDelegatedServices = delegatedServiceViewModel!!.getCountOfDelegatedServices()

        /** Declaring [serviceObject] for request event tracking **/
        val serviceRequestObject = JSONObject()
        serviceRequestObject.put("REQUESTING", serviceObject.serviceName)

        if (countOfDelegatedServices > 0) {
            delegatedServiceViewModel!!.delegatedServiceEntityLiveData
                .observe(this, { delegatedServiceEntity ->
                    val delegatedServiceId = delegatedServiceEntity.delegatedProductId
                    val serviceAgent =
                        detailedServicePrefs.getString(SERVICE_AGENT_ID, SERVICE_AGENT_ID)!!
                    val delegationId =
                        detailedServicePrefs.getString(DELEGATED_SERVICE_ID, DELEGATED_SERVICE_ID)!!
                    if (serviceObject.serviceId == delegatedServiceId) {
                        requestAgentBtn?.text = "VIEW PROGRESS"
                        requestAgentBtn?.isActivated = false
                        requestAgentBtn?.setBackgroundColor(this.resources.getColor(R.color.ummo_3))

                        requestAgentBtn?.setOnClickListener {
                            launchDelegatedService(
                                this@DetailedServiceActivity,
                                delegatedServiceId, serviceAgent, delegationId
                            )
                        }

                        /** Tracking a repeat request **/
                        mixpanelAPI.track("detailedService_repeatingRequest", serviceRequestObject)
                    }
                })

        } else {
            requestAgentBtn!!.setOnClickListener {
                /** Creating bottomSheet service request **/
                val requestBundle = Bundle()
                requestBundle.putSerializable(SERVICE_OBJECT, serviceObject)
                val serviceRequestBottomSheetDialog = ServiceRequestBottomSheet()
                serviceRequestBottomSheetDialog.arguments = requestBundle
                serviceRequestBottomSheetDialog
                    .show(
                        this.supportFragmentManager,
                        ServiceRequestBottomSheet.TAG
                    )

                mixpanelAPI.track("detailedService_requestingService", serviceRequestObject)
            }
        }
    }

    private fun detailedServiceFeeQuery() {
        val detailedServiceFeeQueryLayout =
            findViewById<RelativeLayout>(R.id.detailed_service_query_icon_relative_layout)
        val detailedServiceFeeQueryIcon = findViewById<ImageView>(R.id.detailed_query_image_view)

        detailedServiceFeeQueryLayout.setOnClickListener { openServiceFeeSelfSupport() }
        detailedServiceFeeQueryIcon.setOnClickListener { openServiceFeeSelfSupport() }
    }

    /** Querying Service Fee Self Support **/
    private fun openServiceFeeSelfSupport() {
        val serviceFeeQuery = ServiceFeeQuery()
        serviceFeeQuery.show(this.supportFragmentManager, ServiceFeeQuery.TAG)
        mixpanelAPI.track("detailedService_serviceFeeSelfSupport")

    }

    override fun onBackPressed() {
        super.onBackPressed()

        val intent = Intent(this, MainScreen::class.java)
        val bundle = Bundle()
        bundle.putString(VIEW_SOURCE, DETAILED_SERVICE)
        intent.putExtra(VIEW_SOURCE, bundle)
        startActivity(intent)
        finish()

        mixpanelAPI.track("detailedService_navigateBack")
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
        detailedServicePrefs = getSharedPreferences(ummoUserPreferences, mode)

        toolbar!!.title = mService.serviceName
        mCollapsingToolbarLayout!!.title = mService.serviceName
        serviceNameTextView!!.text = mService.serviceName
        serviceDescriptionTextView!!.text = mService.serviceDescription

        /** Checking if [mService] has ServiceLink **/
        if (mService.serviceLink.isEmpty()) {
            webViewLink!!.text = "No link found for this service."
            webViewLink!!.textSize = 10F
            webViewLink!!.setTextColor(resources.getColor(R.color.greyProfile))
            launch_link_image_view.visibility = View.GONE
        }

        /** Checking if [mService] has ServiceAttachments **/
        if (mService.serviceAttachmentURL.isEmpty()) {
            serviceAttachmentLayout!!.removeAllViews()
            val noAttachmentTextView = TextView(this)
            noAttachmentTextView.text = "No attachments found for this service."
            noAttachmentTextView.textSize = 10F
            noAttachmentTextView.setTextColor(resources.getColor(R.color.greyProfile))
            serviceAttachmentLayout!!.addView(noAttachmentTextView)

        } else {
            val i = 1
            val attachmentImageView = ImageView(this)
            val attachmentNameTextView = TextView(this)
            val attachmentSizeTextView = TextView(this)
            val attachmentRelativeLayout = RelativeLayout(this)
            val attachmentDownloaded = detailedServicePrefs.getBoolean(ATTACHMENT_DOWNLOADED, false)

            /** Configuring the Attachment's [RelativeLayout] container **/
            attachmentRelativeLayout.layoutParams =
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )

            /** Setting AttachmentImageView's parameters **/
            val imageViewParams = RelativeLayout.LayoutParams(200, 200)
            imageViewParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            attachmentImageView.x = 5F
            attachmentImageView.y = 5F
            attachmentImageView.id = i
            attachmentImageView.layoutParams = imageViewParams
            attachmentImageView.setImageResource(R.drawable.pdf)

            /** Setting AttachmentNameTextView's parameters **/
            attachmentNameTextView.text = mService.serviceAttachmentName
            attachmentNameTextView.textSize = 10F
            attachmentNameTextView.id = i + 1
            attachmentNameTextView.x = 5F
            attachmentNameTextView.y = 5F
            attachmentNameTextView.setTextColor(resources.getColor(R.color.ummo_1))

            /** Setting TextView's layout parameters: setting it below ImageView's **/
            val nameTextViewParams = RelativeLayout
                .LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
            nameTextViewParams.addRule(RelativeLayout.BELOW, attachmentImageView.id)
            nameTextViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            attachmentNameTextView.layoutParams = nameTextViewParams

            /** Setting AttachmentSizeTextView's parameters **/
            attachmentSizeTextView.text = mService.serviceAttachmentSize
            attachmentSizeTextView.textSize = 8F
            attachmentNameTextView.id = mService.serviceAttachmentName.length
            attachmentSizeTextView.setTextColor(resources.getColor(R.color.black))

            /** Setting TextView's layout parameters: setting it below ImageView's **/
            val sizeTextViewParams = RelativeLayout
                .LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
            sizeTextViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
            sizeTextViewParams.addRule(RelativeLayout.BELOW, attachmentNameTextView.id)

            attachmentSizeTextView.layoutParams = sizeTextViewParams

            attachmentImageView.setOnClickListener {
                val attachmentObject = JSONObject()
                attachmentObject
                    .put("FILE_NAME", serviceObject.serviceAttachmentName)
                    .put("FILE_URL", serviceObject.serviceAttachmentURL)

                mixpanelAPI.track("attachment_downloadTapped", attachmentObject)

                /** Checking if attachment has been downloaded; handling UX appropriately **/
                if (!attachmentDownloaded) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        askPermissions()
                    } else {
                        downloadAttachment(mService.serviceAttachmentURL)
                    }
                } else {
                    showSnackbarYellow("File already saved in your 'Downloads' folder", 0)
                }
            }

            /** Implanting ImageView && TextView into RelativeLayout **/
            attachmentRelativeLayout.addView(attachmentImageView)
            attachmentRelativeLayout.addView(attachmentNameTextView)
            attachmentRelativeLayout.addView(attachmentSizeTextView)

            /** Implanting RelativeLayout into LinearLayout **/
            serviceAttachmentLayout!!.addView(attachmentRelativeLayout)
            /*serviceAttachmentLayout!!.addView(attachmentImageView)
            serviceAttachmentLayout!!.addView(attachmentNameTextView)*/

        }

        /** Filling up [serviceCostArrayList] with [mService]'s serviceCost **/
        serviceCostArrayList = mService.serviceCost
        serviceDurationTextView!!.text = mService.serviceDuration

        if (mService.delegatable) {
            requestAgentBtn!!.visibility = View.VISIBLE
        } else {
            requestAgentBtn!!.visibility = View.GONE
        }

        docsList = ArrayList(serviceObject.serviceDocuments)

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

                serviceCentresTextView!!.text =
                    "\u25CB " + serviceCentresList!![i].replace("\"\"", "")
                serviceCentresTextView!!.textSize = 14F
                serviceCentresTextView!!.setTextColor(resources.getColor(R.color.black))
                serviceCentresLinearLayout!!.addView(serviceCentresTextView)
            }
        } else {
            Timber.e("onCreate: docsList is EMPTY!")
        }

        serviceBenefitsList = ArrayList(serviceObject.serviceBenefits)
        parseServiceBenefits(serviceBenefitsList!!)

        if (serviceBenefitsList!!.size > 0) {
            toggleServiceBenefits()
        } else {
            serviceBenefitsRelativeLayout.visibility = View.GONE
            dividerView.visibility = View.GONE
        }

    }

    /** We need the User to toggle between showing Service Benefits, and hiding them **/
    private fun toggleServiceBenefits() {
        val toggleServiceBenefitsRelativeLayout = findViewById<RelativeLayout>(R.id.action_layout)
        val toggleServiceBenefitsTextView = findViewById<TextView>(R.id.action_text_view)
        val expandServiceBenefitsImageView = findViewById<ImageView>(R.id.expand_image_view)
        val collapseServiceBenefitsImageView = findViewById<ImageView>(R.id.collapse_image_view)

        toggleServiceBenefitsRelativeLayout.setOnClickListener {

            if (serviceBenefitsLayout?.visibility == View.VISIBLE) {
                serviceBenefitsLayout?.visibility = View.GONE
                toggleServiceBenefitsTextView.text = "Show Benefits"
                expandServiceBenefitsImageView.visibility = View.GONE
                collapseServiceBenefitsImageView.visibility = View.VISIBLE
                mixpanelAPI.track("detailedService_showingBenefits")

            } else {
                serviceBenefitsLayout?.visibility = View.VISIBLE
                toggleServiceBenefitsTextView.text = "Hide Benefits"
                expandServiceBenefitsImageView.visibility = View.VISIBLE
                collapseServiceBenefitsImageView.visibility = View.GONE
                mixpanelAPI.track("detailedService_hidingBenefits")
            }
        }
    }

    /** With this function, we'll be parsing all service benefits into their custom UI components **/
    private fun parseServiceBenefits(mServiceBenefits: ArrayList<ServiceBenefit>) {
        var benefitTitle: String
        var benefitBody: String
        val benefitTitleTextView = TextView(this)
        val benefitBodyTextView = TextView(this)

        for (i in 0 until mServiceBenefits.size) {
            benefitTitle = mServiceBenefits[i].benefitTitle
            benefitBody = mServiceBenefits[i].benefitBody
            benefitTitleTextView.text = benefitTitle
            benefitBodyTextView.text = benefitBody

            if (i == 0) {
                serviceBenefitOneTitle?.text = "${i + 1}. $benefitTitle"
                serviceBenefitOneBody?.text = benefitBody
            } else if (i == 1) {
                serviceBenefitTwoTitle?.text = "${i + 1}. $benefitTitle"
                serviceBenefitTwoBody?.text = benefitBody
            } else if (i == 2) {
                serviceBenefitThreeTitle?.text = "${i + 1}. $benefitTitle"
                serviceBenefitThreeBody?.text = benefitBody
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WRITE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                downloadAttachment(serviceObject.serviceAttachmentURL)
            } else {
                showSnackbarYellow("Permission denied!", -1)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun askPermissions() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat
                    .shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Permission required")
                    .setMessage("Permission required to save files from the Web.")
                    .setPositiveButton("Accept") { dialog, id ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                    }
                    .setNegativeButton("Deny") { dialog, id -> dialog.cancel() }
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                )
            }
        } else {
            // Permission has already been granted
            downloadAttachment(serviceObject.serviceAttachmentURL)
        }
    }

    private fun downloadAttachment(attachmentLink: String?) =
        CoroutineScope(Dispatchers.IO).launch {

            try {
                Timber.e("DOWNLOADING ATTACHMENT -> $attachmentLink")
                showSnackbarWhite("Downloading file...", -1)

                val downloadRequest = DownloadManager
                    .Request(Uri.parse(attachmentLink))
                    .setTitle(serviceObject.serviceAttachmentName)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        serviceObject.serviceAttachmentName
                    )
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setAllowedOverMetered(true)
                val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                attachmentDownloadId = downloadManager.enqueue(downloadRequest)

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Timber.e("Unable to download file -> ${e.message}")
                }
            } catch (malformedURLException: MalformedURLException) {
                withContext(Dispatchers.Main) {
                    Timber.e("URL is deformed -> ${malformedURLException.message}")
                }
            } catch (se: SecurityException) {
                withContext(Dispatchers.Main) {
                    Timber.e("Security Exception -> ${se.message}")
                }
            }

            val broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(p0: Context?, p1: Intent?) {
                    val id: Long? = p1?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    val attachmentObject = JSONObject()
                    attachmentObject.put("file_url", attachmentLink)
                        .put("file_name", serviceObject.serviceAttachmentName)

                    if (id == attachmentDownloadId) {
                        showSnackbarGreen("Download complete", -1)
                        editor.putBoolean(ATTACHMENT_DOWNLOADED, true).apply()
                        mixpanelAPI.track("detailedService_attachmentDownloaded", attachmentObject)

                    }
                }
            }

            registerReceiver(
                broadcastReceiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
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

    private fun launchDelegatedService(
        context: Context?,
        delegatedServiceId: String,
        agentId: String,
        delegationId: String
    ) {

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
        val snackbar = Snackbar.make(
            this@DetailedServiceActivity.findViewById(android.R.id.content),
            message,
            length
        )
        val requestAgentButton = findViewById<Button>(R.id.request_agent_btn)
        snackbar.setTextColor(resources.getColor(R.color.appleWhite))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = requestAgentButton
        snackbar.show()
    }

    private fun showSnackbarGreen(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val snackbar = Snackbar.make(
            this@DetailedServiceActivity.findViewById(android.R.id.content),
            message,
            length
        )
        val requestAgentButton = findViewById<Button>(R.id.request_agent_btn)
        snackbar.setTextColor(resources.getColor(R.color.lawn_green))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
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
        val snackbar = Snackbar.make(
            this@DetailedServiceActivity.findViewById(android.R.id.content),
            message,
            length
        )
        val requestAgentButton = findViewById<Button>(R.id.request_agent_btn)
        snackbar.setTextColor(resources.getColor(R.color.gold))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = requestAgentButton
        snackbar.show()
    }

}