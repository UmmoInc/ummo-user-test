package xyz.ummo.user.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
import xyz.ummo.user.MainActivity;
import xyz.ummo.user.utilities.PrefManager;
import xyz.ummo.user.R;
import xyz.ummo.user.delegate.Login;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alimuzaffar.lib.pin.PinEntryEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.hbb20.CountryCodePicker;
import com.onesignal.OSPermissionState;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SlideIntro extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnNext, signUpButton;
    private PrefManager prefManager;
    private static final String TAG = "SlideIntro";
    private EditText userNameField, userContactField, userEmailField;
    private static String userName, userContact, userEmail;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private boolean mVerificationInProgress = false;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private FirebaseAuth firebaseAuth;
    private int ONBOARDING_VAL, confirmationCode;
    private String phoneVerificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private PinEntryEditText confirmationCodeField;
    private ProgressBar registrationLoader, confirmationLoader, signUpLoader;
    private Boolean isClear = false;
    private Boolean isValid = false;
    private Boolean autoVerified = false;
    private RelativeLayout registrationLayout, progressLayout;
    private CountryCodePicker registrationCcp;
    private final int mode = Activity.MODE_PRIVATE;
    private final String ummoUserPreferences = "UMMO_USER_PREFERENCES";
    private TextView resendCodeButton;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Init Sentry
        Context context = this.getApplicationContext();
        String sentryDSN = getString(R.string.sentryDsn);
        Sentry.init(sentryDSN, new AndroidSentryClientFactory(context));

        //OneSignal
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        //Init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance();

