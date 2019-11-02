package xyz.ummo.user.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.onesignal.OSPermissionSubscriptionState;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import xyz.ummo.user.R;
import xyz.ummo.user.delegate.Login;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "LoginActivity";
    private EditText emailLogin, contactLogin;
    private Button loginButton;
    private TextView toSignUpTextView;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser mCurrentUser;
    private String userId;
    private final int mode = Activity.MODE_PRIVATE;
    private final String loginPrefs = "UMMO_USER_PREFERENCES";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.editTextEmail);
        contactLogin = findViewById(R.id.editTextContact);
        loginButton = findViewById(R.id.buttonLogin);
        toSignUpTextView = findViewById(R.id.textViewSignup);
        loginProgress = findViewById(R.id.progressbar);

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        mAuth = FirebaseAuth.getInstance();
        Log.e(TAG + " onClick", "Implementing onClick cases...");

        switch (view.getId()){
            case R.id.buttonLogin:
                Log.e(TAG + " onClick", "Switching to ButtonLogin");
                userLogin(view);
                break;

            case R.id.textViewSignup:
                Log.e(TAG + " onClick", "Switching to SignUpTextView");
                launchSignUpActivity(view);
                break;

            default:
                break;
        }
    }

    public void launchSignUpActivity(View view){
        startActivity(new Intent(LoginActivity.this, SlideIntro.class));
        overridePendingTransition(R.anim.push_left_out, R.anim.push_left_out);
        finish();
    }

    public void userLogin(View view){
        String email = emailLogin.getText().toString().trim();
        String contact = contactLogin.getText().toString().trim();

        OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
        String oneToken = status.getSubscriptionStatus().getPushToken();
        String onePlayerId = status.getSubscriptionStatus().getUserId();

        if (email.isEmpty()){
            emailLogin.setError("Email is required.");
            emailLogin.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailLogin.setError("Please enter a valid email.");
            emailLogin.requestFocus();
            return;
        }

        if (contact.isEmpty()) {
            contactLogin.setError("Please enter your contact here.");
            contactLogin.requestFocus();
            return;
        }

        if (contact.length() <7){
            contactLogin.setError("Tip: Minimum length should be at least 7");
            contactLogin.requestFocus();
            return;
        }

        loginProgress.setVisibility(View.VISIBLE);

        //TODO: Consider security threats/risks in using email/contact combination
        mAuth.signInWithEmailAndPassword(email, contact) //Contact is acting as a password in this scenario
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        SharedPreferences loginPreferences = getSharedPreferences(loginPrefs, mode);
                        String userName = loginPreferences.getString("USER_NAME","");
                        String userContact = loginPreferences.getString("USER_CONTACT", "");

                        Log.e(TAG, "onLoginComplete userName->"+userName+ " userContact->"+userContact);

                        if (userName != null && userContact != null) {
                            new Login(LoginActivity.this,userName,email,userContact, onePlayerId){
                                @Override
                                public void done(@NotNull byte[] data, @NotNull Number code) {
                                    Log.e(TAG,"Login Result->"+new String(data));
                                    Log.e(TAG,"JWT->"+new String(data));
                                }
                            };
                        }
                        loginProgress.setVisibility(View.GONE);

                        if (task.isSuccessful()){
                            launchHomeScreen();
                            getCurrentUser();
                            Log.e(TAG, "signInWithEmailPassword User->"+ getCurrentUser());
                            Toast.makeText(getApplicationContext(), "Welcome back "+ userName,
                                    Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(),
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }

    private void launchHomeScreen() {
        Intent intent = new Intent(LoginActivity.this, MainScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        finish();
    }

    public String getCurrentUser(){
        mAuthListener = new FirebaseAuth.AuthStateListener(){

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null){
                    userId = mCurrentUser.getUid();
                    mCurrentUser = currentUser;
                    Toast.makeText(getApplicationContext(), "Welcome User-> " + userId,
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        };
        return userId;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null){
            finish();
            Log.e(TAG, "onStart: No extras" );
            startActivity(new Intent(this, MainScreen.class));
        }
    }
}
