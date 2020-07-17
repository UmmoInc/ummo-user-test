package xyz.ummo.user.ui

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import xyz.ummo.user.EditMyProfile
import xyz.ummo.user.R
import xyz.ummo.user.Register
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.delegate.Logout
import xyz.ummo.user.delegate.PublicService
import xyz.ummo.user.delegate.PublicServiceData
import xyz.ummo.user.ui.fragments.*
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel
import xyz.ummo.user.ui.fragments.profile.ProfileFragment
import java.util.*

class MainScreen : AppCompatActivity(), ProfileFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private val fragment: Fragment? = null

//    private var drawer: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var navigationHeader: View? = null
    private var navHeaderUserName: TextView? = null
    private var navHeaderUserEmail: TextView? = null
    private var toolbar: Toolbar? = null
    private var messageIconButton: ImageView? = null
    private var circularProgressBarButton: ProgressBar? = null
    var logoutLayout: LinearLayout? = null
        private set

    private var anyServiceInProgress = false
    private var serviceProgress = 0
    private var mAuth: FirebaseAuth? = null


    // flag to load home fragment when user presses back key
    private val shouldLoadHomeFragOnBackPress = true
    private var mHandler: Handler? = null
    private val mode = Activity.MODE_PRIVATE
    private val delegatedServiceEntity = DelegatedServiceEntity()

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

        /*
        * Starting DelegatedServiceFragment
        * */

        val startFragmentExtra: Int = intent.getIntExtra("OPEN_DELEGATED_SERVICE_FRAG", 0)

        Log.e(TAG, "StartingFragment->$startFragmentExtra")

        if (startFragmentExtra == 1) {
            Log.e(TAG, "Starting DelegatedServiceFrag!")
            val delegatedServiceFragment = DelegatedServiceFragment()
            val delegatedProductId = intent.extras!!.getString("DELEGATED_PRODUCT_ID")
            val serviceAgentId = intent.extras!!.getString("SERVICE_AGENT_ID")
            var progress = ArrayList<String>();
            try {
                progress = listFromJSONArray(JSONArray(intent.extras!!.getString("progress")))
            } catch (jse: JSONException) {
                Log.e("ISSUE with progress", jse.toString())
            }

            val bundle = Bundle()
            val serviceId = intent.extras!!.getString("SERVICE_ID")
            bundle.putString("SERVICE_ID", serviceId)
            bundle.putString("SERVICE_AGENT_ID", serviceAgentId)
            bundle.putString("DELEGATED_PRODUCT_ID", delegatedProductId)
//            bundle.putString("DELEGATED_PRODUCT_ID", intent.extras!!.getString("DELEGATED_PRODUCT_ID"))
            delegatedServiceFragment.arguments = bundle
            val delegatedServiceViewModel = ViewModelProvider(this).get(DelegatedServiceViewModel::class.java)

            delegatedServiceEntity.serviceId = serviceId!!
            delegatedServiceEntity.delegatedProductId = delegatedProductId!!
            delegatedServiceEntity.serviceAgentId = serviceAgentId
            delegatedServiceEntity.serviceProgress = progress

//                delegatedServiceEntity.serviceProgress = serviceProgress //TODO: add real progress
            Log.e(xyz.ummo.user.delegate.TAG, "Populating ServiceEntity: Agent->${delegatedServiceEntity.serviceAgentId}; ProductModel->${delegatedServiceEntity.delegatedProductId}")
            delegatedServiceViewModel.insertDelegatedService(delegatedServiceEntity)

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame, delegatedServiceFragment)
            fragmentTransaction.commit()
            // return
        } else {

            object : PublicService(this) {
                override fun done(data: List<PublicServiceData>, code: Number) {
                    if (code == 200)
                        loadHomeFragment(data)
                    //Do something with list of services
                }
            }
        }

        mAuth = FirebaseAuth.getInstance()

        val mainActPrefs = getSharedPreferences(ummoUserPreferences, mode)

        val userNamePref = mainActPrefs.getString("USER_NAME", "")
        val userEmailPref = mainActPrefs.getString("USER_EMAIL", "")

        Timber.e("Username-> $userNamePref")

//        logoutClick() //TODO: to reconsider implementation

        //initialise  the toolbar icons message icon and circular progress bar icon
        messageIconButton = findViewById(R.id.message_icon_button)
        circularProgressBarButton = findViewById(R.id.circular_progressbar_btn)

        circularProgressBarButton!!.progress = serviceProgress

        mHandler = Handler()

//        drawer = findViewById(R.id.drawer_layout)
//        val toggle = ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
//        drawer!!.addDrawerListener(toggle)
//        toggle.syncState()

//        navigationView = findViewById(R.id.nav_view)
//        navigationView!!.setNavigationItemSelectedListener(this)

//        navigationHeader = navigationView!!.getHeaderView(0)
//        navHeaderUserName = navigationHeader!!.findViewById(R.id.navHeaderUsername)
//        navHeaderUserEmail = navigationHeader!!.findViewById(R.id.navHeaderEmail)

//        navHeaderUserName!!.text = userNamePref
//        navHeaderUserEmail!!.text = userEmailPref

        // initializing navigation menu
