package xyz.ummo.user.ui.fragments.delegatedService

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.kofigyan.stateprogressbar.StateProgressBar
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.DelegationFeedback
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.AppBarDelegatedScreenBinding
import xyz.ummo.user.databinding.ConfirmServiceDeliveredViewBinding
import xyz.ummo.user.databinding.FragmentDelegatedBinding
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel
import xyz.ummo.user.ui.fragments.bottomSheets.DelegationFeeQuery
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceQueryBottomSheetFragment
import xyz.ummo.user.ui.fragments.bottomSheets.ShareServiceProgressBottomSheet
import xyz.ummo.user.ui.fragments.pagesFrags.PagesFragment
import xyz.ummo.user.ui.main.MainScreen.Companion.supportFM
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.eventBusEvents.RatingSentEvent
import xyz.ummo.user.utilities.eventBusEvents.ServiceUpdateEvents

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DelegatedServiceFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DelegatedServiceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

class DelegatedServiceFragment : Fragment {
    // TODO: Rename and change types of parameters
    private val mParam1: String? = null
    private val mParam2: String? = null
    private var agentName: String? = null
    private var productName: String? = null
    private var delegationId: String? = null
    private var serviceAgentId: String? = null

    //    private var delegatedProductId: String? = null
    private var delegatedServiceId: String? = null
    private var serviceState: Int = 0
    private var serviceDate: String? = null
    private var rescheduledServiceDate: String? = null
    private var agentNameTextView: TextView? = null
    private var agentStatusTextView: TextView? = null
    private var delegatedProductNameTextView: TextView? = null
    private var delegationScheduleTextView: TextView? = null
    private var delegatedProductDescriptionTextView: TextView? = null
    private var delegatedProductCostTextView: TextView? = null

    private val delegatedServiceDocsTextView: TextView? = null
    private var delegatedServiceStepsTextView: TextView? = null
    private var progressBar: ProgressBar? = null
    private val stepsTV = ArrayList<TextView>()
    var stepsList: ArrayList<String>? = null
    var docsList: ArrayList<String>? = null
    private val delegatedProductDocsLayout: LinearLayout? = null
    private var delegatedProductStepsLayout: LinearLayout? = null
    private var openChat: ImageView? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var delegatedServiceViewModel: DelegatedServiceViewModel? = null
    private var detailedProductViewModel: DetailedProductViewModel? = null
    private var serviceViewModel: ServiceViewModel? = null
    private var delegatedServiceEntity = DelegatedServiceEntity()
    private var confirmReceiptDialog: AlertDialog? = null
    private var hasConfirmed = false
    private var toolbar: MaterialToolbar? = null
    private var countOfDelegatedServices: Int = 0
    private lateinit var stateProgressBar: StateProgressBar

    private lateinit var viewBinding: FragmentDelegatedBinding
    private lateinit var appBarBinding: AppBarDelegatedScreenBinding
    private lateinit var rateDelegationBinding: ConfirmServiceDeliveredViewBinding
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var delegatedServicePreferences: SharedPreferences

    private lateinit var mixpanel: MixpanelAPI

    private var ratingSentEvent = RatingSentEvent()

    private var bundle = Bundle()

    private var delegationFee = ""
    private var delegationSpec = ""
    private var serviceName = ""

    constructor(entity: DelegatedServiceEntity) {
        delegatedServiceEntity = entity
    }

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mixpanel = MixpanelAPI.getInstance(context,
                resources.getString(R.string.mixpanelToken))

        confirmReceiptDialog = AlertDialog.Builder(context).create()
        appBarBinding = AppBarDelegatedScreenBinding.inflate(layoutInflater)
        rateDelegationBinding = ConfirmServiceDeliveredViewBinding.inflate(layoutInflater)

        /** Initializing [DelegatedServiceViewModel] && [countOfDelegatedServices] **/
        delegatedServiceViewModel = ViewModelProvider(this)
                .get(DelegatedServiceViewModel::class.java)

        countOfDelegatedServices = delegatedServiceViewModel!!.getCountOfDelegatedServices()
        if (countOfDelegatedServices != 0) {
            initDelegatedServiceFrag()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        /** Initializing [viewBinding] && attaching it to [R.layout.fragment_delegated] layout **/
        viewBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_delegated, container, false)

