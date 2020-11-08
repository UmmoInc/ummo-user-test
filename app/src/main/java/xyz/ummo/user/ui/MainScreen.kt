package xyz.ummo.user.ui

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import xyz.ummo.user.EditMyProfile
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.databinding.ActivityMainScreenBinding
import xyz.ummo.user.databinding.AppBarMainScreenBinding
import xyz.ummo.user.databinding.InfoCardBinding
import xyz.ummo.user.delegate.Feedback
import xyz.ummo.user.delegate.PublicService
import xyz.ummo.user.models.Info
import xyz.ummo.user.models.PublicServiceData
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.fragments.pagesFrags.PagesFragment
import xyz.ummo.user.ui.fragments.profile.ProfileFragment
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.fragments.serviceCentres.ServiceCentresFragment
import xyz.ummo.user.utilities.eventBusEvents.NetworkStateEvent
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.SocketStateEvent

class MainScreen : AppCompatActivity() {

    private var startFragmentExtra: Int = 0
    private var toolbar: Toolbar? = null

    private var feedbackIcon: ImageView? = null
//    private var circularProgressBarButton: ProgressBar? = null

    private var anyServiceInProgress = false
    private var serviceProgress = 0
    private var mAuth: FirebaseAuth? = null

    private var mHandler: Handler? = null
    private val delegatedServiceEntity = DelegatedServiceEntity()

    /**Values for launching DelegatedServiceFragment**/
    val bundle = Bundle()
    var serviceId = ""
    var delegatedProductId = ""
    var serviceAgentId = ""
    var progress: ArrayList<String> = ArrayList()

    /** Shared Prefs **/
    private var sharedPrefServiceId: String = ""
    private var sharedPrefAgentId: String = ""
    private var sharedPrefProductId: String = ""

    /** User Preferences & VM **/
    private var sharedPrefUserName: String = ""
    private var sharedPrefUserContact: String = ""
    private var sharedPrefUserEmail: String = ""
    private val profileEntity = ProfileEntity()
    private var profileViewModel: ProfileViewModel? = null

    /** View Binding for Main Screen and App Bar **/
    private lateinit var mainScreenBinding: ActivityMainScreenBinding
    private lateinit var appBarBinding: AppBarMainScreenBinding
    private lateinit var infoCardBinding: InfoCardBinding

    private val appId = 11867
    private val apiKey = "2dzwMEoC3CB59FFu28tvXODHNtShmtDVopoFRqCtkD0hukYlsr5DqWacviLG9vXA"
    private val connectivityReceiver = ConnectivityReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Initializing view binders **/
        mainScreenBinding = ActivityMainScreenBinding.inflate(layoutInflater)
        appBarBinding = AppBarMainScreenBinding.inflate(layoutInflater)
        infoCardBinding = DataBindingUtil.setContentView(this, R.layout.info_card)

        val view = mainScreenBinding.root

        setContentView(view)

        var mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        toolbar = appBarBinding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Ummo"
        supportFM = supportFragmentManager

        mainScreenPrefs = this.getSharedPreferences(ummoUserPreferences, mode)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /** Starting DelegatedServiceFragment **/
        startFragmentExtra = intent.getIntExtra("OPEN_DELEGATED_SERVICE_FRAG", 0)

        checkForAndLaunchDelegatedFragment()

        mAuth = FirebaseAuth.getInstance()

        /**[NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)

//        logoutClick() //TODO: to reconsider implementation

        mHandler = Handler()

        if (savedInstanceState == null) {
            navItemIndex = 0

            CURRENT_TAG = TAG_HOME

        }

        /** Getting Shared Pref Values for the user - to use in various scenarios to follow **/
        sharedPrefUserName = mainScreenPrefs.getString("USER_NAME", "")!!
        sharedPrefUserEmail = mainScreenPrefs.getString("USER_EMAIL", "")!!
        sharedPrefUserContact = mainScreenPrefs.getString("USER_CONTACT", "")!!

        getAndStoreUserInfoLocally()

        /** Instantiating the Feedback function from the `feedback_icon`**/
        val feedbackIcon = findViewById<ActionMenuItemView>(R.id.feedback_icon)
        feedbackIcon.setOnClickListener {
            feedback()
        }

