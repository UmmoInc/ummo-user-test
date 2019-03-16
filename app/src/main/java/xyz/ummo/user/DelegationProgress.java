package xyz.ummo.user;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import xyz.ummo.user.adapters.CustomAdapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;

public class DelegationProgress extends AppCompatActivity {

    //declaration of all necessary variables

    ListView progressList;
    ArrayList<Progress> progresses = new ArrayList();
    String processes[]= {"Form filled", "Service payment", "Collection"};
    ProgressBar progressBar;
    CustomAdapter customAdapter;
    ImageView messageIcon, homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delegation_progress);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Delegation Progress");


        //load so progresses into the arraylist
        loadProcesses();

        //initialise the messageIcon and homeIcon imageview
        messageIcon = findViewById(R.id.message_icon_button);
        homeIcon = findViewById(R.id.home_icon_button);


        //make the messageIcon clickable and set the click action
        messageIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                goToMessagingAgent();

            }
        });

        //make the home icon clickable and set the click action
        homeIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                goToHome();

            }
        });


        //Initialise the progressbar and set the progress color
        progressBar = findViewById(R.id.delegation_progress_bar);
        progressBar.setProgressTintList(ColorStateList.valueOf(R.color.ummo_4));

        //initialie the progress listview adapter
        customAdapter = new CustomAdapter(this, progresses, progressBar);

        //initialise the progress listview and set an adapter to it
        progressList = findViewById(R.id.progress_list);
        progressList.setAdapter(customAdapter);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
        finish();
    }

    public void loadProcesses(){

        // add some progress list objects

        Progress progress = new Progress("Form filled");
        progresses.add(progress);

        progress = new Progress("Service payment");
        progresses.add(progress);

        progress = new Progress("Collection");
        progresses.add(progress);

    }

    public void goToMessagingAgent(){

        Intent intent = new Intent(this, DelegationChat.class );
        intent.putExtra("hasInitiatedService", true);
        finish();
        startActivity(intent);

    }

    public void goToHome(){

        new MainScreen().setAnyServiceInProgress(true);

        Intent intent = new Intent(this, MainScreen.class);
        finish();
        startActivity(intent);

    }
}
