package xyz.ummo.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;

//import com.parse.ParseException;
//import com.parse.ParseUser;
//import com.parse.SignUpCallback;

public class Register extends AppCompatActivity {
    private final String TAG = "Register";
    private EditText userName;
    private EditText userContact;
    private String userNameVal, userContactVal;
    private final int mode = Activity.MODE_PRIVATE;
    private final String registerPrefs = "UMMO_USER_PREFERENCES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Init Sentry
        Context context = this.getApplicationContext();
        String sentryDSN = getString(R.string.sentryDsn);
        Sentry.init(sentryDSN, new AndroidSentryClientFactory(context));
        logWithStaticAPI();
    }

    public void register(View view){

        //Initialize registration
        userName = findViewById(R.id.userNameEditText);
        userNameVal = userName.getText().toString();
        userContact = findViewById(R.id.userContactEditText);
        userContactVal = userContact.getText().toString();

        Log.e(TAG+" onRegister", "user name->"+userNameVal);

        /*ParseUser user = new ParseUser();
        user.setUsername(userNameVal);
        user.setPassword("my pass");

        user.put("mobile_contact", userContactVal);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(Register.this, "Welcome "+userNameVal, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, SignUp.class));
                    finish();

                    Log.e(TAG+" onSignUpBack", "user name->"+userNameVal);
                } else {
                    Toast.makeText(Register.this, "Oops!"+e.getCause(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG+" onSignUpBack", "user name->"+userNameVal);
                }
            }
        });*/
    }

    private void unsafeMethod(){
        throw new UnsupportedOperationException("This needs attention!");
    }

    private void logWithStaticAPI(){

        SharedPreferences registerPreferences = getSharedPreferences(registerPrefs, mode);
        String userName = registerPreferences.getString("USER_NAME","");
        String userEmail = registerPreferences.getString("USER_EMAIL", "");

        Sentry.getContext().recordBreadcrumb(
                new BreadcrumbBuilder().setMessage("User made an action")
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
}