//        myViewPagerAdapter.detectUserContact();
        // Checking for first time launch - before calling setContentView()
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            Log.e("Launch","Not first time");
            launchHomeScreen();
            finish();
        }
        Log.e("Launch","first time");
        setContentView(R.layout.activity_slide_intro);

        viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layoutDots);
        btnNext = findViewById(R.id.btn_next);


        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.register_slide,
                R.layout.contact_confirmation_slide,
                R.layout.sign_up_slide};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        //changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnNext.setOnClickListener(view -> {
            // checking for last page
            // if last page home screen will be launched
            handleSlideProgress(viewPager.getCurrentItem());

            int current = getItem(+1);
            if (current < layouts.length) {

                // move to next screen
                //viewPager.setCurrentItem(current);
                userEmailField = findViewById(R.id.userEmailEditText);
                userContact = registrationCcp.getFullNumberWithPlus();
                /*
                 * Checking viewPager index to capture user sign-up details & auth
                 * */

                if (viewPager.getCurrentItem() == 1) {
                    Log.e(TAG + " btnNext", "CurrentSlide indexCount->" + viewPager.getCurrentItem());
                    userName = myViewPagerAdapter.getUserName();
                    myViewPagerAdapter.contactCorrectnessCheck();

//                    Log.e(TAG + " btnNext", "is the number valid? ->" + isValid);

                    //TODO: Prevent user from proceeding with a wrong number

                    userContact = registrationCcp.getFullNumberWithPlus();
                    verifyPhoneNumber(userContact);

                } else if (viewPager.getCurrentItem() == 2){
                    Log.e(TAG + " btnNext", "CurrentSlide indexCount->" + viewPager.getCurrentItem());
                    handleSlideProgress(2);
                } else {
                    //Log.e(TAG + " btnNext", "CurrentSlide indexCount->" + viewPager.getCurrentItem());
                    userName = myViewPagerAdapter.getUserName();
                    myViewPagerAdapter.contactCorrectnessCheck();
                    if (userName.isEmpty() || userName.length() < 3) {
                        userNameField.setError("Your user name should have at least 3 letters!");
                        userNameField.requestFocus();
                        isClear = false;
                        return;
//                        myViewPagerAdapter.toggleRegistrationLayout();
                        //TODO: Prevent user from proceeding with incorrect details
                    } else if (userContact.isEmpty() || !isValid){
                        userContactField.setError("Please use a valid contact.");
                        userContactField.requestFocus();
                        return;
                    } else {
                        userNameField.setError(null);
                        sendCode(view);
                    }
                }

                viewPager.setCurrentItem(current);
                Log.e(TAG, "Just after setting the viewPager's currentItem, current is->"+current);
                if (viewPager.getCurrentItem() == 2) {
                    handleSlideProgress(2);
                    Log.e(TAG, "Manually setting currentSlide to 2");
                }

            } else {
                handleSlideProgress(2);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    public void signUpClick(){
        Log.e(TAG+" signUpClick", "This is inside the buttonClick");

        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneToken = status.getSubscriptionStatus().getPushToken();
        String onePlayerId = status.getSubscriptionStatus().getUserId();

        signUpButton = findViewById(R.id.sign_up_btn);
        signUpButton.setOnClickListener(v -> {
            userEmailField = findViewById(R.id.userEmailEditText);
            userEmail = myViewPagerAdapter.getUserEmail();
/*
            val status = OneSignal.getPermissionSubscriptionState()
            val oneToken = status.subscriptionStatus.pushToken
            playerId = status.subscriptionStatus.userId*/
            Log.e(TAG, "OneSignal PlayerId-> "+onePlayerId);

            if (!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
                userEmailField.setError("Please use a valid email...");
                userEmailField.requestFocus();
            } else if (userEmail.length() == 0) {
                userEmailField.setError("Please provide an email to proceed...");
                userEmailField.requestFocus();
                Log.e(TAG, "onLogin-1, EMAIL->"+userEmail);
            } else {

                ProgressDialog progress = new ProgressDialog(SlideIntro.this);
                progress.setMessage("Signing up...");
                progress.show();

                Log.e(TAG + " userSignUp-2", "This is inside the buttonClick>");
                new Login(getApplicationContext(), userName, userEmail, userContact, onePlayerId) {
                    @Override
                    public void done(@NotNull byte[] data, @NotNull Number code) {
                        if (code.equals(200)) {
                            launchHomeScreen();
                            SharedPreferences sharedPreferences = getSharedPreferences(ummoUserPreferences, mode);
                            SharedPreferences.Editor editor;
                            editor = sharedPreferences.edit();
                            editor.putBoolean("SIGNED_UP", true);
                            editor.putString("USER_NAME", userName);
                            editor.putString("USER_CONTACT", userContact);
                            editor.putString("USER_EMAIL", userEmail);
                            editor.putString("USER_PID",onePlayerId);
                            editor.apply();
                            progress.dismiss();
                            //startActivity();
                            Log.e(TAG + " onLogin-2", "successfully logging in->" + new String(data));
                        } else {
                            Log.e(TAG + " Error", "Something happened..." + code + " data " + new String(data));
                            Toast.makeText(SlideIntro.this, "Something went Awfully bad", Toast.LENGTH_LONG).show();
                            logWithStaticAPI();
                        }
                    }
                };

                firebaseAuth.createUserWithEmailAndPassword(userEmail, userContact).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "Registered Successfully!",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SlideIntro.this, MainActivity.class));
                            finish();
                        } else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getApplicationContext(),
                                        "Already Registered!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        Objects.requireNonNull(task.getException()).getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }

    private void unsafeMethod(){
        throw new UnsupportedOperationException("This needs attention!");
    }

    private void logWithStaticAPI(){

        SharedPreferences mainActPreferences = getSharedPreferences(ummoUserPreferences, mode);
        String userName = mainActPreferences.getString("USER_NAME","");
        String userEmail = mainActPreferences.getString("USER_EMAIL", "");

        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("Agent made an action")
                        .build());

        Sentry.getContext().setUser(
                new UserBuilder().setUsername(userName).setEmail(userEmail)
                        .build());

        Sentry.capture("Mic check...1,2!");

        try {
            unsafeMethod();
            Log.e(TAG, "logWithStaticAPI, unsafeMethod");
        } catch (Exception e){
            Sentry.capture(e);
        }
    }

    public void resendCode(View view) {

        String phoneNumber = registrationCcp.getFullNumberWithPlus();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks,
                resendToken);
        Log.e(TAG, "resendCode called on contact->"+phoneNumber);
    }

    private void handleSlideProgress(int sliderPosition){
//        Log.e(TAG,"handleSlideProgress Position->"+sliderPosition);

        switch (layouts[sliderPosition]){
            case R.layout.register_slide:
                //handle userName and number
                Log.e(TAG, "handleSlideProgress - currently on registerSlide-> "+sliderPosition);
                break;

            case R.layout.contact_confirmation_slide:
                //handle userContact confirmation
                Log.e(TAG, "handleSlideProgress - currently on codeConfirmationSlide-> "+sliderPosition);
                break;
            case R.layout.sign_up_slide:
                Log.e(TAG+" userSignUp-1", "This is outside the buttonClick>");
                signUpClick();
                break;
            default:
                // I dont know where you are here
                Log.e(TAG, "handleSlideProgress - Well, this is awkward!-> "+sliderPosition);
        }
    }

    public void sendCode(View view){
        userContact = registrationCcp.getFullNumberWithPlus();
        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                userContact,
                60,
                TimeUnit.SECONDS,
                this,
                verificationCallbacks);
        Log.e(TAG, "sendCode called on contact->"+userContact);

        mVerificationInProgress = true;
    }

    // [START sign_in_with_phone]
    private void  signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //Sign in success, update UI with signed-in user info
                            FirebaseUser firebaseAgent = Objects.requireNonNull(task.getResult()).getUser();
                            Log.e(TAG, "signInWithCredential: Success! Agent->"+firebaseAgent);
                            autoVerified = true;
                            // [START_EXCLUDE]
                            //TODO: Handle UX for verification
                            Snackbar.make(findViewById(android.R.id.content), "Auto Verified",
                                    Snackbar.LENGTH_SHORT).show();

                            /*Thread verifier = new Thread() {
                                public void run() {
                                    try {
                                    sleep(5000);
                                } catch (InterruptedException e) {
                                        Log.e(TAG, "verifierException->"+e);
                                    }
                                }
                            };
                            verifier.run();*/

                            //TODO: Improve UX (not ideal)
                            viewPager.setCurrentItem(2,true);
                            handleSlideProgress(2);
                            //progress.dismiss();

                        } else {
                            // Sign in failed, display a message & update the UI
                            Log.e(TAG, "signInWithCredential: Failure", task.getException());

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                // [START_EXCLUDE silent]
                                Snackbar.make(findViewById(android.R.id.content), "Verification failed: "+ task.getException(),
                                        Snackbar.LENGTH_SHORT).show();
                                // [END_EXCLUDE]
                            }
                            // [START_EXCLUDE silent]
                            // Update UI
