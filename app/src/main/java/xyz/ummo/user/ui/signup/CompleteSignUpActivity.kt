package xyz.ummo.user.ui.signup

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.messaging.FirebaseMessaging
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.onesignal.OneSignal
import io.sentry.Sentry
import io.sentry.protocol.User
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.Login
import xyz.ummo.user.api.SocketIO
import xyz.ummo.user.databinding.CompleteSignUpBinding
import xyz.ummo.user.ui.main.MainScreen
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.ContactAutoVerificationEvent
import xyz.ummo.user.utilities.eventBusEvents.NetworkStateEvent
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import java.text.SimpleDateFormat
import java.util.*

class CompleteSignUpActivity : AppCompatActivity() {

    private var fcmToken: String? = ""
    private var readyToSignUp: Boolean = false
    private lateinit var viewBinding: CompleteSignUpBinding
    private lateinit var mixpanel: MixpanelAPI
    private var userName: String = ""
    private var userContact: String = ""
    private var prefManager: PrefManager? = null
    private var firebaseAuth: FirebaseAuth? = null
    private val connectivityReceiver = ConnectivityReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mixpanel = MixpanelAPI.getInstance(
            this.applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        viewBinding = CompleteSignUpBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)

        val intent = intent
        userContact = intent.getStringExtra(USER_CONTACT)!!
        userName = intent.getStringExtra(USER_NAME)!!

        Timber.e("BUNDLE -> ${intent.extras}")

        /** Hiding the toolbar **/
        try {
            this.supportActionBar?.hide()
        } catch (npe: NullPointerException) {
            Timber.e("NPE-> $npe")
        }

        /**[NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)

        /** Init OneSignal **/
        /*OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()*/
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        OneSignal.initWithContext(this)
        OneSignal.setAppId(getString(R.string.onesignal_app_id))

        /** Init firebaseAuth **/
        firebaseAuth = FirebaseAuth.getInstance()

        // Checking for first time launch - before calling setContentView()
        prefManager = PrefManager(this)
        if (!prefManager!!.isFirstTimeLaunch) {
            Timber.e("Not first time")
            launchHomeScreen()
            finish()
        }

        getFCMToken()
//        checkForSocketConnection()

