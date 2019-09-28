package xyz.ummo.user;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import xyz.ummo.user.adapters.CustomAdapter;
import xyz.ummo.user.adapters.CustomStepsAdapter;
import xyz.ummo.user.adapters.servicesAdapter;
import xyz.ummo.user.delegate.DelegateService;
import xyz.ummo.user.delegate.User;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DetailedService extends AppCompatActivity {

    NestedScrollView nestedScrollView;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    TextView serviceDescription, serviceCost, serviceDuration, serviceDocs;
    Toolbar toolbar;
    Button requestAgentBtn;

    ListView stepsList;

    ArrayList<String> steps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_service);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nestedScrollView = findViewById(R.id.nested_scrollview);
        requestAgentBtn = findViewById(R.id.request_agent_btn);


        mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);

        AppBarLayout appBar = findViewById(R.id.app_bar);

        appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (( mCollapsingToolbarLayout.getHeight() + verticalOffset) < (2 * ViewCompat.getMinimumHeight( mCollapsingToolbarLayout))) {
                    toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.black), PorterDuff.Mode.SRC_ATOP);


                } else {

                    toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.UmmoPurple), PorterDuff.Mode.SRC_ATOP);
                }
            }
        });


        String serviceName = getIntent().getStringExtra("serviceName");
        String description = getIntent().getStringExtra("description");
        String cost = getIntent().getStringExtra("cost");
        String duration = getIntent().getStringExtra("duration");
        String _steps = getIntent().getStringExtra("steps");
        String docs = getIntent().getStringExtra("docs");
        String id = getIntent().getStringExtra("id");


        toolbar.setTitle(serviceName);

        serviceDescription = findViewById(R.id.service_description);
        serviceCost = findViewById(R.id.service_cost);
        serviceDuration = findViewById(R.id.service_duration);
        serviceDocs = findViewById(R.id.service_documents);
        serviceDocs.setText(docs);
        serviceDescription.setText(description);
        serviceCost.setText(cost);

        serviceDuration.setText(duration);

        steps = new ArrayList<>();

        steps.add(_steps);
        stepsList = findViewById(R.id.steps);


        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, R.layout.steps_list, R.id.step,steps);

        requestAgentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String jwt = PreferenceManager.getDefaultSharedPreferences(DetailedService.this).getString("jwt", "");
                new DelegateService(DetailedService.this, User.Companion.getUserId(jwt),id){
                    @Override
                    public void done(@NotNull byte[] data, int code) {
                        Log.e("Done",new String(data));
                    }
                };
            }
        });

        int totalHeight = 0;
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            View listItem = arrayAdapter.getView(i, null, stepsList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams lp = stepsList.getLayoutParams();
        int height = totalHeight;

        // arraylist list is in which all data is kept

        lp.height = height;
        stepsList.setLayoutParams(lp);


        stepsList.setAdapter(arrayAdapter);


        mCollapsingToolbarLayout.setTitle(serviceName);
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(this, Services.class);
                startActivity(intent);
                finish();
                return  true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
