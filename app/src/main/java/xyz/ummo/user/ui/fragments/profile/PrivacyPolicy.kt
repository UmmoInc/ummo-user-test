package xyz.ummo.user.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicy : Fragment() {

    private lateinit var privacyPolicyWebView: WebView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var privacyPolicyView: View
    private lateinit var privacyPolicyBinding: FragmentPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Hiding the MainActivity toolbar **/
        toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.visibility = View.GONE

        /** Hiding the Bottom NavBar **/
        bottomNav = requireActivity().findViewById(R.id.bottom_nav)
        bottomNav.visibility = View.GONE

        privacyPolicyBinding.privacyPolicyToolBar.inflateMenu(R.menu.personal_profile_menu)

        privacyPolicyBinding.privacyPolicyToolBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        privacyPolicyBinding.privacyPolicyToolBar.setNavigationOnClickListener {
            openFragment(ProfileFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toolbar.visibility = View.VISIBLE
        bottomNav.visibility = View.VISIBLE
    }

    private fun openFragment(fragment: Fragment) {

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager
            .beginTransaction()

        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        privacyPolicyBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_privacy_policy,
            container,
            false
        )

        privacyPolicyView = privacyPolicyBinding.root

        privacyPolicyWebView = privacyPolicyBinding.privacyPolicyWebView

        privacyPolicyWebView.loadUrl("https://sites.google.com/view/ummo-privacy-policy/home")

        return privacyPolicyView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PrivacyPolicy.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PrivacyPolicy().apply {
                arguments = Bundle().apply {
                }
            }
    }
}