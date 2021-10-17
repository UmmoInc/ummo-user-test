package xyz.ummo.user.ui.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.fragment_personal_info.view.*
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.databinding.FragmentPersonalInfoBinding


/**
 * A simple [Fragment] subclass.
 * Use the [PersonalInfoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PersonalInfoFragment : Fragment() {

    private lateinit var viewBinding: FragmentPersonalInfoBinding
    private lateinit var profileView: View

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_personal_info, container,
            false
        )


        profileView = viewBinding.root

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        fillInProfileFields()

        return profileView
    }

    private fun fillInProfileFields() {
        profileViewModel.profileEntityLiveData.observe(
            viewLifecycleOwner,
            { profileEntity: ProfileEntity ->
                val profileName = profileEntity.profileName
                val spaceIndex = profileName?.indexOf(" ")
                val firstName = profileName!!.substring(0, spaceIndex!!)
                val surname = profileName.substring(spaceIndex + 1, profileName.length)

                profileView.first_name_text_input_edit_text.setText(firstName)
                profileView.last_name_text_input_edit_text.setText(surname)

                profileView.contact_text_input_edit_text.setText(profileEntity.profileContact)
                profileView.email_text_input_edit_text.setText(profileEntity.profileEmail)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /** Hiding the MainActivity toolbar **/
        val toolbar = requireActivity().findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.visibility = View.GONE

        profileView.profile_tool_bar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.profile_menu_help -> {
                    true
                }

                else -> false
            }
        }

        /** Navigating the User back to the Main Profile View **/
        profileView.profile_tool_bar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        profileView.profile_tool_bar.setNavigationOnClickListener {
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
         * @return A new instance of fragment PersonalInfoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PersonalInfoFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}