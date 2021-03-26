package xyz.ummo.user.ui.fragments.bottomSheets

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.adapters.ServiceRequestPagerAdapter
import xyz.ummo.user.api.RequestService
import xyz.ummo.user.api.User
import xyz.ummo.user.api.User.Companion.mode
import xyz.ummo.user.api.User.Companion.ummoUserPreferences
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.databinding.FragmentServiceRequestBottomSheetBinding
import xyz.ummo.user.models.ServiceCostModel
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.ui.MainScreen.Companion.SERVICE_OBJECT
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import java.io.Serializable

class ServiceRequestBottomSheet : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var serviceObjectParam: Serializable? = null
    private var serviceObject: ServiceObject? = null
    private var layouts: IntArray? = null
    var serviceCentresRadioGroup: RadioGroup? = null
    var serviceCentreRadioButton: RadioButton? = null
    private var serviceViewPagerAdapter: ServiceRequestPagerAdapter? = null
    private var serviceCentresList: ArrayList<String>? = null
    private var serviceLayoutInflater: LayoutInflater? = null
    private var serviceCostArrayList = ArrayList<ServiceCostModel>()
    private var serviceSpec = ""
    private var specCost = ""
    private var chosenServiceCentre = ""

    private var serviceCostAdapter: ArrayAdapter<ServiceCostModel>? = null

    private lateinit var viewBinding: FragmentServiceRequestBottomSheetBinding

    private lateinit var serviceRequestBottomSheetPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var mixpanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        serviceRequestBottomSheetPrefs = context!!.getSharedPreferences(ummoUserPreferences, mode)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_service_request_bottom_sheet, container, false)

        val view = viewBinding.root

        /** Unpacking [ServiceObject] from [getArguments]**/
        serviceObjectParam = arguments?.getSerializable(SERVICE_OBJECT)
        serviceObject = serviceObjectParam as ServiceObject

        serviceObjectParam = arguments?.getSerializable(SERVICE_OBJECT)
        Timber.e("SERVICE OBJECT PARAM -> $serviceObjectParam")
        serviceRequestStepOne()
        serviceRequestStepTwo()

        return view
    }

    private fun serviceRequestStepOne() {

        serviceLayoutInflater = LayoutInflater.from(context)
                .context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        /** Introducing Request Sheet to User**/
        val requestAgentText = String.format(resources.getString(R.string.request_ummo_agent), serviceObject!!.serviceName)
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
                    val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(android.R.attr.state_enabled)), intArrayOf(
                            Color.GRAY//disabled
                            , resources.getColor(R.color.ummo_1) //enabled
                    ))
                    serviceCentreRadioButton.buttonTintList = colorStateList
                }
                serviceCentresRadioGroup.addView(serviceCentreRadioButton)
                serviceCentresRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->

                    val checkedBox = radioGroup.findViewById<RadioButton>(checkedId)
                    chosenServiceCentre = checkedBox.text.toString()
                    Timber.e("CHECKED BOX -> $chosenServiceCentre")

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
        serviceCostAdapter = ArrayAdapter(context!!, R.layout.list_item, serviceCostArrayList)

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

            val serviceSpecCost = JSONObject()
            /*serviceSpecCost
                    .put("SERVICE_SPEC", serviceSpec)
                    .put("SPEC_COST", specCost)
            mixpanel.track("detailed_serviceSpecSelected", serviceSpecCost)*/

            viewBinding.confirmServiceRelativeLayout.visibility = View.VISIBLE
            confirmServiceRequest()
        }
    }

    private fun confirmServiceRequest() {
        val confirmServiceCostTextView = viewBinding.confirmServiceCostTextView
        val delegationCostTextView = viewBinding.confirmDelegationCostTextView
        val totalCostTextView = viewBinding.confirmTotalCostTextView
        val confirmPaymentCheckBox = viewBinding.confirmPaymentCheckBox
        val confirmRequestButton = viewBinding.confirmRequestButton
        val delegationFee = JSONObject()

        confirmServiceCostTextView.text = "E$specCost"
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

        Timber.e("SERVICE COST -> $specCost")
        Timber.e("SERVICE FORMATTED -> $formattedServiceCost")
        val serviceCostInt = Integer.parseInt(formattedServiceCost)
        val totalCostInt = serviceCostInt + 50
        totalCostTextView.text = "E$totalCostInt"

        delegationFee.put(MainScreen.CHOSEN_SERVICE_SPEC, serviceSpec)
                .put(MainScreen.TOTAL_DELEGATION_FEE, totalCostInt)

        confirmPaymentCheckBox.setOnClickListener {
            if (confirmPaymentCheckBox.isChecked) {
                confirmRequestButton.setBackgroundColor(context!!.resources.getColor(R.color.ummo_1))
                confirmRequestButton.isClickable = true
                confirmRequestButton.isActivated = true
                confirmRequestButton.isEnabled = true

                confirmRequestButton.setOnClickListener {
                    requestAgentDelegate(serviceObject!!.serviceId, delegationFee, chosenServiceCentre)
                }
                /*paymentTermsEvent.paymentTermsConfirmed = true
                EventBus.getDefault().post(paymentTermsEvent)*/
            } else {
                confirmRequestButton.setBackgroundColor(context!!.resources.getColor(R.color.greyProfile))
                confirmRequestButton.isClickable = false
                confirmRequestButton.isActivated = false
                confirmRequestButton.isEnabled = false
            }
        }
    }

    private fun requestAgentDelegate(mServiceId: String, mDelegationFee: JSONObject, mChosenServiceCentre: String) {
        val jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")

        Timber.e("SERVICE_ID REQUEST->%s", mServiceId)

        if (jwt != null) {
            object : RequestService(context, User.getUserId(jwt), mServiceId, mDelegationFee, mChosenServiceCentre) {
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

                            editor = serviceRequestBottomSheetPrefs.edit()
                            editor.putString("DELEGATION_ID", delegationId)
                            //TODO: remove after service is done
                            editor.putString(DetailedServiceActivity.DELEGATED_SERVICE_ID, serviceObject!!.serviceId)
                            editor.putString(DetailedServiceActivity.SERVICE_AGENT_ID, serviceAgent)
                            editor.putString(MainScreen.DELEGATION_FEE, mDelegationFee.getString(MainScreen.TOTAL_DELEGATION_FEE))
                            editor.putString(MainScreen.DELEGATION_SPEC, mDelegationFee.getString(MainScreen.CHOSEN_SERVICE_SPEC))
                            editor.apply()

                            launchDelegatedService(context,
                                    delegatedServiceId, serviceAgent, delegationId)

                        }
                        404 -> {
                            Timber.e("CODE IS $code")

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
        bundle.putString(DetailedServiceActivity.DELEGATED_SERVICE_ID, delegatedServiceId)
        bundle.putString(DetailedServiceActivity.SERVICE_AGENT_ID, agentId)
        bundle.putString(DetailedServiceActivity.DELEGATION_ID, delegationId)

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