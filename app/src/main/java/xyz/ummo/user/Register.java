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

import io.sentry.core.Sentry;
import io.sentry.core.protocol.User;

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

        try {
            throw new Exception("This is a test");
        } catch (Exception e) {
            Sentry.captureException(e);
        }
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

        SharedPreferences mainActPreferences = getSharedPreferences(registerPrefs, mode);
        String userName = mainActPreferences.getString("USER_NAME","");
        String userEmail = mainActPreferences.getString("USER_EMAIL", "");

        Sentry.addBreadcrumb("User made an action");
        User user = new User();
        user.setEmail(userEmail);
        user.setUsername(userName);
        Sentry.setUser(user);

        try {
            unsafeMethod();
            Log.e(TAG, "logWithStaticAPI, unsafeMethod");
        } catch (Exception e){
            Sentry.captureException(e);
        }
    }
}
