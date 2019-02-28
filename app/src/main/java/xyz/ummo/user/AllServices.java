package xyz.ummo.user;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import xyz.ummo.user.adapters.servicesAdapter;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

public class AllServices extends AppCompatActivity {

    private ArrayList<Service> servicesArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    servicesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_services);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(getIntent().getStringExtra("departmentName"));


        loadServices();;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        adapter = new servicesAdapter(this, servicesArrayList, getIntent().getStringExtra("departmentName"));

        recyclerView= (RecyclerView)findViewById(R.id.services_rv);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainScreen.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu, menu);
        // Associate searchable configuration with the SearchView

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.menu_search).getActionView();

        ImageView icon = searchView.findViewById(androidx.appcompat.R.id.search_button);
        icon.setColorFilter(Color.BLACK);

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.black));
        searchAutoComplete.setTextColor(getResources().getColor(android.R.color.black));

        searchView.setIconifiedByDefault(true);
        searchView.setFocusable(true);
        searchView.setIconified(false);
        searchView.requestFocusFromTouch();

        MenuItem searchMenuItem = menu.findItem( R.id.menu_search); // get my MenuItem with placeholder submenu
        searchMenuItem.expandActionView();


        return true;
    }

    public void loadServices() {

        String[] steps = {"step 1", "step 2", "Step 3", "Step 4"};
        Service service = new Service("Service 1", "Description of service 1 because it is the first service displayed",
                "form 1", "docs 1", "cost 1", "duration 1", steps);
        servicesArrayList.add(service);

        service = new Service("Service 2", "Description of servce 2",
                "form 2", "docs 2", "cost 2", "duration 2", steps);
        servicesArrayList.add(service);

        service = new Service("Service 3", "Description of service 3",
                "form 3", "docs 3", "cost 3", "duration 3", steps);
        servicesArrayList.add(service);

        service = new Service("Service 4", "Description of service 4",
                "form 4", "docs 4", "cost 4", "duration 4", steps);
        servicesArrayList.add(service);

    }

}
