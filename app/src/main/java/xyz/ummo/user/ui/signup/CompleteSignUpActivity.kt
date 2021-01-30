package xyz.ummo.user.ui.signup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.onesignal.OneSignal
import io.sentry.core.Sentry
import io.sentry.core.protocol.User
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.databinding.CompleteSignUpBinding
import xyz.ummo.user.delegate.Login
import xyz.ummo.user.delegate.SocketIO
import xyz.ummo.user.ui.MainScreen
import xyz.ummo.user.utilities.PrefManager
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.NetworkStateEvent
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent
import java.text.SimpleDateFormat
import java.util.*

class CompleteSignUpActivity : AppCompatActivity() {

    private var readyToSignUp: Boolean = false
    private lateinit var viewBinding: CompleteSignUpBinding
    private var userName: String = ""
    private var userContact: String = ""
    private var prefManager: PrefManager? = null
    private var firebaseAuth: FirebaseAuth? = null
    private val connectivityReceiver = ConnectivityReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = CompleteSignUpBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)

        val intent = intent
        userContact = intent.getStringExtra("USER_CONTACT")!!
        userName = intent.getStringExtra("USER_NAME")!!

        /** Hiding the toolbar **/
        try {
            this.supportActionBar?.hide()
        } catch (npe: NullPointerException) {
            Timber.e("NPE-> $npe")
        }

        /**[NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)

        /** Init OneSignal **/
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init()

        /** Init firebaseAuth **/
        firebaseAuth = FirebaseAuth.getInstance()

        // Checking for first time launch - before calling setContentView()
        prefManager = PrefManager(this)
        if (!prefManager!!.isFirstTimeLaunch) {
            Timber.e("Not first time")
            launchHomeScreen()
            finish()
        }

//        checkForSocketConnection()

        completeSignUp()
    }

    override fun onStart() {
        super.onStart()
        /** [NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver
         * - to monitor the network state **/
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
        val mixpanel = MixpanelAPI.getInstance(this,
                resources.getString(R.string.mixpanelToken))

        val status = OneSignal.getPermissionSubscriptionState()
        val onePlayerId = status.subscriptionStatus.userId

        viewBinding.signUpBtn.setOnClickListener {
            val emailField: EditText = viewBinding.userEmailTextInputEditText
            val userEmail: String = viewBinding.userEmailTextInputEditText.text.toString()

            when {
                Patterns.EMAIL_ADDRESS.matcher(userEmail).matches().not() -> {
                    emailField.error = "Please use a valid email..."
                    emailField.requestFocus();
                }
                emailField.length() == 0 -> {
                    emailField.error = "Please provide an email..."
                    emailField.requestFocus()
                }
                else -> {
                    val progress = ProgressDialog(this)
                    progress.setMessage("Signing up...")
                    progress.show()

                    Timber.e("User Name-> $userName")
                    Timber.e("User Contact-> $userContact")
                    Timber.e("User Email-> $userEmail")
                    Timber.e("Player ID-> $onePlayerId")

                    /** UserObject used to store values to be passed onto Mixpanel**/
                    val userObject = JSONObject()

                    /** Retrieving OneSignal PlayerId before signing up **/
                    OneSignal.idsAvailable { userPID, registrationId ->
                        if (!userPID.isNullOrEmpty()) {
                            try {
                                userObject.put("userName", userName)
                                userObject.put("userContact", userContact)
                                userObject.put("userEmail", userEmail)

                                /** Logging Mixpanel User profile **/
                                if (mixpanel != null) {
                                    mixpanel.track("completeSignUp", userObject)
                                    mixpanel.people.identify(userContact)
                                    mixpanel.people.set("PID", userPID)
                                    mixpanel.people.set("User-Name", userName)
                                    mixpanel.people.set("User-Contact", userContact)
                                }

                                //Ummo server sign-up
                                signUp(userName, userEmail, userContact, userPID)
                                //Firebase email-auth sign-up
                                createUserWithEmailAndPassword(userEmail, userContact)
                                progress.dismiss()

                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Timber.e("User Signup JSON Exception -> $e")
                            }
                        } else {
                            Timber.e("USER-PID is null/empty -> $userPID")
                        }
                    }
                }
            }
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
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))
        val simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        val currentDate = simpleDateFormat.format(Date())

        object : Login(applicationContext, name, email, contact, playerId) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    launchHomeScreen()
                    /** Saving user details in Shared Preferences */
                    val sharedPreferences = getSharedPreferences(ummoUserPreferences, mode)
                    val editor: SharedPreferences.Editor
                    editor = sharedPreferences.edit()
                    editor.putBoolean("SIGNED_UP", true)
                    editor.putString("USER_NAME", name)
                    editor.putString("USER_CONTACT", contact)
                    editor.putString("USER_EMAIL", email)
                    editor.putString("USER_PID", playerId)
                    editor.putBoolean("NEW_SESSION", true)
                    editor.apply()

                    /** [MixpanelAPI] 1. Identifying User by contact &&
                     *                2. Tracking sign_up activity **/
                    val userObject = JSONObject()
                    userObject.put("USER_NAME", name)
                    userObject.put("USER_CONTACT", contact)
                    userObject.put("USER_EMAIL", email)
                    userObject.put("SIGN_UP_DATE", currentDate)

                    mixpanel?.people?.identify(contact)
                    mixpanel?.track("userRegistering_userDetails", userObject)
                    Timber.e("successfully logging in-> ${String(data)}")
                } else {
                    Timber.e("Something happened... $code +  $data  + ${String(data)}")
//                    Toast.makeText(this, "Something went Awfully bad", Toast.LENGTH_LONG).show()
                    logWithStaticAPI()
                }
            }
        }
    }

    private fun createUserWithEmailAndPassword(email: String, contact: String) {
        firebaseAuth!!.createUserWithEmailAndPassword(email, contact).addOnCompleteListener { task -> //progressBar.setVisibility(View.GONE);
            if (task.isSuccessful) {
                Toast.makeText(applicationContext,
                        "Registered Successfully!",
                        Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainScreen::class.java))
                finish()
            } else {
                if (task.exception is FirebaseAuthUserCollisionException) {
                    Toast.makeText(applicationContext,
                            "Already Registered!",
                            Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext,
                            Objects.requireNonNull(task.exception)?.message,
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun logWithStaticAPI() {
        val mainActPreferences = getSharedPreferences(ummoUserPreferences, mode)
        val userName = mainActPreferences.getString("USER_NAME", "")
        val userEmail = mainActPreferences.getString("USER_EMAIL", "")
        Sentry.addBreadcrumb("User made an action")
        val user = User()
        user.email = userEmail
        user.username = userName
        Sentry.setUser(user)
        try {
            unsafeMethod()
            Timber.e("logWithStaticAPI, unsafeMethod")
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }

    private fun unsafeMethod() {
        throw UnsupportedOperationException("This needs attention!")
    }

    private fun launchHomeScreen() {

        prefManager?.isFirstTimeLaunch = false
        Timber.e("launchHomeScreen: No Extras")
        startActivity(Intent(this, MainScreen::class.java))
        finish()
    }

    companion object {
        private const val ummoUserPreferences = "UMMO_USER_PREFERENCES"
        private const val mode = Activity.MODE_PRIVATE
    }
}