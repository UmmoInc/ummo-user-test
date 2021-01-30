package xyz.ummo.user.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.pdf.PdfDocument
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
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.data.entity.ServiceProviderEntity
import xyz.ummo.user.databinding.ActivityMainScreenBinding
import xyz.ummo.user.databinding.AppBarMainScreenBinding
import xyz.ummo.user.databinding.InfoCardBinding
import xyz.ummo.user.delegate.*
import xyz.ummo.user.models.Info
import xyz.ummo.user.models.ServiceProviderData
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.fragments.pagesFrags.PagesFragment
import xyz.ummo.user.ui.fragments.pagesFrags.SavedServicesFragment
import xyz.ummo.user.ui.fragments.profile.ProfileFragment
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.broadcastreceivers.ConnectivityReceiver
import xyz.ummo.user.utilities.eventBusEvents.*
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
    private var sharedPrefNewSession: Boolean = false

    private val profileEntity = ProfileEntity()
    private var profileViewModel: ProfileViewModel? = null

    /** View Binding for Main Screen and App Bar **/
    private lateinit var mainScreenBinding: ActivityMainScreenBinding
    private lateinit var appBarBinding: AppBarMainScreenBinding
    private lateinit var infoCardBinding: InfoCardBinding

    /** Welcome Dialog introducing User to Ummo **/
    private val appId = 11867
    private val apiKey = "2dzwMEoC3CB59FFu28tvXODHNtShmtDVopoFRqCtkD0hukYlsr5DqWacviLG9vXA"
    private val connectivityReceiver = ConnectivityReceiver()

    /** [EventBus] Event-values for Service Actions: UP-VOTE, DOWN-VOTE, COMMENT, BOOKMARK **/
    private var serviceBookmarkJSONObject = JSONObject()

    /** Date-time values for tracking events **/
    private lateinit var simpleDateFormat: SimpleDateFormat
    private var currentDate: String = ""

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        currentDate = simpleDateFormat.format(Date())

        /** Initializing view binders **/
        mainScreenBinding = ActivityMainScreenBinding.inflate(layoutInflater)
        appBarBinding = AppBarMainScreenBinding.inflate(layoutInflater)
        infoCardBinding = DataBindingUtil.setContentView(this, R.layout.info_card)

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
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /** Starting DelegatedServiceFragment **/
        startFragmentExtra = intent.getIntExtra("OPEN_DELEGATED_SERVICE_FRAG", 0)

        //checkForAndLaunchDelegatedFragment()

        mAuth = FirebaseAuth.getInstance()

        if (savedInstanceState == null) {
            navItemIndex = 0
            CURRENT_TAG = TAG_HOME
        }

        /** Getting Shared Pref Values for the user - to use in various scenarios to follow**/
        sharedPrefUserName = mainScreenPrefs.getString("USER_NAME", "")!!
        sharedPrefUserEmail = mainScreenPrefs.getString("USER_EMAIL", "")!!
        sharedPrefUserContact = mainScreenPrefs.getString("USER_CONTACT", "")!!
        sharedPrefNewSession = mainScreenPrefs.getBoolean("NEW_SESSION", false)

        Timber.e("NEW SESSION -> $sharedPrefNewSession")
        if (sharedPrefNewSession) {
            welcomeUserAboard()
        }

        getAndStoreUserInfoLocally()

        /** Instantiating the Feedback function from the `feedback_icon`**/
        val feedbackIcon = findViewById<ActionMenuItemView>(R.id.feedback_icon)
        feedbackIcon.setOnClickListener {
            feedback()
        }

        /** Instantiating the Bottom Navigation View **/
        val bottomNavigation: BottomNavigationView = mainScreenBinding.bottomNav
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        bottomNavigation.selectedItemId = R.id.bottom_navigation_home

