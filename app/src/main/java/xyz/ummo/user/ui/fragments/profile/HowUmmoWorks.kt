package xyz.ummo.user.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.fragment_how_ummo_works.view.*
import xyz.ummo.user.R
import xyz.ummo.user.databinding.FragmentHowUmmoWorksBinding

/**
 * A simple [Fragment] subclass.
 * Use the [HowUmmoWorks.newInstance] factory method to
 * create an instance of this fragment.
 */
class HowUmmoWorks : Fragment() {

    private lateinit var viewBinding: FragmentHowUmmoWorksBinding
    private lateinit var howUmmoWorksView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            /*param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)*/
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_how_ummo_works,
            container,
            false
        )
        howUmmoWorksView = viewBinding.root
        return howUmmoWorksView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Hiding the MainActivity toolbar **/
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE

        howUmmoWorksView.how_ummo_works_tool_bar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        howUmmoWorksView.how_ummo_works_tool_bar.setNavigationOnClickListener {
            openFragment(ProfileFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.visibility = View.VISIBLE
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
         * @return A new instance of fragment HowUmmoWorks.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HowUmmoWorks().apply {
                arguments = Bundle().apply {
                    /*putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)*/
                }
            }
    }
}