package xyz.ummo.user.ui.signup

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.ContactVerificationBinding
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.NetworkStateEvent
import xyz.ummo.user.utilities.eventBusEvents.RecaptchaStateEvent
import java.util.concurrent.TimeUnit

class ContactVerificationActivity : AppCompatActivity() {

    private lateinit var viewBinding: ContactVerificationBinding
    private var userContact: String = ""
    private var userName: String = ""

    //Firebase Auth Variable
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mVerificationId: String? = "default"
    private var mVerificationInProgress = false
    private val connectivityReceiver = ConnectivityReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ContactVerificationBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)

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
        userName = intent.getStringExtra("USER_NAME")!!
        userContact = intent.getStringExtra("USER_CONTACT")!!

        val promptText: String = String.format(resources.getString(R.string.code_prompt_text), userContact)
        viewBinding.codePrompt.text = promptText

        initCallback()
        startPhoneNumberVerification(userContact)
        verifyCode()
    }

    override fun onStart() {
        super.onStart()
        /**[NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver -
         * to monitor the network state **/
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)
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
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        viewBinding.verifyContact.setOnClickListener {
            val code = viewBinding.confirmationCode.text.toString()
//            val code = "123456"
            Timber.e("Verifying code -> $code!")

            //TODO: java.lang.IllegalArgumentException: Cannot create PhoneAuthCredential without either verificationProof, sessionInfo, temporary proof, or enrollment ID.
            verifyPhoneNumberWithCode(mVerificationId!!.toString(), code)

            mixpanel?.track("contactVerification_contactVerified")
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks)
        mVerificationInProgress = true
        Timber.e("Verification started!")
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)


        signInWithPhoneAuthCredential(credential)
    }

    @Subscribe
    fun onRecaptchaStateEvent(recaptchaStateEvent: RecaptchaStateEvent){
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        Timber.e("Recaptcha State -> $recaptchaStateEvent")

        if (recaptchaStateEvent.recaptchaPassed!!) {
            showSnackbarGreen("Security check passed", -1)
            /** [Mixpanel] Tracking recaptcha success */
            mixpanel?.track("contactVerification_recaptchaPassed")
        } else {
            showSnackbarRed("Security issues detected. Try again.", -2)
            /** [Mixpanel] Tracking recaptcha failure */
            mixpanel?.track("contactVerification_recaptchaFailed")
        }
    }

    //TODO: track this event with Mixpanel
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                //Sign user in, update UI
                showSnackbar("Successfully signing in")
                val user = task.result?.user
                Timber.e("Signing in, user -> $user")

                startActivity(Intent(this, CompleteSignUpActivity::class.java)
                        .putExtra("USER_CONTACT", userContact)
                        .putExtra("USER_NAME", userName))

                Timber.e("User Name -> $userName")
                Timber.e("User Contact -> $userContact")

                mixpanel?.track("registrationStarted_nextButton")

                finish()
            } else {
                showSnackbar("[FIX] Illegally signing in")
                startActivity(Intent(this, CompleteSignUpActivity::class.java)
                        .putExtra("USER_CONTACT", userContact)
                        .putExtra("USER_NAME", userName))

                Timber.e("User Name -> $userName")
                Timber.e("User Contact -> $userContact")

                finish()

                //TODO: Revive this integrity check to filter out fake numbers
                /*if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    showSnackbar("Invalid code!")
                }
                Timber.e("Sign in failed! -> ${task.exception}")

                showSnackbar("Sign in failed! ${task.exception}")*/
            }
        }
    }

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
                showSnackbar("onVerificationCompleted:$credential")
                Timber.e("onVerificationCompleted: $credential")
                // [START_EXCLUDE silent]
                mVerificationInProgress = false

                // [END_EXCLUDE]

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
                showSnackbar("Verification failed")

            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                /** The SMS verification code has been sent to the provided phone number, we
                now need to ask the user to enter the code and then construct a credential
                by combining the code with a verification ID.*/
                showSnackbar("Verification SMS on the way")
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
        snackbar.setTextColor(resources.getColor(R.color.quantum_googred600))
        snackbar.show()
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))
        snackbar.show()
    }

    private fun showSnackbarGreen(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.quantum_googgreen400))
        snackbar.show()
    }
}