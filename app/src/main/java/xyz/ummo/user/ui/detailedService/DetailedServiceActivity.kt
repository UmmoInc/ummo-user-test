package xyz.ummo.user.ui.detailedService

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.TargetApi
import android.app.Activity
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.content_delegation_progress.*
import kotlinx.android.synthetic.main.content_detailed_service.*
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.ServiceCommentsDiffUtilAdapter
import xyz.ummo.user.data.db.ServiceCommentsDatabase
import xyz.ummo.user.data.db.ServiceUtilityDatabase
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProductEntity
import xyz.ummo.user.data.entity.ServiceCommentEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.repo.serviceSomments.ServiceCommentsRepo
import xyz.ummo.user.data.repo.serviceUtility.ServiceUtilityRepo
import xyz.ummo.user.databinding.ActivityDetailedServiceBinding
import xyz.ummo.user.databinding.ContentDetailedServiceBinding
import xyz.ummo.user.databinding.FragmentServiceCommentsBinding
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.ui.WebViewActivity
import xyz.ummo.user.ui.detailedService.serviceComments.ServiceCommentsViewModel
import xyz.ummo.user.ui.detailedService.serviceComments.ServiceCommentsViewModelFactory
import xyz.ummo.user.ui.fragments.bottomSheets.IntroduceDelegate
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceFeeQuery
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceRequestBottomSheet
import xyz.ummo.user.ui.fragments.bottomSheets.ShareServiceInfoBottomSheet
import xyz.ummo.user.ui.fragments.bottomSheets.serviceComments.ServiceComments
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.fragments.search.AllServicesFragment
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.ConfirmPaymentTermsEvent
import xyz.ummo.user.utilities.eventBusEvents.ServiceSpecifiedEvent
import java.net.MalformedURLException
import java.util.*

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
    var serviceStepsHorizontalScroller: HorizontalScrollView? = null
    var serviceStepsChipGroup: ChipGroup? = null
    lateinit var serviceStepChip: Chip

    var serviceDocsLayout: LinearLayout? = null
    var expandCollapseAction: RelativeLayout? = null

    var serviceCentresLinearLayout: LinearLayout? = null
    var serviceActionsLinearLayout: LinearLayout? = null
    var serviceAttachmentLayout: LinearLayout? = null
    var serviceBenefitsLayout: RelativeLayout? = null
    var serviceCentreRadioButton: RadioButton? = null
    var toolbar: Toolbar? = null
    var requestAgentBtn: Button? = null
    var shareServiceBtn: Button? = null
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
    private var mServiceId: String? = null
    private var _serviceName: String? = null
    private var progress: ProgressDialog? = null
    var agentRequestDialog: AlertDialog.Builder? = null
    var agentNotFoundDialog: AlertDialog.Builder? = null
    private lateinit var detailedServiceBinding: ActivityDetailedServiceBinding
    private lateinit var detailedServiceContentBinding: ContentDetailedServiceBinding

    //    private lateinit var serviceObject: ServiceObject
    private lateinit var serviceEntity: ServiceEntity
    private lateinit var summoningParent: String

    private val paymentTermsEvent = ConfirmPaymentTermsEvent()
    private val serviceSpecifiedEvent = ServiceSpecifiedEvent()

    private lateinit var detailedServicePrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val serviceUtilRatingObject = JSONObject()
    private lateinit var mixpanelAPI: MixpanelAPI

    private var serviceCostAdapter: ArrayAdapter<ServiceCostModel>? = null
    private var serviceCostSpinner: Spinner? = null
    private var serviceCostTextInputLayout: TextInputLayout? = null
    private var serviceCostArrayList = ArrayList<ServiceCostModel>()
    private lateinit var serviceCostItem: ServiceCostModel
    private var serviceSpec = ""
    private var specCost = ""
    private var chosenServiceCentre = ""

    private lateinit var serviceUtilityViewModel: ServiceUtilityViewModel
    private val coroutineScope = CoroutineScope((Dispatchers.Main + parentJob))
    private val simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
    private val currentTimeStamp = simpleDateFormat.format(Date())

    private lateinit var serviceCommentsViewModel: ServiceCommentsViewModel
    private lateinit var serviceCommentsDiffUtilAdapter: ServiceCommentsDiffUtilAdapter
    private lateinit var serviceCommentsViewBinding: FragmentServiceCommentsBinding

    var serviceCommentsHeaderSubtitle: TextView? = null
    var serviceCommentsProgressBar: ProgressBar? = null
    var serviceCommentsRecyclerView: RecyclerView? = null
    var noServiceCommentsRelativeLayout: RelativeLayout? = null
    var nestedServiceCommentsScrollView: NestedScrollView? = null
    var serviceCommentEditText: TextInputEditText? = null
    var serviceCommentTextInputLayout: TextInputLayout? = null

    companion object {
        private val parentJob = Job()
    }

    override fun onStart() {
        super.onStart()
        mixpanelAPI.timeEvent("Viewing DETAILED SERVICE")

        val serviceUtilityRepo = ServiceUtilityRepo(ServiceUtilityDatabase(this), this)
        val serviceUtilityViewModelProviderFactory =
            ServiceUtilityViewModelFactory(serviceUtilityRepo)
        serviceUtilityViewModel = ViewModelProvider(
            this,
            serviceUtilityViewModelProviderFactory
        )[ServiceUtilityViewModel::class.java]

        checkServiceUtilityThumbs()

        /** [START] Instantiating [serviceCommentsViewModel] to save [serviceComments] **/
        val serviceCommentsRepo = ServiceCommentsRepo(ServiceCommentsDatabase(this), this)
        val serviceCommentsViewModelProviderFactory =
            ServiceCommentsViewModelFactory(mServiceId!!, serviceCommentsRepo)

        serviceCommentsViewModel = ViewModelProvider(
            this,
            serviceCommentsViewModelProviderFactory
        )[ServiceCommentsViewModel::class.java]

        coroutineScope.launch(Dispatchers.IO) {
            serviceCommentsViewModel.saveServiceCommentsToRoom()
        }
        /** [END] Instantiating [serviceCommentsViewModel] to save [serviceComments] **/

        /** [START] Retrieving [serviceComments] from [serviceCommentsViewModel] **/
        /*coroutineScope.launch(Dispatchers.IO) {
            serviceCommentsViewModel.getServiceCommentsFromRoomByServiceId(mServiceId!!)
        }*/
        shareServiceOnButtonClick()
        setupServiceCommentsRecyclerView()
        getAllServiceCommentsFromRoomAndDisplay()
        checkIfCommentIsActiveAndHideRequestButton()
        /** [END] Retrieving [serviceComments] from [serviceCommentsViewModel] **/

    }

    private fun shareServiceOnButtonClick() {
        shareServiceBtn!!.setOnClickListener {
            shareServiceInfo()
            mixpanelAPI.track("Share-Service Selected")
        }
    }

    private fun checkIfCommentIsActiveAndHideRequestButton() {
        val serviceCommentBottomSheet = ServiceComments()
        val serviceCommentBundle = Bundle()
        serviceCommentBundle.putString(SERVICE_ID, mServiceId)

        serviceCommentEditText!!.setOnClickListener {
            Timber.e("INPUT FIELD CLICKED!")

            serviceCommentBottomSheet.arguments = serviceCommentBundle
            serviceCommentBottomSheet.show(
                supportFragmentManager,
                ServiceComments.TAG
            )

            val inputMethodManager =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val serviceCommentEditText = findViewById<EditText>(R.id.service_comment_edit_text)

            inputMethodManager.showSoftInput(serviceCommentEditText, InputMethodManager.SHOW_FORCED)
        }

        serviceCommentEditText!!.setOnFocusChangeListener { view, hasFocus ->
            Timber.e("INPUT FIELD HAS FOCUS -> $hasFocus")
            if (hasFocus) {
                serviceCommentBottomSheet.arguments = serviceCommentBundle
                serviceCommentBottomSheet.show(
                    supportFragmentManager,
                    ServiceComments.TAG
                )
                val inputMethodManager =
                    this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val serviceCommentEditText =
                    findViewById<EditText>(R.id.service_comment_edit_text_1)

                inputMethodManager.showSoftInput(
                    serviceCommentEditText,
                    InputMethodManager.SHOW_FORCED
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mixpanelAPI.track("Viewing Detailed Service")
    }

    private fun setupServiceCommentsRecyclerView() {
        serviceCommentsDiffUtilAdapter = ServiceCommentsDiffUtilAdapter()

        val serviceCommentsRecyclerView =
            this.findViewById<RecyclerView>(R.id.service_comment_recycler_view)
        serviceCommentsRecyclerView.apply {
            adapter = serviceCommentsDiffUtilAdapter
            layoutManager = LinearLayoutManager(this@DetailedServiceActivity)
            hasFixedSize()
        }
    }

    /*private fun getAllServiceCommentsFromRoomAndDisplay() {
        serviceCommentsViewModel.serviceCommentsMutableLiveData.observe(this@DetailedServiceActivity) { response ->
            serviceCommentsDiffUtilAdapter.differ.submitList(response)
            showServiceCommentsAndHideEverythingElse()
            checkForServiceComments(response)

            Timber.e("SERVICE COMMENTS RESPONSE -> $response")

            if (true) {
                coroutineScope.launch(Dispatchers.IO) {
                    if (response.isEmpty()) {
                        Timber.e("SERVICE ID -> $mServiceId")
                        serviceCommentsViewModel.getServiceCommentsFromRoomByServiceId(mServiceId!!)

                        this@DetailedServiceActivity.runOnUiThread {
                            showServiceCommentsAndHideEverythingElse()
                        }

                        Handler(Looper.getMainLooper()).post {
                            checkForServiceComments(response)
                        }
                    }
                }
            }
        }
    }*/

    private fun getAllServiceCommentsFromRoomAndDisplay() {
        serviceCommentsViewModel.serviceComments.observe(this@DetailedServiceActivity) { response ->
            serviceCommentsDiffUtilAdapter.differ.submitList(response)
            showServiceCommentsAndHideEverythingElse()
            checkForServiceComments(response as ArrayList<ServiceCommentEntity>)
            Timber.e("SERVICE COMMENTS RESPONSE -> $response")
            serviceCommentEditText?.clearFocus()
            serviceCommentEditText?.isCursorVisible = false
        }
    }

    private fun checkForServiceComments(serviceCommentsArrayList: ArrayList<ServiceCommentEntity>) {
        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                showProgressBar()
            }

            override fun onFinish() {
                if (!serviceCommentsArrayList.isEmpty()) {
                    showServiceCommentsAndHideEverythingElse()
                } else {
                    showThatThereAreNoServiceCommentsYet()
                }
            }
        }

        timer.start()
    }

    private fun showServiceCommentsAndHideEverythingElse() {
        noServiceCommentsRelativeLayout!!.visibility = View.GONE
        serviceCommentsProgressBar!!.visibility = View.GONE
        nestedServiceCommentsScrollView!!.visibility = View.VISIBLE
    }

    private fun showThatThereAreNoServiceCommentsYet() {
        noServiceCommentsRelativeLayout!!.visibility = View.VISIBLE
        serviceCommentsProgressBar!!.visibility = View.GONE
        nestedServiceCommentsScrollView!!.visibility = View.GONE
    }

    private fun showProgressBar() {
        serviceCommentsProgressBar!!.visibility = View.VISIBLE
        nestedServiceCommentsScrollView!!.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceStepChip = Chip(this)

        mixpanelAPI = MixpanelAPI.getInstance(
            this,
            resources.getString(R.string.mixpanelToken)
        )

        /** Binding Layout Views **/
        detailedServiceBinding = ActivityDetailedServiceBinding.inflate(layoutInflater)
        detailedServiceContentBinding = ContentDetailedServiceBinding.inflate(layoutInflater)
        serviceCommentsViewBinding = FragmentServiceCommentsBinding.inflate(layoutInflater)

        val view = detailedServiceBinding.root

        setContentView(view)
        toolbar = findViewById(R.id.toolbar_detailed_service)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        progress = ProgressDialog(this)
        agentRequestDialog = AlertDialog.Builder(this@DetailedServiceActivity)
        agentNotFoundDialog = AlertDialog.Builder(this@DetailedServiceActivity)

        /** [START] Service Comments View Elements **/
        serviceCommentsHeaderSubtitle =
            findViewById(R.id.service_comments_header_subtitle_text_view)
        noServiceCommentsRelativeLayout = findViewById(R.id.no_comments_relative_layout)
        serviceCommentsProgressBar = findViewById(R.id.load_service_comments_progress_bar)
        nestedServiceCommentsScrollView = findViewById(R.id.service_comment_nested_scroll_view)
        serviceCommentEditText = findViewById(R.id.service_comment_edit_text_1)
        serviceCommentTextInputLayout = findViewById(R.id.service_comment_text_input_layout)
        /** [END] Service Comments View Elements **/

        nestedScrollView = findViewById(R.id.nested_scrollview)
        requestAgentBtn = findViewById(R.id.request_agent_btn)
        shareServiceBtn = findViewById(R.id.share_service_btn)
        webViewLink = findViewById(R.id.link_source_text_view)
        mCollapsingToolbarLayout = findViewById(R.id.toolbar_collapsing_layout)
        serviceActionsLinearLayout = findViewById(R.id.service_actions_linear_layout)
        serviceImageView = findViewById(R.id.service_image_view)
        val appBar = findViewById<AppBarLayout>(R.id.service_details_app_bar_layout)
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

        serviceStepsHorizontalScroller = findViewById(R.id.service_steps_horizontal_scroller)
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
//        serviceObject = intent.extras!!.get(SERVICE_OBJECT) as ServiceObject
        serviceEntity = intent.extras!!.get(SERVICE_ENTITY) as ServiceEntity
        summoningParent = intent.extras!!.getString(PARENT, "")

        Timber.e("SERVICE OBJECT -> $serviceEntity")
        Timber.e("SUMMONING PARENT -> $summoningParent")
        mServiceId = serviceEntity.serviceId

        populateDetailedServiceElements(serviceEntity)

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

        showServiceBenefits(serviceEntity)

        checkServiceUtilityThumbs()
        service_util_thumbs_up_image_view.setOnClickListener {
            triggerHelpful()
            reverseNotHelpful()
            coroutineScope.launch(Dispatchers.IO) {
                serviceUtilityViewModel.captureServiceUtility(
                    serviceEntity.serviceId,
                    serviceEntity.serviceName!!,
                    1,
                    0,
                    currentTimeStamp
                )
            }
            editor.putBoolean("HELPFUL-${serviceEntity.serviceId}", true).apply()
            editor.putBoolean("NOT-HELPFUL-${serviceEntity.serviceId}", false).apply()
            serviceUtilRatingObject.put("SERVICE NAME", serviceEntity.serviceName)
            serviceUtilRatingObject.put("TIMESTAMP", currentTimeStamp)
            mixpanelAPI.track("SERVICE INFO HELPFUL", serviceUtilRatingObject)
        }

        service_util_thumbs_down_image_view.setOnClickListener {
            triggerNotHelpful()
            reverseHelpful()
            coroutineScope.launch(Dispatchers.IO) {
                serviceUtilityViewModel.captureServiceUtility(
                    serviceEntity.serviceId,
                    serviceEntity.serviceName!!,
                    0,
                    1,
                    currentTimeStamp
                )
            }
            editor.putBoolean("NOT-HELPFUL-${serviceEntity.serviceId}", true).apply()
            editor.putBoolean("HELPFUL-${serviceEntity.serviceId}", false).apply()
            serviceUtilRatingObject.put("SERVICE NAME", serviceEntity.serviceName)
            serviceUtilRatingObject.put("TIMESTAMP", currentTimeStamp)
            mixpanelAPI.track("SERVICE INFO NOT-HELPFUL", serviceUtilRatingObject)
        }
        service_util_thumbs_up_selected_image_view.setOnClickListener {
            reverseHelpful()
            coroutineScope.launch(Dispatchers.IO) {
                serviceUtilityViewModel.captureServiceUtility(
                    serviceEntity.serviceId,
                    serviceEntity.serviceName!!,
                    -1,
                    0,
                    currentTimeStamp
                )
            }
            editor.putBoolean("HELPFUL-${serviceEntity.serviceId}", false).apply()
            serviceUtilRatingObject.put("SERVICE NAME", serviceEntity.serviceName)
            serviceUtilRatingObject.put("TIMESTAMP", currentTimeStamp)
            mixpanelAPI.track("SERVICE INFO HELPFUL-REVERSED", serviceUtilRatingObject)

        }
        service_util_thumbs_down_selected_image_view.setOnClickListener {
            reverseNotHelpful()
            coroutineScope.launch(Dispatchers.IO) {
                serviceUtilityViewModel.captureServiceUtility(
                    serviceEntity.serviceId,
                    serviceEntity.serviceName!!,
                    0,
                    -1,
                    currentTimeStamp
                )
            }
            editor.putBoolean("NOT-HELPFUL-${serviceEntity.serviceId}", false).apply()
            serviceUtilRatingObject.put("SERVICE NAME", serviceEntity.serviceName)
            serviceUtilRatingObject.put("TIMESTAMP", currentTimeStamp)
            mixpanelAPI.track("SERVICE INFO NOT-HELPFUL-REVERSED", serviceUtilRatingObject)
        }

    }

    /** With the function below, we're ...**/
    private fun showServiceBenefits(mService: ServiceEntity) {
        val serviceName = mService.serviceName
        val serviceBenefitTitle =
            String.format(resources.getString(R.string.service_benefits_title_text), serviceName)
        serviceBenefitsHeaderTextView!!.text = serviceBenefitTitle
    }

    private fun openWebLink() {
        val webViewIntent = Intent(this, WebViewActivity::class.java)
        val webLinkJSON = JSONObject()
        webViewIntent.putExtra("LINK", serviceEntity.serviceLink)
        Timber.e("WEB LINK -> ${serviceEntity.serviceLink}")
        if (serviceEntity.serviceLink!!.isNotEmpty()) {
            showSnackbarWhite("Opening link...", 0)
            startActivity(webViewIntent)
            webLinkJSON.put("LINK_OPENED", serviceEntity.serviceLink)
            mixpanelAPI.track("detailedService_serviceLinkOpened", webLinkJSON)
        } else {
            showSnackbarYellow("No link for '${serviceEntity.serviceName}' found", -1)
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

        /** Declaring [serviceEntity] for request event tracking **/
        val serviceRequestObject = JSONObject()
        serviceRequestObject.put("REQUESTING", serviceEntity.serviceName)

        if (countOfDelegatedServices > 0) {
            delegatedServiceViewModel!!.delegatedServiceEntityLiveData
                .observe(this) { delegatedServiceEntity ->
                    val delegatedServiceId = delegatedServiceEntity.delegatedProductId
                    val serviceAgent =
                        detailedServicePrefs.getString(SERVICE_AGENT_ID, SERVICE_AGENT_ID)!!
                    val delegationId =
                        detailedServicePrefs.getString(DELEGATED_SERVICE_ID, DELEGATED_SERVICE_ID)!!
                    if (serviceEntity.serviceId == delegatedServiceId) {
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
                        mixpanelAPI.track(
                            "Detailed Service - Repeating Request",
                            serviceRequestObject
                        )
                    }
                }

        } else {
            requestAgentBtn!!.setOnClickListener {
                /** Creating bottomSheet service request **/
                val requestBundle = Bundle()
                requestBundle.putSerializable(SERVICE_ENTITY, serviceEntity)

                val introduceDelegateBottomSheetDialog = IntroduceDelegate()
                introduceDelegateBottomSheetDialog.arguments = requestBundle

                val serviceRequestBottomSheetDialog = ServiceRequestBottomSheet()
                serviceRequestBottomSheetDialog.arguments = requestBundle

                val serviceRequested = JSONObject()
                serviceRequested.put(SERVICE_ENTITY, serviceEntity)

                if (detailedServicePrefs.getBoolean(DELEGATION_INTRO_IS_CONFIRMED, false)) {
                    serviceRequestBottomSheetDialog.show(
                        this.supportFragmentManager,
                        ServiceRequestBottomSheet.TAG
                    )
                    mixpanelAPI.track("Detailed Service - Requesting Service", serviceRequested)
                } else {
                    introduceDelegateBottomSheetDialog.show(
                        this.supportFragmentManager,
                        IntroduceDelegate.TAG
                    )
                    mixpanelAPI.track(
                        "Detailed Service - Requesting Service (INTRO)",
                        serviceRequested
                    )
                }
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
        mixpanelAPI.track("Detailed Service - Service Fee Self-Support")

    }

    override fun onBackPressed() {
        super.onBackPressed()

        summoningParent = intent.extras!!.getString(PARENT, "")

        val intent = Intent(this, MainScreen::class.java)
        val bundle = Bundle()
        bundle.putString(VIEW_SOURCE, DETAILED_SERVICE)
        intent.putExtra(VIEW_SOURCE, bundle)

        finish()

        mixpanelAPI.track("Detailed Service - Navigate Back")
    }

    override fun onPause() {
        super.onPause()
        progress!!.dismiss()
        agentRequestDialog!!.setOnDismissListener { Timber.e("onPause: onDialogDismiss!") }
        agentNotFoundDialog!!.setOnDismissListener { Timber.e("onPause: onDialogDismiss!") }
        checkServiceUtilityThumbs()
        finish()
    }

    override fun onResume() {
        super.onResume()
        serviceCommentEditText!!.clearFocus()
        serviceCommentEditText!!.isFocusable = true

        /** Hiding Soft Input Keyboard Window below **/
        val inputMethodManager: InputMethodManager =
            this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(detailedServiceBinding.root.windowToken, 0)

        Timber.e("ON RESUME -> ${serviceCommentEditText!!.hasFocus()}")
    }

    private fun populateDetailedServiceElements(mService: ServiceEntity) {
        Timber.e("UNPACKING SERVICE INTO UI ELEMENTS $mService")
        detailedServicePrefs = getSharedPreferences(ummoUserPreferences, mode)

        toolbar!!.title = mService.serviceName
        mCollapsingToolbarLayout!!.title = mService.serviceName
        serviceNameTextView!!.text = mService.serviceName
        /** Conveniently place in**/
//        serviceCommentsHeaderSubtitle!!.text = mService.serviceName

        serviceDescriptionTextView!!.text = mService.serviceDescription

        /** Checking if [mService] has ServiceLink **/
        if (mService.serviceLink!!.isEmpty()) {
            webViewLink!!.text = "No link found for this service."
            webViewLink!!.textSize = 10F
            webViewLink!!.setTextColor(resources.getColor(R.color.greyProfile))
            launch_link_image_view.visibility = View.GONE
        }

        /** Checking if [mService] has ServiceAttachments **/
        if (mService.serviceAttachmentURL!!.isEmpty()) {
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
            attachmentNameTextView.id = mService.serviceAttachmentName!!.length
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
                    .put("FILE_NAME", serviceEntity.serviceAttachmentName)
                    .put("FILE_URL", serviceEntity.serviceAttachmentURL)

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
        serviceCostArrayList = mService.serviceCost!!
        serviceDurationTextView!!.text = mService.serviceDuration

        if (mService.delegatable!!) {
            requestAgentBtn!!.visibility = View.VISIBLE
        } else {
            requestAgentBtn!!.visibility = View.GONE
        }

        docsList = serviceEntity.serviceDocuments?.let { ArrayList(it) }

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

        /** Parsing Service Steps **/
        if (mService.serviceSteps!!.isNotEmpty()) {
            serviceStepsHorizontalScroller?.removeAllViews()

            serviceStepsChipGroup = ChipGroup(this)
            serviceStepsChipGroup!!.setChipSpacing(2)
//            serviceStepsChipGroup!!.chipSpacingHorizontal = 2
//            serviceStepsChipGroup!!.chipSpacingVertical = 2
//            serviceStepsChipGroup!!.isSelectionRequired = false

            serviceStepsChipGroup!!.isSingleLine = true
            serviceStepsChipGroup!!.layoutParams = ChipGroup.LayoutParams(
                ChipGroup.LayoutParams.WRAP_CONTENT,
                ChipGroup.LayoutParams.WRAP_CONTENT
            )
            serviceStepsChipGroup!!.removeView(serviceStepChip)
            for (i in mService.serviceSteps!!.indices) {
                var mServiceStepChip = Chip(this)
                mServiceStepChip.id = i
                mServiceStepChip.text = mService.serviceSteps!![i].toString().trim()
                mServiceStepChip.textSize = 14F
                mServiceStepChip.textEndPadding = 0F
                mServiceStepChip.chipEndPadding = 0F
                mServiceStepChip.closeIconEndPadding = 0F
                mServiceStepChip.closeIconSize = 0F

                mServiceStepChip.backgroundTintList =
                    getColorStateList(R.color.mtrl_choice_chip_background_color)
                serviceStepsChipGroup!!.addView(mServiceStepChip, i)
            }
            serviceStepsHorizontalScroller?.addView(serviceStepsChipGroup)
        }

        /** Parsing Service Centres into Chip-items **/
        serviceCentresList = serviceEntity.serviceCentres?.let { ArrayList(it) }
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

        serviceBenefitsList = serviceEntity.serviceBenefits
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
                mixpanelAPI.track("Detailed Service - Showing Benefits")

            } else {
                serviceBenefitsLayout?.visibility = View.VISIBLE
                toggleServiceBenefitsTextView.text = "Hide Benefits"
                expandServiceBenefitsImageView.visibility = View.VISIBLE
                collapseServiceBenefitsImageView.visibility = View.GONE
                mixpanelAPI.track("Detailed Service - Hiding Benefits")
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

            when (i) {
                0 -> {
                    serviceBenefitOneTitle?.text = "${i + 1}. $benefitTitle"
                    serviceBenefitOneBody?.text = benefitBody
                }
                1 -> {
                    serviceBenefitTwoTitle?.text = "${i + 1}. $benefitTitle"
                    serviceBenefitTwoBody?.text = benefitBody
                }
                2 -> {
                    serviceBenefitThreeTitle?.text = "${i + 1}. $benefitTitle"
                    serviceBenefitThreeBody?.text = benefitBody
                }
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
                downloadAttachment(serviceEntity.serviceAttachmentURL)
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
            downloadAttachment(serviceEntity.serviceAttachmentURL)
        }
    }

    private fun downloadAttachment(attachmentLink: String?) =
        CoroutineScope(Dispatchers.IO).launch {

            try {
                Timber.e("DOWNLOADING ATTACHMENT -> $attachmentLink")
                showSnackbarWhite("Downloading file...", -1)

                val downloadRequest = DownloadManager
                    .Request(Uri.parse(attachmentLink))
                    .setTitle(serviceEntity.serviceAttachmentName)
                    .setDestinationInExternalPublicDir(
                        Environment.DIRECTORY_DOWNLOADS,
                        serviceEntity.serviceAttachmentName
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
                        .put("file_name", serviceEntity.serviceAttachmentName)

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
            if (summoningParent == MainScreen.toString()) {
                startActivity(intent)
            } else if (summoningParent == "ServicesAdapter") {
                intent.putExtra(FRAGMENT_DESTINATION, AllServicesFragment.toString())
                Timber.e("FRAG DESTINATION -> ${intent.extras!!.getString(FRAGMENT_DESTINATION)}")
                startActivity(intent)
            }
            finish()
            return true
        } else if (item.itemId == R.id.share_service_icon) {
            shareServiceInfo()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.detailed_service_menu, menu)

        return true
    }

    private fun shareServiceInfo() {
        val sharedServiceEntity = JSONObject()
        val shareBundle = Bundle()
        shareBundle.putSerializable(SERVICE_ENTITY, serviceEntity)

        val shareServiceInfoBottomSheet = ShareServiceInfoBottomSheet()
        shareServiceInfoBottomSheet.arguments = shareBundle
        shareServiceInfoBottomSheet.show(
            this.supportFragmentManager,
            ShareServiceInfoBottomSheet.TAG
        )

        sharedServiceEntity.put("service_name", serviceEntity.serviceName)
        mixpanelAPI.track("Detailed Service - Sharing ServiceInfo: PhaseOne")
    }

    private fun checkServiceUtilityThumbs() {
        val serviceHelpful = detailedServicePrefs
            .getBoolean("HELPFUL-${serviceEntity.serviceId}", false)
        val serviceNotHelpful = detailedServicePrefs
            .getBoolean("NOT-HELPFUL-${serviceEntity.serviceId}", false)

        if (serviceHelpful) {
            service_util_thumbs_up_image_view.visibility = View.GONE
            service_util_thumbs_up_selected_image_view.visibility = View.VISIBLE
            service_util_thumbs_down_selected_image_view.visibility = View.GONE
            service_util_thumbs_down_image_view.visibility = View.VISIBLE
        } else if (!serviceHelpful) {
            service_util_thumbs_up_image_view.visibility = View.VISIBLE
            service_util_thumbs_up_selected_image_view.visibility = View.GONE
        }

        if (serviceNotHelpful) {
            service_util_thumbs_down_selected_image_view.visibility = View.VISIBLE
            service_util_thumbs_down_image_view.visibility = View.GONE
            service_util_thumbs_up_selected_image_view.visibility = View.GONE
            service_util_thumbs_up_image_view.visibility = View.VISIBLE
        } else if (!serviceHelpful) {
            service_util_thumbs_down_image_view.visibility = View.VISIBLE
            service_util_thumbs_down_selected_image_view.visibility = View.GONE
        }
    }

    /** When the User confirms that a service's info was helpful, we should:
     *  1. Change the outlined thumbs-up icon to a full, colored thumbs-up icon;
     *  2. TODO: (Possibly) Animate the state change to reward the User for choosing;
     *  3. Save the response in RoomDB
     *  4. TODO: Transmit the response via a ServiceHelpfulRepo to the server/Mongo;
     *  5. TODO: Capture the event with [MixpanelAPI]
     *  6. Store in SharedPrefs **/
    private fun triggerHelpful() {
        colourThumbsUpIcon()
    }

    private fun reverseHelpful() {
        outlineThumbsUpIcon()
    }

    private fun triggerNotHelpful() {
        colourThumbsDownIcon()
    }

    private fun reverseNotHelpful() {
        outlineThumbsDownIcon()
    }

    private fun colourThumbsUpIcon() {
        service_util_thumbs_up_image_view.visibility = View.GONE
        service_util_thumbs_up_selected_image_view.visibility = View.VISIBLE
    }

    private fun outlineThumbsUpIcon() {
        service_util_thumbs_up_image_view.visibility = View.VISIBLE
        service_util_thumbs_up_selected_image_view.visibility = View.GONE
    }

    private fun colourThumbsDownIcon() {
        service_util_thumbs_down_image_view.visibility = View.GONE
        service_util_thumbs_down_selected_image_view.visibility = View.VISIBLE
    }

    private fun outlineThumbsDownIcon() {
        service_util_thumbs_down_image_view.visibility = View.VISIBLE
        service_util_thumbs_down_selected_image_view.visibility = View.GONE
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