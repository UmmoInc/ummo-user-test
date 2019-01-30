package xyz.ummo.user;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class Register extends AppCompatActivity {
    private final String TAG = "Register";
    private EditText userName;
    private EditText userContact;
    private String userNameVal, userContactVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void register(View view){

        //Initialize registration
        userName = findViewById(R.id.userNameEditText);
        userNameVal = userName.getText().toString();
        userContact = findViewById(R.id.userContactEditText);
        userContactVal = userContact.getText().toString();

        Log.e(TAG+" onRegister", "user name->"+userNameVal);

        ParseUser user = new ParseUser();
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
        });
    }
}
