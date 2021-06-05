package xyz.ummo.user.utilities.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_CHOSEN_COMPONENT
import timber.log.Timber

class ShareBroadCastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val clickedComponent : ComponentName = intent.getParcelableExtra(EXTRA_CHOSEN_COMPONENT)!!

        Timber.e("Clicked component -> $clickedComponent")
    }
}