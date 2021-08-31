package xyz.ummo.user.ui.fragments.bottomSheets

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.fragment_share_service_info_bottom_sheet.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentShareServiceInfoBottomSheetBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.utilities.SERVICE_OBJECT
import xyz.ummo.user.utilities.broadcastreceivers.ShareBroadCastReceiver

class ShareServiceInfoBottomSheet : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var shareButton: MaterialButton
    private var serviceInfoText = ""
    private var serviceName = ""
    private lateinit var viewBinding: FragmentShareServiceInfoBottomSheetBinding
    private lateinit var mixpanel: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val serviceObject = arguments?.getSerializable(SERVICE_OBJECT) as ServiceObject
            Timber.e("Shared Service -> ${serviceObject.serviceName}")
        }

        mixpanel = MixpanelAPI.getInstance(
            context,
            context?.resources?.getString(R.string.mixpanelToken)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_share_service_info_bottom_sheet, container, false
        )

        val view = viewBinding.root
        val serviceObject = arguments?.getSerializable(SERVICE_OBJECT) as ServiceObject
        serviceName = serviceObject.serviceName
        val showYouCare = String.format(resources.getString(R.string.show_you_care), serviceName)
        viewBinding.showCareMainTextView.text = showYouCare
        shareButton = view.share_info_button
        shareButton.setOnClickListener { launchShareSheet() }

        return view
    }

    private fun verbalizingServiceName(serviceName: String): String {
        /** Converting the [serviceName] to a verb **/

        var firstNoun = serviceName.substring(0, 11)

        if (firstNoun.contains("Application", true))
            firstNoun = "Apply"

        return firstNoun
    }

    private fun launchShareSheet() {

        val serviceVerb = verbalizingServiceName(serviceName)
        val serviceNameWithoutFirstNoun = serviceName.subSequence(11, serviceName.length)
        val newServiceNameBeingShared = serviceVerb + serviceNameWithoutFirstNoun

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out how to $newServiceNameBeingShared (and more) from the Ummo App. \n\nDon't like waiting in queues? Find out how Ummo can help you save time. \n\nTry it out from Google Play Store today: https://play.google.com/store/apps/details?id=xyz.ummo.user")
            type = "text/plain"
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0,
            Intent(context, ShareBroadCastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT)

        val shareIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent.createChooser(sendIntent, "Share how to $newServiceNameBeingShared with your friends...",
                pendingIntent.intentSender)
        } else {
            Intent.createChooser(sendIntent, "Share how to $newServiceNameBeingShared with your friends...")
        }
        /** Capturing the Share Service Info action (Phase-2)**/
        context?.startActivity(shareIntent)
        mixpanel.track("shareServiceBottomSheet_sharingServiceInfo_phaseTwo")
    }

    companion object {
        const val TAG = "ShareServiceInfoBottomSheet"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShareServiceInfoBottomSheet().apply {
                arguments = Bundle().apply {
                }
            }
    }
}