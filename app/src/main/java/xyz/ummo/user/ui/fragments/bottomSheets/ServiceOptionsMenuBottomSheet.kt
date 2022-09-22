package xyz.ummo.user.ui.fragments.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentServiceOptionsMenuBottomSheetBinding
import xyz.ummo.user.utilities.SERVICE_ENTITY

class ServiceOptionsMenuBottomSheet : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewBinding: FragmentServiceOptionsMenuBottomSheetBinding
    private lateinit var mixpanel: MixpanelAPI
    private lateinit var serviceEntity: ServiceEntity
    private val serviceBundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            serviceEntity = arguments?.getSerializable(SERVICE_ENTITY) as ServiceEntity
            Timber.e("SERVICE ENTITY -> $serviceEntity")
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
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_service_options_menu_bottom_sheet,
            container,
            false
        )

        val view = viewBinding.root

        viewBinding.serviceMenuTitleText.text = serviceEntity.serviceName
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shareServiceSelected()
    }

    private fun shareServiceSelected() {
        val shareServiceInfoBottomSheet = ShareServiceInfoBottomSheet()
        serviceBundle.putSerializable(SERVICE_ENTITY, serviceEntity)

        shareServiceInfoBottomSheet.arguments = serviceBundle

        viewBinding.shareServiceMenuRelativeLayout.setOnClickListener {
            shareServiceInfoBottomSheet.show(
                requireActivity().supportFragmentManager,
                ShareServiceInfoBottomSheet.TAG
            )
        }
        viewBinding.shareServiceMenuImageView.setOnClickListener {
            shareServiceInfoBottomSheet.show(
                requireActivity().supportFragmentManager,
                ShareServiceInfoBottomSheet.TAG
            )
        }

        viewBinding.shareServiceMenuTextView.setOnClickListener {
            shareServiceInfoBottomSheet.show(
                requireActivity().supportFragmentManager,
                ShareServiceInfoBottomSheet.TAG
            )
        }
    }

    companion object {
        const val TAG = "ServiceOptionsMenuBottomSheet"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ServiceOptionsMenuBottomSheet().apply {
                arguments = Bundle().apply {
                }
            }
    }
}