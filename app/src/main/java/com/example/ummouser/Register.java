package com.example.ummouser;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void nextToSignUp(View view){

        Intent i= new Intent(this, SignUp.class);
        finish();
        startActivity(i);

    }
}
