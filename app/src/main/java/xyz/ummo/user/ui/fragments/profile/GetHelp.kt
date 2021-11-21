package xyz.ummo.user.ui.fragments.profile

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentGetHelpBinding

class GetHelp : BottomSheetDialogFragment() {

    private lateinit var viewBinding: FragmentGetHelpBinding
    private lateinit var getHelpView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_get_help,
            container,
            false
        )
        getHelpView = viewBinding.root

        viewBinding.requestHelpButton.setOnClickListener {
            completeRequestOnWhatsApp()
        }

        return getHelpView
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
            val serviceRequestMessage = "Hey there, \n\nI'd like your help with something..."
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

                    return
                }
            }
            timer.start()
        } catch (e: PackageManager.NameNotFoundException) {
            showSnackbarYellow("WhatsApp not installed.", -1)
        }
//        makeStatusNotification("Service","Taking you to WhatsApp", requireContext())
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

    private fun showAlertDialog() {
        val takingYouToWhatsAppBuilder = MaterialAlertDialogBuilder(requireContext())
        val takingYouToWhatsAppView = LayoutInflater.from(requireContext())
            .inflate(R.layout.taking_you_to_whatsapp, null, false)

        takingYouToWhatsAppBuilder.setTitle("Just a second")
        takingYouToWhatsAppBuilder.setView(takingYouToWhatsAppView)
        takingYouToWhatsAppBuilder.setIcon(R.drawable.logo)
        takingYouToWhatsAppBuilder.show()
    }

    companion object {
        const val TAG = "GetHelp"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GetHelp().apply {
                arguments = Bundle().apply {

                }
            }
    }
}