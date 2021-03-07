package xyz.ummo.user.ui.fragments.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentServiceFeeQueryBinding

class ServiceFeeQuery : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewBinding: FragmentServiceFeeQueryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_fee_query,
                container, false)

        val view = viewBinding.root
        return view
    }

    companion object {

        const val TAG = "ServiceFeeQuery"
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ServiceFeeQuery().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}