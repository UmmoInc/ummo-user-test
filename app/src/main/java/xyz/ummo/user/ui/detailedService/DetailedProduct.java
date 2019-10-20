package xyz.ummo.user.ui.detailedService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.nkzawa.emitter.Emitter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import xyz.ummo.user.R;
import xyz.ummo.user.Services;
import xyz.ummo.user.data.entity.DelegatedServiceEntity;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.delegate.DelegateService;
import xyz.ummo.user.delegate.SocketIO;
import xyz.ummo.user.delegate.User;
import xyz.ummo.user.ui.MainScreen;
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment;
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DetailedProduct extends AppCompatActivity {

    NestedScrollView nestedScrollView;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    TextView serviceDescriptionTextView, serviceCostTextView,
            serviceDurationTextView, serviceDocsTextView, serviceStepsTextView;
    LinearLayout serviceDocsLayout, serviceStepsLayout;
    Toolbar toolbar;
    Button requestAgentBtn;
    private volatile Boolean greenResponse = false;
    private DetailedProductViewModel detailedProductViewModel;
    private DelegatedServiceViewModel delegatedServiceViewModel;
    private ProductEntity productEntity = new ProductEntity();
    private DelegatedServiceEntity delegatedServiceEntity = new DelegatedServiceEntity();
//    String serviceId = "";

//    ListView stepsList;

    ArrayList<String> stepsList;
    ArrayList<String> docsList;
    private static final String TAG = "DetailedProduct";
    private String agentRequestStatus = "Requesting agent...";
    private JSONObject agentDelegate = new JSONObject();
    private String agentName, serviceId, delegatedProductId, serviceProgress;
    private String _serviceName, _description, _cost, _duration, _steps, _docs;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_service);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        progress = new ProgressDialog(this);

        nestedScrollView = findViewById(R.id.nested_scrollview);
        requestAgentBtn = findViewById(R.id.request_agent_btn);

        mCollapsingToolbarLayout = findViewById(R.id.toolbar_layout);

        AppBarLayout appBar = findViewById(R.id.app_bar);
        serviceDescriptionTextView = findViewById(R.id.description_text_view);
        serviceCostTextView = findViewById(R.id.service_cost_text_view);
        serviceDurationTextView = findViewById(R.id.service_duration_text_view);
        serviceDocsLayout = findViewById(R.id.service_docs_linear_layout);
        serviceStepsLayout = findViewById(R.id.service_steps_linear_layout);

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

        detailedProductViewModel = ViewModelProviders.of(this)
                .get(DetailedProductViewModel.class);
        delegatedServiceViewModel = ViewModelProviders.of(this).get(DelegatedServiceViewModel.class);

        String _productId = getIntent().getStringExtra("product_id");
        String _serviceId = getIntent().getStringExtra("_id");

        if (_productId != null){
            detailedProductViewModel.getProductEntityLiveDataById(_productId).observe(this, productEntity1 -> {
            _serviceName = productEntity1.getProductName();
            Log.e(TAG, "onCreate: Within ProductVM: Product ID->"+_productId);
            _description = productEntity1.getProductDescription();
            _cost = productEntity1.getProductCost();
            _duration = productEntity1.getProductDuration();
//        stepsList = getIntent().getStringArrayListExtra("steps");
//        docsList = getIntent().getStringArrayListExtra("docs");

            stepsList = new ArrayList<>(productEntity1.getProductSteps());
            /*
            for (int i = 0; i < productEntity1.getProductDocuments().size(); i++){
                _docs = productEntity1.getProductDocuments().get(i);
            }*/

            docsList = new ArrayList<>(productEntity1.getProductDocuments());

            //Filling in UI components
            toolbar.setTitle(_serviceName);
            serviceDescriptionTextView.setText(_description);
            serviceCostTextView.setText(_cost);
            serviceDurationTextView.setText(_duration);

                if (!stepsList.isEmpty()){

                    for (int i = 0; i<stepsList.size(); i++){
                        Log.e(TAG, "onCreate: stepsList->"+stepsList);
                        serviceStepsTextView = new TextView(getApplicationContext());
                        serviceStepsTextView.setId(i);
                        serviceStepsTextView.setText(stepsList.get(i));
                        serviceStepsTextView.setTextSize(14);
                        serviceStepsLayout.addView(serviceStepsTextView);
                    }
                } else {
                    Log.e(TAG, "onCreate: stepsList is EMPTY!");
                }

                if (!docsList.isEmpty()){

                    for (int i = 0; i<docsList.size(); i++){
                        Log.e(TAG, "onCreate: docsList->"+docsList);
                        serviceDocsTextView = new TextView(getApplicationContext());
                        serviceDocsTextView.setId(i);
                        serviceDocsTextView.setText(docsList.get(i).replace("\"\"",""));
                        serviceDocsTextView.setTextSize(14);
                        serviceDocsLayout.addView(serviceDocsTextView);
                    }
                } else {
                    Log.e(TAG, "onCreate: docsList is EMPTY!");
                }

            Log.e(TAG, "onCreate: isDelegated"+productEntity1.getIsDelegated());
        });
        } else if (_serviceId != null){
            Log.e(TAG, "onCreate: SERVICE-ID->"+_serviceId);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.steps_list, R.id.step, stepsList);

        requestAgentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progress.setTitle("Agent Request");
                progress.setMessage(agentRequestStatus);
                progress.show();

                String jwt = PreferenceManager.getDefaultSharedPreferences(DetailedProduct.this).getString("jwt", "");

                Log.e(TAG, "onCreate: SERVICE-ID->"+_serviceId);

                detailedProductViewModel.getProductEntityLiveDataById("5d8fa2db2af11a001758ca4a")
                        .observe(DetailedProduct.this, productEntity1 -> {
                            Log.e(TAG, "onClick: LIVE-DATA:->"+productEntity1.getProductName());
                        });

                if (jwt != null) {
                    new DelegateService(DetailedProduct.this, User.Companion.getUserId(jwt),_productId){
                        @Override
                        public void done(@NotNull byte[] data, int code) {
                            greenResponse = true;
                            Log.e(TAG, "delegateService: Done->"+new String(data));
                            progress.dismiss();

                            try {
                                agentDelegate = new JSONObject(new String(data));
                                agentName = agentDelegate.getString("name");

                                Log.e(TAG, "done: agentName->"+agentName);

                                AlertDialog.Builder agentRequestDialog = new AlertDialog.Builder(DetailedProduct.this);
                                agentRequestDialog.setTitle("Agent Delegate");
//                                agentRequestDialog.setIcon()
                                agentRequestDialog.setMessage(agentName+ " is available...");
                                agentRequestDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        agentRequestStatus = "Waiting for a response from "+agentName+"...";
                                        ProgressDialog progress = new ProgressDialog(DetailedProduct.this);
                                        progress.setTitle("Agent Request");
                                        progress.setMessage(agentRequestStatus);
                                        progress.show();
                                    }
                                });
                                agentRequestDialog.show();

                                detailedProductViewModel.getProductEntityLiveDataById(_productId).observe(DetailedProduct.this, productEntity1 ->{
                                    productEntity1.setIsDelegated(true);
                                });

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                            progress.setMessage(getResources().getString(R.string.loading_agent_message));
                        }
                    };
                }
            }
        });

        /*int totalHeight = 0;
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            View listItem = arrayAdapter.getView(i, null, stepsList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams lp = stepsList.getLayoutParams();
        int height = totalHeight;

        lp.height = height;
        stepsList.setLayoutParams(lp);

        stepsList.setAdapter(arrayAdapter);*/

        mCollapsingToolbarLayout.setTitle(_serviceName);
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

    }

    @Override
    protected void onPause() {
        super.onPause();
        progress.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void launchDelegatedService(){
        String serviceId = getIntent().getStringExtra("SERVICE_ID");

        if (!serviceId.isEmpty()){
            Log.e(TAG, "newDelegatedService Bundle->"+serviceId);
        } else {
            Log.e(TAG, "newDelegatedService NO Bundle!");
        }
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

    private static class DelegateAsyncTask extends AsyncTask<Integer, Void, String> {
       private WeakReference<DetailedProduct> detailedProductWeakReference;
        String newAgentRequestStatus = "";

       DelegateAsyncTask(DetailedProduct activity){
           detailedProductWeakReference = new WeakReference<>(activity);
       }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... integers) {

            for (int i = 0; i < integers[0]; i++){
                Log.e(TAG, "doInBackGround: "+i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "doInBackGroundException: "+e);
                    e.printStackTrace();
                }
            }
            return newAgentRequestStatus;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            DetailedProduct detailedProduct = detailedProductWeakReference.get();
            if (detailedProduct == null || detailedProduct.isFinishing()){
                return;
            }

            detailedProduct.agentRequestStatus = newAgentRequestStatus;
        }
    }
}

