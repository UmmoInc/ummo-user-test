package xyz.ummo.user.utilities.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_CHOSEN_COMPONENT
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

class ShareBroadCastReceiver: BroadcastReceiver() {
    private lateinit var mixpanelAPI: MixpanelAPI

    override fun onReceive(context: Context, intent: Intent) {
        val clickedComponent : ComponentName = intent.getParcelableExtra(EXTRA_CHOSEN_COMPONENT)!!

        Timber.e("Clicked component -> $clickedComponent")
        mixpanelAPI = MixpanelAPI.getInstance(
            context,
            context.resources?.getString(R.string.mixpanelToken)
        )

        val shareDestination = JSONObject()
        shareDestination.put("shared_to", clickedComponent)
        mixpanelAPI.track("sharingServiceInfo_phaseThree", shareDestination)

    }
}