//        checkForSocketConnection()

        getServiceProviderData()

    }

    private fun welcomeUserAboard() {
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        val introDialogBuilder = MaterialAlertDialogBuilder(this)
        introDialogBuilder.setTitle("Welcome to Ummo's Beta Test").setIcon(R.drawable.logo)

        val introDialogView = LayoutInflater.from(this)
                .inflate(R.layout.intro_dialog_layout, null)

        introDialogBuilder.setView(introDialogView)

        introDialogBuilder.setPositiveButton("I'm in") { dialogInterface, i ->
            Timber.e("USER IS IN!!!")
            val pagesFragment = PagesFragment()
            openFragment(pagesFragment)
            val editor = mainScreenPrefs.edit()
            editor.putBoolean("NEW_SESSION", false).apply()

            /** [MixpanelAPI] Tracking when the User first experiences Ummo **/
            val welcomeEventObject = JSONObject()
            welcomeEventObject.put("EVENT_DATE_TIME", currentDate)
            mixpanel?.track("welcomePromptUser_userConfirmation", welcomeEventObject)
        }

        introDialogBuilder.show()
    }

    override fun onStart() {
        super.onStart()
        /** [NetworkStateEvent-2] Registering the Connectivity Broadcast Receiver -
         * to monitor the network state **/
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(connectivityReceiver, intentFilter)

        /** [NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)
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
            //TODO: replace with HomeAffairsFragment
//            val serviceCentreFragment = ServiceCentresFragment()
            //openFragment(serviceCentreFragment)
        }
    }

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

    @Subscribe
    fun onServiceUpvoted(serviceUpvoteServiceEvent: UpvoteServiceEvent) {
        if (serviceUpvoteServiceEvent.serviceUpvote!!)
            showSnackbarBlue("Service Upvoted", -1)
    }

    @Subscribe
    fun onServiceDownvoted(serviDownvoteServiceEvent: DownvoteServiceEvent) {
        if (serviDownvoteServiceEvent.serviceDownvote!!)
            showSnackbarRed("Service Downvoted", -1)
    }

    @Subscribe
    fun onServiceBookmarkedEvent(serviceBookmarkedEvent: ServiceBookmarkedEvent) {
        Timber.e("SERVICE-BOOK-MARKED-EVENT -> ${serviceBookmarkedEvent.serviceId}")
        Timber.e("SERVICE-BOOK-MARKED-EVENT -> ${serviceBookmarkedEvent.serviceBookmarked}")

        val bookmarkingServicesList = serviceViewModel?.getServicesList()

        for (i in bookmarkingServicesList?.indices!!) {
            if (serviceBookmarkedEvent.serviceId.equals(bookmarkingServicesList[i].serviceId)) {
                serviceEntity.bookmarked = serviceBookmarkedEvent.serviceBookmarked
                //serviceViewModel?.updateService(serviceEntity)
                Timber.e("BOOK MARKING SERVICE -> ${serviceEntity.serviceId}: ${serviceEntity.bookmarked}")

                if (serviceBookmarkedEvent.serviceBookmarked!!)
                    showSnackbarYellow("Service Bookmarked", -1)
                else
                    showSnackbarYellow("Service removed from your bookmarks", -1)

            }
        }
    }

    override fun onStop() {
        super.onStop()
        /** [NetworkStateEvent-4] Unregistering the Connectivity Broadcast Receiver - app is in the background,
         * so we don't need to stay online (for NOW) **/
        unregisterReceiver(connectivityReceiver)

        /** [NetworkStateEvent-1] Register for EventBus events **/
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
    }

    fun feedback() {
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

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
                    Timber.e("Feedback Error: Data -> ${String(data)}")
                }
            }
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
                Timber.e("Going to SERVICE-PROVIDERS FRAG")
