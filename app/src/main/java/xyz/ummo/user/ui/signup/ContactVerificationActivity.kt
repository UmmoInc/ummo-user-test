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
import kotlinx.android.synthetic.main.contact_verification.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.ContactVerificationBinding
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ContactVerificationBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)

        mAuth = FirebaseAuth.getInstance()
        //Hiding the toolbar
        try {
            this.supportActionBar?.hide()
        } catch (npe: NullPointerException) {
            Timber.e("NPE-> $npe")
        }
        //Retrieving user's contact from intentExtras
        val intent = intent
        userContact = intent.getStringExtra("USER_CONTACT")!!
        userName = intent.getStringExtra("USER_NAME")!!

        val promptText: String = String.format(resources.getString(R.string.code_prompt_text), userContact)
        viewBinding.codePrompt.text = promptText

        initCallback()
        startPhoneNumberVerification(userContact)
        verifyCode()
    }

    private fun verifyCode() {
        viewBinding.verifyContact.setOnClickListener {
            val code = "123456"
            verifyPhoneNumberWithCode(mVerificationId!!.toString(), code)
            Timber.e("Verifying code!")
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

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                //Sign user in, update UI
                showSnackbar("Successfully signing in")
                val user = task.result?.user
                Timber.i("Signing in, user -> $user")

                startActivity(Intent(this, CompleteSignUp::class.java)
                        .putExtra("USER_CONTACT", userContact)
                        .putExtra("USER_NAME", userName))

                Timber.e("User Name -> $userName")
                Timber.e("User Contact -> $userContact")

                finish()
            } else {

                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    showSnackbar("Invalid code!")
                }
                Timber.i("Sign in failed! -> ${task.exception}")

                showSnackbar("Sign in failed! ${task.exception}")
            }
        }
    }

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
                showSnackbar("onVerificationCompleted:$credential")
                Timber.e("onVerificationCompleted: $credential")
                // [START_EXCLUDE silent]
                mVerificationInProgress = false

                // [END_EXCLUDE]

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                // [START_EXCLUDE silent]
                showSnackbar("onVerificationFailed")
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
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                showSnackbar("onCodeSent:$verificationId")
                Timber.e("onCodeSent: $verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                super.onCodeAutoRetrievalTimeOut(verificationId)
                showSnackbar("Code timeout")
                Timber.e("onCodeAutoRetrievalTimeOut: $verificationId")
            }
        }
        // [END phone_auth_callbacks]
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}