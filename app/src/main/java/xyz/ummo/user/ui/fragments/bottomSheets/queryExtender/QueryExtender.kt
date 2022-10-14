package xyz.ummo.user.ui.fragments.bottomSheets.queryExtender

import android.app.Activity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentQueryExtenderBinding
import xyz.ummo.user.ui.fragments.search.AllServicesFragment
import xyz.ummo.user.utilities.*

class QueryExtender : BottomSheetDialogFragment() {

    private lateinit var viewBinding: FragmentQueryExtenderBinding
    private lateinit var serviceUrgencyRadioGroup: RadioGroup
    private lateinit var serviceCommunicationRadioGroup: RadioGroup
    private lateinit var mixpanel: MixpanelAPI
    private var service: String? = null
    private var serviceUrgency: String? = null
    private var communicationPreferences: String? = null
    private var serviceQueryJSONObject = JSONObject()
    private var whatsappToBeReached: String? = null
    private var contactToBeReached: String? = null
    private var emailToBeReached: String? = null

    companion object {
        const val TAG = "QueryExtender"
        fun newInstance() = QueryExtender()
    }

    private lateinit var viewModel: QueryExtenderViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_query_extender, container, false)

        serviceUrgencyRadioGroup = viewBinding.serviceUrgencyRadioGroup
        serviceCommunicationRadioGroup = viewBinding.communicationOptionsRadioGroup

        mixpanel = MixpanelAPI.getInstance(
            requireContext(),
            resources.getString(R.string.mixpanelToken)
        )

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        processQueryName()
    }

    private fun processQueryName() {
        viewBinding.serviceQueryNameEditText.setImeActionLabel(
            "Continue",
            KeyEvent.KEYCODE_ENTER
        )

        viewBinding.serviceQueryNameTextInput.setEndIconOnClickListener {
            service = viewBinding.serviceQueryNameEditText.text.toString()
            Timber.i("Service Name -> $service")

            viewBinding.serviceUrgencyRelativeLayout.visibility = View.VISIBLE

            /** Hiding Soft Input Keyboard Window below **/
            val inputMethodManager: InputMethodManager =
                requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)

            processQueryUrgency()
        }
    }

    private fun processQueryUrgency() {

        serviceUrgencyRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.immediately_radio_button -> {
                    serviceUrgency = "IMMEDIATELY"
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)

                }
                R.id.soon_radio_button -> {
                    serviceUrgency = "SOON"
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)

                }
                R.id.curious_radio_button -> {
                    serviceUrgency = "CURIOUS"
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)

                }
            }
            viewBinding.communicationOptionsRelativeLayout.visibility = View.VISIBLE
            processQueryCommunication()
        }
    }

    private fun processQueryCommunication() {
        serviceCommunicationRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.whatsapp_text -> {
                    communicationPreferences = WHATSAPP
                    confirmCommunicationDetails(WHATSAPP)
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)
                }
                R.id.email_text -> {
                    communicationPreferences = EMAIL
                    confirmCommunicationDetails(EMAIL)
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)
                }
                R.id.call_text -> {
                    communicationPreferences = CALL
                    confirmCommunicationDetails(CALL)
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    private fun confirmCommunicationDetails(communicationPreference: String) {
        val sharedPreferences = requireActivity().getSharedPreferences(ummoUserPreferences, mode)
        val contact = sharedPreferences.getString(USER_CONTACT, "")
        val email = sharedPreferences.getString(USER_EMAIL, "")
        when (communicationPreference) {
            "WHATSAPP" -> {

                viewBinding.whatsappContactConfirmationEditText.setText(contact)

                viewBinding.whatsappContactConfirmationTextInput.setEndIconOnClickListener {
                    submitQueryExtension(viewBinding.whatsappContactConfirmationEditText.text.toString())
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)
                }

                viewBinding.whatsappContactConfirmationRelativeLayout.visibility = View.VISIBLE
                viewBinding.mobileContactConfirmationRelativeLayout.visibility = View.GONE
                viewBinding.emailAddressConfirmationRelativeLayout.visibility = View.GONE
            }
            "EMAIL" -> {

                viewBinding.emailAddressConfirmationEditText.setText(email)

                viewBinding.emailAddressConfirmationTextInput.setEndIconOnClickListener {
                    submitQueryExtension(viewBinding.emailAddressConfirmationEditText.text.toString())
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)
                }

                viewBinding.emailAddressConfirmationRelativeLayout.visibility = View.VISIBLE
                viewBinding.whatsappContactConfirmationRelativeLayout.visibility = View.GONE
                viewBinding.mobileContactConfirmationRelativeLayout.visibility = View.GONE
            }
            "CALL" -> {

                viewBinding.mobileContactConfirmationEditText.setText(contact)

                viewBinding.mobileContactConfirmationTextInput.setEndIconOnClickListener {
                    submitQueryExtension(viewBinding.mobileContactConfirmationEditText.text.toString())
                    viewBinding.queryExtenderNestedScrollview.fullScroll(View.FOCUS_DOWN)
                }

                viewBinding.mobileContactConfirmationRelativeLayout.visibility = View.VISIBLE
                viewBinding.whatsappContactConfirmationRelativeLayout.visibility = View.GONE
                viewBinding.emailAddressConfirmationRelativeLayout.visibility = View.GONE
            }
        }
    }

    private fun submitQueryExtension(contactInfo: String) {
        viewBinding.submitQueryExtensionButton.visibility = View.VISIBLE

        viewBinding.submitQueryExtensionButton.setOnClickListener {
            serviceQueryJSONObject.put(SERVICE_QUERY, service).put(QUERY_URGENCY, serviceUrgency)
                .put(QUERY_COMMUNICATION, communicationPreferences).put("CONTACT_INFO", contactInfo)
            Timber.i("QUERY EXTENSION OBJ -> $serviceQueryJSONObject")

            val timer = object : CountDownTimer(3000, 1000) {
                override fun onTick(p0: Long) {
                    hideEverythingAndShowLoader()
                    mixpanel.track("Extended Query", serviceQueryJSONObject)
                }

                override fun onFinish() {
                    hideEverythingAndThankTheUser()
                }
            }

            timer.start()
        }
    }

    private fun hideEverythingAndShowLoader() {
        viewBinding.serviceExtenderIntroHeaderRelativeLayout.visibility = View.GONE
        viewBinding.serviceQueryNameRelativeLayout.visibility = View.GONE
        viewBinding.serviceUrgencyRelativeLayout.visibility = View.GONE
        viewBinding.communicationOptionsRelativeLayout.visibility = View.GONE
        viewBinding.submitQueryExtensionButton.visibility = View.GONE
        viewBinding.queryExtenderReceivedImageView.visibility = View.GONE
        viewBinding.whatsappContactConfirmationRelativeLayout.visibility = View.GONE
        viewBinding.emailAddressConfirmationRelativeLayout.visibility = View.GONE
        viewBinding.mobileContactConfirmationRelativeLayout.visibility = View.GONE

        viewBinding.queryExtenderReceivedRelativeLayout.visibility = View.VISIBLE
    }

    private fun hideEverythingAndThankTheUser() {

        val timer = object : CountDownTimer(2000, 1000) {
            override fun onTick(p0: Long) {
                viewBinding.serviceExtenderIntroHeaderRelativeLayout.visibility = View.GONE
                viewBinding.serviceQueryNameRelativeLayout.visibility = View.GONE
                viewBinding.serviceUrgencyRelativeLayout.visibility = View.GONE
                viewBinding.communicationOptionsRelativeLayout.visibility = View.GONE
                viewBinding.submitQueryExtensionButton.visibility = View.GONE
                viewBinding.loadServiceQueryProgressBar.visibility = View.GONE

                viewBinding.queryExtenderReceivedRelativeLayout.visibility = View.VISIBLE
                viewBinding.queryExtenderReceivedImageView.visibility = View.VISIBLE
            }

            override fun onFinish() {
                this@QueryExtender.dismiss()
                openFragment(AllServicesFragment())

            }
        }
        timer.start()
    }

    fun openFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(QueryExtenderViewModel::class.java)
        // TODO: Use the ViewModel
    }

}