//        setUpNavigationView()

        if (savedInstanceState == null) {
            navItemIndex = 0

            CURRENT_TAG = TAG_HOME

        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        bottomNavigation.selectedItemId = R.id.nav_home
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

    private fun displaySelectedScreen(itemId: Int) {
        var selectedFragment: Fragment? = null

        var data: List<PublicServiceData>

        when (itemId) {
            R.id.nav_home -> selectedFragment = HomeFragment()
            R.id.nav_profile -> selectedFragment = ProfileFragment()
//            R.id.nav_payment_methods -> selectedFragment = PaymentMethodsFragment()
//            R.id.nav_service_history -> selectedFragment = ServiceHistoryFragment()
            R.id.nav_delegated_service -> selectedFragment = DelegatedServicesFragment()
        }

        if (selectedFragment != null) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.frame, selectedFragment)
            fragmentTransaction.commit()
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.closeDrawers()
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

        Log.e(TAG, "Navigation Item Selected: $id")

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

    override fun onFragmentInteraction(uri: Uri) {
        //you can leave it empty
    }

    private fun loadHomeFragment(data: List<PublicServiceData>) {
        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
//        if (supportFragmentManager.findFragmentByTag(CURRENT_TAG) != null) {
//            drawer!!.closeDrawers()
//            return
//        }
        val mPendingRunnable = Runnable {
            // update the main content by replacing fragments
            val fragment = getHomeFragment(data)
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                    android.R.anim.fade_out)
            fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG)
            fragmentTransaction.commitAllowingStateLoss()
        }

        // If mPendingRunnable is not null, then add to the message queue

        mHandler!!.post(mPendingRunnable)

        //Closing drawer on item click
//        drawer!!.closeDrawers()

        // refresh toolbar menu
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

            /**
             * Temporarily stashing these items for the time being
             **/
            /*2 -> {
                // payment methods fragment
                val paymentMethodsFragment = PaymentMethodsFragment()
                messageIconButton!!.visibility = View.GONE
                circularProgressBarButton!!.visibility = View.GONE
                title = "Payment Method"

                return paymentMethodsFragment
            }

            3 -> {
                // service history fragment
                return ServiceHistoryFragment()
            }*/

            4 -> {
                // legal terms fragment
                mixpanel?.track("delegatedServiceTapped_navDrawer")

                return DelegatedServicesFragment()
            }

            else -> return HomeFragment(data)
        }
    }

    /*private fun setUpNavigationView() {
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView!!.setNavigationItemSelectedListener { menuItem ->

            displaySelectedScreen(menuItem.itemId)
            //Checking if the item is in checked state or not, if not make it in checked state
            menuItem.isChecked = !menuItem.isChecked
            menuItem.isChecked = true

            // loadHomeFragment(data);

            true
        }

        val actionBarDrawerToggle = object : ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

        }

        //Setting the actionbarToggle to drawer layout
        drawer?.setDrawerListener(actionBarDrawerToggle)

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState()
    }*/

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val mixpanel = MixpanelAPI.getInstance(applicationContext,
                resources.getString(R.string.mixpanelToken))

        when (item.itemId) {

            R.id.navigation_home -> {
                val homeFragment = HomeFragment()
                openFragment(homeFragment)

                mixpanel?.track("homeTapped_bottomNav")
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_assigned -> {
                val delegatedServiceFragment = DelegatedServicesFragment()
                openFragment(delegatedServiceFragment)

                mixpanel?.track("getAssigned_bottomNav")

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_profile-> {
                val profileFragment = ProfileFragment()
                openFragment(profileFragment)

                mixpanel?.track("profile_bottomNav")

                return@OnNavigationItemSelectedListener true
            }
        }
        false
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

            R.id.id_number -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your ID Number"
            }

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

    fun finishEditProfile(view: View) {

        val fragment = ProfileFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment, "TAG_PROFILE")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    /*private fun logoutClick() {
        logoutLayout = findViewById(R.id.logoutLinear)
        logoutLayout!!.setOnClickListener {
            mAuth!!.signOut()
            val progress = ProgressDialog(this@MainScreen)
            progress.setMessage("Logging out...")
            progress.show()
            object : Logout(this@MainScreen) {
                override fun done() {

                    val mixpanel = MixpanelAPI.getInstance(this@MainScreen,
                            resources.getString(R.string.mixpanelToken))

                    mixpanel?.track("logoutTapped")
                    startActivity(Intent(applicationContext, SlideIntro::class.java))
                }
            }
        }
    }*/

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
        private val TAG_HOME = "home"
        private val TAG_PROFILE = "profile"
        private val TAG_PAYMENTS = "paymentMethods"
        private val TAG_SERVICE_HISTORY = "serviceHistory"
        private val TAG_LEGAL_TERMS = "legalTerms"
        var CURRENT_TAG = TAG_HOME
        lateinit var supportFM : FragmentManager

        // index to identify current nav menu item
        var navItemIndex = 0
        private const val ummoUserPreferences = "UMMO_USER_PREFERENCES"
        private const val TAG = "MainScreen"
    }
}