        /** Instantiating the Bottom Navigation View **/
        val bottomNavigation: BottomNavigationView = mainScreenBinding.bottomNav
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

//        checkForSocketConnection()

    }

    override fun onStart() {
        super.onStart()
        /** [NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver -
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
        } else {
            showSnackbarBlue("Connecting...", -1)
            /** Reloading Service Centre Fragment **/
            val serviceCentreFragment = ServiceCentresFragment()
            //openFragment(serviceCentreFragment)
        }
    }

    /** DO NOT DELETE!!!**/
    @Subscribe
    fun onSocketStateEvent(socketStateEvent: SocketStateEvent) {
        Timber.e("SOCKET-EVENT -> ${socketStateEvent.socketConnected}")

        if (!socketStateEvent.socketConnected!!) {
            showSnackbarRed("Can't reach Ummo network", -2)
        } else {
            showSnackbarBlue("Ummo network found...", -1)
            val pagesFragment = PagesFragment()
            openFragment(pagesFragment)
        }
    }

    override fun onStop() {
        super.onStop()
        /** [NetworkStateEvent-4] Unregistering the Connectivity Broadcast Receiver - app is in the background,
         * so we don't need to stay online (for NOW) **/
        unregisterReceiver(connectivityReceiver)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    fun feedback() {

        val feedbackDialogView = LayoutInflater.from(this)
                .inflate(R.layout.feedback_dialog, null)

        val feedbackDialogBuilder = MaterialAlertDialogBuilder(this)

        feedbackDialogBuilder.setTitle("Feedback")
                .setIcon(R.drawable.logo)
                .setView(feedbackDialogView)

        feedbackDialogBuilder.setPositiveButton("Submit") { dialogInterface, i ->
            val feedbackEditText = feedbackDialogView.findViewById<TextInputEditText>(R.id.feedbackEditText)
            val feedbackText = feedbackEditText.text?.trim().toString()

            Timber.e("Feedback Submitted-> $feedbackText")
            if (feedbackText.isNotEmpty()) {
                submitFeedback(feedbackText, sharedPrefUserContact)
            } else {
                showSnackbarRed("You forgot your feedback", -1)
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

        object : Feedback(this, feedbackString, userContact) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    Timber.e("Feedback Submitted -> ${String(data)}")
                    showSnackbarBlue("Thank you for your feedback :)", 0)
                } else {
                    Timber.e("Feedback Error: Code -> $code")
                    Timber.e("Feedback Error: Data -> ${String()}")
                }
            }
        }
    }

    private fun showSnackbarBlue(message: String, length: Int) {
        /** Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE**/
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor( resources.getColor(R.color.ummo_4))
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun showSnackbarRed(message: String, length: Int) {
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor( resources.getColor(R.color.quantum_googred600))
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun checkForAndLaunchDelegatedFragment() {
        Timber.e("StartFragment->$startFragmentExtra")

        if (startFragmentExtra == 1) {
            Timber.e("Starting DelegatedServiceFrag!")
            val delegatedServiceFragment = DelegatedServiceFragment()

            delegatedProductId = intent.extras?.getString("DELEGATED_PRODUCT_ID")!!
            serviceAgentId = intent.extras!!.getString("SERVICE_AGENT_ID")!!
            serviceId = intent.extras!!.getString("SERVICE_ID")!!
            Timber.e("SERVICE-ID -> $serviceId")

            bundle.putString("SERVICE_ID", serviceId)
            bundle.putString("SERVICE_AGENT_ID", serviceAgentId)
            bundle.putString("DELEGATED_PRODUCT_ID", delegatedProductId)

//            bundle.putString("DELEGATED_PRODUCT_ID", intent.extras!!.getString("DELEGATED_PRODUCT_ID"))
            delegatedServiceFragment.arguments = bundle
            val delegatedServiceViewModel = ViewModelProvider(this)
                    .get(DelegatedServiceViewModel::class.java)

            delegatedServiceEntity.serviceId = serviceId
            delegatedServiceEntity.delegatedProductId = delegatedProductId
            delegatedServiceEntity.serviceAgentId = serviceAgentId
            delegatedServiceEntity.serviceProgress = progress

//                delegatedServiceEntity.serviceProgress = serviceProgress //TODO: add real progress
            Timber.e("Populating ServiceEntity: Agent->${
                delegatedServiceEntity
                        .serviceAgentId
            }; ProductModel->${delegatedServiceEntity.delegatedProductId}")

            delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
            fragmentTransaction.commit()
            // return
        } else {

            object : PublicService(this) {
                override fun done(data: List<PublicServiceData>, code: Number) {
                    if (code == 200) {

                        val serviceCentreFragment = ServiceCentresFragment()
                        //openFragment(serviceCentreFragment)
                    }

                    Timber.e("PUBLIC SERVICE DATA -> $data")
                    //Do something with list of services
                }
            }
        }
    }

    private fun listFromJSONArray(arr: JSONArray): ArrayList<String> {
        return try {
            val tbr = ArrayList<String>()
            for (i in 0 until arr.length()) {
                tbr.add(arr.getString(i))
            }
            tbr
        } catch (e: JSONException) {
            ArrayList()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.top_app_bar, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.feedback_icon -> {
                Timber.e("FEEDBACK TAPPED!")
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        val delegatedServiceViewModel = ViewModelProvider(this)
                .get(DelegatedServiceViewModel::class.java)

        when (item.itemId) {

            R.id.bottom_navigation_home -> {
                supportActionBar?.title = "Ummo"

                /** Modify info card **/
//                infoCardBinding.infoBodyTextView.text = "Welcome to Ummo. Your time is important to us."
                infoCardBinding.info = Info("Welcome to Ummo", "Your time is important to us")
//                val homeFragment = HomeFragment()
                Timber.e("Going to SERVICE-CENTRES FRAG")
//                val serviceCentreFragment = ServiceCentresFragment()
                val pagesFragment = PagesFragment()
                openFragment(pagesFragment)

                mixpanel?.track("homeTapped_bottomNav")
                return@OnNavigationItemSelectedListener true
            }

            R.id.bottom_navigation_service -> {

                /** Modify info card **/
                infoCardBinding.infoBodyTextView.text = "Congratulations, you have a service running."

                sharedPrefServiceId = mainScreenPrefs.getString("SERVICE_ID", "")!!
                sharedPrefAgentId = mainScreenPrefs.getString("SERVICE_AGENT_ID", "")!!
                sharedPrefProductId = mainScreenPrefs.getString("DELEGATED_PRODUCT_ID", "")!!

                if (sharedPrefServiceId.isEmpty()) {
//                    launchDelegatedServiceWithoutArgs() TODO: figure out what causes the null
                    val bottomNav = findViewById<View>(R.id.bottom_nav)
                    val snackbar = Snackbar.make(this.findViewById(android.R.id.content), "No Services yet...", Snackbar.LENGTH_LONG)
                    snackbar.anchorView = bottomNav
                    snackbar.show()

                } else {
                    launchDelegatedServiceWithArgs(sharedPrefServiceId, sharedPrefAgentId, sharedPrefProductId)
                }

                mixpanel?.track("getService_bottomNav")

                return@OnNavigationItemSelectedListener true
            }

            R.id.bottom_navigation_profile -> {
                val profileFragment = ProfileFragment()
                openFragment(profileFragment)

                mixpanel?.track("profile_bottomNav")

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun getAndStoreUserInfoLocally() {
        /** Inserting ProfileModel info into ProfileEntity, then ProfileViewModel **/
        profileEntity.profileName = sharedPrefUserName
        profileEntity.profileContact = sharedPrefUserContact
        profileEntity.profileEmail = sharedPrefUserEmail
        profileViewModel?.insertProfile(profileEntity)
        Timber.e("PROFILE ENTITY -> $profileEntity")
    }

    private fun launchDelegatedServiceWithArgs(serviceId: String, agentId: String, productId: String) {
        val bundle = Bundle()
        bundle.putString("SERVICE_ID", serviceId)
        bundle.putString("DELEGATED_PRODUCT_ID", productId)
        bundle.putString("SERVICE_AGENT_ID", agentId)

        val progress = java.util.ArrayList<String>()
        val delegatedServiceEntity = DelegatedServiceEntity()
        val delegatedServiceViewModel = ViewModelProvider((this as FragmentActivity?)!!)
                .get(DelegatedServiceViewModel::class.java)

        delegatedServiceEntity.serviceId = serviceId
        delegatedServiceEntity.delegatedProductId = productId
        delegatedServiceEntity.serviceAgentId = agentId
        delegatedServiceEntity.serviceProgress = progress
        delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

        val fragmentActivity = this as FragmentActivity
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val delegatedServiceFragment = DelegatedServiceFragment()
        delegatedServiceFragment.arguments = bundle
        fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
        fragmentTransaction.commit()
    }

    private fun launchDelegatedServiceWithoutArgs() {
        val fragmentActivity = this as FragmentActivity
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val delegatedServiceFragment = DelegatedServiceFragment()
        delegatedServiceFragment.arguments = bundle
        fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
        fragmentTransaction.commit()
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }

    fun setAnyServiceInProgress(anyServiceInProgress: Boolean) {
        this.anyServiceInProgress = anyServiceInProgress
    }

    fun setServiceProgress(serviceProgress: Int) {
        this.serviceProgress = serviceProgress
    }

    fun goToEditProfile(view: View) {

        val textViewToEdit: TextView

        var textToEdit = " "
        var toolBarTitle = " "

        when (view.id) {

            R.id.profile_name -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your full name"
            }

            /*R.id.id_number -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your ID Number"
            }*/

            R.id.profile_contact -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your phone number"
            }

            R.id.profile_email -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your email"
            }
        }

        val myProfileFragment = ProfileFragment()
        val intent = Intent(this, EditMyProfile::class.java)
        val tag = myProfileFragment.tag
        intent.putExtra(EditMyProfile.CONST_TAG, tag)
        intent.putExtra("name", textToEdit)
        intent.putExtra("toolBarTitle", toolBarTitle)
        startActivity(intent)
    }

    fun finishEditProfile() {

        val fragment = ProfileFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment, "TAG_PROFILE")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        // tags used to attach the fragments
        private const val TAG_HOME = "home"
        var CURRENT_TAG = TAG_HOME
        lateinit var supportFM: FragmentManager

        // index to identify current nav menu item
        var navItemIndex = 0
        private lateinit var mainScreenPrefs: SharedPreferences
        private const val mode = Activity.MODE_PRIVATE
        private const val ummoUserPreferences: String = "UMMO_USER_PREFERENCES"
    }

    init {

    }
}
