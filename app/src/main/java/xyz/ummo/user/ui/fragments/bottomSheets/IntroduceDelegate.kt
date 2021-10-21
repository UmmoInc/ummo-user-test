package xyz.ummo.user.ui.fragments.bottomSheets

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentIntroduceDelegateBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.main.MainScreen.Companion.supportFM
import xyz.ummo.user.utilities.DELEGATION_INTRO_IS_CONFIRMED
import xyz.ummo.user.utilities.SERVICE_OBJECT
import xyz.ummo.user.utilities.mode
import xyz.ummo.user.utilities.ummoUserPreferences
import java.io.Serializable

class IntroduceDelegate : BottomSheetDialogFragment() {

    private lateinit var serviceObject: ServiceObject
    private lateinit var serviceObjectParam: Serializable
    private lateinit var viewBinding: FragmentIntroduceDelegateBinding
    private lateinit var rootView: View
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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
        serviceObjectParam = arguments?.getSerializable(SERVICE_OBJECT)!!
        serviceObject = serviceObjectParam as ServiceObject
        Timber.e("SERVICE OBJECT PARAM -> $serviceObjectParam")

        viewBinding.confirmDelegationIntroButton.setOnClickListener {
            launchServiceRequestBottomSheet(serviceObject)
        }
        // Inflate the layout for this fragment
        return rootView
    }

    private fun launchServiceRequestBottomSheet(serviceObject: ServiceObject) {

        sharedPreferences = requireContext().getSharedPreferences(ummoUserPreferences, mode)
        editor = sharedPreferences.edit()
        editor.putBoolean(DELEGATION_INTRO_IS_CONFIRMED, true).apply()
        /** Creating bottomSheet service request **/
        val requestBundle = Bundle()
        requestBundle.putSerializable(SERVICE_OBJECT, serviceObject)
        val serviceRequestBottomSheetDialog = ServiceRequestBottomSheet()
        serviceRequestBottomSheetDialog.arguments = requestBundle
        serviceRequestBottomSheetDialog
            .show(
                supportFM,
                ServiceRequestBottomSheet.TAG
            )
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