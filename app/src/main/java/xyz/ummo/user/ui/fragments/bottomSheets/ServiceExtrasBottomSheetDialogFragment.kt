package xyz.ummo.user.ui.fragments.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_service_extras_modal_bottom_sheet.*
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R

class ServiceExtrasBottomSheetDialogFragment : BottomSheetDialogFragment(){

    private lateinit var requestAgentRelativeLayout: RelativeLayout

    companion object {
        const val TAG = "CustomBottomSheetDialog"
        @JvmStatic
        fun newInstance(serviceInfo: JSONObject) = ServiceExtrasBottomSheetDialogFragment().apply {

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_service_extras_modal_bottom_sheet, container, false)
        requestAgentRelativeLayout = view.findViewById(R.id.request_agent_relative_layout)

        if (arguments != null) {
            Timber.e("Arguments available -> $arguments")
            if (arguments!!["SERVICE_DELEGATABLE"] == true) {
                requestAgentRelativeLayout.visibility = View.VISIBLE
            } else
                requestAgentRelativeLayout.visibility = View.GONE
        } else {
            Timber.e("No Arguments!")
        }
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }
}