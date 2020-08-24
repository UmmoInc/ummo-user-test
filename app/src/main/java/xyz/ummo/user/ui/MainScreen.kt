package xyz.ummo.user.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import xyz.ummo.user.EditMyProfile
import xyz.ummo.user.R
import xyz.ummo.user.Register
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.delegate.Logout
import xyz.ummo.user.delegate.PublicService
import xyz.ummo.user.models.PublicServiceData
import xyz.ummo.user.ui.fragments.HomeFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.fragments.profile.ProfileFragment
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.fragments.serviceCentres.ServiceCentresFragment
import java.util.*
import kotlin.collections.ArrayList

class MainScreen : AppCompatActivity(), /*ProfileFragment.OnFragmentInteractionListener,*/ NavigationView.OnNavigationItemSelectedListener {

    private var startFragmentExtra: Int = 0
    private var toolbar: Toolbar? = null
    private var messageIconButton: ImageView? = null
    private var circularProgressBarButton: ProgressBar? = null

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
    /** Shared Prefs**/
    private var sharedPrefServiceId: String = ""
    private var sharedPrefAgentId: String = ""
    private var sharedPrefProductId: String = ""
    /** User Preferences & VM **/
    private var sharedPrefUserName: String = ""
    private var sharedPrefUserContact: String = ""
    private var sharedPrefUserEmail: String = ""
    private val profileEntity = ProfileEntity()
    private var profileViewModel: ProfileViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        var mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Ummo"
        supportFM = supportFragmentManager
        //Log.e(TAG,"Getting USER_ID->"+new PrefManager(this).getUserId());

        mainScreenPrefs = this.getSharedPreferences(ummoUserPreferences, mode)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        /**
         * Starting DelegatedServiceFragment
         **/
        startFragmentExtra = intent.getIntExtra("OPEN_DELEGATED_SERVICE_FRAG", 0)

        checkForAndLaunchDelegatedFragment()

        getAndStoreUserInfoLocally()

        mAuth = FirebaseAuth.getInstance()

//        logoutClick() //TODO: to reconsider implementation

        //initialise  the toolbar icons message icon and circular progress bar icon
        messageIconButton = findViewById(R.id.message_icon_button)
        circularProgressBarButton = findViewById(R.id.circular_progressbar_btn)

        circularProgressBarButton!!.progress = serviceProgress

        mHandler = Handler()

        if (savedInstanceState == null) {
            navItemIndex = 0

            CURRENT_TAG = TAG_HOME

        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.nav_home

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
                        openFragment(serviceCentreFragment)
                    }
//                        loadHomeFragment(data)
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

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        Timber.e("Navigation Item Selected: $id")

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_profile) {

        } /*else if (id == R.id.nav_payment_methods) {

        } else if (id == R.id.nav_service_history) {

        }*/ else if (id == R.id.nav_delegated_service) {

        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    /*override fun onFragmentInteraction(uri: Uri) {
        //you can leave it empty
    }*/

    private fun loadHomeFragment(data: List<PublicServiceData>) {

        val mPendingRunnable = Runnable {
            // update the main content by replacing fragments
            val fragment = getHomeFragment(data)
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG)
            fragmentTransaction.commitAllowingStateLoss()
        }

        mHandler!!.post(mPendingRunnable)

        invalidateOptionsMenu()
    }

    private fun getHomeFragment(data: List<PublicServiceData>): Fragment {

        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        when (navItemIndex) {
            0 -> {
                // home
                title = "Ummo"
                messageIconButton!!.visibility = View.VISIBLE
                circularProgressBarButton!!.visibility = View.VISIBLE

                mixpanel?.track("homeTapped_navDrawer")
                return HomeFragment(data)
            }
            1 -> {
                // My ProfileModel
                val myProfileFragment = ProfileFragment()

                messageIconButton!!.visibility = View.GONE
                circularProgressBarButton!!.visibility = View.GONE
                title = "ProfileModel"

                mixpanel?.track("profileTapped_navDrawer")

                return myProfileFragment
            }

            4 -> {
                // legal terms fragment
                mixpanel?.track("delegatedServiceTapped_navDrawer")
                checkForAndLaunchDelegatedFragment()
                return DelegatedServiceFragment()
//                return DelegatedServicesFragment()
            }

            else -> return HomeFragment(data)
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        val delegatedServiceViewModel = ViewModelProvider(this)
                .get(DelegatedServiceViewModel::class.java)

        when (item.itemId) {

            R.id.navigation_home -> {
//                val homeFragment = HomeFragment()
                val serviceCentreFragment = ServiceCentresFragment()
                openFragment(serviceCentreFragment)

                mixpanel?.track("homeTapped_bottomNav")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_service -> {

                sharedPrefServiceId = mainScreenPrefs.getString("SERVICE_ID", "")!!
                sharedPrefAgentId = mainScreenPrefs.getString("SERVICE_AGENT_ID", "")!!
                sharedPrefProductId = mainScreenPrefs.getString("DELEGATED_PRODUCT_ID", "")!!

                sharedPrefUserName = mainScreenPrefs.getString("USER_NAME", "")!!
                sharedPrefUserEmail = mainScreenPrefs.getString("USER_EMAIL", "")!!
                sharedPrefUserContact = mainScreenPrefs.getString("USER_CONTACT", "")!!

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
            R.id.navigation_profile -> {
                val profileFragment = ProfileFragment()
                openFragment(profileFragment)

                mixpanel?.track("profile_bottomNav")

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun getAndStoreUserInfoLocally() {
        /** Getting User info from SharedPreferences **/
        sharedPrefUserName = mainScreenPrefs.getString("USER_NAME", "")!!
        sharedPrefUserEmail = mainScreenPrefs.getString("USER_EMAIL", "")!!
        sharedPrefUserContact = mainScreenPrefs.getString("USER_CONTACT", "")!!

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

    fun logout(view: View) {
        mAuth!!.signOut()
        val progress = ProgressDialog(this@MainScreen)
        progress.setMessage("Logging out...")
        progress.show()
        object : Logout(this) {
            override fun done() {
                startActivity(Intent(this@MainScreen, Register::class.java))
            }
        }
        // prefManager.unSetFirstTimeLaunch();
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
