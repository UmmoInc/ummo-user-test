package xyz.ummo.user

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

import xyz.ummo.user.delegate.Logout
import xyz.ummo.user.delegate.PublicServiceData
import xyz.ummo.user.fragments.HomeFragment
import xyz.ummo.user.fragments.LegalTermsFragment
import xyz.ummo.user.fragments.MyProfileFragment
import xyz.ummo.user.fragments.PaymentMethodsFragment
import xyz.ummo.user.fragments.ServiceHistoryFragment

import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class MainScreen : AppCompatActivity(), MyProfileFragment.OnFragmentInteractionListener, NavigationView.OnNavigationItemSelectedListener {

    private val fragment: Fragment? = null

    private var drawer: DrawerLayout? = null
    private var navigationView: NavigationView? = null
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        title = "Ummo"
        //Log.e(TAG,"Getting USER_ID->"+new PrefManager(this).getUserId());
        object : xyz.ummo.user.delegate.PublicService(this) {
            override fun done(data: List<PublicServiceData>, code: Number) {
                loadHomeFragment(data)
                //Do something with list of services
            }
        }

        mAuth = FirebaseAuth.getInstance()

        val mainActPrefs = getSharedPreferences(ummoUserPreferences, mode)

        val userNamePref = mainActPrefs.getString("USER_NAME", "")
        Log.e(TAG, "Username->" + userNamePref!!)

        logoutClick()

        //initialise  the toolbar icons message icon and circular progress bar icon
        messageIconButton = findViewById(R.id.message_icon_button)
        circularProgressBarButton = findViewById(R.id.circular_progressbar_btn)

        circularProgressBarButton!!.progress = serviceProgress

        mHandler = Handler()

        drawer = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.nav_view)
        navigationView!!.setNavigationItemSelectedListener(this)


        // initializing navigation menu
        setUpNavigationView()

        if (savedInstanceState == null) {
            navItemIndex = 0

            CURRENT_TAG = TAG_HOME

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

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_payment_methods) {

        } else if (id == R.id.nav_service_history) {

        } else if (id == R.id.nav_legal_terms) {

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
        if (supportFragmentManager.findFragmentByTag(CURRENT_TAG) != null) {
            drawer!!.closeDrawers()

            return
        }
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
        drawer!!.closeDrawers()

        // refresh toolbar menu
        invalidateOptionsMenu()
    }

    private fun getHomeFragment(data: List<PublicServiceData>): Fragment {
        when (navItemIndex) {
            0 -> {
                // home
                title = "Ummo"
                messageIconButton!!.visibility = View.VISIBLE
                circularProgressBarButton!!.visibility = View.VISIBLE

                return HomeFragment(data)
            }
            1 -> {
                // My Profile
                val myProfileFragment = MyProfileFragment()

                messageIconButton!!.visibility = View.GONE
                circularProgressBarButton!!.visibility = View.GONE
                title = "Profile"

                return myProfileFragment
            }

            2 -> {
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
            }

            4 -> {
                // legal terms fragment
                return LegalTermsFragment()
            }

            else -> return HomeFragment(data)
        }
    }

    private fun setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView!!.setNavigationItemSelectedListener { menuItem ->
            // This method will trigger on item Click of navigation menu
            //Check to see which item was being clicked and perform appropriate action
            when (menuItem.itemId) {
                //Replacing the main content with ContentFragment Which is our Inbox View;
                R.id.nav_home -> {
                    navItemIndex = 0
                    CURRENT_TAG = TAG_HOME
                }
                R.id.nav_profile -> {
                    navItemIndex = 1
                    CURRENT_TAG = TAG_PROFILE
                }
                R.id.nav_payment_methods -> {
                    navItemIndex = 2
                    CURRENT_TAG = TAG_PAYMENTS
                }
                R.id.nav_service_history -> {
                    navItemIndex = 3
                    CURRENT_TAG = TAG_SERVICE_HISTORY
                }
                R.id.nav_legal_terms -> {
                    navItemIndex = 4
                    CURRENT_TAG = TAG_LEGAL_TERMS
                }
                else -> navItemIndex = 0
            }

            //Checking if the item is in checked state or not, if not make it in checked state
            if (menuItem.isChecked) {
                menuItem.isChecked = false
            } else {
                menuItem.isChecked = true
            }
            menuItem.isChecked = true

            // loadHomeFragment(data);

            true
        }


        val actionBarDrawerToggle = object : ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            /* override fun onDrawerClosed(drawerView: View?) {
                 // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                 super.onDrawerClosed(drawerView)
             }

             override fun onDrawerOpened(drawerView: View?) {
                 // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                 super.onDrawerOpened(drawerView)
             }*/
        }

        //Setting the actionbarToggle to drawer layout
        drawer?.setDrawerListener(actionBarDrawerToggle)

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState()
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

            R.id.full_name -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your full name"
            }

            R.id.id_number -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your ID Number"
            }

            R.id.contact -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your phone number"
            }

            R.id.email -> {
                textViewToEdit = view.findViewById(view.id)
                textToEdit = textViewToEdit.text.toString()
                toolBarTitle = "Enter your email"
            }
        }

        val myProfileFragment = MyProfileFragment()
        val intent = Intent(this, EditMyProfile::class.java)
        val tag = myProfileFragment.tag
        intent.putExtra(EditMyProfile.CONST_TAG, tag)
        intent.putExtra("name", textToEdit)
        intent.putExtra("toolBarTitle", toolBarTitle)
        startActivity(intent)
    }

    fun finishEditProfile(view: View) {

        val fragment = MyProfileFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment, "TAG_PROFILE")
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun logoutClick() {
        logoutLayout = findViewById(R.id.logoutLinear)
        logoutLayout!!.setOnClickListener {
            mAuth!!.signOut()
            val progress = ProgressDialog(this@MainScreen)
            progress.setMessage("Logging out...")
            progress.show()
            object : Logout(this@MainScreen) {
                override fun done() {
                    startActivity(Intent(applicationContext, SlideIntro::class.java))
                }
            }
        }
    }

    fun logout(view: View) {
        mAuth!!.signOut()
        val progress = ProgressDialog(this@MainScreen)
        progress.setMessage("Logging out...")
        progress.show()
        object : Logout(this) {
            override fun done() {
                startActivity(Intent(this@MainScreen, SlideIntro::class.java))
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

        // index to identify current nav menu item
        var navItemIndex = 0
        private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
        private val TAG = "MainScreen"
    }
}
