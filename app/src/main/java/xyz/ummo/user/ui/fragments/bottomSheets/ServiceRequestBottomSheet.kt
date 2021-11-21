package xyz.ummo.user.ui.fragments.bottomSheets

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.RequestService
import xyz.ummo.user.api.User
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.databinding.FragmentServiceRequestBottomSheetBinding
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.ui.main.MainScreen.Companion.supportFM
import xyz.ummo.user.ui.main.MainViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.workers.SocketConnectWorker
import java.io.Serializable

class ServiceRequestBottomSheet : BottomSheetDialogFragment() {
    private var serviceDate: String? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var serviceObjectParam: Serializable? = null
    private var serviceObject: ServiceObject? = null
    private var layouts: IntArray? = null
    var serviceCentresRadioGroup: RadioGroup? = null
    var serviceCentreRadioButton: RadioButton? = null
    private var serviceCentresList: ArrayList<String>? = null
    private var serviceLayoutInflater: LayoutInflater? = null
    private var serviceCostArrayList = ArrayList<ServiceCostModel>()
    private var serviceSpec = ""
    private var specCost = ""
    private var chosenServiceCentre = ""
    private var currentSelectedDate: Long? = null

    private var serviceCostAdapter: ArrayAdapter<ServiceCostModel>? = null

    private lateinit var viewBinding: FragmentServiceRequestBottomSheetBinding

    private lateinit var serviceRequestBottomSheetPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var mixpanel: MixpanelAPI
    private lateinit var requestingAgentBuilder: MaterialAlertDialogBuilder
    private lateinit var requestingAssistance: AlertDialog
    private lateinit var takingYouToWhatsAppView: View
    private val serviceBeingRequested = JSONObject()

