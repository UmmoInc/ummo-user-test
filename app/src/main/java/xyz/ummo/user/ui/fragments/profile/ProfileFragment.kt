package xyz.ummo.user.ui.fragments.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
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
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.GeneralFeedback
import xyz.ummo.user.api.Logout
import xyz.ummo.user.api.RequestEmailVerification
import xyz.ummo.user.api.User
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.ui.fragments.scanner.CheckIn
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.ui.main.MainScreen.Companion.supportFM
import xyz.ummo.user.ui.signup.RegisterActivity
import xyz.ummo.user.utilities.EMAIL_VERIFIED
import xyz.ummo.user.utilities.USER_CONTACT
import xyz.ummo.user.utilities.USER_EMAIL

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
    private lateinit var prefEditor: SharedPreferences.Editor
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private lateinit var mixpanel: MixpanelAPI

    /** Variables for `Viewing Personal Info` **/
    private lateinit var viewPersonalInfoRL: RelativeLayout
    private lateinit var viewPersonalInfoTV: TextView
    private lateinit var viewPersonalInfoIV: ImageView

    /** Account Verification Icon **/
    private lateinit var accountVerificationIcon: ImageView

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

    private var userVerified: Boolean? = null

    /** User Profile SharedPref Val **/
    private var userContact = ""
    private var userEmail = ""
    private var userId = ""
    private var jwt = ""

    private lateinit var emailConcatenate: TextView

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

    private fun requestVerification(userId: String) {
        object : RequestEmailVerification(requireContext(), userId) {
            override fun done(data: ByteArray, code: Number) {
                Timber.e("REQUESTING EMAIL -> ${String(data)}")
            }
        }
    }

    private fun checkEmailVerification() {
        if (profilePrefs.getBoolean(EMAIL_VERIFIED, false)) {
            accountVerificationIcon.setImageResource(R.drawable.ic_baseline_verified_user_24)
            accountVerificationIcon.setOnClickListener { notifyUserOfEmailVerificationStatusDone() }
        } else {
            accountVerificationIcon.setImageResource(R.drawable.ic_twotone_hourglass_top_24)
            accountVerificationIcon.setOnClickListener { notifyUserOfEmailVerificationStatusPending() }
        }
    }

    private fun notifyUserOfEmailVerificationStatusPending() {
        val notifyUserOfEmailStatusView = LayoutInflater
            .from(requireContext()).inflate(R.layout.notify_user_of_email_status, null)

        val notifyUserOfEmailStatusDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        emailConcatenate = notifyUserOfEmailStatusView
            .findViewById(R.id.email_verification_prompt_text_view)

        val emailVerificationText =
            String.format(resources.getString(R.string.verify_email_prompt), userEmail)
        emailConcatenate.text = emailVerificationText

        notifyUserOfEmailStatusDialogBuilder.setTitle("Please Verify Your Email")
            .setIcon(R.drawable.logo)
            .setView(notifyUserOfEmailStatusView)

        notifyUserOfEmailStatusDialogBuilder.setPositiveButton("Resend") { dialogInterface, i ->
            Timber.e("RESENDING EMAIL") //TODO: resend email verification
            requestVerification(userId)
            mixpanel.track("profile_resendingEmail")
        }

        notifyUserOfEmailStatusDialogBuilder.show()
    }

    private fun notifyUserOfEmailVerificationStatusDone() {
        val notifyUserOfEmailStatusView = LayoutInflater
            .from(requireContext()).inflate(R.layout.notify_user_of_email_status_done, null)

        val notifyUserOfEmailStatusDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        notifyUserOfEmailStatusDialogBuilder.setTitle("Congratulations!")
            .setIcon(R.drawable.logo)
            .setView(notifyUserOfEmailStatusView)

        notifyUserOfEmailStatusDialogBuilder.setPositiveButton("GOT IT") { dialogInterface, i ->

        }

        notifyUserOfEmailStatusDialogBuilder.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        profilePrefs = requireActivity().getSharedPreferences(ummoUserPreferences, mode)
        prefEditor = profilePrefs.edit()

        userContact = profilePrefs.getString(USER_CONTACT, "").toString()
        userEmail = profilePrefs.getString(USER_EMAIL, "").toString()
        jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "").toString()
        userId = User.getUserId(jwt)

        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /** {START} Assigning view variables to views **/
        profileName = view.findViewById(R.id.name_text_view)

        accountVerificationIcon = view.findViewById(R.id.verification_status_image_view)

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
                /** Parsing through the [profileEntity1]'s [profileName] to extract the first name **/
                val endOfFirstNameIndex = profileEntity1.profileName?.indexOf(" ", 0, true)
                val firstName = profileEntity1.profileName?.substring(0, endOfFirstNameIndex!!)
                profileName!!.text = profileEntity1.profileName
            })

        viewPersonalInfoRL.setOnClickListener { launchPersonalInfoFragment() }
        viewPersonalInfoTV.setOnClickListener { launchPersonalInfoFragment() }
        viewPersonalInfoIV.setOnClickListener { launchPersonalInfoFragment() }

        howUmmoWorksRL.setOnClickListener { launchHowUmmoWorksFragment() }
        howUmmoWorksIV.setOnClickListener { launchHowUmmoWorksFragment() }
        howUmmoWorksTV.setOnClickListener { launchHowUmmoWorksFragment() }

        getHelpRL.setOnClickListener { launchGetHelpFragment() }
        getHelpIV.setOnClickListener { launchGetHelpFragment() }
        getHelpTV.setOnClickListener { launchGetHelpFragment() }

        giveFeedbackRL.setOnClickListener { launchGiveFeedbackFragment() }
        giveFeedbackIV.setOnClickListener { launchGiveFeedbackFragment() }
        giveFeedbackTV.setOnClickListener { launchGiveFeedbackFragment() }

        privacyRL.setOnClickListener { launchPrivacyPolicyFragment() }
        privacyIV.setOnClickListener { launchPrivacyPolicyFragment() }
        privacyTV.setOnClickListener { launchPrivacyPolicyFragment() }

        termsRL.setOnClickListener { launchTermsOfServiceFragment() }
        termsIV.setOnClickListener { launchTermsOfServiceFragment() }
        termsTV.setOnClickListener { launchTermsOfServiceFragment() }

        webpageRL.setOnClickListener { launchWebsiteFragment() }
        webpageIV.setOnClickListener { launchWebsiteFragment() }
        webpageTV.setOnClickListener { launchWebsiteFragment() }

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

        /** Checking User verification status **/
        checkEmailVerification()

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
        mixpanel.track("profile_personaInfo_launched")
        openFragment(PersonalInfoFragment())
    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchHowUmmoWorksFragment() {
        mixpanel.track("profile_howUmmoWorks_launched")
        openFragment(HowUmmoWorks())
    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchGetHelpFragment() {
        mixpanel.track("profile_getHelp_launched")
        val getHelp = GetHelp()
        getHelp.show(
            /** [supportFM] is borrowed from [MainScreen]'s companion object **/
            supportFM, GetHelp.TAG
        )
    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchTermsOfServiceFragment() {
        mixpanel.track("profile_termsOfService_launched")
        openFragment(TermsOfService())
    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchPrivacyPolicyFragment() {
        mixpanel.track("profile_privacy_launched")
        openFragment(PrivacyPolicy())
    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchWebsiteFragment() {
        mixpanel.track("profile_website_launched")
        openFragment(VisitUmmoPage())
    }

    /** We're using this function to take the User to view how Ummo works **/
    private fun launchGiveFeedbackFragment() {

        val feedbackDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.feedback_dialog, null)

        val feedbackDialogBuilder = MaterialAlertDialogBuilder(requireContext())

        feedbackDialogBuilder.setTitle("Feedback")
            .setIcon(R.drawable.logo)
            .setView(feedbackDialogView)

        feedbackDialogBuilder.setPositiveButton("Submit") { dialogInterface, i ->
            val feedbackEditText =
                feedbackDialogView.findViewById<TextInputEditText>(R.id.feedbackEditText)
            val feedbackText = feedbackEditText.text?.trim().toString()

            Timber.e("Feedback Submitted-> $feedbackText")
            if (feedbackText.isNotEmpty()) {

                /** [MixpanelAPI] Tracking when the User first experiences Ummo **/
                val feedbackEventObject = JSONObject()
                feedbackEventObject.put("FEEDBACK", feedbackText)
                mixpanel.track("feedback_submitted", feedbackEventObject)

                submitFeedback(feedbackText, userContact)
            } else {
                showSnackbarYellow("You forgot your feedback", -1)
                mixpanel.track("feedback_cancelled")
            }

        }

        feedbackDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
            Timber.e("Feedback Cancelled")
        }

        feedbackDialogBuilder.show()
    }

    /** This function sends the feedback over HTTP Post by overriding `done` from #Feedback
     * It's used by #feedback **/
    private fun submitFeedback(feedbackString: String, userContact: String) {

        object : GeneralFeedback(requireContext(), feedbackString, userContact) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    Timber.e("Feedback Submitted -> ${String(data)}")
                    showSnackbarBlue("Thank you for your feedback :)", 0)
                } else {
                    Timber.e("Feedback Error: Code -> $code")
                    Timber.e("Feedback Error: Data -> ${String(data)}")
                    showSnackbarYellow("Uhm. Feedback is stuck somewhere", 0)

                }
            }
        }
    }

    private fun showSnackbarYellow(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(requireActivity().findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.gold))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val bottomNav = requireActivity().findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(requireActivity().findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
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

    /*class ProfileViewModelFactory(private val application: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                ProfileViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }*/
}