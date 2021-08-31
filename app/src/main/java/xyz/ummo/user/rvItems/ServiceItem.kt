package xyz.ummo.user.rvItems

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.content_detailed_service.view.*
import kotlinx.android.synthetic.main.service_card.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.*
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity.Companion.DELEGATED_SERVICE_ID
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity.Companion.DELEGATION_ID
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceExtrasBottomSheetDialogFragment
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceFeeQuery
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceRequestBottomSheet
import xyz.ummo.user.ui.fragments.bottomSheets.ShareServiceInfoBottomSheet
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.*
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ServiceItem(
    private val service: ServiceObject,
    val context: Context?,
    savedUserActions: JSONObject
) : Item<GroupieViewHolder>() {

    private var serviceId: String = ""
    private val bundle = Bundle()

    /** Shared Preferences for storing user actions **/
    private lateinit var serviceItemPrefs: SharedPreferences
    private lateinit var savingServiceOfflineAnimation: AnimationDrawable

    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private var upVote: Boolean = false
    private var downVote: Boolean = false
    private var commentedOn: Boolean = false

    //    private var bookmarked: Boolean = false
    private var anonymousComment: Boolean = false
    private val isUpvotedEvent = UpvoteServiceEvent()
    private val isDownvotedEvent = DownvoteServiceEvent()
    private val serviceCommentEvent = ServiceCommentEvent()
    private val serviceBookmarkedEvent = ServiceBookmarkedEvent()

    //    private val isBookmarkedEvent = ServiceBookmarkedEvent()
    private val paymentTermsEvent = ConfirmPaymentTermsEvent()
    private val delegateStateEvent = DelegateStateEvent()
    private val passingServiceEvent = PassingServiceEvent()
    private val serviceSpecifiedEvent = ServiceSpecifiedEvent()

    private var isUpvotedPref: Boolean = false
    private var isDownvotedPref: Boolean = false

    private val agentRequestDialog = MaterialAlertDialogBuilder(context!!)

    /** Initializing ServiceViewModel **/
    private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
        .get(ServiceViewModel::class.java)

    private var delegatedServiceModel = ViewModelProvider(context as FragmentActivity)
        .get(DelegatedServiceViewModel::class.java)

    private var serviceEntity = ServiceEntity()

    private var userContactPref = ""
    private val inflater = LayoutInflater.from(context)

    private var serviceCostAdapter: ArrayAdapter<ServiceCostModel>? = null
    private var serviceCostSpinner: Spinner? = null
    private var serviceCostArrayList = ArrayList<ServiceCostModel>()
    private lateinit var serviceCostItem: ServiceCostModel
    private var serviceSpec = ""
    private var specCost = ""
    private var serviceCosts = ArrayList<ServiceCostModel>()
    private lateinit var serviceSpecs: Array<String?>
    private lateinit var specCosts: Array<Int?>

    //private lateinit var serviceCentresTextView: TextView
    private lateinit var serviceRequirementsTextView: TextView

    init {
        upVote = savedUserActions.getBoolean("UP-VOTE")
        downVote = savedUserActions.getBoolean("DOWN-VOTE")
        commentedOn = savedUserActions.getBoolean("COMMENTED-ON")
//        bookmarked = savedUserActions.getBoolean("BOOKMARKED")

        serviceId = service.serviceId

    }

    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")

    override fun getLayout(): Int {
        return R.layout.service_card
    }

    @SuppressLint("SimpleDateFormat")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val delegatedServiceViewModel = ViewModelProvider((context as FragmentActivity?)!!)
            .get(DelegatedServiceViewModel::class.java)

        /** Date chunk below is being used to capture a selection's time-stamp **/
        val simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        val currentDate = simpleDateFormat.format(Date())

        val mixpanel = MixpanelAPI.getInstance(
            context,
            context?.resources?.getString(R.string.mixpanelToken)
        )
        val serviceItemObject = JSONObject()

//        Timber.e("UP-VOTE -> $upVote")
//        Timber.e("DOWN-VOTE -> $downVote")
//        Timber.e("COMMENTED-ON -> $commentedOn")
//        Timber.e("BOOKMARKED -> $bookmarked")

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        userContactPref = serviceItemPrefs.getString("USER_CONTACT", "")!!

        isUpvotedPref = serviceItemPrefs.getBoolean("UP-VOTE-${service.serviceId}", false)
        isDownvotedPref = serviceItemPrefs.getBoolean("DOWN-VOTE-${service.serviceId}", false)

        viewHolder.itemView.service_title_text_view.text = service.serviceName //1
        viewHolder.itemView.service_description_text_view.text = service.serviceDescription //2
        viewHolder.itemView.service_eligibility_text_view.text = service.serviceEligibility //3

        checkIfServiceIsSavedOffline(serviceId, viewHolder)

        /** Parsing and displaying the service centres in the Service Centres linear layout **/
        if (service.serviceCentres.isNotEmpty()) {
            viewHolder.itemView.service_centres_chip_group.removeAllViews()
//            viewHolder.itemView.service_centres_linear_layout.removeAllViews()
            for (i in service.serviceCentres.indices) {

                val serviceCentreChipItem = inflater.inflate(
                    R.layout.service_centre_chip_item,
                    null, false
                ) as Chip

                serviceCentreChipItem.text = service.serviceCentres[i]
                Timber.e("CENTRE_CHIP -> ${service.serviceCentres[i]}")

                viewHolder.itemView.service_centres_chip_group.setOnCheckedChangeListener { group, checkedId ->
                    serviceItemObject.put("SERVICE_CENTRE_CHIP", service.serviceCentres[i])
                    Timber.e("SERVICE CENTRE CHECKED [GROUP] -> ${service.serviceCentres[i]}")
                    mixpanel?.track("serviceCard_serviceCentreChecked", serviceItemObject)
                }

                serviceCentreChipItem.setOnCheckedChangeListener { compoundButton, b ->
                    Timber.e("SERVICE CENTRE CHECKED [GROUP] -> ${service.serviceCentres[i]}")
                }
                /*if (serviceCentreChipItem.isChecked) {
                    serviceItemObject.put("CENTRE_CHIP", service.serviceCentres)
                    mixpanel?.track("serviceCard_centreChipChecked", serviceItemObject)

                    Timber.e("CENTRE_CHIP_CHECKED -> ${service.serviceCentres}")
                }*/

                viewHolder.itemView.service_centres_chip_group.addView(serviceCentreChipItem)
            }
        }

        /** Parsing Service Costs**/
        parseServiceCostBySpec(service)
        /** Listening for Item Selected **/
        /*addListenerOnSpinnerItemSelected(viewHolder)
//        viewHolder.itemView.service_cost_text_view.text = service.serviceCost //6
        serviceCostSpinner = viewHolder.itemView.service_cost_spinner
        serviceCostAdapter = ArrayAdapter(context,
                R.layout.support_simple_spinner_dropdown_item, serviceCostArrayList)
        serviceCostSpinner?.adapter = serviceCostAdapter*/

        selectingServiceSpec(viewHolder)

        viewHolder.itemView.service_duration_text_view.text = service.serviceDuration //7
//        viewHolder.itemView.service_requirements_text_view.text = service.serviceDocuments.toString() //8

        /** Parsing and displaying the service requirements in the Service Requirements linear layout **/
        if (service.serviceDocuments.isNotEmpty()) {
            viewHolder.itemView.service_requirements_chip_group.removeAllViews()
            for (i in service.serviceDocuments.indices) {
                val serviceRequirementsChipItem = inflater.inflate(
                    R.layout.service_centre_chip_item,
                    null, false
                ) as Chip

                serviceRequirementsChipItem.text = service.serviceDocuments[i]
                viewHolder.itemView.service_requirements_chip_group.addView(
                    serviceRequirementsChipItem
                )

                viewHolder.itemView.service_requirements_chip_group.setOnCheckedChangeListener { group, checkedId ->
                    Timber.e("DOCS CHECKED -> ${service.serviceDocuments[i]}")
                    serviceItemObject.put("SERVICE_DOC_CHIP", service.serviceDocuments[i])
                    mixpanel?.track("serviceCard_serviceDocChecked", serviceItemObject)

                }
            }
        }

        //TODO: Assign serviceEntity to serviceValues
        assignServiceEntity(serviceEntity)

        viewHolder.itemView.service_info_title_relative_layout.setOnClickListener {
            showServiceDetails()

            val mixpanelServiceObject = JSONObject()
            mixpanelServiceObject.put("SERVICE_NAME", serviceEntity.serviceName)
            mixpanel?.track("serviceCard_cardTitleTapped", mixpanelServiceObject)
        }

        viewHolder.itemView.approve_count_text_view.text = service.usefulCount.toString() //9
        viewHolder.itemView.approved_count_text_view.text = service.usefulCount.toString() //9.1

        viewHolder.itemView.disapprove_count_text_view.text = service.notUsefulCount.toString() //10
        viewHolder.itemView.disapproved_count_text_view.text =
            service.notUsefulCount.toString() //10.1

        viewHolder.itemView.service_comments_count_text_view.text =
            service.serviceComments.size.toString() //11
//        viewHolder.itemView.save_count_text_view.text = service.serviceShareCount.toString() //12
//        viewHolder.itemView.share_count_text_view.text = service.serviceShareCount.toString() //12

        /** If service is not delegatable, we don't need to draw the $RequestAgentButton **/
        if (service.delegatable) {
            viewHolder.itemView.request_agent_button.visibility = View.VISIBLE
        } else {
            viewHolder.itemView.request_agent_button.visibility = View.GONE
        }

        markDelegatedAlready(viewHolder)

        /** The below two methods check for any actions the user might have taken before a
         * click-action is lodged:
         * 1. If a service-up-vote || service-down-vote exists, deactivate the UP-VOTE option
         * 2. else, leave things as they're... **/
        if (upVote) {
            upVoteTriggeredChangeStates(viewHolder)
        } else {
            reverseUpVoteTriggeredChangeStates(viewHolder)
        }

        if (downVote) {
            downVoteTriggeredChangeStates(viewHolder)
        } else {
            reverseDownVoteTriggeredChangeStates(viewHolder)
        }

        if (commentedOn) {
            commentTriggeredChangeStates(viewHolder)
        } else {
            reverseCommentTriggeredChangeStates(viewHolder)
        }

        viewHolder.itemView.service_info_icon_relative_layout.setOnClickListener {

            bundle.putBoolean("SERVICE_DELEGATABLE", service.delegatable)
            val serviceExtrasBottomSheetDialogFragment = ServiceExtrasBottomSheetDialogFragment()
            serviceExtrasBottomSheetDialogFragment.arguments = bundle
            serviceExtrasBottomSheetDialogFragment
                .show(
                    context.supportFragmentManager,
                    ServiceExtrasBottomSheetDialogFragment.TAG
                )

            serviceItemObject.put("SERVICE_ID", serviceId)
            mixpanel?.track("serviceCard_infoIconTapped", serviceItemObject)
        }

        /** Capturing the Share Service Info action (Phase-1)**/
        viewHolder.itemView.service_share_icon_relative_layout.setOnClickListener {
            val sharedServiceObject = JSONObject()
            val shareBundle = Bundle()
            shareBundle.putSerializable(SERVICE_OBJECT, service)

            val shareServiceInfoBottomSheet = ShareServiceInfoBottomSheet()
            shareServiceInfoBottomSheet.arguments = shareBundle
            shareServiceInfoBottomSheet.show(
                context.supportFragmentManager,
                ShareServiceInfoBottomSheet.TAG
            )

            sharedServiceObject.put("service_name", service.serviceName)
            mixpanel?.track("serviceCard_sharingServiceInfo_phaseOne")
        }

        viewHolder.itemView.service_query_icon_relative_layout.setOnClickListener {

            bundle.putString(SERVICE_ID, serviceId)
            val serviceFeeQueryBottomSheetFragment = ServiceFeeQuery()
            serviceFeeQueryBottomSheetFragment.show(
                context.supportFragmentManager,
                ServiceFeeQuery.TAG
            )

            serviceItemObject.put("SERVICE_ID", serviceId)
            mixpanel?.track("serviceCard_queryIconTapped", serviceItemObject)
        }

        /** Expand Service Card to reveal more info - Layout-Click... **/
        viewHolder.itemView.action_layout.setOnClickListener {
            if (viewHolder.itemView.action_text_view.text == "MORE INFO") {
                /*viewHolder.itemView.expandable_relative_layout.visibility = View.VISIBLE
                viewHolder.itemView.expand_image_view.visibility = View.GONE
                viewHolder.itemView.collapse_image_view.visibility = View.VISIBLE
                viewHolder.itemView.action_text_view.text = "CLOSE"*/

                showServiceDetails()

                serviceItemObject.put("SERVICE_NAME", serviceEntity.serviceName)

                mixpanel?.track("serviceCard_infoExpanded", serviceItemObject)

            } else if (viewHolder.itemView.action_text_view.text == "CLOSE") {
                /*viewHolder.itemView.expandable_relative_layout.visibility = View.GONE
                viewHolder.itemView.expand_image_view.visibility = View.VISIBLE
                viewHolder.itemView.collapse_image_view.visibility = View.GONE
                viewHolder.itemView.action_text_view.text = "MORE INFO"

                serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                        .put("SERVICE_ID", serviceId)
                mixpanel?.track("serviceCard_infoCollapsed", serviceItemObject)*/

                showServiceDetails()

                serviceItemObject.put("SERVICE_NAME", serviceEntity.serviceName)

                mixpanel?.track("serviceCard_infoExpanded", serviceItemObject)

            }
        }

        /** Expand Service Card to reveal more info - Text-Click... **/
        viewHolder.itemView.action_text_view.setOnClickListener {
            if (viewHolder.itemView.action_text_view.text == "MORE INFO") {

                showServiceDetails()

                serviceItemObject.put("SERVICE_NAME", serviceEntity.serviceName)
                mixpanel?.track("serviceCard_infoExpanded", serviceItemObject)

            } else if (viewHolder.itemView.action_text_view.text == "CLOSE") {

                serviceItemObject.put("SERVICE_NAME", serviceEntity.serviceName)

                mixpanel?.track("serviceCard_infoCollapsed", serviceItemObject)

            }
        }

        /** [1] Approve Service Click Handlers **/
        viewHolder.itemView.approve_service_relative_layout.setOnClickListener {

            upVoteService(viewHolder, currentDate)
            /** #UX Trigger icon-change on click **/
            upVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_UPVOTED", serviceId)
            mixpanel?.track("serviceCard_serviceUpvoted", serviceItemObject)
            serviceItemObject.remove("SERVICE_UPVOTED")

        }
        viewHolder.itemView.approve_service_image.setOnClickListener {

            upVoteService(viewHolder, currentDate)
            /** #UX Trigger icon-change on click **/
            upVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_UPVOTED", serviceId)
            mixpanel?.track("serviceCard_serviceUpvoted", serviceItemObject)
            serviceItemObject.remove("SERVICE_UPVOTED")

        }

        /** [1.1] Reverse Approve Service Click Handlers **/
        viewHolder.itemView.approved_service_relative_layout.setOnClickListener {
            undoServiceUpvote(currentDate)
            reverseUpVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_UPVOTED_UNDO", serviceId)
            mixpanel?.track("serviceCard_serviceUpvoted_undo", serviceItemObject)
            serviceItemObject.remove("SERVICE_UPVOTED_UNDO")
        }
        viewHolder.itemView.approved_service_image.setOnClickListener {
            undoServiceUpvote(currentDate)
            reverseUpVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_UPVOTED_UNDO", serviceId)
            mixpanel?.track("serviceCard_serviceUpvoted_undo", serviceItemObject)
            serviceItemObject.remove("SERVICE_UPVOTED_UNDO")
        }

        /** [2] Disapprove Service Click Handlers **/
        viewHolder.itemView.disapprove_service_relative_layout.setOnClickListener {
            downVoteService(viewHolder, currentDate)
            /** #UX Trigger icon-change on click **/
            downVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_DOWNVOTED", serviceId)
            mixpanel?.track("serviceCard_serviceDownvoted", serviceItemObject)
            serviceItemObject.remove("SERVICE_DOWNVOTED")
        }
        viewHolder.itemView.disapprove_service_image.setOnClickListener {
            downVoteService(viewHolder, currentDate)
            /** #UX Trigger icon-change on click **/
            downVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_DOWNVOTED", serviceId)
            mixpanel?.track("serviceCard_serviceDownvoted", serviceItemObject)
            serviceItemObject.remove("SERVICE_DOWNVOTED")
        }

        /** [2.1] Reverse Disapprove Service Click Handlers **/
        viewHolder.itemView.disapproved_service_relative_layout.setOnClickListener {
            undoServiceDownvote(currentDate)
            reverseDownVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_DOWNVOTED", serviceId)
            mixpanel?.track("serviceCard_serviceDownvoted_undo", serviceItemObject)
            serviceItemObject.remove("SERVICE_DOWNVOTED")
        }
        viewHolder.itemView.disapproved_service_image.setOnClickListener {
            undoServiceDownvote(currentDate)
            reverseDownVoteTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_DOWNVOTED", serviceId)
            mixpanel?.track("serviceCard_serviceDownvoted_undo", serviceItemObject)
            serviceItemObject.remove("SERVICE_DOWNVOTED")
        }

        /** [3] Service Feedback Click Handlers **/
        viewHolder.itemView.service_comments_relative_layout.setOnClickListener {
            makeServiceComment(viewHolder, currentDate)
            //TODO: remove commentTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_COMMENTED_ON", serviceId)
            mixpanel?.track("serviceCard_serviceCommentTapped", serviceItemObject)
            serviceItemObject.remove("SERVICE_COMMENTED_ON")
        }
        viewHolder.itemView.service_comments_image.setOnClickListener {
            makeServiceComment(viewHolder, currentDate)
            //TODO: remove commentTriggeredChangeStates(viewHolder)

            serviceItemObject.put("EVENT_DATE_TIME", currentDate)
                .put("SERVICE_COMMENTED_ON", serviceId)
            mixpanel?.track("serviceCard_serviceCommentTapped", serviceItemObject)
            serviceItemObject.remove("SERVICE_COMMENTED_ON")
        }

        /** [4] Save Service Click Handlers **/
        viewHolder.itemView.save_service_relative_layout.setOnClickListener {
            Timber.e("SAVING SERVICE ENTITY -> ${serviceEntity.serviceName}")
            serviceViewModel.addService(serviceEntity)
            serviceBookmarkedEvent.serviceName = serviceEntity.serviceName
            serviceBookmarkedEvent.serviceBookmarked = true
            EventBus.getDefault().post(serviceBookmarkedEvent)

            val timer = object : CountDownTimer(2000, 1000) {
                override fun onTick(p0: Long) {
                    //TODO: Show animation
//                    viewHolder.itemView.save_service_imag
                }

                override fun onFinish() {
                    /** Tracking "SERVICE_DOWNLOAD" event **/
                    serviceItemObject.put("SERVICE_DOWNLOADED", service.serviceName)
                    serviceItemObject.put("SERVICE_ID", service.serviceId)
                    mixpanel?.track("serviceCard_serviceDownloaded", serviceItemObject)

                    viewHolder.itemView.save_service_image.setImageResource(R.drawable.ic_saved_offline_pin_24)
                }
            }
            timer.start()
        }

        viewHolder.itemView.save_service_image.setOnClickListener {
            Timber.e("SAVING SERVICE ENTITY -> ${serviceEntity.serviceName}")
            serviceViewModel.addService(serviceEntity)
            serviceBookmarkedEvent.serviceName = serviceEntity.serviceName
            serviceBookmarkedEvent.serviceBookmarked = true
            EventBus.getDefault().post(serviceBookmarkedEvent)

            val timer = object : CountDownTimer(2000, 1000) {
                override fun onTick(p0: Long) {
//                    viewHolder.itemView.save_service_imag
                }

                override fun onFinish() {
                    /** Tracking "SERVICE_DOWNLOAD" event **/
                    serviceItemObject.put("SERVICE_DOWNLOADED", service.serviceName)
                    serviceItemObject.put("SERVICE_ID", service.serviceId)
                    mixpanel?.track("serviceCard_serviceDownloaded", serviceItemObject)

                    viewHolder.itemView.save_service_image.setImageResource(R.drawable.ic_saved_offline_pin_24)
                }
            }
            timer.start()
        }
        requestingAgent(viewHolder)
    }

    private fun showServiceDetails() {
        val intent = Intent(context, DetailedServiceActivity::class.java)

        /** Passing Service to [DetailedServiceActivity] via [Serializable] object **/
        intent.putExtra(SERVICE_OBJECT, service as Serializable)
        Timber.e("SHOWING SERVICE DETAILS -> $service")

        serviceViewModel.addService(serviceEntity)
        context!!.startActivity(intent)
    }

    private fun requestingAgent(viewHolder: GroupieViewHolder) {
        val delegatedServiceViewModel = ViewModelProvider((context as FragmentActivity?)!!)
            .get(DelegatedServiceViewModel::class.java)

        val mixpanel = MixpanelAPI.getInstance(
            context,
            context?.resources?.getString(R.string.mixpanelToken)
        )

        val serviceItemObject = JSONObject()

        val countOfDelegatedServices = delegatedServiceViewModel.getCountOfDelegatedServices()

        /** Preventing User from delegating more than one service at a time. **/
        if (countOfDelegatedServices > 0) {
            delegatedServiceViewModel.delegatedServiceEntityLiveData.observe(context!!) { delegatedServiceEntity: DelegatedServiceEntity ->
                val delegatedServiceId = delegatedServiceEntity.delegatedProductId

                when {
                    serviceEntity.serviceId != delegatedServiceId -> {
                        viewHolder.itemView.request_agent_button!!.text = "Service pending..."
                        viewHolder.itemView.request_agent_button
                            .setBackgroundColor(context.resources.getColor(R.color.ummo_3))
                        viewHolder.itemView.request_agent_button!!.icon =
                            context.resources.getDrawable(R.drawable.ic_service_locked_24)

                        viewHolder.itemView.request_agent_button.setOnClickListener {
                            delegateStateEvent.delegateStateEvent = SERVICE_PENDING
                            EventBus.getDefault().post(delegateStateEvent)
                        }

                        /** Tracking a repeat request **/
                        serviceItemObject.put("REQUESTING", service.serviceName)
                        mixpanel?.track("serviceCard_repeatingRequest", serviceItemObject)
                    }
                    serviceEntity.serviceId.equals(delegatedServiceId) -> {
                        viewHolder.itemView.request_agent_button.setOnClickListener {
                            delegateStateEvent.delegateStateEvent = CURRENT_SERVICE_PENDING
                            EventBus.getDefault().post(delegateStateEvent)
                        }
                    }
                }
            }
        } else {
            Timber.e("SERVICE COST SELECTED [0] -> $specCost")

            /* if (specCost.isEmpty()) {
                 Timber.e("NO SERVICE COST SELECTED!")
                 viewHolder.itemView.request_agent_button.setOnClickListener {
                     serviceSpecifiedEvent.specifiedEvent = false
                     EventBus.getDefault().post(serviceSpecifiedEvent)
                     return@setOnClickListener
                 }
             } else {*/
            viewHolder.itemView.request_agent_button.setOnClickListener {
//                    makeRequest()

                /** Tracking a new service request **/
                serviceItemObject.put("REQUESTING", service.serviceName)
                mixpanel?.track("serviceCard_requestingService", serviceItemObject)

                /** Saving selected [service] to RoomDB **/
                serviceViewModel.addService(serviceEntity)

                /** Creating bottomSheet service request **/
                val requestBundle = Bundle()
                requestBundle.putSerializable(SERVICE_OBJECT, service)
                val serviceRequestBottomSheetDialog = ServiceRequestBottomSheet()
                serviceRequestBottomSheetDialog.arguments = requestBundle
                serviceRequestBottomSheetDialog
                    .show(
                        context!!.supportFragmentManager,
                        ServiceRequestBottomSheet.TAG
                    )
            }
//            }
        }
    }

    private fun parseServiceCostBySpec(serviceObject: ServiceObject) {
        serviceCostArrayList = serviceObject.serviceCost
        Timber.e("SERVICE COST ARRAY LIST -> $serviceCostArrayList")
    }

    private fun checkIfServiceIsSavedOffline(mServiceId: String, viewHolder: GroupieViewHolder) {

        Timber.e("SERVICE ID -> $mServiceId")
        val offlineServices = serviceViewModel.getServicesList()

        for (i in offlineServices.indices) {
            if (mServiceId == offlineServices[i].serviceId) {
                Timber.e("OFFLINE SERVICE -> ${offlineServices[i].serviceName}")
                viewHolder.itemView.you_saved_this_text_view.visibility = View.VISIBLE
                viewHolder.itemView.save_service_image.setImageResource(R.drawable.ic_saved_offline_pin_24)
            }
        }
    }

    private fun markDelegatedAlready(viewHolder: GroupieViewHolder) {
        val delegatedServiceViewModel = ViewModelProvider((context as FragmentActivity?)!!)
            .get(DelegatedServiceViewModel::class.java)

        val countOfDelegatedServices = delegatedServiceViewModel.getCountOfDelegatedServices()

        if (countOfDelegatedServices > 0) {
            delegatedServiceModel.delegatedServiceEntityLiveData.observe(
                context as FragmentActivity,
                { delegatedServiceEntity: DelegatedServiceEntity ->

                    val delegatedServiceId = delegatedServiceEntity.delegatedProductId
                    Timber.e("MARKING DELEGATED ALREADY -> $delegatedServiceId")
                    if (serviceEntity.serviceId == delegatedServiceId) {
                        Timber.e("SERVICE ${serviceEntity.serviceName} has been delegated!")
                        viewHolder.itemView.request_agent_button.text =
                            "IN-PROGRESS" //TODO: direct User to Delegation progress

                        viewHolder.itemView.request_agent_button
                            .setBackgroundColor(context.resources.getColor(R.color.Grey))
                        viewHolder.itemView.request_agent_button.icon =
                            context.resources.getDrawable(R.drawable.ic_hourglass_top_24)
                        viewHolder.itemView.request_agent_button.isActivated = false
                    }
                })
        } else {
            Timber.e("NOTHING DELEGATED YET")
        }
    }

    private fun launchDelegatedService(
        context: Context?,
        delegatedServiceId: String,
        agentId: String,
        delegationId: String
    ) {

        val bundle = Bundle()
        bundle.putString(DELEGATED_SERVICE_ID, delegatedServiceId)
        bundle.putString(AGENT_ID, agentId)
        bundle.putString(DELEGATION_ID, delegationId)

        Timber.e("DELEGATION_ID -> $delegationId")
        Timber.e("DELEGATED_SERVICE_ID -> $delegatedServiceId")
        Timber.e("SERVICE_AGENT_ID -> $agentId")

        val progress = ArrayList<String>()
        val delegatedServiceEntity = DelegatedServiceEntity()
        val delegatedServiceViewModel = ViewModelProvider((context as FragmentActivity?)!!)
            .get(DelegatedServiceViewModel::class.java)

        /** Setting Service as Delegated **/
        serviceEntity.isDelegated = true
        Timber.e("SERVICE ${serviceEntity.serviceName} has been delegated!")

        serviceViewModel.addService(serviceEntity)

        //TODO
        delegatedServiceEntity.delegationId = delegationId
        delegatedServiceEntity.delegatedProductId = delegatedServiceId
        delegatedServiceEntity.serviceAgentId = agentId
        delegatedServiceEntity.serviceProgress = progress
        delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

        val fragmentActivity = context as FragmentActivity
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val delegatedServiceFragment = DelegatedServiceFragment()
        delegatedServiceFragment.arguments = bundle
//        fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
        fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
        fragmentTransaction.commit()
    }

    /** When the user Up-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $upVote option, to prevent them from up-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $downVote option, in case they want to change their minds
     * TODO: Allow the user to undo their upVote by reversing the action altogether **/
    private fun upVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        /** Assigning upVote counts to both textViews; each textView needs a fresh copy of count **/
        viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
        viewHolder.itemView.approved_count_text_view.text = serviceEntity.usefulCount.toString()
        /** Displaying the textView that tells the user what action they've taken **/
        viewHolder.itemView.you_up_voted_this_text_view.visibility = View.VISIBLE
        /** Toggling b/n Approved & Approve (soon to be Upvoted & Upvote) **/
        viewHolder.itemView.approved_service_relative_layout.visibility = View.VISIBLE
        viewHolder.itemView.approve_service_relative_layout.visibility = View.GONE
        /** Ensuring that we don't have both Upvote & Downvote checked simultaneously **/
        reverseDownVoteTriggeredChangeStates(viewHolder)

    }

    private fun reverseUpVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        /** Assigning upVote counts to both textViews; each textView needs a fresh copy of count **/
        viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
        viewHolder.itemView.approved_count_text_view.text = serviceEntity.usefulCount.toString()
        /** Displaying the textView that tells the user what action they've taken **/
        viewHolder.itemView.you_up_voted_this_text_view.visibility = View.INVISIBLE
        /** Toggling b/n Approved & Approve (soon to be Upvoted & Upvote) **/
        viewHolder.itemView.approved_service_relative_layout.visibility = View.GONE
        viewHolder.itemView.approve_service_relative_layout.visibility = View.VISIBLE
    }

    private fun selectingServiceSpec(viewHolder: GroupieViewHolder) {
        val mixpanel = MixpanelAPI.getInstance(
            context,
            context?.resources?.getString(R.string.mixpanelToken)
        )

        val autoCompleteTextView =
            viewHolder.itemView.findViewById<AutoCompleteTextView>(R.id.card_service_cost_text_View)

        serviceCostAdapter = ArrayAdapter(
            context!!,
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

            Timber.e("CURRENCY INDEX -> $currencyIndex")
            serviceSpec = selectedText.substring(0, currencyIndex - 2)
            specCost = selectedText.substring(currencyIndex + 1)

            Timber.e("SPEC-COST -> $specCost")
            Timber.e("SERVICE-SPEC -> $serviceSpec")

            serviceSpecifiedEvent.specifiedEvent = true
            EventBus.getDefault().post(serviceSpecifiedEvent)

            val serviceSpecCost = JSONObject()
            serviceSpecCost
                .put("SERVICE_SPEC", serviceSpec)
                .put("SPEC_COST", specCost)
            mixpanel.track("serviceCard_serviceSpecSelected", serviceSpecCost)


            requestingAgent(viewHolder)
        }
    }

    private fun upVoteService(viewHolder: GroupieViewHolder, date: String) {

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        val servicePreviouslyDownvoted = serviceItemPrefs
            .getBoolean("DOWN-VOTE-${serviceEntity.serviceId}", false)

        Timber.e("SERVICE PREV. DOWNVOTED -> $servicePreviouslyDownvoted")

        /** Undoing a previous downvote by User (for coherence) **/
        if (servicePreviouslyDownvoted) {
            undoServiceDownvote(date)
        }

        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val upVoteEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        serviceEntity.usefulCount = serviceEntity.usefulCount?.plus(1)
        Timber.e("UPVOTE COUNT -> ${serviceEntity.usefulCount}")

        upVoteEditor.putBoolean("UP-VOTE-${serviceEntity.serviceId}", true)
            .putBoolean("DOWN-VOTE-${serviceEntity.serviceId}", false).apply()

        Timber.e("SERVICE-USEFUL -> ${serviceEntity.usefulCount}")
        /** Updating Service in [Room] **/
        serviceViewModel.updateService(serviceEntity)
        /** Updating Service [Backend] **/
        makeServiceUpdate(viewHolder, "THUMBS_UP", date)

        /** Publish Upvote Event via EventBus **/
        isUpvotedEvent.serviceUpvote = true
        isUpvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isUpvotedEvent)

        isDownvotedEvent.serviceDownvote = false
        isDownvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isDownvotedEvent)
    }

    private fun undoServiceUpvote(date: String) {
        /** Check if the count is not zero - because we can't have a negative count **/
        if (serviceEntity.usefulCount != 0) {
            serviceEntity.usefulCount = serviceEntity.usefulCount?.minus(1)
            Timber.e("UPVOTE COUNT -> ${serviceEntity.usefulCount}")

        }

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val upVoteEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        upVoteEditor.putBoolean("UP-VOTE-${serviceEntity.serviceId}", false).apply()

        Timber.e("SERVICE-USEFUL -> ${serviceEntity.usefulCount}")
        /** Updating Service in [Room] **/
        serviceViewModel.updateService(serviceEntity)
        //TODO: undo service upvote - server side

        /** Publish Upvote Event via EventBus **/
        isUpvotedEvent.serviceUpvote = false
        isUpvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isUpvotedEvent)

    }

    /** When the user Down-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $downVote option, to prevent them from down-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $upVote option, in case they want to change their minds
     *  TODO: Allow the user to undo their upVote by reversing the action altogether **/
    private fun downVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        /** Assigning downVote counts to both textViews; each textView needs a fresh copy of count **/
        viewHolder.itemView.disapprove_count_text_view.text =
            serviceEntity.notUsefulCount.toString()
        viewHolder.itemView.disapproved_count_text_view.text =
            serviceEntity.notUsefulCount.toString()
        /** Displaying the textView that tells the user what action they've taken **/
        viewHolder.itemView.you_downvoted_this_text_view.visibility = View.VISIBLE
        /** Toggling b/n Disapproved & Disapprove (soon to be Upvoted & Upvote) **/
        viewHolder.itemView.disapproved_service_relative_layout.visibility = View.VISIBLE
        viewHolder.itemView.disapprove_service_relative_layout.visibility = View.GONE
        /** Ensuring that we don't have both Upvote & Downvote checked simultaneously **/
        reverseUpVoteTriggeredChangeStates(viewHolder)
    }

    private fun reverseDownVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        /** Assigning downVote counts to both textViews; each textView needs a fresh copy of count **/
        viewHolder.itemView.disapprove_count_text_view.text =
            serviceEntity.notUsefulCount.toString()
        viewHolder.itemView.disapproved_count_text_view.text =
            serviceEntity.notUsefulCount.toString()
        /** Displaying the textView that tells the user what action they've taken **/
        viewHolder.itemView.you_downvoted_this_text_view.visibility = View.INVISIBLE
        /** Toggling b/n Disapproved & Disapprove (soon to be Upvoted & Upvote) **/
        viewHolder.itemView.disapproved_service_relative_layout.visibility = View.GONE
        viewHolder.itemView.disapprove_service_relative_layout.visibility = View.VISIBLE
    }

    private fun downVoteService(viewHolder: GroupieViewHolder, date: String) {

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        val servicePreviouslyUpvoted = serviceItemPrefs
            .getBoolean("UP-VOTE-${serviceEntity.serviceId}", false)

        if (servicePreviouslyUpvoted) {
            undoServiceUpvote(date)
        }
        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val downVoteEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        downVoteEditor.putBoolean("UP-VOTE-${serviceEntity.serviceId}", false)
            .putBoolean("DOWN-VOTE-${serviceEntity.serviceId}", true).apply()

        serviceEntity.notUsefulCount = serviceEntity.notUsefulCount?.plus(1)
//        Timber.e("SERVICE-NOT-USEFUL -> ${serviceEntity.notUsefulCount}")
        serviceViewModel.updateService(serviceEntity)

        makeServiceUpdate(viewHolder, "THUMBS_DOWN", date)

        /** Publish Downvote Event via EventBus & flip Upvote **/
        isDownvotedEvent.serviceDownvote = true
        isDownvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isDownvotedEvent)

        isUpvotedEvent.serviceUpvote = false
        isUpvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isUpvotedEvent)
    }

    private fun undoServiceDownvote(date: String) {
        /** Check if the count is not zero - because we can't have a negative count **/
        if (serviceEntity.notUsefulCount != 0) {
            serviceEntity.notUsefulCount = serviceEntity.notUsefulCount?.minus(1)
            Timber.e("DOWNVOTE COUNT -> ${serviceEntity.notUsefulCount}")

        }

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val downVoteEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        downVoteEditor.putBoolean("DOWN-VOTE-${serviceEntity.serviceId}", false).apply()

        Timber.e("SERVICE-NOT-USEFUL -> ${serviceEntity.notUsefulCount}")
        /** Updating Service in [Room] **/
        serviceViewModel.updateService(serviceEntity)
        //TODO: undo service upvote - server side

        /** Publish Upvote Event via EventBus **/
        isDownvotedEvent.serviceDownvote = false
        isUpvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isDownvotedEvent)
    }

    /** When the user Down-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $downVote option, to prevent them from down-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $upVote option, in case they want to change their minds
     *  TODO: Allow the user to undo their upVote by reversing the action altogether **/
    /*private fun bookmarkTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.save_service_image.setImageResource(R.drawable.ic_filled_bookmark_24)
        viewHolder.itemView.you_saved_this_text_view.visibility = View.VISIBLE
    }

    private fun reverseBookmarkChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.save_service_image.setImageResource(R.drawable.ic_outline_bookmark_border_24)
        viewHolder.itemView.you_saved_this_text_view.visibility = View.INVISIBLE
    }*/

    /** When the user makes a comment, we want them to immediately see a UI-state change that
     * lets them know that their action has been captured:
     * 1. We update the comment icon to a filled one
     * 2. We let them know that they've commented by showing them a text; **/
    private fun commentTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.service_comments_count_text_view.text =
            serviceEntity.serviceComments!!.size.toString()
        viewHolder.itemView.service_comments_image.setImageResource(R.drawable.ic_chat_bubble_filled_24)
        viewHolder.itemView.you_commented_on_this_text_view.visibility = View.VISIBLE
    }

    private fun reverseCommentTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        //TODO: undo 18
        viewHolder.itemView.service_comments_count_text_view.text =
            serviceEntity.serviceComments!!.size.toString()
        viewHolder.itemView.service_comments_image.setImageResource(R.drawable.ic_outline_chat_bubble_grey_24)
        viewHolder.itemView.you_commented_on_this_text_view.visibility = View.INVISIBLE
    }

    private fun assignServiceEntity(mServiceEntity: ServiceEntity) {
        mServiceEntity.serviceId = service.serviceId //0
        mServiceEntity.serviceName = service.serviceName //1
        mServiceEntity.serviceDescription = service.serviceDescription //2
        mServiceEntity.serviceEligibility = service.serviceEligibility //3
        mServiceEntity.serviceCentres = service.serviceCentres //4
        mServiceEntity.delegatable = service.delegatable //5
        //TODO: undo 16
//        mServiceEntity.serviceCost = service.serviceCost //6
        mServiceEntity.serviceDocuments = service.serviceDocuments //7
        mServiceEntity.serviceDuration = service.serviceDuration //8
        mServiceEntity.usefulCount = service.usefulCount //9
        mServiceEntity.notUsefulCount = service.notUsefulCount //10
        mServiceEntity.serviceComments = service.serviceComments //11
        mServiceEntity.commentCount = service.serviceCommentCount //12
        mServiceEntity.serviceShares = service.serviceShareCount //13
        mServiceEntity.serviceViews = service.serviceViewCount //14
        mServiceEntity.serviceProvider = service.serviceProvider //15
    }

    private fun makeServiceUpdate(viewHolder: GroupieViewHolder, updateType: String, date: String) {
        val serviceUpdate = JSONObject()

        try {
            serviceUpdate.put("_id", serviceEntity.serviceId)
                .put("update_time", date)
                .put("update_type", updateType)
                .put("user_contact", userContactPref)

//            Timber.e("SERVICE-UPDATE-OBJECT -> $serviceUpdate")

            object : UpdateService(context!!, serviceUpdate) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {

//                        Timber.e("SERVICE UPDATED -> ${String(data)}")
                    } else {
                        Timber.e("SERVICE-UPDATE-ERROR-> $code")
                    }
                }
            }
        } catch (jse: JSONException) {
            Timber.e("JSONException ->$jse")
        }
    }

    /** This lets us wrap it all up and plug it into the action-triggers that the user taps to
     * trigger the comment process **/
    private fun makeServiceComment(viewHolder: GroupieViewHolder, date: String) {

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val serviceCommentEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        serviceCommentEditor.putBoolean("COMMENTED-ON-${serviceEntity.serviceId}", true).apply()

        assignServiceEntity(serviceEntity)
        showCommentDialog(viewHolder, date)
    }

    /** This is where we publish the comment captured by #captureServiceComment:
     * 1. $serviceCommentObject takes 4 values: a) serviceId; b) comment; c) date; d) userContact
     * 2. published to the server with the #ServiceComment object override
     * 3. TODO: let the user know that their comment has been published & congratulate them! **/
    private fun commentOnService(
        viewHolder: GroupieViewHolder,
        serviceComment: String,
        date: String
    ) {
        val serviceCommentObject = JSONObject()

        try {
            serviceCommentObject.put("_id", serviceEntity.serviceId)
                .put("service_comment", serviceComment)
                .put("comment_date", date)
                .put("anonymous_comment", anonymousComment)
                .put("user_contact", userContactPref)

            serviceCommentEvent.serviceCommentedOn = true
            serviceCommentEvent.serviceName = serviceEntity.serviceName
            EventBus.getDefault().post(serviceCommentEvent)

            object : ServiceComment(context!!, serviceCommentObject) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {
//                        Timber.e("SERVICE COMMENT -> ${String(data)}")
                        commentTriggeredChangeStates(viewHolder)

                    } else {
                        Timber.e("SERVICE-COMMENT-ERROR-> $code")
                    }
                }
            }
        } catch (jse: JSONException) {
            Timber.e("JSONException ->$jse")
        }
    }

    /** With this function, we're simply displaying the commentDialog  && capturing the comment
     * before inserting it into #captureServiceComment **/
    private fun showCommentDialog(viewHolder: GroupieViewHolder, date: String) {
        val commentDialogView = LayoutInflater.from(context)
            .inflate(R.layout.service_comment_dialog, null)

        val commentDialogBuilder = MaterialAlertDialogBuilder(context!!)

        commentDialogBuilder
            .setTitle("Comment on ${service.serviceName}")
            .setIcon(R.drawable.logo)
            .setView(commentDialogView)

        val anonymousCheckBox = commentDialogView
            .findViewById<CheckBox>(R.id.anonymous_comment_check_box)

        anonymousCheckBox.setOnClickListener {
            anonymousComment = anonymousCheckBox.isChecked
        }

        commentDialogBuilder.setPositiveButton("Comment") { dialogInterface, i ->
            val serviceCommentEditText = commentDialogView
                .findViewById<TextInputEditText>(R.id.service_comment_edit_text)

            val serviceComment = serviceCommentEditText.text?.trim().toString()
            captureServiceComment(viewHolder, serviceComment, date)
        }

        commentDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
            Timber.e("Feedback Cancelled")
        }

        commentDialogBuilder.show()
    }

    /** With this function, we're:
     * 1. doing the actual saving of the comment to RoomDB
     * 2. calling #commentOnService to publish the comment to the back-end/server **/
    private fun captureServiceComment(
        viewHolder: GroupieViewHolder,
        mServiceComment: String,
        date: String
    ) {
        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        serviceEntity.serviceComments?.add(mServiceComment)
        serviceViewModel.updateService(serviceEntity)
        commentOnService(viewHolder, mServiceComment, date)
//        Timber.e("SERVICE COMMENT ${serviceEntity.serviceComments?.size} CAPTURED FOR -> ${serviceEntity.serviceName}!")
    }

    /*private fun bookmarkService(date: String) {
        */
    /** Initializing sharedPreferences **//*
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        */
    /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
     * 1. switch action-icon based on user's previous action
     * 2. toggle b/n icons based on user changing their mind on an action **//*
        val serviceBookmarkEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        serviceBookmarkEditor.putBoolean("BOOKMARKED-${serviceEntity.serviceId}", true).apply()

        serviceEntity.bookmarked = true
        serviceViewModel.updateService(serviceEntity)
        Timber.e("BOOK-MARKING SERVICE -> ${serviceEntity.serviceName}")

        publishServiceBookmark(date)

        isBookmarkedEvent.serviceBookmarked = true
        isBookmarkedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isBookmarkedEvent)
    }*/

    /*private fun removeBookmark(date: String) {
        */
    /** Initializing sharedPreferences **//*
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        */
    /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
     * 1. switch action-icon based on user's previous action
     * 2. toggle b/n icons based on user changing their mind on an action **//*
        val serviceBookmarkEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        serviceBookmarkEditor.putBoolean("BOOKMARKED-${serviceEntity.serviceId}", false).apply()

        serviceEntity.bookmarked = false
        serviceViewModel.updateService(serviceEntity)
        Timber.e("REMOVING SERVICE BOOK-MARK -> ${serviceEntity.serviceName}")

        isBookmarkedEvent.serviceBookmarked = false
        isBookmarkedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isBookmarkedEvent)

        //TODO: remove bookmark from user-profile (backend)
    }*/

    private fun publishServiceBookmark(date: String) {
        val bookmarkObject = JSONObject()
        try {
            bookmarkObject
                .put("bookmark_date", date)
                .put("user_contact", userContactPref)
                .put("bookmarked_service", serviceEntity.serviceId)

            object : BookmarkService(context!!, bookmarkObject) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {
                        Timber.e("BOOKMARKING SERVICE -> ${String(data)}")
                    } else {
                        Timber.e("SERVICE-BOOKMARK-ERROR-> $code")
                    }
                }
            }
        } catch (jse: JSONException) {
            Timber.e("JSONException ->$jse")
        }
    }

    @Subscribe
    fun onServiceCommentedOnEvent(
        viewHolder: GroupieViewHolder,
        serviceCommentEvent: ServiceCommentEvent
    ) {
        Timber.e("SERVICE-COMMENTED-ON-EVENT -> ${serviceCommentEvent.serviceName}")
        Timber.e("SERVICE-COMMENTED-ON-EVENT -> ${serviceCommentEvent.serviceCommentedOn}")

        var serviceCommentCount =
            viewHolder.itemView.service_comments_count_text_view.text.toString().toInt()
        serviceCommentCount += 1
        viewHolder.itemView.service_comments_count_text_view.text = serviceCommentCount.toString()
    }
}