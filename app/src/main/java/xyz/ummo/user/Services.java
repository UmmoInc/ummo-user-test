package xyz.ummo.user;

import android.app.ProgressDialog;
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
import xyz.ummo.user.delegate.GetProducts;

import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Services extends AppCompatActivity {

    private ArrayList<Service> servicesArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    servicesAdapter adapter;
    private ProgressDialog progress;
    private final static String TAG = "Services";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        try {
            setTitle(getIntent().getStringExtra("departmentName"));
            String public_service = getIntent().getStringExtra("public_service");
            loadServices(public_service);
        }catch (Exception e){
            Log.e("Bohoo",e.toString());
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        Log.e("LOG","Oncreate");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        adapter = new servicesAdapter(this, servicesArrayList, getIntent().getStringExtra("departmentName"));

        recyclerView = findViewById(R.id.services_rv);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainScreen.class);
                startActivity(intent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        // Inflate menu to add items to action bar if it is present.
        inflater.inflate(R.menu.menu, menu);
        // Associate searchable configuration with the SearchView
        //njjngdjnjdgnh
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

        return true;
    }


    public void loadServices(String public_service) {
        Log.e("Load", "Services/Products");
        new GetProducts(this,public_service) {
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {
                System.out.println(new String(data));
                try {
                    JSONArray productsJsonArray = new JSONArray(new String(data));
                    servicesArrayList.clear();
                    for (int i = 0; i < productsJsonArray.length(); i++) {
                        JSONObject productJsonObject = productsJsonArray.getJSONObject(i);
                        //TODO Service should be properly named as a product somewhere. I don't know why it was improperly named
                        // Can we stick to the conventions we had to keep our work simple. PS If anyone will ever read this comment
                        // I think we need to discuss this one
                        List<String> steps = new ArrayList<>();
                        if (productJsonObject.has("procurement_process")) {
                            JSONArray stepsJsonArray = productJsonObject.getJSONArray("procurement_process");
                            Log.e("Steps",stepsJsonArray.toString());
                            for (int j = 0; j < stepsJsonArray.length(); j++) {
                                steps.add(stepsJsonArray.getString(j));
                            }
                        }

                        String docs = "";

                        if (productJsonObject.getJSONObject("requirements").has("documents")) {
                            JSONArray docsArray = productJsonObject.getJSONObject("requirements").getJSONArray("documents");
                            for (int j = 0; j < docsArray.length(); j++) {
                                docs += docsArray.getString(j);
                                docs += j == docsArray.length() - 1 ? "" : ", ";
                            }
                        }

                        Service service = new Service(
                                productJsonObject.getString("product_name"),
                                "NOT in ERD",
                                "TF is a Form?",
                                docs,
                                productJsonObject.getJSONObject("requirements").getString("procurement_cost"),
                                productJsonObject.getString("duration"),
                                steps
                        );
                        Log.e(TAG, "GetPersonalDocs->"+service.getPersonalDocs());
                        servicesArrayList.add(service);
                        Log.e(TAG, "Added docs->"+docs);
                    }


                } catch (JSONException jse) {
                    Log.e("JSONERROR", jse.toString());
                    adapter.notifyDataSetChanged();
                }
                Log.e("Adapter","Should update here");
              //  adapter = new servicesAdapter(Services.this, servicesArrayList, getIntent().getStringExtra("departmentName"));
                adapter.notifyDataSetChanged();
            }
        };

    }

    public void requestAgent(View view) {
        progress = new ProgressDialog(this);
        progress.setMessage("Downloading Music");
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();

        final int totalProgressTime = 100;
        final Thread t = new Thread() {
            @Override
            public void run() {
                int jumpTime = 0;

                while (jumpTime < totalProgressTime) {
                    try {
                        sleep(200);
                        jumpTime += 5;
                        progress.setProgress(jumpTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        t.start();
    }
}