//                val serviceCentreFragment = ServiceCentresFragment()
                val pagesFragment = PagesFragment()
                openFragment(pagesFragment)

                val homeEventObject = JSONObject()
                homeEventObject.put("EVENT_DATE_TIME", currentDate)
                mixpanel?.track("bottomNavigation_homeTapped", homeEventObject)

                return@OnNavigationItemSelectedListener true
            }

            R.id.bottom_navigation_service -> {

                /** Modify info card **/
                /* infoCardBinding.infoBodyTextView.text = "Congratulations, you have a service running."

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

                 mixpanel?.track("getService_bottomNav")*/

                supportActionBar?.title = "Your Service Bookmarks"

                val savedServicesFragment = SavedServicesFragment()
                openFragment(savedServicesFragment)

                val bookmarkEventObject = JSONObject()
                bookmarkEventObject.put("EVENT_DATE_TIME", currentDate)
                mixpanel?.track("bottomNavigation_bookmarksTapped", bookmarkEventObject)

                return@OnNavigationItemSelectedListener true
            }

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

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
    }

    /** The following section is reserved for fetching, rendering & storing Service Providers and
     * the services they're associated with.
     * We're using these values for the PagesFragment - I find it suitable doing all of this here,
     * instead of on the PagesFragment itself (this fragment shouldn't worry about this kind of work
     * simply focus on hosting the three fragments under it **/

    /** This function fetches service-providers && #decomposes them with
     * `decomposeServiceProviderData(arrayList)`
     * TODO: since its a network operation, it needs to be moved away from the UI to another class**/
    private fun getServiceProviderData() {

        object : GetServiceProvider(this) {

            override fun done(data: List<ServiceProviderData>, code: Number) {

                if (code == 200) {
                    serviceProviderData.addAll(data)
                    Timber.e(" GETTING SERVICE PROVIDER DATA ->%s", serviceProviderData)

                    decomposeServiceProviderData(serviceProviderData)

                } else {
                    Timber.e("No PublicService READY!")
                }
            }
        }
    }

    /** The `decomposeServiceProviderData` function takes an arrayList of #ServiceProviderData;
     * then, for each serviceProvider, we store that data with `storeServiceProviderData` **/
    private fun decomposeServiceProviderData(mServiceProviderData: ArrayList<ServiceProviderData>) {
        Timber.e("DECOMPOSING SERVICE PROVIDER DATA -> $mServiceProviderData")
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        for (i in 0 until mServiceProviderData.size) {
            Timber.e("SERVICE-PROVIDER-DATA i->[$i] -> ${mServiceProviderData[i]}")
            /** Storing [serviceProviderData] below  **/
            storeServiceProviderData(mServiceProviderData[i])

            /** When getting services by serviceProvider, we often encounter a bug that jumbles up
             * the services' destination: e.g., `Passport Service` will sometimes get stored under
             * the `Revenue` tab.
             * We might have to try wrapping them up before saving them **/
            Timber.e("SERVICE-PROVIDER-NAME -> ${mServiceProviderData[i].serviceProviderName}")

            when (mServiceProviderData[i].serviceProviderName) {
                "Ministry Of Home Affairs" -> {
                    getServicesFromServerByServiceProviderId(mServiceProviderData[i].serviceProviderId)
                }
                "Ministry Of Finance" -> {
                    getServicesFromServerByServiceProviderId(mServiceProviderData[i].serviceProviderId)
                }
                "Ministry Of Commerce" -> {
                    getServicesFromServerByServiceProviderId(mServiceProviderData[i].serviceProviderId)
                }
            }

//            getServicesFromServerByServiceProviderId(mServiceProviderData[i].serviceProviderId)
//            getServicesFromServerByServiceProviderId("5faab29cacc12a05daa75b42")
        }
    }

    private fun storeServiceProviderData(mSingleServiceProviderData: ServiceProviderData) {
        /*serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)*/

        serviceProviderEntity.serviceProviderId = mSingleServiceProviderData.serviceProviderId
        serviceProviderEntity.serviceProviderName = mSingleServiceProviderData.serviceProviderName
        serviceProviderEntity.serviceProviderDescription = mSingleServiceProviderData.serviceProviderDescription
        serviceProviderEntity.serviceProviderContact = mSingleServiceProviderData.serviceProviderContact
        serviceProviderEntity.serviceProviderEmail = mSingleServiceProviderData.serviceProviderEmail
        serviceProviderEntity.serviceProviderAddress = mSingleServiceProviderData.serviceProviderAddress

        Timber.e("STORING SERVICE PROVIDER DATA [ID]-> ${serviceProviderEntity.serviceProviderId}")
        Timber.e("STORING SERVICE PROVIDER DATA [NAME] -> ${serviceProviderEntity.serviceProviderName}")
        serviceProviderViewModel?.addServiceProvider(serviceProviderEntity)
    }

    /** This function gets services from a given service provider (via serviceProviderID).
     * Likewise, it needs to be moved to a different class that handles network requests **/
    private fun getServicesFromServerByServiceProviderId(serviceProviderId: String) {

        object : GetServicesByServiceProviderId(this, serviceProviderId) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    try {
                        val servicesArray = JSONArray(String(data))

                        Timber.e("SERVICES-ARRAY -> $servicesArray")

                        for (i in 0 until servicesArray.length()) {
                            serviceObject = servicesArray.getJSONObject(i)

                            Timber.e("SERVICE-ASSIGNED [$i] -> $serviceObject")
                            captureServicesByServiceProvider(serviceObject)
                        }

                    } catch (jse: JSONException) {
                        Timber.e("FAILED TO GET SERVICES -> $jse")
                    }
                }
            }
        }
    }

    private fun getAllServicesFromServer() {
        object : GetAllServices(this) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    val allServices = JSONArray(String(data))
                    Timber.e("GETTING ALL SERVICES -> $allServices")

                    for (i in 0 until allServices.length()) {
                        serviceObject = allServices.getJSONObject(i)
                        Timber.e("GETTING ALL SERVICES [$i] -> $serviceObject")
                    }
                } else {
                    Timber.e("ERROR GETTING ALL SERVICES -> $code")
                }
            }
        }
    }

    private fun captureServicesByServiceProvider(mServiceObject: JSONObject) {

        val serviceViews = 0 //13
        Timber.e("TESTING SERVICE-DATA-> $mServiceObject")

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
        delegatable = mServiceObject/*.getJSONObject("service_requirements")
                */.getBoolean("delegatable")

        /** [SERVICE-ASSIGNMENT: 6]
         * 1. Declaring $serviceCost value
         * 2. TODO: Assigning $serviceCost value to service JSON value **/
        val serviceCost = mServiceObject.getString("service_cost")

        /** [SERVICE-ASSIGNMENT: 7]
         * 1. Declaring $serviceDocuments values
         * 2. TODO: Assigning $serviceDocuments value from service JSON value **/
        val serviceDocumentsJSONArray: JSONArray = mServiceObject/*.getJSONObject("service_requirements") //7
                */.getJSONArray("service_documents")
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
        var notUsefulCount = 0

        /** [SERVICE-ASSIGNMENT: 10]
         * 1. Declaring $upVote value
         * 2. TODO: Assigning $upVote value from service JSON value **/
        var usefulCount = 0

        /** [SERVICE-ASSIGNMENT: 11]
         * 1. Declaring $serviceComments values
         * 2. TODO: Assigning $serviceComments value from service JSON value **/
        val commentsJSONArray = mServiceObject.getJSONArray("service_comments")
        val commentsArrayList = ArrayList(listOf<String>())
        for (k in 0 until commentsJSONArray.length()) {
            commentsArrayList.add(commentsJSONArray.getString(k))
            Timber.e("COMMENT-INIT -> ${commentsArrayList.size}!")
        }

        /** [SERVICE-ASSIGNMENT: 12]
         * 1. Declaring $serviceShares value
         * 2. TODO: Stash $serviceShares value; replace with SAVE **/
        val serviceShares = 0

        //serviceCost = service.getString("service_cost")

        /** [SERVICE-ASSIGNMENT: 13]
         * 1. Declaring $serviceUpdates values
         * 2. TODO: parse through serviceUpdates & get values for enumerated values ["UPVOTE", etc] **/
        val serviceUpdatesJSONArray = mServiceObject.getJSONArray("service_updates")
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
        }

        /** [SERVICE-ASSIGNMENT: 14]
         * 1. Declaring $serviceProvider value
         * 2. Assigning $serviceProvider value to service JSON value **/
        val serviceProvider: String = mServiceObject.getString("service_provider") //14
        Timber.e("SAVING SERVICE BY SERVICE-PROVIDER -> $serviceProvider")

        serviceEntity.serviceId = serviceId //0
        serviceEntity.serviceName = serviceName //1
        serviceEntity.serviceDescription = serviceDescription //2
        serviceEntity.serviceEligibility = serviceEligibility //3
        serviceEntity.serviceCentres = serviceCentresArrayList //4
        serviceEntity.delegatable = delegatable //5
        serviceEntity.serviceCost = serviceCost //6
        serviceEntity.serviceDocuments = serviceDocumentsArrayList //7
        serviceEntity.serviceDuration = serviceDuration //8
        serviceEntity.notUsefulCount = notUsefulCount //9
        serviceEntity.usefulCount = usefulCount //10
        serviceEntity.serviceComments = commentsArrayList //11
        serviceEntity.commentCount = commentsArrayList.size //11
        serviceEntity.serviceViews = serviceViews //12
        serviceEntity.serviceShares = serviceShares //13
        serviceEntity.serviceProvider = serviceProvider //14
