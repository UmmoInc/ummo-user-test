package xyz.ummo.user.ui.signup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import timber.log.Timber
import xyz.ummo.user.databinding.RegisterBinding
import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var registerBinding: RegisterBinding
    private lateinit var fullFormattedPhoneNumber: String
    private var userName: String = ""

    //Firebase Phone Auth Variables
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mVerificationId: String? = "default"
    private var mVerificationInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Init'ing Firebase Auth **/
        mAuth = FirebaseAuth.getInstance()

        /** Hiding the toolbar **/
        try {
            this.supportActionBar?.hide()
        } catch (npe: NullPointerException) {
            Timber.e("NPE-> $npe")
        }

        /** Binding the view to registerBinding **/
        registerBinding = RegisterBinding.inflate(layoutInflater)
        val view = registerBinding.root
        setContentView(view)

        initCallback()
        register()
    }

    /** Begin Phone Number Verification with PhoneAuthProvider from Firebase **/
    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, mCallbacks)
        mVerificationInProgress = true
        Timber.i("Verification started!")
    }

    /** Signing up with PhoneAuth Credential - with Firebase's Auth instance (mAuth);
     * used by `initCallback()
     * **/
    private fun signUpWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                showSnackbar("Successfully signing in...", -1)
            } else {
                showSnackbar("Try again!", 0)

                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    showSnackbar("Invalid code...", 0)
                }

                Timber.i("Sign in failed -> ${task.exception}")
                showSnackbar("Sign in failed!", -2)
            }
        }
    }

    /**  **/
    private fun initCallback() {
        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                showSnackbar("onVerificationCompleted:$credential", -1)
                Timber.e("onVerificationCompleted: $credential")
                // [START_EXCLUDE silent]
                mVerificationInProgress = false

                // [END_EXCLUDE]

                signUpWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                // [START_EXCLUDE silent]
                showSnackbar("onVerificationFailed", 0)
                Timber.e("onVerificationFailed: $e")
                mVerificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    showSnackbar("Invalid phone number.", 0)
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    showSnackbar("Quota exceeded.", 0)
                }

                // Show a message and update the UI
                showSnackbar("Verification failed", -2)

            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                showSnackbar("onCodeSent:$verificationId", -1)
                Timber.e("onCodeSent: $verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                super.onCodeAutoRetrievalTimeOut(verificationId)
                showSnackbar("Code timeout", 0)
                Timber.e("onCodeAutoRetrievalTimeOut: $verificationId")
            }
        }
        // [END phone_auth_callbacks]
    }

    /**
     * Register function takes the contact entered by the user & parses it into an official contact by
     * checking it against the international standards for mobile numbers
     * **/
    private fun register() {
        registerBinding.registerButton.setOnClickListener {

            registerBinding.registrationCcp.registerCarrierNumberEditText(registerBinding.userContactEditText)
            fullFormattedPhoneNumber = registerBinding.registrationCcp.fullNumberWithPlus.toString().trim()
            userName = registerBinding.userNameEditText.text.toString().trim()

            if (registerBinding.registrationCcp.isValidFullNumber) {
                //TODO: begin registration process
                startPhoneNumberVerification(fullFormattedPhoneNumber)

                val intent = Intent(this, ContactVerificationActivity::class.java)

                Timber.e("User Name -> $userName")
                Timber.e("User Contact -> $fullFormattedPhoneNumber")

                intent.putExtra("USER_CONTACT", fullFormattedPhoneNumber)
                intent.putExtra("USER_NAME", userName)
                startActivity(intent)

                finish()
            } else {
                showSnackbar("Please enter a correct number.", 0)
                registerBinding.userContactEditText.error = "Edit your contact."
            }
        }
    }

    private fun showSnackbar(message: String, duration: Int) {
        Snackbar.make(findViewById(android.R.id.content), message, duration).show() //LENGTH_SHORT
    }
}