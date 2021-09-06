package xyz.ummo.user.ui.fragments.profile

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.Logout
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.ui.fragments.scanner.CheckIn
import xyz.ummo.user.ui.signup.RegisterActivity

@Keep
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

    private var mAuth: FirebaseAuth? = null

    private lateinit var profilePrefs: SharedPreferences
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private lateinit var mixpanel: MixpanelAPI

    /** Variables for `Viewing Personal Info` **/
    private lateinit var viewPersonalInfoRL: RelativeLayout
    private lateinit var viewPersonalInfoTV: TextView
    private lateinit var viewPersonalInfoIV: ImageView

    /** Variables for `How Ummo Works (Support)` **/
    private lateinit var howUmmoWorksRL: RelativeLayout
    private lateinit var howUmmoWorksTV: TextView
    private lateinit var howUmmoWorksIV: ImageView

    /** Variables for `Get Help (Support)` **/
    private lateinit var getHelpRL: RelativeLayout
    private lateinit var getHelpTV: TextView
    private lateinit var getHelpIV: ImageView

    /** Variables for `Get Help (Support)` **/
    private lateinit var giveFeedbackRL: RelativeLayout
    private lateinit var giveFeedbackTV: TextView
    private lateinit var giveFeedbackIV: ImageView

    /** Variables for `Terms of Service (Legal)` **/
    private lateinit var termsRL: RelativeLayout
    private lateinit var termsTV: TextView
    private lateinit var termsIV: ImageView

    /** Variables for `Privacy (Legal)` **/
    private lateinit var privacyRL: RelativeLayout
    private lateinit var privacyTV: TextView
    private lateinit var privacyIV: ImageView

    /** Variables for `Webpage (About)` **/
    private lateinit var webpageRL: RelativeLayout
    private lateinit var webpageTV: TextView
    private lateinit var webpageIV: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }

        mixpanel = MixpanelAPI.getInstance(
            context,
            resources.getString(R.string.mixpanelToken)
        )

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        profilePrefs = requireActivity().getSharedPreferences(ummoUserPreferences, mode)
        val editor = profilePrefs.edit()

        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /** {START} Assigning view variables to views **/
        profileName = view.findViewById(R.id.name_text_view)

        viewPersonalInfoRL = view.findViewById(R.id.view_personal_info_relative_layout)
        viewPersonalInfoTV = view.findViewById(R.id.view_personal_info_text_view)
        viewPersonalInfoIV = view.findViewById(R.id.view_personal_info_image_view)

        howUmmoWorksRL = view.findViewById(R.id.how_ummo_works_relative_layout)
        howUmmoWorksTV = view.findViewById(R.id.how_ummo_works_text_view)
        howUmmoWorksIV = view.findViewById(R.id.how_ummo_works_image_view)

        getHelpRL = view.findViewById(R.id.get_help_relative_layout)
        getHelpTV = view.findViewById(R.id.get_help_text_view)
        getHelpIV = view.findViewById(R.id.get_help_image_view)

        giveFeedbackRL = view.findViewById(R.id.give_feedback_relative_layout)
        giveFeedbackTV = view.findViewById(R.id.give_feedback_text_view)
        giveFeedbackIV = view.findViewById(R.id.give_feedback_image_view)

        termsRL = view.findViewById(R.id.terms_relative_layout)
        termsTV = view.findViewById(R.id.terms_text_view)
        termsIV = view.findViewById(R.id.terms_image_view)

        privacyRL = view.findViewById(R.id.privacy_relative_layout)
        privacyTV = view.findViewById(R.id.privacy_text_view)
        privacyIV = view.findViewById(R.id.privacy_image_view)

        webpageRL = view.findViewById(R.id.website_relative_layout)
        webpageTV = view.findViewById(R.id.website_text_view)
        webpageIV = view.findViewById(R.id.website_image_view)
        /** {END} Assigning view variables to views **/

        //TODO: Undo && migrate to PersonalInfoFragment
        profileViewModel?.profileEntityLiveData?.observe(
            viewLifecycleOwner,
            { profileEntity1: ProfileEntity ->
                profileName!!.text = profileEntity1.profileName
//            profileContact!!.text = profileEntity1.profileContact
//            profileEmail!!.text = profileEntity1.profileEmail
            })

        viewPersonalInfoRL.setOnClickListener { launchPersonalInfoFragment() }
        viewPersonalInfoTV.setOnClickListener { launchPersonalInfoFragment() }
        viewPersonalInfoIV.setOnClickListener { launchPersonalInfoFragment() }

        /*profileViewBinding.logoutButton.setOnClickListener {
            logout()
        }*/

//        profileViewBinding.launchScannerFAB.setOnClickListener { launchCheckInScanner() }

//        profileIntroCardView = view.findViewById(R.id.profile_info_card)

        /*if (profilePrefs.getBoolean("PROFILE_CARD_CLOSED", false)) {
            profileIntroCardView!!.visibility = View.GONE
        } else
            profileIntroCardView!!.visibility = View.VISIBLE

        closeCardImageView!!.setOnClickListener {
            cardDismissedEvent.cardDismissed = true
            EventBus.getDefault().post(cardDismissedEvent)

            profileIntroCardView!!.visibility = View.GONE
            editor.putBoolean("PROFILE_CARD_CLOSED", true).apply()

            mixpanel.track("profileFrag_cardDismissed")
        }*/

        return view
    }

    private fun openFragment(fragment: Fragment) {

        val fragmentTransaction: FragmentTransaction = requireActivity().supportFragmentManager
            .beginTransaction()

        fragmentTransaction.replace(R.id.frame, fragment)
        fragmentTransaction.commit()
    }

    /** We're using this function to take the User to their Personal Info view **/
    private fun launchPersonalInfoFragment() {
        openFragment(PersonalInfoFragment())
    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchHowUmmoWorksFragment() {

    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchGetHelpFragment() {

    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchGiveFeedbackFragment() {

    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchTermsOfServiceFragment() {

    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchPrivacyPolicyFragment() {

    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchWebsiteFragment() {

    }

    private fun launchCheckInScanner() {
        Timber.e("LAUNCHING SCANNER")
        val checkInFragment = CheckIn()
        val fragmentActivity = context as FragmentActivity
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame, checkInFragment)
        fragmentTransaction.commit()
    }

    private fun logout() {
        mAuth!!.signOut()
        val progress = ProgressDialog(requireContext())
        progress.setMessage("Logging out...")
        progress.show()
        object : Logout(requireContext()) {
            override fun done() {
                startActivity(Intent(requireContext(), RegisterActivity::class.java))
            }
        }
        // prefManager.unSetFirstTimeLaunch();
    }

    /*private fun dismissInfoCard() {
        profileInfoCard.infoCancelImageView.setOnClickListener {
            Timber.e("CARD DISMISSED!")
        }
    }*/

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

    class ProfileViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                ProfileViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}