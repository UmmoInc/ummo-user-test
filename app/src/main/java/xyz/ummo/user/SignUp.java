package xyz.ummo.user;

import android.content.Intent;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Objects;

import androidx.annotation.RequiresApi;

public class SignUp extends AppCompatActivity {

    private final String TAG = "SignUp";
    private String userEmailVal;
    private EditText userEmail;
    private Button signupButton;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle("Sign-Up");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        signupButton = findViewById(R.id.sign_up_btn);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG+" onClick", "user email->"+userEmailVal);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                Intent i= new Intent(this, Register.class);
                finish();
                startActivity(i);
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent= new Intent(this, Register.class);
        startActivity(intent);
    }

    public void signUp(View view){

        userEmail = findViewById(R.id.userEmailEditText);
        userEmailVal = userEmail.getText().toString();

        Log.e(TAG+" onClick", "user email->"+userEmailVal);

        ParseUser user = new ParseUser();
        user.setEmail(userEmailVal);

        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(SignUp.this, "Welcome ...", Toast.LENGTH_SHORT).show();
                    Log.e(TAG+" onSignUpBack", "user email->"+userEmailVal);
//                    startActivity(new Intent(SignUp.this, MainActivity.class));
                } else {
                    Toast.makeText(SignUp.this, "Oops!", Toast.LENGTH_SHORT).show();
                    Log.e(TAG+" onSignUpBack", "user email->"+userEmailVal);
                }
            }
        });
//        Intent i= new Intent(this, Location.class);
//        finish();
//        startActivity(i);
    }

}
