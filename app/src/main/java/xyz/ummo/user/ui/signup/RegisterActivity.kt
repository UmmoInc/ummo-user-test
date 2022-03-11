package xyz.ummo.user.ui.signup

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.safetynet.SafetyNet
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.perf.metrics.AddTrace
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.SafetyNetReCAPTCHA
import xyz.ummo.user.databinding.RegisterBinding
import xyz.ummo.user.ui.signup.ContactVerificationActivity.Companion.TRYING_AGAIN
import xyz.ummo.user.utilities.USER_CONTACT
import xyz.ummo.user.utilities.USER_NAME
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.NetworkStateEvent
import xyz.ummo.user.utilities.eventBusEvents.RecaptchaStateEvent
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent

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
    private var snackbar: Snackbar? = null
    private val connectivityReceiver = ConnectivityReceiver()
    private val recaptchaStateEvent = RecaptchaStateEvent()

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

        /**[NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)

        termsAndConditions()
        register()
    }

    private fun termsAndConditions() {
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        registerBinding.legalTermsTextView.isClickable = true
        registerBinding.legalTermsTextView.movementMethod = LinkMovementMethod.getInstance()
        val legalTerms = "<div>By signing up, you agree to Ummo's <a href='https://sites.google.com/view/ummo-terms-and-conditions/home'>Terms of Use</a> & <a href='https://sites.google.com/view/ummo-privacy-policy/home'> Privacy Policy </a></div>"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerBinding.legalTermsTextView.text = Html.fromHtml(legalTerms, Html.FROM_HTML_MODE_LEGACY)

            Timber.e("USING HTML FLAG")
        } else {
            registerBinding.legalTermsTextView.text = Html.fromHtml(legalTerms)
            Timber.e("NOT USING HTML FLAG")
        }
    }

    override fun onStart() {
        super.onStart()
        /** [NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver - to monitor the network state **/
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
        } else {
            showSnackbarBlue("Connecting...", -1)
        }
    }

    @Subscribe
    fun onSocketStateEvent(socketStateEvent: SocketStateEvent) {
        if (!socketStateEvent.socketConnected!!) {
            showSnackbarRed("Can't reach Ummo network", -2)
        } else {
            showSnackbarBlue("Ummo network found...", -1)
        }
    }

    override fun onStop() {
        super.onStop()
        /** [NetworkStateEvent-4] Unregistering the Connectivity Broadcast Receiver - app is in the background,
         * so we don't need to stay online (for NOW) **/
        unregisterReceiver(connectivityReceiver)

        Timber.e("onSTOP")
    }

    //TODO: Reload user details from savedInstanceState bundle - instead of re-typing
    override fun onResume() {
        super.onResume()
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        intent.getIntExtra(TRYING_AGAIN, 0)

        if (intent.getIntExtra(TRYING_AGAIN, 0) == 1) {
            registerBinding.userNameTextInputEditText.setText(intent.getStringExtra(USER_NAME))
            registerBinding.userContactTextInputEditText.error = "Please try again."
            registerBinding.userNameTextInputEditText.requestFocus()

            mixpanel?.track("registration_tryingAgain")
        }
        Timber.e("WE'RE BACK")
    }

    /**
     * Register function takes the contact entered by the user & parses it into an official contact by
     * checking it against the international standards for mobile numbers
     * **/
    private fun register() {
        var nameApproved = false
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        registerBinding.registerButton.setOnClickListener {

            registerBinding.registrationCcp.registerCarrierNumberEditText(registerBinding.userContactTextInputEditText)
            fullFormattedPhoneNumber = registerBinding.registrationCcp.fullNumberWithPlus.toString().trim()
            userName = registerBinding.userNameTextInputEditText.text.toString().trim()

            if (userName.isBlank()) {
                showSnackbar("Please enter your name.", 0)
                registerBinding.userNameTextInputEditText.error = "Enter your name here."
                registerBinding.userNameTextInputEditText.requestFocus()
                mixpanel?.track("registrationStarted_nameLeftBlank_issue")

            } else if (!userName.contains(" ")) {
                showSnackbar("You forgot your last name.", 0)
                registerBinding.userNameTextInputEditText.error = "Please include your last name."
                registerBinding.userNameTextInputEditText.requestFocus()
                mixpanel?.track("registrationStarted_surnameNotIncluded_issue")

            } else if (userName.length < 3) {
                registerBinding.userNameTextInputEditText.error = "Please enter your real name."
                registerBinding.userNameTextInputEditText.requestFocus()
                mixpanel?.track("registrationStarted_nameTooShort_issue")

            } else if (registerBinding.registrationCcp.isValidFullNumber) {

                val intent = Intent(this, ContactVerificationActivity::class.java)

                intent.putExtra(USER_CONTACT, fullFormattedPhoneNumber)
                intent.putExtra(USER_NAME, userName)

                /** Delaying [startActivity] for 2 seconds **/
                val timer = object : CountDownTimer(1000, 1000) {
                    override fun onTick(p0: Long) {
//                        Timber.e("SKIPPING VERIF. $p0")
                        registerBinding.registerProgressBarLayout.visibility = View.VISIBLE
                    }

                    override fun onFinish() {
                        registerBinding.registerProgressBarLayout.visibility = View.GONE

                        startActivity(intent)
                        reCAPTCHA()
                        mixpanel?.track("registrationStarted_nextButton")

                        finish()
                    }
                }

                timer.start()

            } else {
                mixpanel?.track("registrationStarted_incorrectContact")

                showSnackbar("Please enter a correct number.", 0)
                registerBinding.userContactTextInputEditText.error = "Edit your contact."
            }
        }
    }

    @AddTrace(name = "recaptcha_security_test")
    private fun reCAPTCHA() {
        Timber.e("reCAPTCHA SUCCESSFUL - 0!")

        SafetyNet.getClient(this).verifyWithRecaptcha("6Ldc8ikaAAAAAIYNDzByhh1V7NWcAOZz-ozv-Tno")
            .addOnSuccessListener { response ->
                Timber.e("reCAPTCHA SUCCESSFUL - 1!")
                val userResponseToken = response.tokenResult
                if (response.tokenResult?.isNotEmpty() == true) {
                    Timber.e("reCAPTCHA Token -> $userResponseToken")

                    GlobalScope.launch {
                        Timber.e("reCAPTCHA Token -> $userResponseToken")

                        Timber.e("GLOBAL SCOPE THREAD NAME -> ${Thread.currentThread().name}")
                        verifyCaptchaFromServer(userResponseToken!!)
                    }
                }
            }
            .addOnFailureListener { e ->
                Timber.e("reCAPTCHA FAILED - 2!")
                if (e is ApiException) {
                    Timber.e("reCAPTCHA ERROR -> ${CommonStatusCodes.getStatusCodeString(e.statusCode)}")
                } else
                    Timber.e("reCAPTCHA ERROR (unknown) -> ${e.message}")
            }
    }

    @AddTrace(name = "verifying_captcha_from_server")
    private fun verifyCaptchaFromServer(responseToken: String) {
        object : SafetyNetReCAPTCHA(this, responseToken) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    Timber.e("reCAPTCHA Verified from Server -> ${String(data)}")
                    recaptchaStateEvent.recaptchaPassed = true
                    EventBus.getDefault().post(recaptchaStateEvent)
                } else {
                    recaptchaStateEvent.recaptchaPassed = false
                    EventBus.getDefault().post(recaptchaStateEvent)
                    Timber.e("reCAPTCHA could NOT be verified -> ${String(data)}")
                }
            }
        }
    }

    //TODO: Deprecate
    private fun showSnackbar(message: String, duration: Int) {
        Snackbar.make(findViewById(android.R.id.content), message, duration).show() //LENGTH_SHORT
    }

    private fun showSnackbarRed(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.orange_red))
        snackbar.show()
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))
        snackbar.show()
    }
}