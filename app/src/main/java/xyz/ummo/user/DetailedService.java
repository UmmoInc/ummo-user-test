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

import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailedService extends AppCompatActivity {

    NestedScrollView nestedScrollView;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    TextView serviceDescription, serviceCost, serviceDuration;
    Toolbar toolbar;

    ListView stepsList;


    ArrayList<String> steps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_service);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        nestedScrollView = findViewById(R.id.nested_scrollview);


        mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);

        AppBarLayout appBar = (AppBarLayout)  findViewById(R.id.app_bar);

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
;       String cost = getIntent().getStringExtra("cost");
        String duration = getIntent().getStringExtra("duration");

        toolbar.setTitle(serviceName);

        serviceDescription = findViewById(R.id.service_description);
        serviceCost = findViewById(R.id.service_cost);
        serviceDuration = findViewById(R.id.service_duration);

        serviceDescription.setText(description);
        serviceCost.setText(cost);
        serviceDuration.setText(duration);

        steps = new ArrayList<String>();


        stepsList = findViewById(R.id.steps);
        loadSteps();

        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, R.layout.steps_list, R.id.step,steps);

        int totalHeight = 0;
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            View listItem = arrayAdapter.getView(i, null, stepsList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams lp = (ViewGroup.LayoutParams) stepsList.getLayoutParams();
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

    public void loadSteps(){

        steps.add("step 1");
        steps.add("step 2");
        steps.add("step 3");
        steps.add("step 4");
        steps.add("step 5");
        steps.add("step 6");

    }
}
