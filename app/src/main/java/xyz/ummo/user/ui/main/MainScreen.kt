package xyz.ummo.user.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupieViewHolder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.*
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.ActivityMainScreenBinding
import xyz.ummo.user.databinding.AppBarMainScreenBinding
import xyz.ummo.user.databinding.DelegationIntroCardBinding
import xyz.ummo.user.models.ServiceProviderData
import xyz.ummo.user.ui.fragments.categories.ServiceCategories
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.fragments.profile.ProfileFragment
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.*
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.*
//import xyz.ummo.user.utilities.oneSignal.UmmoNotificationOpenedHandler.Companion.OPEN_DELEGATION
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainScreen : AppCompatActivity() {

    private var serviceObject = JSONObject()
    private val serviceEntity = ServiceEntity()
    private var homeAffairsServiceEntities = ServiceEntity()
    private var revenueServiceEntities = ServiceEntity()
    private var commerceServiceEntities = ServiceEntity()
    private var serviceViewModel: ServiceViewModel? = null
    private var serviceProviderViewModel: ServiceProviderViewModel? = null
    private var serviceProviderEntity = ServiceProviderEntity()
    private var serviceProviderData: ArrayList<ServiceProviderData> = ArrayList()

    private var startFragmentExtra: Int = 0
    private var toolbar: Toolbar? = null

    private var feedbackIcon: ImageView? = null
//    private var circularProgressBarButton: ProgressBar? = null

    private var mAuth: FirebaseAuth? = null

    private val delegatedServiceEntity = DelegatedServiceEntity()

    /**Values for launching DelegatedServiceFragment**/
    var bundle = Bundle()
    var serviceId = ""
    var delegatedProductId = ""
    var serviceAgentId = ""
    var progress: ArrayList<String> = ArrayList()

    /** User Preferences & VM **/
    private var sharedPrefUserName: String = ""
    private var sharedPrefUserContact: String = ""
    private var sharedPrefUserEmail: String = ""
    private var sharedPrefNewSession: Boolean = false

    private val profileEntity = ProfileEntity()
    private var profileViewModel: ProfileViewModel? = null

    /** View Binding for Main Screen and App Bar **/
    private lateinit var mainScreenBinding: ActivityMainScreenBinding
    private lateinit var appBarBinding: AppBarMainScreenBinding
    private lateinit var delegationIntroCardBinding: DelegationIntroCardBinding

    private lateinit var badge: BadgeDrawable
    private var serviceUpdated = false

    private var delegatedServiceId: String? = null
    private var delegationId: String? = null
    private var agentId: String? = null

    private lateinit var mixpanel: MixpanelAPI

    /** Welcome Dialog introducing User to Ummo **/
    private val appId = 11867
    private val apiKey = "2dzwMEoC3CB59FFu28tvXODHNtShmtDVopoFRqCtkD0hukYlsr5DqWacviLG9vXA"
    private val connectivityReceiver = ConnectivityReceiver()

    /** [EventBus] Event-values for Service Actions: UP-VOTE, DOWN-VOTE, COMMENT, BOOKMARK **/
    private var serviceBookmarkJSONObject = JSONObject()

    /** Date-time values for tracking events **/
    private lateinit var simpleDateFormat: SimpleDateFormat
    private var currentDate: String = ""

    /** Setting up MainViewModel **/
    private var mainViewModel: MainViewModel? = null

    /** SharedPref Editor **/
    private lateinit var editor: SharedPreferences.Editor

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkingForDelegatedServiceFromRoom()

        Timber.e("ON_CREATE")
//        bundle = intent.getBundleExtra(VIEW_SOURCE)!!

        if (!bundle.isEmpty)
            Timber.e("VIEW_SOURCE -> $bundle")

        /** Init MainViewModel **/
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        /** Setting up Socket Worker from MainViewModel **/
        mainViewModel!!.socketConnect()

        /** Setting up Service Worker from MainViewModel **/
        mainViewModel!!.serviceHandler()

        /** Setting up User Worker from MainViewModel **/
        mainViewModel!!.userHandler()

        /** Setting up Agent Worker from MainViewModel **/
        mainViewModel!!.agentHandler()

        /** Locking screen orientation to [ActivityInfo.SCREEN_ORIENTATION_PORTRAIT] **/
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        currentDate = simpleDateFormat.format(Date())

        /** Initializing view binders **/
        mainScreenBinding = ActivityMainScreenBinding.inflate(layoutInflater)
        appBarBinding = AppBarMainScreenBinding.inflate(layoutInflater)
        delegationIntroCardBinding =
            DataBindingUtil.setContentView(this, R.layout.delegation_intro_card)

        val view = mainScreenBinding.root

        setContentView(view)

        /** Initializing ServiceProviderViewModel **/
        serviceProviderViewModel = ViewModelProvider(this)
            .get(ServiceProviderViewModel::class.java)

        /** Initializing ServiceViewModel **/
        serviceViewModel = ViewModelProvider(this)
            .get(ServiceViewModel::class.java)

        toolbar = appBarBinding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Ummo"
        supportFM = supportFragmentManager

        mainScreenPrefs = this.getSharedPreferences(ummoUserPreferences, mode)
        editor = mainScreenPrefs.edit()
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /** Starting DelegatedServiceFragment **/
        startFragmentExtra = intent.getIntExtra("OPEN_DELEGATED_SERVICE_FRAG", 0)

        //checkForAndLaunchDelegatedFragment()

        mixpanel = MixpanelAPI.getInstance(
            applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        mAuth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {
            navItemIndex = 0
            CURRENT_TAG = TAG_HOME
        }

        /** Getting Shared Pref Values for the user - to use in various scenarios to follow**/
        sharedPrefUserName = mainScreenPrefs.getString(USER_NAME, "")!!
        sharedPrefUserEmail = mainScreenPrefs.getString(USER_EMAIL, "")!!
        sharedPrefUserContact = mainScreenPrefs.getString(USER_CONTACT, "")!!
        sharedPrefNewSession = mainScreenPrefs.getBoolean(NEW_SESSION, false)

        if (sharedPrefNewSession) {
            welcomeUserAboard()
            notifyUserToCheckInbox()
        }

        getAndStoreUserInfoLocally()

        /** Checking User Email verifier **/
        verifyEmail()

        /** Instantiating the Feedback function from the `feedback_icon`**/
        val feedbackIcon = findViewById<ActionMenuItemView>(R.id.feedback_icon)
        feedbackIcon.setOnClickListener {
            feedback()
        }

        val userSupportIcon = findViewById<ActionMenuItemView>(R.id.user_support)
        userSupportIcon.setOnClickListener {
            mixpanel.track("supportCentre_launched")
            userSupport()
        }

        /** Instantiating the Bottom Navigation View **/
        val bottomNavigation: BottomNavigationView = mainScreenBinding.bottomNav
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        badge = bottomNavigation.getOrCreateBadge(R.id.bottom_navigation_delegates)

        if (bottomNavigation.selectedItemId == R.id.bottom_navigation_delegates) {
            badge.isVisible = false
        }

        showBadge()
//        getDynamicLinks()
        bottomNavigation.selectedItemId = R.id.bottom_navigation_home
//        checkForSocketConnection()
        getServiceProviderData()

        val openDelegation = intent.extras?.getInt(OPEN_DELEGATION)
        val delegationState = intent.extras?.getString(DELEGATION_STATE)
        val delegatedServiceFragment = DelegatedServiceFragment()

        if (openDelegation == 1) {
            openFragment(DelegatedServiceFragment())
            Timber.e("$DELEGATION_STATE -> $delegationState")
        }
    }

    private fun getDynamicLinks() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent).addOnSuccessListener { pendingDynamicLinkData ->
                val deepLink: Uri? = pendingDynamicLinkData.link
                Timber.e("Show Dynamic Link -> $deepLink")
            }.addOnFailureListener { e -> Timber.e("Error getting Dynamic Link -> $e") }
    }

    private fun showBadge() {
        if (serviceUpdated)
            badge.isVisible = true
    }

    private fun getServiceProviderData() {
        object : GetServiceProvider(this) {
            override fun done(data: List<ServiceProviderData>, code: Number) {
                if (code == 200) {
                    var serviceProvider: ServiceProviderData

                    for (i in data.indices) {
                        serviceProvider = data[i]
                        Timber.e("SERVICE-PROVIDER -> $serviceProvider")
                    }
                } else {
                    Timber.e("NO SERVICE PROVIDERS FOUND -> $code")
                }
            }
        }
    }

    private fun welcomeUserAboard() {

        val welcomeEventObject = JSONObject()
        welcomeEventObject.put("EVENT_DATE_TIME", currentDate)

        val introDialogBuilder = MaterialAlertDialogBuilder(this)
        introDialogBuilder.setTitle("Welcome to Ummo").setIcon(R.drawable.logo)

        val introDialogView = LayoutInflater.from(this)
            .inflate(R.layout.intro_dialog_layout, null)

        introDialogBuilder.setView(introDialogView)

        introDialogBuilder.setPositiveButton("Send Survey") { dialogInterface, i ->
            Timber.e("USER IS IN!!!")
//            val pagesFragment = PagesFragment()
            val serviceCategories = ServiceCategories()
            openFragment(serviceCategories)

            editor.putBoolean(NEW_SESSION, false).apply()

            /** [MixpanelAPI] Tracking when the User first experiences Ummo **/

            mixpanel.track("welcomePromptUser_sendSurvey", welcomeEventObject)

            showSnackbarBlue("Thank you. We'll send it later today :)", 0)
        }

        introDialogBuilder.setNegativeButton("No thanks") { dialogInterface, i ->
            editor.putBoolean(NEW_SESSION, false).apply()

            mixpanel.track("welcomePromptUser_dontSendSurvey", welcomeEventObject)

        }

        introDialogBuilder.setOnDismissListener {
            editor.putBoolean("NEW_SESSION", false).apply()
        }

        introDialogBuilder.show()
    }

    private fun notifyUserToCheckInbox() {

        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                showSnackbarYellow("Please check your email inbox for verification", 0)
//                editor.putBoolean(EMAIL_REMINDER_SENT, true)
            }
        }

        timer.start()
    }

    private fun checkingForDelegatedServiceFromRoom() {
        val delegatedServiceViewModel = ViewModelProvider(this)
            .get(DelegatedServiceViewModel::class.java)
        val countOfDelegatedService = delegatedServiceViewModel.getCountOfDelegatedServices()

        if (countOfDelegatedService == 0)
            return
        else {
            delegatedServiceId = intent.getBundleExtra(DELEGATED_SERVICE_ID).toString()
            delegationId = intent.getBundleExtra(DELEGATION_ID).toString()
            agentId = intent.getBundleExtra(AGENT_ID).toString()

            Timber.e("TAKING US TO DELEGATION-FRAG")
            bundle.putString(DELEGATED_SERVICE_ID, delegatedServiceId)
            bundle.putString(AGENT_ID, agentId)
            bundle.putString(DELEGATION_ID, delegationId)

            showSnackbarBlue("Taking you to your service", -1)
            val delegatedServiceFragment = DelegatedServiceFragment()
//            delegatedServiceFragment.arguments = bundle
            openFragment(delegatedServiceFragment)
        }

    }

    override fun onStart() {
        super.onStart()
        Timber.e("ON_START")
        checkingForDelegatedServiceFromRoom()
        /** [NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver -
         * to monitor the network state **/
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)

        /** [NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)
    }

    @Subscribe
    fun onServicesReloaded(reloadingServicesEvent: ReloadingServicesEvent) {
        if (reloadingServicesEvent.reloadingServices == true) {
            showSnackbarBlue("Reloading services...", -1)
            getAllServicesFromServer()
        }
    }

    @Subscribe
    fun onServiceCategorySelection(loadingCategoryServicesEvent: LoadingCategoryServicesEvent) {
        if (loadingCategoryServicesEvent.loadingService == true) {
            showSnackbarBlue("Loading ${loadingCategoryServicesEvent.categoryLoading} services", -1)
        }
    }

    @Subscribe
    fun onVerifiedStateEvent(userVerificationEvent: UserVerificationEvent) {

        val sharedPreferences = (this).getSharedPreferences(ummoUserPreferences, mode)
        val editor = sharedPreferences!!.edit()

        val userVerified: Boolean? = userVerificationEvent.userVerified!!
        Timber.e("USER VERIFIED -> $userVerified")
        editor.putBoolean(EMAIL_VERIFIED, userVerified!!).apply()
    }

    @Subscribe
    fun onServiceStateChange(serviceUpdateEvents: ServiceUpdateEvents) {

        val sharedPreferences = (this).getSharedPreferences(ummoUserPreferences, mode)
        val editor = sharedPreferences!!.edit()

        when (serviceUpdateEvents.serviceObject.getString("status")) {
            "PENDING" -> {
                editor.putInt(SERVICE_STATE, 0).apply()
            }
            "STARTED" -> {
                editor.putInt(SERVICE_STATE, 1).apply()

            }
            "DELAYING" -> {
                editor.putInt(SERVICE_STATE, -1).apply()

            }
            "DONE" -> {
                editor.putInt(SERVICE_STATE, 2).apply()

            }
            "DELIVERED" -> {
                editor.putInt(SERVICE_STATE, 3).apply()

            }
        }
    }

    /** [NetworkStateEvent-3] Subscribing to the NetworkState Event (via EventBus) **/
    /*@Subscribe
    fun onNetworkStateEvent(networkStateEvent: NetworkStateEvent) {
        Timber.e("ON-EVENT -> ${networkStateEvent.noConnectivity}")

        */
    /** Toggling between network states && displaying an appropriate Snackbar **//*
        if (networkStateEvent.noConnectivity!!) {
            showSnackbarRed("Please check connection...", -2)
        } else {
            showSnackbarBlue("Connecting...", -1)
            */
    /** Reloading Service Centre Fragment **//*
            //TODO: replace with HomeAffairsFragment
//            val serviceCentreFragment = ServiceCentresFragment()
            //openFragment(serviceCentreFragment)
        }
    }*/

    @Subscribe
    fun onSocketStateEvent(socketStateEvent: SocketStateEvent) {
        if (!socketStateEvent.socketConnected!!) {
            showSnackbarRed("Can't reach Ummo network", -2)
        } else {
            showSnackbarBlue("Ummo network found...", -1)
//            val pagesFragment = PagesFragment()
            val serviceCategories = ServiceCategories()
//            openFragment(serviceCategories)
        }
    }

    @Subscribe
    fun onServiceSpecifiedEvent(serviceSpecifiedEvent: ServiceSpecifiedEvent) {
        if (!serviceSpecifiedEvent.specifiedEvent) {
            showSnackbarYellow("Please select your vehicle weight first", -1)
        }
    }

    @Subscribe
    fun onServiceUpdatedEvent(serviceUpdateEvents: ServiceUpdateEvents) {
        if (serviceUpdateEvents.serviceUpdatedEvent!!) {
            serviceUpdated = true
            val sharedPreferences = (this).getSharedPreferences(ummoUserPreferences, mode)
            val editor = sharedPreferences!!.edit()

            editor.putInt("SERVICE_STATE", 0).apply()
        }
    }

    @Subscribe
    fun onRatingSentEvent(ratingSentEvent: RatingSentEvent) {
        if (ratingSentEvent.ratingSent == true) {
            val editor: SharedPreferences.Editor
            val sharedPreferences = getSharedPreferences(ummoUserPreferences, mode)
            editor = sharedPreferences!!.edit()

            if (sharedPreferences.getInt(SERVICE_STATE, 0) == 3)
                editor.remove(SERVICE_STATE).apply()

            showSnackbarBlue("Thank you, your rating has been sent", -1)
        }
    }

    @Subscribe
    fun onDelegateStateEvent(delegateStateEvent: DelegateStateEvent) {
        if (delegateStateEvent.delegateStateEvent.equals(SERVICE_PENDING)) {
            showSnackbarYellow("Please wait for your other service to finish", -1)
        } else if (delegateStateEvent.delegateStateEvent.equals(CURRENT_SERVICE_PENDING)) {
            openFragment(DelegatedServiceFragment())
        }
    }

    @Subscribe
    fun onServiceUpvoted(serviceUpvoteServiceEvent: UpvoteServiceEvent) {
        if (serviceUpvoteServiceEvent.serviceUpvote!!)
            showSnackbarBlue("Service Upvoted", -1)
    }

    @Subscribe
    fun onServiceCommentedOnEvent(
        viewHolder: GroupieViewHolder,
        serviceCommentEvent: ServiceCommentEvent
    ) {
        Timber.e("SERVICE-COMMENTED-ON-EVENT -> ${serviceCommentEvent.serviceName}")
        Timber.e("SERVICE-COMMENTED-ON-EVENT -> ${serviceCommentEvent.serviceCommentedOn}")

        if (serviceCommentEvent.serviceCommentedOn!!) {
            showSnackbarBlue(
                "Thank you for helping improve ${serviceCommentEvent.serviceName}.",
                -1
            )
        }
    }

    @Subscribe
    fun onPaymentTermsConfirmed(paymentTermsEvent: ConfirmPaymentTermsEvent) {
        if (paymentTermsEvent.paymentTermsConfirmed == false)
            showSnackbarYellow("Please confirm Payment Terms first", -1)
    }

    @Subscribe
    fun onCardClosedEvent(cardDismissedEvent: CardDismissedEvent) {
        if (cardDismissedEvent.cardDismissed == true) {
            showSnackbarBlue("Card closed", -1)
        }
    }

    @Subscribe
    fun onServiceDownvoted(serviceDownvoteServiceEvent: DownvoteServiceEvent) {
        if (serviceDownvoteServiceEvent.serviceDownvote!!)
            showSnackbarRed("Service Downvoted", -1)
    }

    @Subscribe
    fun onServiceBookmarkedEvent(serviceBookmarkedEvent: ServiceBookmarkedEvent) {
        Timber.e("SERVICE-BOOK-MARKED-EVENT -> ${serviceBookmarkedEvent.serviceName}")
        Timber.e("SERVICE-BOOK-MARKED-EVENT -> ${serviceBookmarkedEvent.serviceBookmarked}")

        if (serviceBookmarkedEvent.serviceBookmarked!!)
            showSnackbarYellow("Saving ${serviceBookmarkedEvent.serviceName} offline", -1)
//        else
//            showSnackbarYellow("Service removed from your bookmarks", -1)
//        val bookmarkingServicesList = serviceViewModel?.getServicesList()

        /*for (i in bookmarkingServicesList?.indices!!) {
            if (serviceBookmarkedEvent.serviceName.equals(bookmarkingServicesList[i].serviceId)) {
                serviceEntity.bookmarked = serviceBookmarkedEvent.serviceBookmarked
                //serviceViewModel?.updateService(serviceEntity)
                Timber.e("BOOK MARKING SERVICE -> ${serviceEntity.serviceId}: ${serviceEntity.bookmarked}")

                if (serviceBookmarkedEvent.serviceBookmarked!!)
                    showSnackbarYellow("Service Bookmarked", -1)
                else
                    showSnackbarYellow("Service removed from your bookmarks", -1)
            }
        }*/
    }

    override fun onStop() {
        super.onStop()
        /** [NetworkStateEvent-4] Unregistering the Connectivity Broadcast Receiver - app is in the background,
         * so we don't need to stay online (for NOW) **/
        unregisterReceiver(connectivityReceiver)

        /** [NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        Timber.e("ON_RESUME")
        checkingForDelegatedServiceFromRoom()
    }

    fun feedback() {
        val mixpanel = MixpanelAPI.getInstance(
            applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        val feedbackDialogView = LayoutInflater.from(this)
            .inflate(R.layout.feedback_dialog, null)

        val feedbackDialogBuilder = MaterialAlertDialogBuilder(this)

        feedbackDialogBuilder.setTitle("Feedback")
            .setIcon(R.drawable.logo)
            .setView(feedbackDialogView)

        feedbackDialogBuilder.setPositiveButton("Submit") { dialogInterface, i ->
            val feedbackEditText =
                feedbackDialogView.findViewById<TextInputEditText>(R.id.feedbackEditText)
            val feedbackText = feedbackEditText.text?.trim().toString()

            Timber.e("Feedback Submitted-> $feedbackText")
            if (feedbackText.isNotEmpty()) {
                submitFeedback(feedbackText, sharedPrefUserContact)

                /** [MixpanelAPI] Tracking when the User first experiences Ummo **/
                val feedbackEventObject = JSONObject()
                feedbackEventObject.put("EVENT_DATE_TIME", currentDate)
                    .put("FEEDBACK", feedbackText)
                mixpanel?.track("feedback_submitted", feedbackEventObject)

            } else {
                showSnackbarRed("You forgot your feedback", -1)
                mixpanel?.track("feedback_cancelled")
            }

        }

        feedbackDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
            Timber.e("Feedback Cancelled")
        }

        feedbackDialogBuilder.show()
    }

    private fun userSupport() {
        val mixpanel = MixpanelAPI.getInstance(
            applicationContext,
            resources.getString(R.string.mixpanelToken)
        )

        val userSupportDialogView = LayoutInflater.from(this)
            .inflate(R.layout.user_support_dialog, null)

        val whatsAppImageView = userSupportDialogView.findViewById<ImageView>(R.id.chat_image_view)
        val whatsAppTextView = userSupportDialogView.findViewById<TextView>(R.id.chat_text_view)
        val callImageView = userSupportDialogView.findViewById<ImageView>(R.id.call_image_view)
        val callTextView = userSupportDialogView.findViewById<TextView>(R.id.call_text_view)

        val userDialogBuilder = MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.logo)
            .setView(userSupportDialogView)

        whatsAppImageView.setOnClickListener { launchWhatsApp() }
        whatsAppTextView.setOnClickListener { launchWhatsApp() }
        callImageView.setOnClickListener { launchPhoneDialer() }
        callTextView.setOnClickListener { launchPhoneDialer() }

        userDialogBuilder.show()

    }

    private fun launchWhatsApp() {

        val contact = "+26876804065"

        val url = "https://api.whatsapp.com/send?phone=$contact"
        try {
            val pm: PackageManager = this.packageManager
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
            mixpanel.track("supportCentre_whatsAppChatInitiated")
        } catch (e: PackageManager.NameNotFoundException) {
            showSnackbarYellow("WhatsApp not installed.", -1)
            e.printStackTrace()
        }
    }

    private fun launchPhoneDialer() {

        mixpanel.track("supportCentre_phoneDialerInitiated")

        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:+26876804065")
        startActivity(intent)
    }


    /** This function sends the feedback over HTTP Post by overriding `done` from #Feedback
     * It's used by #feedback **/
    private fun submitFeedback(feedbackString: String, userContact: String) {

        object : GeneralFeedback(this, feedbackString, userContact) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    Timber.e("Feedback Submitted -> ${String(data)}")
                    showSnackbarBlue("Thank you for your feedback :)", 0)
                } else {
                    Timber.e("Feedback Error: Code -> $code")
                    Timber.e("Feedback Error: Data -> ${String(data)}")
                }
            }
        }
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            val mixpanel = MixpanelAPI.getInstance(
                applicationContext,
                resources.getString(R.string.mixpanelToken)
            )

            when (item.itemId) {

                R.id.bottom_navigation_home -> {
                    supportActionBar?.title = "Ummo"

                    /** Modify info card **/
                    val serviceCategories = ServiceCategories()
                    openFragment(serviceCategories)

                    val homeEventObject = JSONObject()
                    homeEventObject.put("EVENT_DATE_TIME", currentDate)
                    mixpanel?.track("bottomNavigation_homeTapped", homeEventObject)

                    return@OnNavigationItemSelectedListener true
                }

                R.id.bottom_navigation_delegates -> {
                    val delegatedServiceFragment = DelegatedServiceFragment()
                    openFragment(delegatedServiceFragment)

                    badge.isVisible = false

                    val delegatedServiceEventObject = JSONObject()
                    delegatedServiceEventObject.put("EVENT_DATE_TIME", currentDate)
                    mixpanel?.track(
                        "bottomNavigation_delegatedServiceTapped",
                        delegatedServiceEventObject
                    )

                    return@OnNavigationItemSelectedListener true
                }

                /*R.id.bottom_navigation_service -> {

                    */
                /** Modify info card **//*
                *//* infoCardBinding.infoBodyTextView.text = "Congratulations, you have a service running."

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

                 mixpanel?.track("getService_bottomNav")*//*

                supportActionBar?.title = "Your Service Bookmarks"

                val savedServicesFragment = SavedServicesFragment()
                openFragment(savedServicesFragment)

                val bookmarkEventObject = JSONObject()
                bookmarkEventObject.put("EVENT_DATE_TIME", currentDate)
                mixpanel?.track("bottomNavigation_bookmarksTapped", bookmarkEventObject)

                return@OnNavigationItemSelectedListener true
            }*/

                R.id.bottom_navigation_profile -> {
                    val profileFragment = ProfileFragment()
                    openFragment(profileFragment)

                    val profileEventObject = JSONObject()
                    profileEventObject.put("EVENT_DATE_TIME", currentDate)
                    mixpanel?.track("bottomNavigation_profileTapped", profileEventObject)

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

        Timber.e("PROFILE ENTITY -> ${profileEntity.profileContact}")
    }

    private fun verifyEmail() {
        profileViewModel?.emailVerifiedEvent()
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }

    private fun getAllServicesFromServer() {
        object : GetAllServices(this) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    val allServices = JSONObject(String(data)).getJSONArray("payload")

                    for (i in 0 until allServices.length()) {
                        serviceObject = allServices.getJSONObject(i)
                        saveServicesLocally(serviceObject)
                    }
                } else {
                    Timber.e("ERROR GETTING ALL SERVICES -> $code")
                }
            }
        }
    }

    private fun saveServicesLocally(mServiceObject: JSONObject) {

        val serviceViews = 0 //13

        /** [SERVICE-ASSIGNMENT: 0]
         * 1. Declaring $serviceID value
         * 2. Assigning $serviceID value from service JSON value **/
        val serviceId: String = mServiceObject.getString("_id") //0

        /** [SERVICE-ASSIGNMENT: 1]
         * 1. Declaring $serviceName value
         * 2. Assigning $serviceName value from service JSON value **/
        val serviceName: String = mServiceObject.getString("service_name") //1

        /** [SERVICE-ASSIGNMENT: 2]
         * 1. Declaring $serviceDescription value
         * 2. Assigning $serviceDescription value from service JSON value **/
        val serviceDescription: String = mServiceObject.getString("service_description") //2

        /** [SERVICE-ASSIGNMENT: 3]
         * 1. Declaring $serviceEligibility value
         * 2. Assigning $serviceEligibility value from service JSON value **/
        val serviceEligibility: String = mServiceObject.getString("service_eligibility") //3

        /** [SERVICE-ASSIGNMENT: 4]
         * 1. Declaring $serviceCentres value
         * 2. Assigning $serviceCentres value to service JSON value **/
        val serviceCentresJSONArray: JSONArray = mServiceObject.getJSONArray("service_centres")
        val serviceCentresArrayList = ArrayList(listOf<String>())
        for (j in 0 until serviceCentresJSONArray.length()) {
            serviceCentresArrayList.add(serviceCentresJSONArray.getString(j))
        }

        /** [SERVICE-ASSIGNMENT: 5]
         * 1. Declaring $presenceRequired value
         * 2. Assigning $presenceRequired value from service JSON value **/
        val delegatable: Boolean?
        delegatable = mServiceObject.getBoolean("delegatable")

        /** [SERVICE-ASSIGNMENT: 6]
         * 1. Declaring $serviceCost value
         * 2. TODO: Assigning $serviceCost value to service JSON value **/
        val serviceCostJSONArray = mServiceObject.getJSONArray("service_cost")

        /** [SERVICE-ASSIGNMENT: 7]
         * 1. Declaring $serviceDocuments values
         * 2. TODO: Assigning $serviceDocuments value from service JSON value **/
        val serviceDocumentsJSONArray: JSONArray = mServiceObject.getJSONArray("service_documents")
        val serviceDocumentsArrayList = ArrayList(listOf<String>())
        for (k in 0 until serviceDocumentsJSONArray.length()) {
            serviceDocumentsArrayList.add(serviceDocumentsJSONArray.getString(k))
        }

        /** [SERVICE-ASSIGNMENT: 8]
         * 1. Declaring $serviceDuration value
         * 2. Assigning $serviceDuration value to service JSON value **/
        val serviceDuration: String = mServiceObject.getString("service_duration")

        /** [SERVICE-ASSIGNMENT: 9]
         * 1. Declaring $downVote value
         * 2. TODO: Assigning $downVote value from service JSON value **/
        var notUsefulCount = mServiceObject.getInt("not_useful_count")

        /** [SERVICE-ASSIGNMENT: 10]
         * 1. Declaring $upVote value
         * 2. TODO: Assigning $upVote value from service JSON value **/
        var usefulCount = mServiceObject.getInt("useful_count")

        /** [SERVICE-ASSIGNMENT: 11]
         * 1. Declaring $serviceComments values
         * 2. TODO: Assigning $serviceComments value from service JSON value **/
        val commentsJSONArray = mServiceObject.getJSONArray("service_comments")
        val commentsArrayList = ArrayList(listOf<String>())
        for (k in 0 until commentsJSONArray.length()) {
            commentsArrayList.add(commentsJSONArray.getString(k))
        }

        /** [SERVICE-ASSIGNMENT: 12]
         * 1. Declaring $serviceShares value
         * 2. TODO: Stash $serviceShares value; replace with SAVE **/
        val serviceShares = 0

        //serviceCost = service.getString("service_cost")

        /** [SERVICE-ASSIGNMENT: 13]
         * 1. Declaring $serviceUpdates values
         * 2. TODO: parse through serviceUpdates & get values for enumerated values ["UPVOTE", etc] **/
        /*val serviceUpdatesJSONArray = mServiceObject.getJSONArray("service_updates")
        val serviceUpdatesArrayList = ArrayList(listOf<String>())
        var serviceUpdateObject: JSONObject
        var updateType: String
        for (m in 0 until serviceUpdatesJSONArray.length()) {
//            Timber.e("SERVICE-UPDATES -> ${serviceUpdatesJSONArray[m]}")
            serviceUpdateObject = serviceUpdatesJSONArray.getJSONObject(m)
            updateType = serviceUpdateObject.getString("update_type")

            Timber.e("UPDATE-TYPE -> $updateType")

            when (updateType) {
                "THUMBS_UP" -> {
                    usefulCount += 1
                }
                "THUMBS_DOWN" -> {
                    notUsefulCount += 1
                }
            }

            Timber.e("[$m] $usefulCount UP-VOTES; $notUsefulCount DOWN-VOTES")
        }*/

        /** [SERVICE-ASSIGNMENT: 14]
         * 1. Declaring $serviceProvider value
         * 2. Assigning $serviceProvider value to service JSON value **/
        val serviceProvider: String = mServiceObject.getString("service_provider") //14

        //TODO: undo 8
/*
        serviceEntity.serviceId = serviceId //0
        serviceEntity.serviceName = serviceName //1
        serviceEntity.serviceDescription = serviceDescription //2
        serviceEntity.serviceEligibility = serviceEligibility //3
        serviceEntity.serviceCentres = serviceCentresArrayList //4
        serviceEntity.delegatable = delegatable //5
        serviceEntity.serviceCost = serviceCostJSONArray //6
        serviceEntity.serviceDocuments = serviceDocumentsArrayList //7
        serviceEntity.serviceDuration = serviceDuration //8
        serviceEntity.notUsefulCount = notUsefulCount //9
        serviceEntity.usefulCount = usefulCount //10
        serviceEntity.serviceComments = commentsArrayList //11
        serviceEntity.commentCount = commentsArrayList.size //11
        serviceEntity.serviceViews = serviceViews //12
        serviceEntity.serviceShares = serviceShares //13
        serviceEntity.serviceProvider = serviceProvider //14*/
//        serviceEntity.bookmarked = bookmarked //15

        Timber.e("SERVICE - ENTITY [NAME] -> ${serviceEntity.serviceName}")
        Timber.e("SERVICE - ENTITY [DESCR.] -> ${serviceEntity.serviceDescription}")
        Timber.e("SERVICE - ENTITY [DELEG.] -> ${serviceEntity.delegatable}")
        Timber.e("SERVICE - ENTITY [UPVOTE] -> ${serviceEntity.usefulCount}")
        Timber.e("SERVICE - ENTITY [DOWNVOTE] -> ${serviceEntity.notUsefulCount}")
        //TODO: undo 17
//        Timber.e("SERVICE - ENTITY [COST] -> ${serviceEntity.serviceCost}")

        if (serviceEntity.serviceId != null)
            serviceViewModel?.addService(serviceEntity)

//        Timber.e("SAVING SERVICE -> ${serviceEntity.serviceName}|| DELEGATABLE-ENTITY -> ${serviceEntity.delegatable} || OG: $delegatable")
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

    fun showSnackbarBlue(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))

        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun showSnackbarYellow(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.gold))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun showSnackbarRed(message: String, length: Int) {
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar =
            Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.orange_red))
        val textView =
            snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    companion object {

        // tags used to attach the fragments
        private const val TAG_HOME = "home"
        var CURRENT_TAG = TAG_HOME
        lateinit var supportFM: FragmentManager

        // index to identify current nav menu item
        var navItemIndex = 0
        private lateinit var mainScreenPrefs: SharedPreferences

    }

}
