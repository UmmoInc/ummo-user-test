package xyz.ummo.user.utilities.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import xyz.ummo.user.utilities.NetworkStateEvent

class ConnectivityReceiver : BroadcastReceiver() {
    var noConnectivity: Boolean = false

    override fun onReceive(context: Context?, intent: Intent?) {
        /** The NetworkStateEvent() class is used by EventBus to update the network state **/
        val networkStateEvent = NetworkStateEvent()

        //TODO: Replace/Update deprecated code
        if (ConnectivityManager.CONNECTIVITY_ACTION == intent?.action) {
            noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)

            if (noConnectivity) {
//                Timber.e("DISCONNECTED [True] -> $noConnectivity")
                /** Capturing offline (no connection) event **/
                networkStateEvent.noConnectivity = noConnectivity
                /** Posting network event (Bus station) **/
                EventBus.getDefault().post(networkStateEvent)
            } else {
//                Timber.e("CONNECTED [False]-> $noConnectivity")
                /** Connection restored **/
                networkStateEvent.noConnectivity = noConnectivity
                EventBus.getDefault().post(networkStateEvent)
            }
        }
    }
}