        completeSignUp()
    }

    private fun getFCMToken() {
        /** Instantiating Firebase Instance **/
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.e("Fetching FCM registration token failed -> ${task.exception}")
                return@OnCompleteListener
            }

            fcmToken = task.result
            Timber.e("FCM TOKEN -> $fcmToken")
        })
    }

    override fun onStart() {
        super.onStart()
        /** [NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver
         * - to monitor the network state **/
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)
    }

    @Subscribe
    fun onContactAutoVerificationEvent(contactAutoVerificationEvent: ContactAutoVerificationEvent) {
        Timber.e("CONTACT AUTO-VERIFIED -> ${contactAutoVerificationEvent.contactAutoVerified}")
        mixpanel.track("Complete-Sign-up: Contact-Auto-Verified")

        if (contactAutoVerificationEvent.contactAutoVerified!!) {
            showSnackbarBlue("Contact auto verified", -1)
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

        /** [NetworkStateEvent-4] Unregistering the Connectivity Broadcast Receiver
         * - app is in the background,
         * so we don't need to stay online (for NOW) **/
        unregisterReceiver(connectivityReceiver)
    }

    private fun completeSignUp() {
//        val status = OneSignal.getPermissionSubscriptionState()
        val deviceState = OneSignal.getDeviceState()
        var onePlayerId = deviceState!!.userId

        viewBinding.signUpBtn.setOnClickListener {
            val emailField: EditText = viewBinding.userEmailTextInputEditText
            val userEmail: String = viewBinding.userEmailTextInputEditText.text.toString()

            when {
                Patterns.EMAIL_ADDRESS.matcher(userEmail).matches().not() -> {
                    emailField.error = "Please use a valid email..."
                    emailField.requestFocus()
                }

                emailField.length() == 0 -> {
                    emailField.error = "Please provide an email..."
                    emailField.requestFocus()
                }
                else -> {
                    val jseObject = JSONObject()
                    val progress = ProgressDialog(this)
                    progress.setMessage("Signing up...")
                    progress.show()

                    Timber.e("User Name-> $userName")
                    Timber.e("User Contact-> $userContact")
                    Timber.e("User Email-> $userEmail")
                    Timber.e("Player ID-> $onePlayerId")

                    /** Retrieving OneSignal PlayerId before signing up **/
                    if (!onePlayerId.isNullOrEmpty()) {
                        try {
                            mixpanel.track("Complete-Sign-up: Sign-up Successful")
                            setupMixpanelUserIdentity(userName, userContact, userEmail)
                            //Ummo server sign-up
                            signUp(userName, userEmail, userContact, onePlayerId)
                            //Firebase email-auth sign-up
                            createUserWithEmailAndPassword(userEmail, userContact)
                            progress.dismiss()

                        } catch (e: JSONException) {
                            e.printStackTrace()
                            jseObject.put("JSE", e)
                            mixpanel.track("Complete-Sign-up: GLITCHED", jseObject)
                            Timber.e("User Signup JSON Exception -> $e")
                        }
                    } else {
                        Timber.e("USER-PID is null/empty -> $onePlayerId")
                        jseObject.put("Exception", "Missing PlayerID")
                        mixpanel.track("Complete-Sign-up: GLITCHED", jseObject)

                        onePlayerId = generatePID()
                        setupMixpanelUserIdentity(userName, userContact, userEmail)
                        signUp(userName, userEmail, userContact, onePlayerId)
                        //Firebase email-auth sign-up
                        createUserWithEmailAndPassword(userEmail, userContact)

                        progress.dismiss()
                        showSnackbarBlue("We missed something. Trying again...", -1)
                    }
                }
            }
        }
    }

    private fun generatePID(): String {
        return OneSignal.getDeviceState()!!.userId
    }

    private fun setupMixpanelUserIdentity(name: String, contact: String, email: String) {
        val mixpanel = MixpanelAPI.getInstance(
            this,
            resources.getString(R.string.mixpanelToken)
        )

        /** UserObject used to store values to be passed onto Mixpanel**/
        val userObject = JSONObject()
        userObject.put("userName", name)
        userObject.put("userContact", contact)
        userObject.put("userEmail", email)

        /** Logging Mixpanel User profile **/
        if (mixpanel != null) {
            mixpanel.track("completeSignUp", userObject)
            mixpanel.people.identify(userContact)
//                                    mixpanel.people.set("PID", userPID)
            mixpanel.people.set("User-Name", userName)
            mixpanel.people.set("User-Contact", userContact)
        }
    }

    /** Using a Socket instance, we're checking if our connection to the server is successful or not
     * then handling that appropriately for the user to be aware of what to do with it #UX **/
    private fun checkForSocketConnection() {
        SocketIO.mSocket!!.on("connect") {
            showSnackbarBlue("LET'S GO", 0)
            readyToSignUp = true
            Timber.e("READY TO SIGN-UP [TRUE]-> $readyToSignUp")
        }

        SocketIO.mSocket!!.on("connect_error") {
            showSnackbarRed("GIVE US A SEC", -2)
            readyToSignUp = false
            Timber.e("READY TO SIGN-UP [FALSE]-> $readyToSignUp")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun signUp(name: String, email: String, contact: String, playerId: String) {
        val simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        val currentDate = simpleDateFormat.format(Date())

        object : Login(applicationContext, name, email, contact, playerId) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    launchHomeScreen()
                    /** Saving user details in Shared Preferences */
                    val sharedPreferences = getSharedPreferences(ummoUserPreferences, mode)
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putBoolean(SIGNED_UP, true)
                    editor.putString(USER_NAME, name)
                    editor.putString(USER_CONTACT, contact)
                    editor.putString(USER_EMAIL, email)
                    editor.putString(USER_PID, playerId)
                    editor.putBoolean(NEW_SESSION, true)
                    editor.apply()

                    /** [MixpanelAPI] 1. Identifying User by contact &&
                     *                2. Tracking sign_up activity **/
                    val userObject = JSONObject()
                    userObject.put(USER_NAME, name)
                    userObject.put(USER_CONTACT, contact)
                    userObject.put(USER_EMAIL, email)
                    userObject.put(SIGN_UP_DATE, currentDate)
                    userObject.put(MIXPANEL_NAME, name)

                    mixpanel.people?.identify(contact)
                    mixpanel.identify(contact)
                    mixpanel.track("Complete-Sign-up: DONE", userObject)
                    Timber.e("successfully logging in-> ${String(data)}")
                    identifyUserWithSentry()
                } else {
                    Timber.e("Something happened... $code +  $data  + ${String(data)}")
                    //TODO: create an appropriate feedback for the user when they encounter this.
//                    Toast.makeText(this, "Something went Awfully bad", Toast.LENGTH_LONG).show()
                    logWithStaticAPI()
                }
            }
        }
    }

    private fun createUserWithEmailAndPassword(email: String, contact: String) {
        firebaseAuth!!.createUserWithEmailAndPassword(email, contact)
            .addOnCompleteListener { task -> //progressBar.setVisibility(View.GONE);
                if (task.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Registered Successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, MainScreen::class.java))
                    finish()
                } else {
                    if (task.exception is FirebaseAuthUserCollisionException) {
                        Toast.makeText(
                            applicationContext,
                            "Already Registered!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            Objects.requireNonNull(task.exception).message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    private fun showSnackbarRed(message: String, length: Int) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.orange_red))
        snackbar.show()
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(this.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))

        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun logWithStaticAPI() {
        identifyUserWithSentry()
        try {
            unsafeMethod()
            Timber.e("logWithStaticAPI, unsafeMethod")
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private fun identifyUserWithSentry() {
        val mainActPreferences = getSharedPreferences(ummoUserPreferences, mode)

        Sentry.addBreadcrumb("User made an action")
        val user = User().apply {
            email = mainActPreferences.getString(USER_EMAIL, "")
            username = mainActPreferences.getString(USER_NAME, "")
        }
        Sentry.setUser(user)
    }

    private fun unsafeMethod() {
        throw UnsupportedOperationException("This needs attention!")
    }

    private fun launchHomeScreen() {

        prefManager?.isFirstTimeLaunch = false
        Timber.e("launchHomeScreen: No Extras")
        startActivity(Intent(this, MainScreen::class.java).putExtra(FRAGMENT_DESTINATION, ""))
        finish()
    }

    companion object {
        private const val ummoUserPreferences = "UMMO_USER_PREFERENCES"
        private const val mode = MODE_PRIVATE
    }
}