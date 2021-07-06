package xyz.ummo.user.ui.fragments.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.fragment_service_query_bottom_sheet.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentServiceQueryBottomSheetBinding
import xyz.ummo.user.ui.MainScreen.Companion.SERVICE_OBJECT

class ServiceQueryBottomSheetFragment : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var serviceObjectParam: String? = null
    private var param2: String? = null
    private lateinit var viewBinding: FragmentServiceQueryBottomSheetBinding
    private lateinit var contributionEditText: TextInputEditText
    private lateinit var contributeImageView: ImageView
    private lateinit var sendingContributionLayout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
//            serviceObjectParam = it.getString(SERVICE_OBJECT)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_service_query_bottom_sheet,
                container, false)

        val view = viewBinding.root
        contributionEditText = view.contribution_text_input_edit_text
        contributeImageView = view.contribute_button
        sendingContributionLayout = view.sending_contribution_relative_layout

        contributeImageView.setOnClickListener { makeServiceContribution(contributionEditText.text.toString().trim()) }

        return view
    }

    private fun makeServiceContribution(contribution: String) {

        if (arguments != null) {
            Timber.e("Arguments available -> $arguments")

            Timber.e("SERVICE CONTRIBUTION -> $contribution")
            sendingContributionLayout.visibility = View.VISIBLE
            view?.safety_precaution_relative_layout!!.visibility = View.GONE
            view?.service_caution_relative_layout!!.visibility = View.GONE
            view?.update_invitation_relative_layout!!.visibility = View.GONE
            view?.service_update_linear_layout!!.visibility = View.GONE
        } else {
            Timber.e("No Arguments!")
        }
    }

    companion object {
        const val TAG = "CustomBottomSheetDialog"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ServiceQueryBottomSheetFragment().apply {
                    arguments = Bundle().apply {
//                        putString(ARG_PARAM1, param1)
//                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}