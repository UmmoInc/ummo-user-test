package xyz.ummo.user;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import xyz.ummo.user.fragments.MyProfileFragment;

import android.view.View;
import android.widget.EditText;

public class EditMyProfile extends AppCompatActivity {


    EditText editText;
    String textToEdit, toolBarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_profile);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editText = findViewById(R.id.edit_profile_text);

        textToEdit = getIntent().getStringExtra("name");
        editText.setText(textToEdit);

        toolBarTitle = getIntent().getStringExtra("toolBarTitle");
        toolbar.setTitle(toolBarTitle);

        setTitle(toolBarTitle);
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent intent= new Intent(this, MyProfileFragment.class);
        startActivity(intent);
    }

    public void goBackToMyProfile(View view){

        finish();
        Intent intent= new Intent(this,MyProfileFragment.class);
        startActivity(intent);
    }

    public void finishEditProfile(View view){

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_my_profile,new MyProfileFragment()).commitNow();
    }
}