//                            updateUI(STATE_SIGNIN_FAILED);
                            // [END_EXCLUDE]
                        }
                    }
                });
    }
    // [END sign_in_with_phone]


    private void setUpVerificationCallbacks() {
        //[START initialize_auth]
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.e(TAG, "onVerificationCompleted:" + credential);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                //updateUI(STATE_VERIFY_SUCCESS, credential);
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.e(TAG, "onVerificationFailed", e);
                // [START_EXCLUDE silent]
                mVerificationInProgress = false;
                // [END_EXCLUDE]

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    userContactField.setError("Invalid phone number!");
                    // [END_EXCLUDE]
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show();
                    // [END_EXCLUDE]
                }
                // Show a message and update the UI
                // [START_EXCLUDE]
                //updateUI(STATE_VERIFY_FAILED);
                // [END_EXCLUDE]
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.e(TAG, "onCodeSent: verificaitonID -> " + verificationId);
                Log.e(TAG, "onCodeSent: token-> " + token);

                // Save verification ID and resending token so we can use them later
                phoneVerificationId = verificationId;
                resendToken = token;

//                resendCodeButton.setEnabled(true);
            }
        };
        // [END phone_auth_callbacks]
    }

    public void verifyPhoneNumber(final String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                                        //User's userContact to verify
                60,                                            //Timeout duration
                TimeUnit.SECONDS,                              //Unit of timeout
                this,                                          //Activity
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.e(TAG, " onVerificationFailed", e);
                        logWithStaticAPI();
                        ONBOARDING_VAL = 0;
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Log.e(TAG + " onVerif.Fail", "Invalid Request!");
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            Log.e(TAG + " onVerif.Fail", "SMS Quota exceeded!");
                        }
                    }

                    @Override
                    public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verificationId, forceResendingToken);

