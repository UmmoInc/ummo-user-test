package xyz.ummo.user.ui.fragments.bottomSheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_delegation_fee_query.view.*
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentDelegationFeeQueryBinding
import xyz.ummo.user.ui.MainScreen.Companion.SERVICE_SPEC
import xyz.ummo.user.ui.MainScreen.Companion.SPEC_FEE

class DelegationFeeQuery : BottomSheetDialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var viewBinding: FragmentDelegationFeeQueryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(DELEGATION_FEE)
            param2 = it.getString(DELEGATED_SERVICE_FEE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_delegation_fee_query,
                container, false)

        val view = viewBinding.root

        val delegatedServiceFeeDetails = view.delegated_service_fee_support_text_view
        val delegatedDetails = arguments?.getString(SERVICE_SPEC, "SERVICE SPEC")
        val delegationDetailsText = String.format(resources.getString(R.string.delegated_service_fee_query_support_text), delegatedDetails)
        delegatedServiceFeeDetails.text = delegationDetailsText

        return view
    }

    companion object {

        const val TAG = "DelegationFeeQuery"
        private const val DELEGATION_FEE = "delegation_fee"
        private const val DELEGATED_SERVICE_FEE = "delegated_service_fee"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                DelegationFeeQuery().apply {
                    arguments = Bundle().apply {
                        putString(DELEGATION_FEE, param1)
                        putString(DELEGATED_SERVICE_FEE, param2)
                    }
                }
    }
}