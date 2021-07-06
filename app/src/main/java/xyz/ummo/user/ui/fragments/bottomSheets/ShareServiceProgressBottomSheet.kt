package xyz.ummo.user.ui.fragments.bottomSheets

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.fragment_share_service_info_bottom_sheet.view.*
import kotlinx.android.synthetic.main.fragment_share_service_progress_bottom_sheet.view.*
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentShareServiceProgressBottomSheetBinding
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.utilities.broadcastreceivers.ShareBroadCastReceiver

class ShareServiceProgressBottomSheet : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewBinding: FragmentShareServiceProgressBottomSheetBinding
    private lateinit var mixpanelAPI: MixpanelAPI
    private var serviceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

        mixpanelAPI = MixpanelAPI.getInstance(
            context,
            context?.resources?.getString(R.string.mixpanelToken)
        )

        serviceName = arguments?.getString(MainScreen.SERVICE_NAME, serviceName)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_share_service_progress_bottom_sheet, container, false
        )

        val view = viewBinding.root
        view.share_progress_button.setOnClickListener { launchShareSheet() }

        return view
    }

    private fun launchShareSheet() {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "I'm currently using Ummo to get my government service without waiting in long queues. \n\nFind out how you can also save time.\n" +
                    "\n" +
                    "Try it out from Google Play Store today: https://play.google.com/store/apps/details?id=xyz.ummo.user")
            type = "text/plain"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0,
            Intent(context, ShareBroadCastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val shareIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Intent.createChooser(
                sendIntent, "Share how to $serviceName with your friends...",
                pendingIntent.intentSender
            )
        } else {
            Intent.createChooser(sendIntent, "Share how to $serviceName with your friends...")
        }
        /** Capturing the Share Service Info action (Phase-2)**/
        context?.startActivity(shareIntent)
        mixpanelAPI.track("shareServiceBottomSheet_sharingServiceProgress_phaseTwo")
    }

    companion object {
        const val TAG = "ShareServiceProgressBottomSheet"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShareServiceProgressBottomSheet().apply {
                arguments = Bundle().apply {
                }
            }
    }
}