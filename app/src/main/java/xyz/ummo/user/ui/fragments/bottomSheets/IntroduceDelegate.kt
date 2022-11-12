package xyz.ummo.user.ui.fragments.bottomSheets

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentIntroduceDelegateBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.main.MainScreen.Companion.supportFM
import xyz.ummo.user.utilities.DELEGATION_INTRO_IS_CONFIRMED
import xyz.ummo.user.utilities.SERVICE_ENTITY
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences
import java.io.Serializable

class IntroduceDelegate : BottomSheetDialogFragment() {

    private lateinit var serviceEntity: ServiceEntity
    private lateinit var serviceEntityParam: Serializable
    private lateinit var viewBinding: FragmentIntroduceDelegateBinding
    private lateinit var rootView: View
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_introduce_delegate,
            container,
            false
        )
        rootView = viewBinding.root

        /** Unpacking [ServiceObject] from [getArguments]**/
        serviceEntityParam = arguments?.getSerializable(SERVICE_ENTITY)!!
        serviceEntity = serviceEntityParam as ServiceEntity
        Timber.e("SERVICE OBJECT PARAM -> $serviceEntityParam")

        viewBinding.confirmDelegationIntroButton.setOnClickListener {
            launchServiceRequestBottomSheet(serviceEntity)
        }

        viewBinding.introduceDelegateNestedScrollView.post {
            viewBinding.introduceDelegateNestedScrollView.fullScroll(
                View.FOCUS_DOWN
            )
        }

        termsAndConditions()
        // Inflate the layout for this fragment
        return rootView
    }

    private fun termsAndConditions() {
        val tsAndCs =
            "<div>Ummo and all its affiliates are COVID-19 compliant. \n\n<a href='https://sites.google.com/view/ummo-terms-and-conditions/home'>Terms and Conditions</a> apply.</div>"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            viewBinding.serviceGuaranteeTextView.text =
                Html.fromHtml(tsAndCs, Html.FROM_HTML_MODE_LEGACY)
        } else {
            viewBinding.serviceGuaranteeTextView.text = Html.fromHtml(tsAndCs)
        }
    }

    private fun launchServiceRequestBottomSheet(serviceEntity: ServiceEntity) {

        sharedPreferences = requireContext().getSharedPreferences(ummoUserPreferences, mode)
        editor = sharedPreferences.edit()
        editor.putBoolean(DELEGATION_INTRO_IS_CONFIRMED, true).apply()
        /** Creating bottomSheet service request **/
        val requestBundle = Bundle()
        requestBundle.putSerializable(SERVICE_ENTITY, serviceEntity)
        val serviceRequestBottomSheetDialog = ServiceRequestBottomSheet()
        serviceRequestBottomSheetDialog.arguments = requestBundle
        serviceRequestBottomSheetDialog
            .show(
                supportFM,
                ServiceRequestBottomSheet.TAG
            )

        this.dismiss()
    }

    companion object {

        const val TAG = "IntroduceDelegate"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            IntroduceDelegate().apply {
                arguments = Bundle().apply {

                }
            }
    }
}