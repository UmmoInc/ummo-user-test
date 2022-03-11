package xyz.ummo.user.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_ummo_browser.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentUmmoBrowserBinding
import xyz.ummo.user.ui.main.MainScreen
import java.util.*

private const val LAUNCH_URL = "https://www.ummo.xyz"
//private const val ARG_PARAM2 = "param2"

class UmmoBrowser : Fragment() {
    // TODO: Rename and change types of parameters
    private var launchURL: String? = null
    private var param2: String? = null

    private lateinit var ummoBrowserBinding: FragmentUmmoBrowserBinding
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var browserView: View
    private lateinit var ummoBrowserWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            launchURL = it.getString(LAUNCH_URL)
//            param2 = it.getString(ARG_PARAM2)
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

        ummoBrowserBinding.ummoBrowserToolBar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        ummoBrowserBinding.ummoBrowserToolBar.setNavigationOnClickListener {
            startActivity(Intent(context, MainScreen::class.java).putExtra(LAUNCH_URL, ""))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        toolbar.visibility = View.VISIBLE
        bottomNav.visibility = View.VISIBLE
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        ummoBrowserBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_ummo_browser, container, false)
        browserView = ummoBrowserBinding.root
        ummoBrowserWebView = ummoBrowserBinding.ummoBrowserWebView
        ummoBrowserWebView.settings.javaScriptEnabled = true
        Timber.e("OPEN URL -> $launchURL")
        ummoBrowserWebView.loadUrl(launchURL!!)

        return browserView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param launchURL Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UmmoBrowser.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(launchURL: String) =
                UmmoBrowser().apply {
                    arguments = Bundle().apply {
                        putString(LAUNCH_URL, launchURL)
//                        putString(ARG_PARAM2, date)
                    }
                }
    }
}