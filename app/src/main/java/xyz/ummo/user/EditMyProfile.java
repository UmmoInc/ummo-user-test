package xyz.ummo.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import xyz.ummo.user.fragments.HomeFragment;
import xyz.ummo.user.fragments.MyProfileFragment;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class EditMyProfile extends AppCompatActivity
        implements  MyProfileFragment.OnFragmentInteractionListener{


    EditText editText;
    String textToEdit, toolBarTitle;

    public static String CONST_TAG;

    private String mParam1;
    private String mParam2;


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
        //Intent intent= new Intent(this, MyProfileFragment.class);
//        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    public void goBackToMyProfile(View view){
onBackPressed();
     //   finish();
    //    Intent intent= new Intent(this,MyProfileFragment.class);
     //   startActivity(intent);
    }

    public void finishEditProfile(View view){
        Log.e("FINISH","EDITPRO");

        //MyProfileFragment fragment = MyProfileFragment.newInstance("param1", "param2");

        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //transaction.add(R.id.frame, fragment , "");
        //transaction.addToBackStack(null);
        //llltransaction.commit();
    }
}