package xyz.ummo.user.ui.fragments.bottomSheets

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.databinding.FragmentRequestServiceAssistanceBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.*

class RequestServiceAssistance : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var userName: String? = null

    /** Declaring vars for storing parsed service data before sending over WhatsApp **/
    private var serviceName: String? = null
    private var serviceCentre: String? = null
    private var serviceDate: String? = null
    private var serviceDocs = ArrayList<String>()

    private lateinit var viewBinding: FragmentRequestServiceAssistanceBinding
    private lateinit var mixpanelAPI: MixpanelAPI
    private lateinit var serviceRequest: JSONObject
    private lateinit var serviceObject: ServiceObject

    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var takingYouToWhatsAppBuilder: MaterialAlertDialogBuilder
    private lateinit var takingYouToWhatsApp: AlertDialog
    private lateinit var takingYouToWhatsAppView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            try {
                serviceRequest = JSONObject(arguments?.getString(REQUESTED_SERVICE)!!)
                serviceObject = arguments?.getSerializable(SERVICE_OBJECT) as ServiceObject
                Timber.e("REQUESTED SERVICE -> $serviceRequest")
                Timber.e("SERVICE OBJECT -> $serviceObject")

            } catch (jse: JSONException) {
                Timber.e("ERROR PARSING JSON")
                jse.printStackTrace()
            }
        }

        sharedPrefs = requireContext().getSharedPreferences(ummoUserPreferences, mode)
        editor = sharedPrefs.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_request_service_assistance,
            container,
            false
        )

        val view = viewBinding.root

        /** This is when the User completes the request action && gets taken to their WhatsApp to
         * begin messaging Ummo Customer Service **/
        viewBinding.requestServiceAssistanceButton.setOnClickListener {
            completeRequestOnWhatsApp()
        }

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /** Retrieving the User's name to attach to the request message down by [completeRequestOnWhatsApp]**/
        profileViewModel.profileEntityLiveData.observe(
            viewLifecycleOwner
        ) { profileEntity: ProfileEntity ->
            userName = profileEntity.profileName
        }

        return view
    }

    /** This function will format && return our [String] Service Request message appropriately:
     * **/
    private fun processServiceObject(serviceRequest: JSONObject): String {
        val requestTextOne: String
        val requestTextTwo: String
        var requestTextFinal = ""

        return try {
            serviceName = serviceRequest.getString("product_name")
            serviceCentre = serviceRequest.getString("chosen_service_centre")
            serviceDate = serviceRequest.getString("service_date")

            /** * 1. Format based on the keyword - "Renew" **/
            if (serviceName!!.startsWith("Renew", true)) {
                requestTextOne = "renewing my "
                requestTextTwo = serviceName!!.substring(6, serviceName!!.length)
                requestTextFinal = requestTextOne + requestTextTwo
            } else if (serviceName!!.startsWith("Register", true)) {
                requestTextOne = "registering my "
                requestTextTwo = serviceName!!.substring(11, serviceName!!.length)
                requestTextFinal = requestTextOne + requestTextTwo
            }

            /** Formatted message being returned - to be used by the User as they will **/
            "Hi there, \n\nI'd like assistance with $requestTextFinal. " +
                    "\n\nI prefer the $serviceCentre, on $serviceDate. \n\nRegards,\n$userName"
        } catch (jse: JSONException) {
            Timber.e("Failed to process Service Object -> $jse")
            ""
        }
    }

    private fun completeRequestOnWhatsApp() {
        try {
            /** Checking if WhatsApp is installed on User's device**/
            val packageManager = requireActivity().packageManager
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            /** Creating an intent that will:
             * 1. launch User's WhatsApp
             * 2. open Ummo's account
             * 3. prefill it with a message for the User to edit && send **/

            /** Ummo's WhatsApp Business Account contact **/
            val contact = "26876804065"

            /** The request message we are prefilling for the User **/
            val serviceRequestMessage = processServiceObject(serviceRequest)
            val intent = Intent(Intent.ACTION_SEND)

            intent.type = "text/plain"
            intent.setPackage("com.whatsapp")
            intent.putExtra("jid", PhoneNumberUtils.stripSeparators(contact) + "@s.whatsapp.net")
            intent.putExtra(Intent.EXTRA_TEXT, serviceRequestMessage)

            /** Showing the User a loader before directing them to WhatsApp **/
            val timer = object : CountDownTimer(2000, 1000) {

                override fun onTick(p0: Long) {
                    showAlertDialog()
                }

                override fun onFinish() {
                    startActivity(intent)
//                    editor.putString(DELEGATION_ID, delegation)
                    editor.putBoolean(WHATSAPP_LAUNCHED, true).apply()

                    return
                }
            }
            timer.start()
        } catch (e: PackageManager.NameNotFoundException) {
            showSnackbarYellow("WhatsApp not installed.", -1)
        }
//        makeStatusNotification("Service","Taking you to WhatsApp", requireContext())
    }

    private fun showAlertDialog() {
        takingYouToWhatsAppBuilder = MaterialAlertDialogBuilder(requireContext())
        takingYouToWhatsAppView = LayoutInflater.from(requireContext())
            .inflate(R.layout.taking_you_to_whatsapp, null, false)

        takingYouToWhatsAppBuilder.setTitle("Just a second")
        takingYouToWhatsAppBuilder.setView(takingYouToWhatsAppView)
        takingYouToWhatsAppBuilder.setIcon(R.drawable.logo)
        takingYouToWhatsApp = takingYouToWhatsAppBuilder.show()
    }

    override fun onResume() {
        super.onResume()

        /** Checking if there's a delegated service:
         * if TRUE, then we launch the DelegatedService view
         * else, resume normally **/
        when {
            sharedPrefs.getBoolean(WHATSAPP_LAUNCHED, false) -> {
                Timber.e("WE HAVE A DELEGATED SERVICE")
                editor.putBoolean(SERVICE_DELEGATED, true)
            }
            sharedPrefs.getBoolean(SERVICE_DELEGATED, false) -> {
                launchDelegatedService()
            }
            else -> {
                Timber.e("NO DELEGATED SERVICE")
            }
        }
    }

    private fun launchDelegatedService() {

        editor.putBoolean(SERVICE_DELEGATED, true)

        val intent = Intent(activity, MainScreen::class.java)
        startActivity(intent)
    }

    /** Supposed solution to "The specified child has a parent already.**/
    override fun onDestroyView() {

        if (view != null) {
            val parentViewGroup = requireView().parent as ViewGroup?
            parentViewGroup?.removeAllViews()
        }
        super.onDestroyView()
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

    companion object {
        const val TAG = "RequestServiceAssistance"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RequestServiceAssistance().apply {
                arguments = Bundle().apply {
                }
            }
    }
}