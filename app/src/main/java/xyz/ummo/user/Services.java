package xyz.ummo.user;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xyz.ummo.user.adapters.servicesAdapter;

import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class Services extends AppCompatActivity {

    private ArrayList<Service> servicesArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    servicesAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        loadServices();;
         LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        adapter = new servicesAdapter(this, servicesArrayList);

        recyclerView= (RecyclerView)findViewById(R.id.services_rv);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

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

    public void loadServices() {
        Service service = new Service("Service 1", "Description of servce 1 because it is the first service displayed",
                "form 1", "docs 1", "cost 1", "duration 1");
        servicesArrayList.add(service);

        service = new Service("Service 2", "Description of servce 2",
                "form 2", "docs 2", "cost 2", "duration 2");
        servicesArrayList.add(service);

        service = new Service("Service 3", "Description of service 3",
                "form 3", "docs 3", "cost 3", "duration 3");
        servicesArrayList.add(service);

        service = new Service("Service 4", "Description of service 4",
                "form 4", "docs 4", "cost 4", "duration 4");
        servicesArrayList.add(service);

    }

    public void requestAgent(View view){

        Intent i= new Intent(this, AgentRequest.class);
        finish();
        startActivity(i);
    }
}