//        serviceEntity.bookmarked = bookmarked //15

        /** 1) Checking #serviceProviderId;
         *  2) Save each service entity by serviceProviderId **/

        val serviceProviders: List<ServiceProviderEntity>? = serviceProviderViewModel
                ?.getServiceProviderList()
        Timber.e("SERVICE-PROVIDERS-CHECK -> $serviceProviders")

        serviceViewModel?.addService(serviceEntity)
        Timber.e("SAVING SERVICE -> ${serviceEntity.serviceId} FROM -> ${serviceEntity.serviceProvider}")
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

            //TODO: replace this process with equivalent conversion
            /*object : PublicService(this) {
                override fun done(data: List<PublicServiceData>, code: Number) {
                    if (code == 200) {

                        val serviceCentreFragment = ServiceCentresFragment()
                        //openFragment(serviceCentreFragment)
                    }

                    //Timber.e("PUBLIC SERVICE DATA -> $data")
                    //Do something with list of services
                }
            }*/
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

    private fun showSnackbarBlue(message: String, length: Int) {
        /**
         * Length is 0 for Snackbar.LENGTH_LONG
         *  Length is -1 for Snackbar.LENGTH_SHORT
         *  Length is -2 for Snackbar.LENGTH_INDEFINITE
         *  **/
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.ummo_4))

        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
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
        val snackbar = Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.quantum_yellow700))

        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.textSize = 14F
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun showSnackbarRed(message: String, length: Int) {
        val bottomNav = findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(this@MainScreen.findViewById(android.R.id.content), message, length)
        snackbar.setTextColor(resources.getColor(R.color.quantum_googred600))
        val textView = snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
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
        private const val mode = Activity.MODE_PRIVATE
        private const val ummoUserPreferences: String = "UMMO_USER_PREFERENCES"
    }

}
