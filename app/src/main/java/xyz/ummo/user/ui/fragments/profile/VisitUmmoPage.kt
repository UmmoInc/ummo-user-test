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
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentVisitUmmoPageBinding

class VisitUmmoPage : Fragment() {

    private lateinit var visitUmmoPageWebView: WebView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var visitUmmoPageView: View
    private lateinit var visitUmmoPageBinding: FragmentVisitUmmoPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        visitUmmoPageBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_visit_ummo_page, container, false)
        visitUmmoPageView = visitUmmoPageBinding.root
        visitUmmoPageWebView = visitUmmoPageBinding.visitUmmoPageWebView
        visitUmmoPageWebView.loadUrl("https://www.ummo.xyz")
        return visitUmmoPageView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Hiding the MainActivity toolbar **/
        toolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.visibility = View.GONE

        visitUmmoPageBinding.visitUmmoPageToolBar.inflateMenu(R.menu.personal_profile_menu)
        visitUmmoPageBinding.visitUmmoPageToolBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        visitUmmoPageBinding.visitUmmoPageToolBar.setNavigationOnClickListener {
            openFragment(ProfileFragment())
        }
    }

    private fun openFragment(fragment: Fragment) {

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager
            .beginTransaction()

        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VisitUmmoPage.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VisitUmmoPage().apply {
                arguments = Bundle().apply {
                }
            }
    }
}