        rateDelegationBinding = DataBindingUtil
                .inflate(inflater, R.layout.confirm_service_delivered_view, container, false)

        val view = viewBinding.root

        delegatedServicePreferences = (activity)?.getSharedPreferences(ummoUserPreferences, mode)!!

        delegationId = delegatedServicePreferences.getString(DELEGATION_ID, "")

        delegatedServiceId = delegatedServicePreferences.getString(DELEGATED_SERVICE_ID, "")
        serviceState = delegatedServicePreferences.getInt(SERVICE_STATE, 0)
        delegationFee = delegatedServicePreferences.getString(DELEGATION_FEE, "Couldn't load fee...")!!
        delegationSpec = delegatedServicePreferences.getString(DELEGATION_SPEC, "Couldn't load service spec...")!!
        serviceDate = delegatedServicePreferences.getString(SERVICE_DATE, "Couldn't load Service Date...")
        serviceViewModel = ViewModelProvider(this).get(ServiceViewModel::class.java)

        /** The commented block below will be used in a later version of the app **/
        /*agentNameTextView = viewBinding.delegatedAgentNameTextView
        agentStatusTextView = viewBinding.delegatedAgentStatusTextView
        openChat = viewBinding.openChatButton
        progressBar = viewBinding.serviceProgressBar*/

        /** Initializing text-view elements **/
        delegatedProductNameTextView = viewBinding.delegatedServiceHeaderName
        delegationScheduleTextView = viewBinding.serviceScheduleTextView
        delegationScheduleTextView!!.text = serviceDate
//        delegatedProductDescriptionTextView = viewBinding.descriptionTextView
        delegatedProductCostTextView = viewBinding.serviceCostTextView
//        delegatedProductStepsLayout = view/**/Binding.delegatedServiceStepsLayout
        stateProgressBar = viewBinding.delegationStateProgressBar

        /** Defining the states to be shown on the [StateProgressBar].
         *  Then setting the animation duration of the state transitions. **/
        val delegationStates = arrayOf("PENDING", "IN-PROGRESS", "DONE", "DELIVERED")
        stateProgressBar.setStateDescriptionData(delegationStates)
        stateProgressBar.animationDuration = 1000

        agentNameTextView?.text = agentName

        bundle.putString(SPEC_FEE, delegationFee)
        bundle.putString(SERVICE_SPEC, delegationSpec)

        viewBinding.delegationFeeQueryImageView.setOnClickListener { queryDelegationFee() }
        viewBinding.delegationFeeQueryIconRelativeLayout.setOnClickListener { queryDelegationFee() }
        viewBinding.rescheduleTextView.setOnClickListener { rescheduleService() }

        /** Handling WhatsApp Service Support **/
        viewBinding.whatsappSupportCard.whatsappSupportCard.setOnClickListener { triggerWhatsAppServiceSupport() }
        viewBinding.whatsappSupportCard.whatsappSupportTextView.setOnClickListener { triggerWhatsAppServiceSupport() }
        viewBinding.whatsappSupportCard.whatsappImageView.setOnClickListener { triggerWhatsAppServiceSupport() }
        viewBinding.whatsappSupportCard.whatsappSupportTextView.setOnClickListener { triggerWhatsAppServiceSupport() }
        viewBinding.whatsappSupportCard.whatsappSupportTitleTextView.setOnClickListener { triggerWhatsAppServiceSupport() }

//        viewBinding.shareDelegationCard.shareDelegationTextView.setOnClickListener {shareServiceProgress() }
//        viewBinding.shareDelegationCard.shareDelegationCard.setOnClickListener { shareServiceProgress() }
//        initDelegatedServiceFrag()

//        updateServiceState()

//        goToDelegationChat()

        checkingForDelegatedServices()