//                        Log.e(TAG+" onCodeSent", "VerificationId ->"+verificationId);
                        ONBOARDING_VAL = 1;
                        mVerificationId = verificationId;
                        mResendToken = forceResendingToken;
                        Log.e(TAG + " onCodeSent", "Verification ID ->" + mVerificationId);
                        Log.e(TAG + " onCodeSent", "Token ->" + mResendToken);
                    }

                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        Log.e(TAG, " onVerificationCompleted:" + phoneAuthCredential);
//                        verifyPhoneNumber(phoneNumber); //TODO: Second call
                        ONBOARDING_VAL = 2;

                        /*firebaseAuth.signInWithCredential(phoneAuthCredential)
                                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()){
                                            FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                                            Log.e(TAG, "onVerificationCompleted ->"+ user);
                                        }
                                        else {
                                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                                Log.e(TAG, "onVerificationCompleted Exception, invalid code!"+);
                                            }
                                        }
                                    }
                                });*/
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(String codeAutoRetrievalT) {
                        super.onCodeAutoRetrievalTimeOut(codeAutoRetrievalT);
                        Log.e(TAG, " onCodeAutoRetrievalTimeOut ->" + codeAutoRetrievalT);
                        ONBOARDING_VAL = 3;
                    }

                });
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);

        if (dots.length == 2)
            dotsLayout.setVisibility(View.GONE);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    public void launchLoginActivity(View view){
        startActivity(new Intent(SlideIntro.this, LoginActivity.class));
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        Log.e(TAG, "launchLogInScreen successfully!"+view);
        finish();
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(SlideIntro.this, MainScreen.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == layouts.length - 1) {
                // last page. make button text to GOT IT
                //btnNext.setText(getString(R.string.start));
                btnNext.setVisibility(View.GONE);
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public boolean textViewSet(TextView textView) {

        return !textView.getText().equals("");
    }

    public void handlingDeepLinks() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        Uri deepLink = null;

                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }

                        Log.e(TAG + " handlingDLs", " onSuccess Deep Link ->" + deepLink);
                    }
                })
                .addOnFailureListener(this, e -> Log.e(TAG + " handlingDLs", " onFailure ->" + e));
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        MyViewPagerAdapter() {
        }

        String getUserNumber() {
            if (userContactField != null) {
                return userContactField.getText().toString();
            } else {
                return "";
            }
        }

        String getUserName() {
            if (userNameField != null) {
                return userNameField.getText().toString();
            } else {
                return "";
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public String getUserCode() {
            if (confirmationCodeField != null) {
                return Objects.requireNonNull(confirmationCodeField.getText()).toString();
            } else {
                return "";
            }
        }

        String getUserEmail() {
            if (userEmailField != null) {
                Log.e(TAG,"getUserEmail - HAS!");
                return userEmailField.getText().toString();
            } else {
                Log.e(TAG,"getUserEmail - NO HAVE!");
                return "";
            }
        }

        void toggleRegistrationLayout() {
            if (!isClear) {
                registrationLayout.setVisibility(View.GONE);
                isClear = true;
            } else
                registrationLayout.setVisibility(View.VISIBLE);
        }

        void contactCorrectnessCheck() {
            registrationCcp.registerCarrierNumberEditText(userContactField);
            isValid = registrationCcp.isValidFullNumber();
//            Log.e(TAG, "contactCorrectnessCheck validity:->" + isValid);
//            Log.e(TAG, "contactCorrectnessCheck registrationCCP->" + registrationCcp.getFullNumberWithPlus());
        }

        /*@RequiresApi(api = Build.VERSION_CODES.M)
        void detectUserContact() {
            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
            String autoContact = telephonyManager.getLine1Number();
            Log.e(TAG+"","Detecting userContact->"+autoContact);
        }*/

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            userNameField = findViewById(R.id.userNameEditText);
            userContactField = findViewById(R.id.userContactEditText);
            confirmationCodeField = findViewById(R.id.confirmation_code);

            if (position == 2) {
                userEmailField = findViewById(R.id.userEmailEditText);
            }

//            registrationLoader = findViewById(R.id.registration_loader);
            confirmationLoader = findViewById(R.id.confirmation_loader);
            signUpLoader = findViewById(R.id.signUp_loader);

            registrationLayout = findViewById(R.id.registration_relative_layout);

            progressLayout = findViewById(R.id.progress_relative_layout);

            registrationCcp = findViewById(R.id.registration_ccp);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }
}
