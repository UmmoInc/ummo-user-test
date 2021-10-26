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
import xyz.ummo.user.databinding.FragmentTermsOfServiceBinding

class TermsOfService : Fragment() {

    private lateinit var termsOfServiceWebView: WebView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var termsOfServiceView: View
    private lateinit var termsOfServiceBinding: FragmentTermsOfServiceBinding

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

//        termsOfServiceBinding.termsOfServiceToolBar.inflateMenu(R.menu.personal_profile_menu)
        termsOfServiceBinding.termsOfServiceToolBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        termsOfServiceBinding.termsOfServiceToolBar.setNavigationOnClickListener {
            openFragment(ProfileFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        toolbar.visibility = View.VISIBLE
        bottomNav.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        termsOfServiceBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_terms_of_service, container, false)
        termsOfServiceView = termsOfServiceBinding.root

        termsOfServiceWebView = termsOfServiceBinding.termsOfServiceWebView
        termsOfServiceWebView.loadUrl("https://sites.google.com/view/ummo-terms-and-conditions/home")

        return termsOfServiceView
    }

    private fun openFragment(fragment: Fragment) {

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager
            .beginTransaction()

        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TermsOfService().apply {
                arguments = Bundle().apply {
                }
            }
    }
}