        return view
    }

    private fun triggerWhatsAppServiceSupport() {
        val contact = "+26876804065"

        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm: PackageManager = requireActivity().packageManager
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            mixpanel.track("supportCentre_whatsAppChatInitiated")
        } catch (e: PackageManager.NameNotFoundException) {
            showSnackbarYellow("WhatsApp not installed.", -1)
            e.printStackTrace()
        }
    }

    private fun shareServiceProgress() {
        val shareBundle = Bundle()
        shareBundle.putString(SERVICE_NAME, serviceName)
        val shareServiceProgressBottomSheet = ShareServiceProgressBottomSheet()
        shareServiceProgressBottomSheet.arguments = shareBundle
        shareServiceProgressBottomSheet.show(supportFM, ShareServiceProgressBottomSheet.TAG)
    }

    private fun rescheduleService() {

        Timber.e("CURRENT SERVICE-DATE -> $serviceDate")
        /** For a better UX, we need the User to not accidentally select a date from the past **/
        val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
        /** Creating a MaterialDateBuilder object **/
        val dateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
                .setCalendarConstraints(constraintsBuilder.build())
                .setTitleText("Pick a Date for your Service")

        val datePicker = dateBuilder.build()
        datePicker.show(this.childFragmentManager, datePicker.tag)

        /** Removing old Service Date from [delegatedServicePreferences] **/
        editor = delegatedServicePreferences.edit()
        editor.remove(SERVICE_DATE)

        datePicker.addOnPositiveButtonClickListener {
            viewBinding.serviceScheduleTextView.text = datePicker.headerText
            rescheduledServiceDate = datePicker.headerText
            delegatedServiceEntity.serviceDate = rescheduledServiceDate
            delegatedServiceViewModel!!.updateDelegatedService(delegatedServiceEntity)
            Timber.e("RESCHEDULED SERVICE DATE -> $rescheduledServiceDate")
            /** Updating the [delegatedServicePreferences] value for Service Date **/
            editor.putString(SERVICE_DATE, rescheduledServiceDate).apply()
        }

    }

    override fun onStart() {
        super.onStart()
        updateServiceState()
    }

    private fun queryDelegationFee() {
//        bundle.putString(MainScreen.SERVICE_ID, serviceId)
        val delegationFeeQuery = DelegationFeeQuery()
        delegationFeeQuery.arguments = bundle
        delegationFeeQuery.show(requireFragmentManager(), ServiceQueryBottomSheetFragment.TAG)
        mixpanel.track("delegatedServiceFrag_delegationFeeSelfSupport")
    }

    private fun initDelegatedServiceFrag() {
        delegatedServicePreferences = (activity)?.getSharedPreferences(ummoUserPreferences, mode)!!

        editor = delegatedServicePreferences.edit()

        val initDelegateAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val completingAlertDialogView = LayoutInflater.from(context)
            .inflate(R.layout.completing_request_dialog, null)

        initDelegateAlertDialogBuilder.setTitle("Making Request...")
            .setIcon(R.drawable.logo)
            .setView(completingAlertDialogView)

        val alertDialog = initDelegateAlertDialogBuilder.create()
//        val requestTextView = requireView().findViewById<TextView>(R.id.request_text_view)

        if (!delegatedServicePreferences.getBoolean(DELEGATE_INITIALIZED, false)) {
            alertDialog.show()

            val timer = object : CountDownTimer(5000, 1000) {
                override fun onTick(p0: Long) {
                    Timber.e("TIMER IS -> $p0")
//                    requestTextView.text = "Finishing up"
                }

                override fun onFinish() {
                    alertDialog.dismiss()
                    if (isAdded) {

                        /** Once the timer is done counting down, we want the User to receive a
                         * visual confirmation of their service request.
                         * We're using a Snackbar to achieve this. **/
                        val bottomNavigationView =
                            requireActivity().findViewById<View>(R.id.bottom_nav)
                        val snackbar = Snackbar.make(
                            requireActivity().findViewById(android.R.id.content),
                            "Your request was successful! We'll reach you shortly.",
                            Snackbar.LENGTH_LONG
                        )
                        snackbar.setTextColor(context!!.resources.getColor(R.color.ummo_4))
                        val textView = snackbar.view
                            .findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                        textView.textSize = 14F
                        snackbar.anchorView = bottomNavigationView
                        snackbar.show()

                        /** After everything is done, we then store this state in the SharedPref **/
                        editor.putBoolean(DELEGATE_INITIALIZED, true).apply()

                    }
                }
            }
            timer.start()
        }

    }

    private fun showSnackbarYellow(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(requireActivity().findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.gold))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun showWhatsAppOption() {
        /** For this part, we want the User to know that we'll be doing most of the Service
         * Engagement via WhatsApp.
         * We then want them to know how to reach us. We'll achieve this by displaying the
         * "Talk to us" option. **/


    }

    private fun checkingForDelegatedServices() {
        editor = delegatedServicePreferences.edit()

        when {
            countOfDelegatedServices == 0 -> {
                viewBinding.delegationLayout.visibility = View.GONE
                viewBinding.noDelegationLayout.visibility = View.VISIBLE
                viewBinding.whatsappSupportCard.whatsappSupportCard.visibility = View.VISIBLE
                takeMeHome()
                editor.remove(DELEGATE_INITIALIZED).apply()

            }
            /** Displaying Delegated Service from Room (local) **/
            countOfDelegatedServices != 0 -> {
                viewBinding.delegationLayout.visibility = View.VISIBLE
                viewBinding.noDelegationLayout.visibility = View.GONE
                viewBinding.serviceIntroCard.delegationIntroCardView.visibility = View.GONE
                Timber.e("DELEGATED---SERVICE---ID-> $delegatedServiceId")
                inflateDelegatedServiceView(delegatedServiceId)
            }
        }
    }

    /** With this function, we're displaying the service-delivery confirmation.
     * Within it, we're passing the $rating && $review values to the [rateDelegation] function.
     * Then we transmit (via [EventBus]), the [ratingSentEvent] to MainScreen for displaying snackbar.
     * Finally, we then [clearDelegationCache] to remove the [DelegatedServiceEntity]. **/
    private fun confirmServiceDelivery() {
        val confirmServiceDeliveryDialogBuilder = MaterialAlertDialogBuilder(requireContext())
        val confirmServiceDeliveryView = LayoutInflater
                .from(context).inflate(R.layout.confirm_service_delivered_view, null)

        confirmServiceDeliveryDialogBuilder.setTitle("Confirm Service Delivery")
                .setIcon(R.drawable.logo).setView(confirmServiceDeliveryView)
                .setPositiveButton("SEND") { dialogInterface, i ->

                    val delegationFeedbackText = confirmServiceDeliveryView
                            .findViewById<TextInputEditText>(R.id.delegation_feedback_text_field)
                    val delegationRatingBar = confirmServiceDeliveryView.findViewById<RatingBar>(R.id.delegation_rating_bar)
                    Timber.e("RATING VALUE -> ${delegationRatingBar.rating}")
                    Timber.e("CONFIRMING DELIVERY -> ${delegationFeedbackText.text?.trim()}")

                    Timber.e("DELEGATION ID CONFIRMATION -> $delegationId")
                    rateDelegation(delegationId!!,
                            delegationRatingBar.rating.toInt(),
                            delegationFeedbackText.text?.trim().toString())

                    ratingSentEvent.ratingSent = true
                    EventBus.getDefault().post(ratingSentEvent)

                    clearDelegationCache()
                }
                .setNegativeButton("LATER") { dialogInterface, i ->
                    Timber.e("REJECTING DELIVERY")
                }

        confirmServiceDeliveryDialogBuilder.show()
    }

    /** [rateDelegation] captures the [delegationId], the delegation rating & reviews;
     * Sends them to the back-end via [DelegationFeedback]
     * TODO: needs to be moved to AppRepo **/
    private fun rateDelegation(mDelegationId: String, mRating: Int, mReview: String) {
        object : DelegationFeedback(mDelegationId, mRating, mReview) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200 || code == 201) {
                    Timber.e("DELEGATION RATED -> ${String(data)}!")
                } else {
                    Timber.e("ERROR RATING DELEGATION -> $code")
                    Timber.e("ERROR RATING DELEGATION -> ${String(data)}")
                }
            }
        }
    }

    /** In order to update the [serviceState], we check the sharedPreferences for the state and
     * make the necessary UI changes to reflect the current state (via [stateProgressBar].**/
    private fun updateServiceState() {
        val sharedPreferences = (activity)?.getSharedPreferences(ummoUserPreferences, mode)

        when (sharedPreferences?.getInt(SERVICE_STATE, 0)) {
            -1 -> {
//                viewBinding.delegationStateProgressBar.backgroundColor = context!!.resources.getColor(R.color.Grey)
//                viewBinding.delegationStateProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.TWO)
                viewBinding.delegationStateProgressBar.isEnabled = false
                viewBinding.delegationStateProgressBar.isActivated = false
                viewBinding.delegationStateProgressBar.visibility = View.GONE
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.VISIBLE
                Timber.e("updatedServiceState DELAYED")
            }
            0 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)
                Timber.e("updatedServiceState PENDING")

            }
            1 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
                Timber.e("updatedServiceState STARTED")

            }
            2 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                Timber.e("updatedServiceState DONE")
            }
            3 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR)
                Timber.e("updatedServiceState DELIVERED")

                confirmServiceDelivery()

            }
        }
    }

    @Subscribe
    fun onServiceStateChange(serviceUpdateEvents: ServiceUpdateEvents) {

        val sharedPreferences = requireContext().getSharedPreferences(ummoUserPreferences, mode)
        val editor = sharedPreferences!!.edit()

        when (serviceUpdateEvents.serviceObject.getString("status")) {
            "PENDING" -> {
                editor.putInt(SERVICE_STATE, 0).apply()
                Timber.e("onServiceState PENDING")
            }
            "STARTED" -> {
                editor.putInt(SERVICE_STATE, 1).apply()
                Timber.e("onServiceState STARTED")

            }
            "DELAYED" -> {
                editor.putInt(SERVICE_STATE, -1).apply()
                Timber.e("onServiceState DELAYED")

            }
            "DONE" -> {
                editor.putInt(SERVICE_STATE, 2).apply()
                Timber.e("onServiceState DONE")

            }
            "DELIVERED" -> {
                editor.putInt(SERVICE_STATE, 3).apply()
                Timber.e("onServiceState DELIVERED")

            }
        }
    }

    /** Once the User has rated & reviewed a service, we clear it off the phone's memory by removing
     * the value from RoomDB **/
    private fun clearDelegationCache() {

        delegatedServiceViewModel!!.deleteAllDelegatedServices()

        viewBinding.noDelegationLayout.visibility = View.VISIBLE
        viewBinding.delegationLayout.visibility = View.GONE

    }

    private fun inflateDelegatedServiceView(delegatedServiceId: String?) {
        delegatedServicePreferences = (activity)?.getSharedPreferences(ummoUserPreferences, mode)!!

        //TODO: cater for null-safety on non-null live-data
        if (serviceViewModel != null) {
            serviceViewModel!!.getServiceEntityLiveDataById(delegatedServiceId!!)
                    .observe(viewLifecycleOwner, { serviceEntity: ServiceEntity ->
                        serviceName = serviceEntity.serviceName!!
                        delegatedProductNameTextView!!.text = serviceEntity.serviceName
//                            delegatedProductDescriptionTextView!!.text = serviceEntity.serviceDescription

                        delegatedProductCostTextView!!.text = "E$delegationFee"
                    })
        } else {
            Timber.e("NO SERVICE VIEW MODEL!")
            return
        }
    }

    private fun takeMeHome() {
        viewBinding.homeButton.setOnClickListener {
            val pagesFragment: Fragment = PagesFragment()

            mixpanel.track("delegatedServiceFragment_takingUserHome")

            val fm: FragmentManager = activity?.supportFragmentManager!!
            val transaction: FragmentTransaction = fm.beginTransaction()
            transaction.replace(R.id.frame, pagesFragment)
            transaction.commit()
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        const val DELEGATE_INITIALIZED = "DELEGATE_INITIALIZED"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DelegatedServiceFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): DelegatedServiceFragment {
            val fragment = DelegatedServiceFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            Timber.e("newInstance: SetArgument%s", args)
            fragment.arguments = args
            return fragment
        }
    }

}