    /** Borrowing MainViewModel for service requests via ServiceHandler **/
    private var mainViewModel: MainViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
        }

        serviceRequestBottomSheetPrefs =
            requireContext().getSharedPreferences(ummoUserPreferences, mode)

        /** Initing [mainViewModel] to use in [requestAgentDelegate] below **/
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_service_request_bottom_sheet, container, false
        )

        val view = viewBinding.root

        mixpanel = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        /** Unpacking [ServiceObject] from [getArguments]**/
        serviceObjectParam = arguments?.getSerializable(SERVICE_OBJECT)
        serviceObject = serviceObjectParam as ServiceObject
        Timber.e("SERVICE OBJECT PARAM -> $serviceObjectParam")

        serviceRequestStepOne()
        serviceRequestStepTwo()

        return view
    }

    private fun serviceRequestStepOne() {

        serviceLayoutInflater = LayoutInflater.from(context)
            .context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        /** Introducing Request Sheet to User**/
        val requestAgentText = String.format(
            resources.getString(R.string.request_ummo_agent),
            serviceObject!!.serviceName
        )
        val requestAgentTextView = viewBinding.requestDescriptionTextView
        requestAgentTextView.text = requestAgentText

        /** Populating the Service Centre RadioGroup**/
        val serviceCentresList = ArrayList(serviceObject!!.serviceCentres)
        var serviceCentreRadioButton: RadioButton?
        val serviceCentresRadioGroup: RadioGroup = viewBinding.serviceCentreRadioGroupDialogFragment

        if (serviceCentresList.isNotEmpty()) {

            serviceCentresRadioGroup.removeAllViews()
            for (i in serviceCentresList.indices) {
                serviceCentreRadioButton = RadioButton(requireContext())
                serviceCentreRadioButton.id = i
                serviceCentreRadioButton.text = serviceCentresList[i].replace("\"\"", "")
                serviceCentreRadioButton.textSize = 14F

                /** Setting RadioButton color state-list **/
                if (Build.VERSION.SDK_INT >= 21) {
                    val colorStateList = ColorStateList(
                        arrayOf(
                            intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(android.R.attr.state_enabled)
                        ), intArrayOf(
                            Color.GRAY//disabled
                            , resources.getColor(R.color.ummo_1) //enabled
                        )
                    )
                    serviceCentreRadioButton.buttonTintList = colorStateList
                }
                serviceCentresRadioGroup.addView(serviceCentreRadioButton)
                serviceCentresRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->

                    val checkedBox = radioGroup.findViewById<RadioButton>(checkedId)
                    chosenServiceCentre = checkedBox.text.toString()
                    Timber.e("CHECKED BOX -> $chosenServiceCentre")

                    serviceBeingRequested.put("CHOSEN_CENTRE", chosenServiceCentre)
                    mixpanel.track("requestBottomSheet_pickingServiceCentre", serviceBeingRequested)

                    viewBinding.serviceCostRelativeLayout.visibility = View.VISIBLE
                }
            }

        } else {
            Timber.e("onCreate: docsList is EMPTY!")
        }
    }

    private fun serviceRequestStepTwo() {
        val autoCompleteTextView = viewBinding.sheetServiceCostTextView

        serviceCostArrayList = serviceObject!!.serviceCost
        serviceCostAdapter =
            ArrayAdapter(requireContext(), R.layout.list_item, serviceCostArrayList)

        autoCompleteTextView.setAdapter(serviceCostAdapter)

        autoCompleteTextView.setOnItemClickListener { adapterView, autoView, i, l ->
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

            Timber.e("SPEC-COST -> $specCost")
            Timber.e("SERVICE-SPEC -> $serviceSpec")

            serviceBeingRequested.put("CHOSEN_SPEC", specCost)
            mixpanel.track("requestBottomSheet_pickingCostSpec", serviceBeingRequested)

            viewBinding.serviceBookingRelativeLayout.visibility = View.VISIBLE
            serviceRequestStepThree()
        }
    }

    private fun serviceRequestStepThree() {
        val pickDateButton: MaterialButton = viewBinding.reserveDateButton

        /** For a better UX, we need the User to not accidentally select a date from the past **/
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.now())

        /** Creating a MaterialDateBuilder object **/
        val dateBuilder: MaterialDatePicker.Builder<*> = MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraintsBuilder.build())
            .setTitleText("Pick a Date for your Service")

        val datePicker = dateBuilder.build()

        pickDateButton.setOnClickListener {
            datePicker.show(this.childFragmentManager, datePicker.toString())
        }

        datePicker.addOnPositiveButtonClickListener {
            viewBinding.selectedDateTextView.visibility = View.VISIBLE
            viewBinding.selectedDateTextView.text = datePicker.headerText
            serviceDate = datePicker.headerText
            Timber.e("DATE PICKER -> ${datePicker.headerText}")
            viewBinding.reserveDateButton.text = "Change date..."
            viewBinding.reserveDateButton.setBackgroundColor(resources.getColor(R.color.ummo_3))

            serviceBeingRequested.put("CHOSEN_DATE", datePicker.headerText)

            confirmServiceRequest()

            mixpanel.track("requestBottomSheet_pickingServiceDate", serviceBeingRequested)
        }
    }

    /*private fun showDatePicker() {
        *//*val selectedDateInMillis = currentSelectedDate ?: System.currentTimeMillis()

        MaterialDatePicker.Builder.datePicker().setSelection(selectedDateInMillis).build().apply {
            addOnPositiveButtonClickListener { dateInMillis -> onDateSelected(dateInMillis) }
        }.show(this.childFragmentManager, MaterialDatePicker::class.java.canonicalName)*//*

    }

    private fun onDateSelected(dateTimeStampInMillis: Long) {
        currentSelectedDate = dateTimeStampInMillis
        val dateTime: LocalDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(currentSelectedDate!!), ZoneId.systemDefault())
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val dateAsFormattedText: String = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        viewBinding.selectedDateTextView.visibility = View.VISIBLE
        viewBinding.selectedDateTextView.text = dateAsFormattedText
        viewBinding.reserveDateButton.text = "Choose another date?"

        confirmServiceRequest()
    }*/

    private fun confirmServiceRequest() {
        viewBinding.confirmServiceRelativeLayout.visibility = View.VISIBLE
        viewBinding.confirmPaymentCheckBox.visibility = View.VISIBLE

        val confirmServiceCostTextView = viewBinding.confirmServiceCostTextView
        val delegationCostTextView = viewBinding.confirmDelegationCostTextView
        val totalCostTextView = viewBinding.confirmTotalCostTextView
        val confirmPaymentCheckBox = viewBinding.confirmPaymentCheckBox
        val confirmRequestButton = viewBinding.confirmRequestButton
        val delegationFee = JSONObject()

        confirmServiceCostTextView.text = "E$specCost"
        /** 1) Removing the currency from the fee
         *  2) Converting fee string to int
         *  3) Adding [Delegation Fee] to get Total Cost (int)
         *  4) Displaying Total Cost **/

        val serviceCost: String = specCost

        val formattedServiceCost: String = if (serviceCost.contains(",")) {
            serviceCost.replace(",", "")
        } else {
            serviceCost
        }

        val totalCostInt: Int

        Timber.e("SERVICE COST -> $specCost")
        Timber.e("SERVICE FORMATTED -> $formattedServiceCost")
        val serviceCostInt = Integer.parseInt(formattedServiceCost)

        totalCostInt = when {
            chosenServiceCentre.contains("Manzini", true) -> {
                delegationCostTextView.text = 119.toString()
                serviceCostInt + 119
            }
            chosenServiceCentre.contains("Mahhala", true) -> {
                delegationCostTextView.text = 99.toString()
                serviceCostInt + 99
            }
            chosenServiceCentre.contains("Mbabane", true) -> {
                delegationCostTextView.text = 149.toString()
                serviceCostInt + 149
            }
            else -> {
                delegationCostTextView.text = 99.toString()
                serviceCostInt + 99
            }
        }
        totalCostTextView.text = "E$totalCostInt"

        delegationFee.put(CHOSEN_SERVICE_SPEC, serviceSpec)
            .put(TOTAL_DELEGATION_FEE, totalCostInt)

        confirmPaymentCheckBox.setOnClickListener {
            if (confirmPaymentCheckBox.isChecked) {
                confirmRequestButton.setBackgroundColor(requireContext().resources.getColor(R.color.ummo_1))
                confirmRequestButton.isClickable = true
                confirmRequestButton.isActivated = true
                confirmRequestButton.isEnabled = true

                mixpanel.track("requestBottomSheet_confirmingPaymentTerms", serviceBeingRequested)

                confirmRequestButton.setOnClickListener {
                    requestAgentDelegate(
                        serviceObject!!.serviceId,
                        delegationFee,
                        chosenServiceCentre
                    )
                }
                /*paymentTermsEvent.paymentTermsConfirmed = true
                EventBus.getDefault().post(paymentTermsEvent)*/
            } else {
                confirmRequestButton.setBackgroundColor(requireContext().resources.getColor(R.color.greyProfile))
                confirmRequestButton.isClickable = false
                confirmRequestButton.isActivated = false
                confirmRequestButton.isEnabled = false
            }
        }
    }

    private fun requestAgentDelegate(
        mServiceId: String,
        mDelegationFee: JSONObject,
        mChosenServiceCentre: String
    ) {
        val jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")
        val serviceRequestObject = JSONObject()

        viewBinding.confirmRequestButton.setBackgroundColor(resources.getColor(R.color.ummo_3))
        viewBinding.confirmRequestButton.text = "Requesting..."
        Timber.e("SERVICE_ID REQUEST->%s", mServiceId)

        if (jwt != null) {

            /*try {
                serviceRequestObject.put("user_id", User.getUserId(jwt))
                    .put("product_name", serviceObject!!.serviceName)
                    .put("product_id", mServiceId)
                    .put("delegation_fee", mDelegationFee)
                    .put("chosen_service_centre", mChosenServiceCentre)
                    .put("service_date", serviceDate)
                Timber.e("SUCCESSFULLY SCHEDULING SERVICE VIA SOCKET")

                */
            /** [scheduleServiceSocketEvent] takes the $serviceRequestObject && emits this event
             * via [SocketConnectWorker] **//*
                scheduleServiceSocketEvent(serviceRequestObject)

                */
            /** [launchWhatsAppSheet] takes $serviceRequestObject && processes this JSONObject
             * to create a WhatsApp-ready text to complete the service request **//*
//                launchWhatsAppSheet(serviceRequestObject, serviceObjectParam!!)
            } catch (jse: JSONException) {
                Timber.e("FAILED TO REQUEST SERVICE -> $jse")
            }*/

            object : RequestService(
                context,
                User.getUserId(jwt),
                mServiceId,
                mDelegationFee,
                mChosenServiceCentre,
                serviceDate
            ) {
                override fun done(data: ByteArray, code: Int) {
                    Timber.e("delegatedService: Done->%s", String(data))
                    Timber.e("delegatedService: Status Code->%s", code)

                    when (code) {
                        200 -> {
//                            alertDialogBuilder.dismiss()

                            val delegation = JSONObject(String(data)).getJSONObject("payload")
                            Timber.e("SERVICE OBJ -> $delegation")
                            val delegatedServiceId = delegation.getString("product")
                            val delegationId = delegation.getString("_id")
                            val serviceAgent = delegation.getString("agent")

                            editor = serviceRequestBottomSheetPrefs.edit()
                            editor.putString("DELEGATION_ID", delegationId)
                            //TODO: remove after service is done
                            editor.putString(
                                DELEGATED_SERVICE_ID,
                                serviceObject!!.serviceId
                            )
                            editor.putString(SERVICE_AGENT_ID, serviceAgent)
                            editor.putString(
                                DELEGATION_FEE,
                                mDelegationFee.getString(TOTAL_DELEGATION_FEE)
                            )
                            editor.putString(
                                DELEGATION_SPEC,
                                mDelegationFee.getString(CHOSEN_SERVICE_SPEC)
                            )
                            editor.putString(SERVICE_DATE, serviceDate)
                            editor.apply()

                            /** Showing the User a loader before directing them to the Delegated Screen **/
                            val timer = object : CountDownTimer(2000, 1000) {

                                override fun onTick(p0: Long) {
                                    showAlertDialog()
                                }

                                override fun onFinish() {
                                    launchDelegatedService(
                                        context,
                                        delegatedServiceId, serviceAgent, delegationId
                                    )

                                    return
                                }
                            }
                            timer.start()

                            /** Saving Service Delegation in Room **/
                            val delegatedServiceEntity = DelegatedServiceEntity()
                            val delegatedServiceViewModel =
                                ViewModelProvider((context as FragmentActivity?)!!)
                                    .get(DelegatedServiceViewModel::class.java)

                            val progress = java.util.ArrayList<String>()

                            /** Setting Service as Delegated **/
                            delegatedServiceEntity.delegationId = delegationId
                            delegatedServiceEntity.delegatedProductId = delegatedServiceId
                            delegatedServiceEntity.serviceAgentId = serviceAgent
                            delegatedServiceEntity.serviceProgress = progress
                            delegatedServiceEntity.serviceDate = serviceDate
                            delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

                            /** Requesting service via ServiceHandler *
                            //                            mainViewModel?.serviceHandler()
                             * We'll launch the BottomSheet that explains to the User what they
                             * should expect from this point onwards... */

                            mixpanel.track(
                                "requestBottomSheet_completingRequest",
                                serviceBeingRequested
                            )

                        }
                        404 -> {
                            Timber.e("CODE IS $code")

                        }
                    }
                }
            }
        }
    }

    private fun scheduleServiceSocketEvent(serviceJSON: JSONObject) {
        val socket = SocketConnectWorker.SocketIO.mSocket
        socket?.emit("service/schedule", serviceJSON)
    }

    private fun showAlertDialog() {
        requestingAgentBuilder = MaterialAlertDialogBuilder(requireContext())
        takingYouToWhatsAppView = LayoutInflater.from(requireContext())
            .inflate(R.layout.taking_you_to_whatsapp, null, false)

        requestingAgentBuilder.setTitle("Just a second")
        requestingAgentBuilder.setView(takingYouToWhatsAppView)
        requestingAgentBuilder.setIcon(R.drawable.logo)
        requestingAssistance = requestingAgentBuilder.show()
    }

    private fun launchWhatsAppSheet(requestedService: JSONObject, serviceObject: Serializable) {

//        mainViewModel!!.socketConnect()

        val requestServiceAssistance = RequestServiceAssistance()

        /** 1. Creating a $requestBundle and
         *  2. Passing the Requested Service to the WhatsApp BottomSheet **/
        val requestBundle = Bundle()
        requestBundle.putString(REQUESTED_SERVICE, requestedService.toString())
        requestBundle.putSerializable(SERVICE_OBJECT, serviceObject)
        requestServiceAssistance.arguments = requestBundle

        Timber.e("REQUESTED SERVICE $requestedService")
        requestServiceAssistance.show(
            /** [supportFM] is borrowed from [MainScreen]'s companion object **/
            supportFM, RequestServiceAssistance.TAG
        )
    }

    fun launchDelegatedService(
        context: Context?,
        delegatedServiceId: String,
        agentId: String,
        delegationId: String
    ) {

        val bundle = Bundle()
        bundle.putString(DELEGATED_SERVICE_ID, delegatedServiceId)
        bundle.putString(SERVICE_AGENT_ID, agentId)
        bundle.putString(DELEGATION_ID, delegationId)
        bundle.putString(TAKE_ME_TO, DELEGATED_SERVICE_FRAGMENT)

        Timber.e("DELEGATION_ID -> $delegationId")
        Timber.e("DELEGATED_SERVICE_ID -> $delegatedServiceId")
        Timber.e("SERVICE_AGENT_ID -> $agentId")
        Timber.e("SERVICE_DATE -> $serviceDate")

        val progress = java.util.ArrayList<String>()
        val delegatedServiceEntity = DelegatedServiceEntity()
        val delegatedServiceViewModel = ViewModelProvider((context as FragmentActivity?)!!)
            .get(DelegatedServiceViewModel::class.java)

        /** Setting Service as Delegated **/
        delegatedServiceEntity.delegationId = delegationId
        delegatedServiceEntity.delegatedProductId = delegatedServiceId
        delegatedServiceEntity.serviceAgentId = agentId
        delegatedServiceEntity.serviceProgress = progress
        delegatedServiceEntity.serviceDate = serviceDate
        delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

        val intent = Intent(activity, MainScreen::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    companion object {

        const val TAG = "ServiceRequestBottomSheet"

        @JvmStatic
        fun newInstance(serviceObject: ServiceObject) =
            ServiceRequestBottomSheet().apply {
                arguments = Bundle().apply {

                }
            }
    }
}