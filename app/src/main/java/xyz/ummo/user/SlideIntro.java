package xyz.ummo.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import xyz.ummo.user.delegate.Login;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.hbb20.CountryCodePicker;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SlideIntro extends AppCompatActivity {

    private ViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnNext;
    private PrefManager prefManager;
    private static final String TAG = "SlideIntro";
    private EditText userNameField, userContactField, userEmailField;
    private String name, contact, email;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth firebaseAuth;
    private int ONBOARDING_VAL, confirmationCode;
    private PinView confirmationCodeField;
    private ProgressBar registrationLoader, confirmationLoader, signUpLoader;
    private Boolean isClear = false;
    private Boolean isValid = false;
    private RelativeLayout registrationLayout, progressLayout;
    private CountryCodePicker registrationCcp;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        myViewPagerAdapter.detectUserContact();
        // Checking for first time launch - before calling setContentView()
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }
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
            int current = getItem(+1);
            if (current < layouts.length) {
                // move to next screen
                viewPager.setCurrentItem(current);
                userEmailField = findViewById(R.id.userEmailEditText);

                /*
                 * Checking viewPager index to capture user sign-up details & auth
                 * */
                if (viewPager.getCurrentItem() == 1) {
                    Log.e(TAG + " btnNext", "User has entered contact!->" + viewPager.getCurrentItem());
                    name = myViewPagerAdapter.getUserName();
                    myViewPagerAdapter.contactCorrectnessCheck();

                    Log.e(TAG + " btnNext", "is the number valid? ->" + isValid);

                    //TODO: Prevent user from proceeding with a wrong number

                    /*Authenticating phone number
                     * Assigning the number retrieved from Country Code Picker (incl. '+268' country code
                     */
                    contact = registrationCcp.getFullNumberWithPlus();
                    verifyPhoneNumber(contact);
                    Log.e(TAG + " btnNext", "CCP retrieved contact ->" + contact);

                    if (name.isEmpty() || name.length() < 3) {
                        userNameField.setError("Your name should have at least 3 letters!");
                        isClear = false;
                        myViewPagerAdapter.toggleRegistrationLayout();
                        //TODO: Prevent user from proceeding with incorrect details
                    } else {
                        userNameField.setError(null);
                        isClear = true;
                        myViewPagerAdapter.toggleRegistrationLayout();
                    }

                }

            } else {
                email = myViewPagerAdapter.getUserEmail();
                if (email.length() == 0) return;
                new Login(this, name, email, contact) {
                    @Override
                    public void done(@NotNull byte[] data, @NotNull Number code) {
                        if (code.equals(200)) {
                            launchHomeScreen();
                            //startActivity();
                            Log.e("Result", new String(data));
                        } else {
                            Log.e("Error", "Something happened");
                            Toast.makeText(SlideIntro.this, "Something went Awfully bad", Toast.LENGTH_LONG).show();
                            //Show an error
                        }
                    }
                };

            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    public void verifyPhoneNumber(final String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                                        //User's contact to verify
                60,                                            //Timeout duration
                TimeUnit.SECONDS,                              //Unit of timeout
                this,                                          //Activity
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        Log.e(TAG, " onVerificationFailed", e);
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
//                        Log.e(TAG, " onVerificationCompleted:" + phoneAuthCredential);
                        verifyPhoneNumber(phoneNumber);
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
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
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
                btnNext.setText(getString(R.string.start));
                //btnSkip.setVisibility(View.GONE);
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
                Log.e("has", "Yes");
                return userEmailField.getText().toString();
            } else {
                Log.e("no email", "NOP");
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
            Log.e(TAG + "", "contactCorrectnessCheck validity:->" + isValid);
            Log.e(TAG + "", "contactCorrectnessCheck registrationCCP->" + registrationCcp.getFullNumberWithPlus());
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

            registrationLoader = findViewById(R.id.registration_loader);
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
