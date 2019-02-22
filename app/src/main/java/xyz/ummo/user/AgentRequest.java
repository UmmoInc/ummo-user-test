package xyz.ummo.user;

import android.content.Intent;
import android.os.Bundle;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AgentRequest extends AppCompatActivity {

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    RelativeLayout agentDetailsBody;
    View separatorLine;
    TextView nearestAgentTitle, looadingNearestAgentText;
    Button confirmAgentButton;
    String serviceName, form, personalDocs, cost, duration;
    TextView serviceTitle, serviceForm, servicePersonalDocs,
            serviceCost, serviceDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        agentDetailsBody = findViewById(R.id.agent_details_cont);
        separatorLine= findViewById(R.id.view7);
        nearestAgentTitle = findViewById(R.id.nearest_agent_title);
        confirmAgentButton = findViewById(R.id.confirm_agent_btn);
        looadingNearestAgentText = findViewById(R.id.loading_agent_message);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        serviceTitle= findViewById(R.id.service_title);
        serviceForm = findViewById(R.id.service_form);
        servicePersonalDocs = findViewById(R.id.service_personal_docs);
        serviceCost = findViewById(R.id.service_cost);
        serviceDuration = findViewById(R.id.service_duration);

        serviceName = getIntent().getStringExtra("serviceName");
        form = getIntent().getStringExtra("form");
        personalDocs = getIntent().getStringExtra("personalDocs");
        cost = getIntent().getStringExtra("cost");
        duration = getIntent().getStringExtra("duration");

        serviceTitle.setText(serviceName);
        serviceForm.setText(form);
        servicePersonalDocs.setText(personalDocs);
        serviceCost.setText(cost);
        serviceDuration.setText(duration);


        // Start long running operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (progressStatus < 100) {
                    progressStatus += 1;
                    // Update the progress bar and display the
                    //current value in the text view
                    handler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressStatus);
                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        looadingNearestAgentText.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        nearestAgentTitle.setVisibility(View.VISIBLE);
        separatorLine.setVisibility(View.VISIBLE);
        agentDetailsBody.setVisibility(View.VISIBLE);
        confirmAgentButton.setVisibility(View.VISIBLE);

        ViewTooltip
                .on(confirmAgentButton)
                .position(ViewTooltip.Position.TOP)
                .text(getResources().getString(R.string.agent_charge_tip))
                .show();

        confirmAgentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                confirmAgent();

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void confirmAgent(){

        Intent i= new Intent(this, DelegationChat.class);
        finish();
        startActivity(i);


    }


}
