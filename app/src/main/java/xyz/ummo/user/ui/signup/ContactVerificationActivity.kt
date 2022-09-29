package xyz.ummo.user.ui.signup

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.perf.metrics.AddTrace
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.ContactVerificationBinding
import xyz.ummo.user.utilities.USER_CONTACT
import xyz.ummo.user.utilities.USER_NAME
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.ContactAutoVerificationEvent
import xyz.ummo.user.utilities.eventBusEvents.NetworkStateEvent
import xyz.ummo.user.utilities.eventBusEvents.RecaptchaStateEvent
import java.util.concurrent.TimeUnit

class ContactVerificationActivity : AppCompatActivity() {

    private lateinit var viewBinding: ContactVerificationBinding
    private var userContact: String = ""
    private var userName: String = ""
    private var bundle = Bundle()

    //Firebase Auth Variable
    private lateinit var mixpanel: MixpanelAPI
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mVerificationId: String? = "default"
    private var mVerificationInProgress = false
    private val connectivityReceiver = ConnectivityReceiver()
    private val contactAutoVerificationEvent = ContactAutoVerificationEvent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ContactVerificationBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)

        mixpanel = MixpanelAPI.getInstance(
            this.applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        /** [NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)

        mAuth = FirebaseAuth.getInstance()
        /** Hiding the toolbar **/
        try {
            this.supportActionBar?.hide()
        } catch (npe: NullPointerException) {
            Timber.e("NPE-> $npe")
        }
        //Retrieving user's contact from intentExtras
        val intent = intent
        userName = intent.getStringExtra(USER_NAME)!!
        userContact = intent.getStringExtra(USER_CONTACT)!!

        val promptText: String =
            String.format(resources.getString(R.string.code_prompt_text), userContact)
        viewBinding.codePrompt.text = promptText

        initCallback()
        startPhoneNumberVerification(userContact)
        verifyCode()

        skippingVerification()
    }

    override fun onStart() {
        super.onStart()
        /**[NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver -
         * to monitor the network state **/
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)
    }

    private fun skippingVerification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            viewBinding.verificationChronometer.isCountDown = true
        }
        viewBinding.verificationChronometer.base = SystemClock.elapsedRealtime() + 30000
        viewBinding.verificationChronometer.start()

        val timer = object : CountDownTimer(30000, 1000) {
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                viewBinding.verificationChronometer.stop()
                noVerificationCode()
            }
        }

        timer.start()
    }

    private fun noVerificationCode() {
        val noVerificationCodeDialogBuilder = MaterialAlertDialogBuilder(this)
        val noVerificationCodeView = LayoutInflater.from(this)
            .inflate(R.layout.no_verification_code_view, null)

        noVerificationCodeDialogBuilder
            .setTitle("No Verification Code?")
            .setIcon(R.drawable.logo)
            .setView(noVerificationCodeView)
            .setPositiveButton("Continue") { dialogInterface, i ->
                Timber.e("VERIFY LATER")
                viewBinding.verifyContact.text = "VERIFY LATER"
                viewBinding.verifyContact.setBackgroundColor(resources.getColor(R.color.ummo_4))
                /** Indicating that the User has not confirmed their contact yet**/
                bundle.putInt(CONFIRMED, 0)
                mixpanel.track("Contact-Verification: Skipping-Verification")
            }
            .setNegativeButton("Try Again") { dialogInterface, i ->

                /** Taking User back to [RegisterActivity] to correct contact entry **/
                val intent = Intent(this, RegisterActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                intent.putExtra(TRYING_AGAIN, 1)
                intent.putExtra(USER_NAME, userName)
                intent.putExtra(USER_CONTACT, userContact)
                startActivity(intent)

                mixpanel.track("Contact-Verification: Repeating-Registration")

                finish()
            }

        /** Checking if the Activity is active - without this check, we'll crash because it may
         * show the dialog when the Activity isn't active **/
        if (!isFinishing) {
            noVerificationCodeDialogBuilder.show()
        }
    }

    /** [NetworkStateEvent-3] Subscribing to the NetworkState Event (via EventBus) **/
    @Subscribe
    fun onNetworkStateEvent(networkStateEvent: NetworkStateEvent) {
        Timber.e("ON-EVENT -> ${networkStateEvent.noConnectivity}")

        /** Toggling between network states && displaying an appropriate Snackbar **/
        if (networkStateEvent.noConnectivity!!) {
            showSnackbarRed("Please check connection...", -2)
        } /*else {
            showSnackbarBlue("Connecting...", -1)
        }*/
    }

    override fun onStop() {
        super.onStop()
        /** [NetworkStateEvent-4] Unregistering the Connectivity Broadcast Receiver - app is in the background,
         * so we don't need to stay online (for NOW) **/
        unregisterReceiver(connectivityReceiver)
    }

    private fun verifyCode() {
        viewBinding.verifyContact.setOnClickListener {
            val code = viewBinding.confirmationCode.text.toString()
//            val code = "123456"
            Timber.e("Verifying code -> $code!")
            val codeJson = JSONObject()
            codeJson.put("CODE", code)

            //TODO: java.lang.IllegalArgumentException: Cannot create PhoneAuthCredential
            // without either verificationProof, sessionInfo, temporary proof, or enrollment ID.
            verifyPhoneNumberWithCode(mVerificationId!!.toString(), code)

            mixpanel.track("Contact-Verification: Verifying-Code", codeJson)

        }
    }

    @AddTrace(name = "starting_phone_number_verification")
    private fun startPhoneNumberVerification(phoneNumber: String) {
//        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks)

        initCallback()

        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)

        mVerificationInProgress = true
        Timber.e("Verification started!")
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val mixpanel = MixpanelAPI.getInstance(
            applicationContext,
            resources.getString(R.string.mixpanelToken)
        )
        when {
            code.isBlank() -> {
                /** Checking if the User has confirmed Later Verification & not just jumping to skip
                 * the verification process - [bundle] will contain [CONFIRMED] **/
                if (bundle.getInt(CONFIRMED) != 0) {
                    showSnackbarRed("Please enter your verification code first", 0)
                } else {
                    showSnackbarRed("TO BE VERIFIED LATER", 0)
                    val timer = object : CountDownTimer(1000, 1000) {
                        override fun onTick(p0: Long) {
                            viewBinding.verifyContactProgressBarLayout.visibility = View.VISIBLE
                        }

                        override fun onFinish() {
                            viewBinding.verifyContactProgressBarLayout.visibility = View.GONE
                            mixpanel?.track("registration_takingUserToFinalStep")
                            takeUserToFinalStep()
                        }
                    }
                    timer.start()
                }
            }
            code.length != 6 -> {
                showSnackbarRed("Please make sure you enter the complete code", 0)
            }
            else -> {

                try {
                    val credential = PhoneAuthProvider.getCredential(verificationId, code)
                    signInWithPhoneAuthCredential(credential)
                } catch (ex: Exception) {
                    showSnackbarRed("EXC -> $ex", 0)
                }
            }
        }
    }

    @Subscribe
    fun onRecaptchaStateEvent(recaptchaStateEvent: RecaptchaStateEvent) {
        Timber.e("Recaptcha State -> $recaptchaStateEvent")

        if (recaptchaStateEvent.recaptchaPassed!!) {
            showSnackbarGreen("Security check passed", -1)
            /** [MixpanelAPI] Tracking recaptcha success */
            mixpanel.track("Contact-Verification: Recaptcha-Passed")
        } else {
            showSnackbarRed("Security issues detected. Try again.", -2)
            /** [MixpanelAPI] Tracking recaptcha failure */
            mixpanel.track("Contact-Verification: Recaptcha-Failed")
        }
    }

    //TODO: track this event with Mixpanel
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                //Sign user in, update UI
                showSnackbar("Successfully signing in")
                val user = task.result.user
                Timber.e("Signing in, user -> $user")

                mixpanel.track("Contact-Verification: Successfully-Verified")

                takeUserToFinalStep()
            } else {
                mixpanel.track("Contact-Verification: Skipping-Verification")


                takeUserToFinalStep()

                //TODO: Revive this integrity check to filter out fake numbers
                /*if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    showSnackbar("Invalid code!")
                }
                Timber.e("Sign in failed! -> ${task.exception}")

                showSnackbar("Sign in failed! ${task.exception}")*/
            }
        }
    }

    private fun takeUserToFinalStep() {
        startActivity(
            Intent(this, CompleteSignUpActivity::class.java)
                .putExtra(USER_CONTACT, userContact)
                .putExtra(USER_NAME, userName)
        )

        finish()
    }

    @AddTrace(name = "contact_verification_responses")
    private fun initCallback() {
        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                /** This callback will be invoked in two situations:
                1 - Instant verification. In some cases the phone number can be instantly
                verified without needing to send or enter a verification code.
                2 - Auto-retrieval. On some devices Google Play services can automatically
                detect the incoming verification SMS and perform verification without
                user action. */
                Timber.e("onVerificationCompleted: $credential")
                // [START_EXCLUDE silent]
                mVerificationInProgress = false
                // [END_EXCLUDE]
                contactAutoVerificationEvent.contactAutoVerified = true
                EventBus.getDefault().post(contactAutoVerificationEvent)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                /** This callback is invoked if an invalid request for verification is made,
                for instance if the the phone number format is not valid.
                [START_EXCLUDE silent] */

                showSnackbar("Verification Failed: Please try again.")
                Timber.e("onVerificationFailed: $e")
                mVerificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    showSnackbar("Invalid phone number.")
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showSnackbar("Quota exceeded.")
                }
                // Show a message and update the UI
                showSnackbar("Verification failed. Please try again.")

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                /** The SMS verification code has been sent to the provided phone number, we
                now need to ask the user to enter the code and then construct a credential
                by combining the code with a verification ID.*/
                showSnackbarBlue("Verification SMS on the way", -1)
                Timber.e("onCodeSent: $verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                super.onCodeAutoRetrievalTimeOut(verificationId)
                //TODO: add action that'll take user back to RegisterActivity
                showSnackbarBlue("Please re-enter your contact", -1)
                Timber.e("onCodeAutoRetrievalTimeOut: $verificationId")
            }
        }
        // [END phone_auth_callbacks]
    }

    override fun onBackPressed() {

    }

    //TODO: Deprecate
    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showSnackbarRed(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.orange_red))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.show()
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.show()
    }

    private fun showSnackbarGreen(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.lawn_green))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.show()
    }

    companion object {
        const val TRYING_AGAIN = "TRYING_AGAIN"
        const val CONFIRMED = "CONFIRMED"
    }
}