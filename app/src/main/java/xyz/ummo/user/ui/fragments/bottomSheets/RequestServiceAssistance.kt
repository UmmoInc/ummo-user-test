package xyz.ummo.user.ui.fragments.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentRequestServiceAssistanceBinding
import xyz.ummo.user.utilities.REQUESTED_SERVICE

class RequestServiceAssistance : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var viewBinding: FragmentRequestServiceAssistanceBinding
    private lateinit var mixpanelAPI: MixpanelAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            try {
                val serviceObject = JSONObject(arguments?.getString(REQUESTED_SERVICE)!!)
                Timber.e("REQUESTED SERVICE OBJECT -> $serviceObject")
            } catch (jse: JSONException) {
                Timber.e("ERROR PARSING JSON")
                jse.printStackTrace()
            }
        }
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

        return view
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