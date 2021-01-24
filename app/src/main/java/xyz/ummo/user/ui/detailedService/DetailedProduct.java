package xyz.ummo.user.ui.detailedService;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.nkzawa.emitter.Emitter;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProviders;

import timber.log.Timber;
import xyz.ummo.user.R;
import xyz.ummo.user.data.entity.DelegatedServiceEntity;
import xyz.ummo.user.data.entity.ProductEntity;
import xyz.ummo.user.delegate.DelegateService;
import xyz.ummo.user.delegate.User;
import xyz.ummo.user.ui.MainScreen;
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceViewModel;

import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private final String ummoUserPreferences = "UMMO_USER_PREFERENCES";
    private final int mode = Activity.MODE_PRIVATE;

    ArrayList<String> stepsList;
    ArrayList<String> docsList;
    private static final String TAG = "DetailedProduct";
    private String agentRequestStatus = "Requesting agent...";
    private JSONObject agentDelegate = new JSONObject();
    private String agentName, serviceId, delegatedProductId, serviceProgress;
    private String _serviceName, _description, _cost, _duration, _steps, _docs;
    private ProgressDialog progress;
    AlertDialog.Builder agentRequestDialog, agentNotFoundDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_service);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(this,
                        getResources().getString(R.string.mixpanelToken));

        progress = new ProgressDialog(this);
        agentRequestDialog = new AlertDialog.Builder(DetailedProduct.this);
        agentNotFoundDialog = new AlertDialog.Builder(DetailedProduct.this);

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

        SharedPreferences detailedProductPrefs = getSharedPreferences(ummoUserPreferences, mode);
        SharedPreferences.Editor editor;
        editor = detailedProductPrefs.edit();

        detailedProductViewModel = ViewModelProviders.of(this)
                .get(DetailedProductViewModel.class);
        delegatedServiceViewModel = ViewModelProviders.of(this).get(DelegatedServiceViewModel.class);

        String _productId = getIntent().getStringExtra("product_id");
        String _serviceId = getIntent().getStringExtra("_id");

        if (_productId != null){
            detailedProductViewModel.getProductEntityLiveDataById(_productId).observe(this, productEntity1 -> {
                _serviceName = productEntity1.getProductName();
                Timber.e("onCreate: Within ProductVM: ProductModel ID->%s", _productId);
                _description = productEntity1.getProductDescription();
                _cost = productEntity1.getProductCost();
//                _duration = productEntity1.getProductDuration();

                stepsList = new ArrayList<>(productEntity1.getProductSteps());

//                docsList = new ArrayList<>(productEntity1.getProductDocuments());

                //Filling in UI components
                toolbar.setTitle(_serviceName);
                serviceDescriptionTextView.setText(_description);
                serviceCostTextView.setText(_cost);
                serviceDurationTextView.setText(_duration);

                if (!stepsList.isEmpty()){
                    serviceStepsLayout.removeAllViews();
                    for (int i = 0; i<stepsList.size(); i++){
                        Timber.e("onCreate: stepsList->%s", stepsList);
                        serviceStepsTextView = new TextView(getApplicationContext());
                        serviceStepsTextView.setId(i);
                        serviceStepsTextView.setText(stepsList.get(i));
                        serviceStepsTextView.setTextSize(14);
                        serviceStepsLayout.addView(serviceStepsTextView);
                    }
                } else {
                    Timber.e("onCreate: stepsList is EMPTY!");
                }

                if (!docsList.isEmpty()){
                    serviceDocsLayout.removeAllViews();
                    for (int i = 0; i<docsList.size(); i++){
                        Timber.e("onCreate: docsList->%s", docsList);
                        serviceDocsTextView = new TextView(getApplicationContext());
                        serviceDocsTextView.setId(i);
                        serviceDocsTextView.setText(docsList.get(i).replace("\"\"",""));
                        serviceDocsTextView.setTextSize(14);
                        serviceDocsLayout.addView(serviceDocsTextView);
                    }
                } else {
                    Timber.e("onCreate: docsList is EMPTY!");
                }

                Timber.e("onCreate: isDelegated%s", productEntity1.getIsDelegated());
            });
        } else if (_serviceId != null){
            Timber.e("onCreate: SERVICE-ID->%s", _serviceId);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.steps_list, R.id.step, stepsList);

        requestAgentBtn.setOnClickListener(v -> requestAgentDelegate(_productId));

        mCollapsingToolbarLayout.setTitle(_serviceName);
        mCollapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
        mCollapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

    }

    public void requestAgentDelegate(String mProductId) {

        SharedPreferences detailedProductPrefs = getSharedPreferences(ummoUserPreferences, mode);
        SharedPreferences.Editor editor;
        editor = detailedProductPrefs.edit();
            /*if (mixpanel != null) {
                mixpanel.track("requestAgentTapped");
            }*/

        progress.setTitle("Agent Request");
        progress.setMessage(agentRequestStatus);
        progress.show();

        String jwt = PreferenceManager.getDefaultSharedPreferences(DetailedProduct.this).getString("jwt", "");

        if (jwt != null) {
            assert mProductId != null;
            new DelegateService(DetailedProduct.this, User.Companion.getUserId(jwt),mProductId){
                @Override
                public void done(@NotNull byte[] data, int code) {
                    Timber.e("delegatedService: Done->%s", new String(data));
                    Timber.e("delegatedService: Status Code->%s", code);

                    progress.dismiss();

                    if (code == 200){
                        try {
                            agentDelegate = new JSONObject(new String(data));
                            agentName = agentDelegate.getString("name");

                            Timber.e("done: agentName->%s", agentName);

                            agentRequestDialog.setTitle("Agent Delegate");
//                                agentRequestDialog.setIcon()
                            agentRequestDialog.setMessage(agentName+ " is available...");
                            agentRequestDialog.setPositiveButton("Continue", (dialog, which) -> {

                                    /*if (mixpanel != null) {
                                        mixpanel.track("requestAgentContinue");
                                    }*/

                                agentRequestStatus = "Waiting for a response from "+agentName+"...";
                                ProgressDialog progress = new ProgressDialog(DetailedProduct.this);
                                progress.setTitle("Agent Request");
                                progress.setMessage(agentRequestStatus);
                                progress.show(); // TODO: 10/22/19 -> handle leaking window
                                editor.clear(); //Removing old key-values from a previous session

                                editor.putString("DELEGATED_AGENT", agentName);
                                editor.putString("DELEGATED_PRODUCT", mProductId);
                                editor.apply();
                            });
                            agentRequestDialog.show();

                            detailedProductViewModel.getProductEntityLiveDataById(mProductId).observe(DetailedProduct.this, productEntity1 ->{
                                productEntity1.setIsDelegated(true);
                                Timber.e("done: isDelegated-> TRUE");
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else if (code == 404){
                        Timber.e("done: Status Code 500!!!");

                        agentNotFoundDialog.setTitle("Agent Delegate");
                        agentNotFoundDialog.setMessage("No Agent currently available.");
                        agentNotFoundDialog.setPositiveButton("Dismiss", (dialog, which) -> {

                                /*if (mixpanel != null) {
                                    mixpanel.track("requestAgentDismiss");
                                }*/

                            Timber.e("done: Dismissed!");
                            requestAgentBtn.setText(getResources().getString(R.string.retry_agent_request));
                        });
                        agentNotFoundDialog.show();

                    }else{
                        Timber.e("done: Status Code 500!!!");

                        Toast.makeText(DetailedProduct.this, "BOMDAS!", Toast.LENGTH_LONG).show();

                        agentNotFoundDialog.setTitle("Agent Delegate");
                        agentNotFoundDialog.setMessage("We honestly don't know what happened, please check if there is internet");
                        agentNotFoundDialog.setPositiveButton("Dismiss", (dialog, which) -> {
                            Timber.e("done: Dismissed!");
                            requestAgentBtn.setText("RETRY AGENT REQUEST");
                        });
                        agentNotFoundDialog.show();
                    }
//                            progress.setMessage(getResources().getString(R.string.loading_agent_message));
                }
            };
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        progress.dismiss();

        agentRequestDialog.setOnDismissListener(dialog -> Timber.e("onPause: onDialogDismiss!"));

        agentNotFoundDialog.setOnDismissListener(dialog -> Timber.e("onPause: onDialogDismiss!"));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void launchDelegatedService(){
        String serviceId = getIntent().getStringExtra("SERVICE_ID");

        assert serviceId != null;
        if (!serviceId.isEmpty()){
            Timber.e("newDelegatedService Bundle->%s", serviceId);
        } else {
            Timber.e("newDelegatedService NO Bundle!");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(this, MainScreen.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                Timber.e("doInBackGround: %s", i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Timber.e("doInBackGroundException: %s", e);
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