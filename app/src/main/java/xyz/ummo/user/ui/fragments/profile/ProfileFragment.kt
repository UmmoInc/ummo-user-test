package xyz.ummo.user.ui.fragments.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.databinding.FragmentMyProfileBinding
import xyz.ummo.user.databinding.InfoCardBinding

class ProfileFragment : Fragment() {
    private var profileName: TextView? = null
    private var profileContact: TextView? = null
    private var profileEmail: TextView? = null
    private var profileViewModel: ProfileViewModel? = null
    private val profileEntity = ProfileEntity()

    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mListener: OnFragmentInteractionListener? = null

    //ViewBinding
    private lateinit var profileViewBinding: FragmentMyProfileBinding
    private lateinit var profileInfoCard: InfoCardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        profileViewBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_my_profile, container, false)

        profileInfoCard = DataBindingUtil
                .inflate(inflater, R.layout.info_card, container, false)

        val view = profileViewBinding.root

        profileName = profileViewBinding.profileName
        profileContact = profileViewBinding.profileContact
        profileEmail = profileViewBinding.profileEmail

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        profileViewModel?.profileEntityLiveData?.observe(viewLifecycleOwner, Observer { profileEntity1: ProfileEntity ->
            profileName!!.text = profileEntity1.profileName
            profileContact!!.text = profileEntity1.profileContact
            profileEmail!!.text = profileEntity1.profileEmail
        })

        dismissInfoCard()

        return view
    }

    private fun dismissInfoCard() {
        profileInfoCard.infoCancelImageView.setOnClickListener {
            Timber.e("CARD DISMISSED!")
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    /*override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = if (context is OnFragmentInteractionListener) {
            context
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener")
        }
    }*/

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}