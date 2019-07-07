package xyz.ummo.user;

import android.content.Intent;
import android.os.Bundle;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import xyz.ummo.user.delegate.GetAgents;
import xyz.ummo.user.delegate.StartServiceHandshake;

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

    public TextView agentName, agentContact, publicRating;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_request);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle("Agent Request");

        new GetAgents(this) {
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {
                try {
                    JSONArray agentsJsonArray = new JSONArray(new String(data));
                    Random rand = new Random();
                    int index = rand.nextInt(agentsJsonArray.length());
                    JSONObject agentJsonObject = agentsJsonArray.getJSONObject(index);
                    String contact = agentJsonObject.getString("contact");
                    String cost = getIntent().getStringExtra("cost");
                    String name = agentJsonObject.getString("name");
                    agentName.setText(name);
                    agentContact.setText(contact);
                    publicRating.setText(cost);
                } catch (JSONException jse) {
                    Log.e("jSE", jse.toString());
                }

            }
        };


        agentName = findViewById(R.id.agent_name);
        agentContact = findViewById(R.id.agent_contact);
        publicRating = findViewById(R.id.agent_public_rating);

        agentDetailsBody = findViewById(R.id.agent_details_cont);
        separatorLine = findViewById(R.id.view7);
        nearestAgentTitle = findViewById(R.id.nearest_agent_title);
        confirmAgentButton = findViewById(R.id.confirm_agent_btn);
        looadingNearestAgentText = findViewById(R.id.loading_agent_message);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        serviceTitle = findViewById(R.id.service_title);
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
    public void onBackPressed() {
        Intent intent = new Intent(this, Services.class);
        intent.putExtra("departmentName", getIntent().getStringExtra("departmentName"));
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, Services.class);
                intent.putExtra("departmentName", getIntent().getStringExtra("departmentName"));
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void confirmAgent() {

        Intent intent = new Intent(this, DelegationChat.class);
        new StartServiceHandshake(getIntent().getStringExtra("id"),"",""){
            @Override
            public void handshakeFinished(boolean accepted) {

                if(accepted){
                    delegate();
                }
            }
        };
        intent.putExtra("hasInitiatedService", false);
        finish();
        startActivity(intent);

    }

    public void delegate(){

    }

}
