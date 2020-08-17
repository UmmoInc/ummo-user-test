package xyz.ummo.user.ui.fragments.delegatedService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import timber.log.Timber;
import xyz.ummo.user.DelegationChat;
import xyz.ummo.user.R;
import xyz.ummo.user.data.entity.DelegatedServiceEntity;
import xyz.ummo.user.delegate.ConfirmService;
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel;
import xyz.ummo.user.ui.serviceFeedback.Feedback;
import xyz.ummo.user.ui.serviceIssue.ServiceIssue;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DelegatedServiceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DelegatedServiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DelegatedServiceFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1, mParam2;
    private String agentName, productName, serviceId, serviceAgentId, delegatedProductId;

    private TextView agentNameTextView, agentStatusTextView,
            delegatedProductNameTextView, delegatedProductDescriptionTextView,
            delegatedProductCostTextView, /*delegatedProductDurationTextView,*/
            delegatedServiceDocsTextView, delegatedServiceStepsTextView;
    private ProgressBar progressBar;
    private ArrayList<TextView> stepsTV = new ArrayList<>();

    ArrayList<String> stepsList;
    ArrayList<String> docsList;

    private LinearLayout delegatedProductDocsLayout, delegatedProductStepsLayout;

    private ImageView openChat;

    private final int mode = Activity.MODE_PRIVATE;
    private final String ummoUserPreferences = "UMMO_USER_PREFERENCES";

    private OnFragmentInteractionListener mListener;
    private DelegatedServiceViewModel delegatedServiceViewModel;
    private DetailedProductViewModel detailedProductViewModel;
    private DelegatedServiceEntity delegatedServiceEntity = new DelegatedServiceEntity();
    private AlertDialog confirmReceiptDialog;
    private Boolean hasConfirmed = false;

    public DelegatedServiceFragment(DelegatedServiceEntity entity) {

        delegatedServiceEntity = entity;

        // Required empty public constructor
    }

    public DelegatedServiceFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DelegatedServiceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DelegatedServiceFragment newInstance(String param1, String param2) {
        DelegatedServiceFragment fragment = new DelegatedServiceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        Timber.e("newInstance: SetArgument%s", args);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/

        confirmReceiptDialog = new AlertDialog.Builder(getContext()).create();

        if (getArguments() != null) {

            SharedPreferences sharedPreferences = Objects.requireNonNull(getActivity()).getSharedPreferences(ummoUserPreferences, mode);
            agentName = sharedPreferences.getString("DELEGATED_AGENT", "");

            serviceId = getArguments().getString("SERVICE_ID");
            serviceAgentId = getArguments().getString("SERVICE_AGENT_ID");
            delegatedProductId = getArguments().getString("DELEGATED_PRODUCT_ID");

            delegatedServiceViewModel = ViewModelProviders.of(this)
                    .get(DelegatedServiceViewModel.class);
            Timber.e("onCreate: Service id%s", serviceId);

//            delegatedServiceEntity = delegatedServiceViewModel.getDelegatedServiceEntityLiveData();//getDelegatedServiceById(serviceId).getValue();

            detailedProductViewModel = ViewModelProviders.of(this)
                    .get(DetailedProductViewModel.class);
        }

        Timber.e("onCreate: arguments: SERVICE-ID->%s", serviceId);
        Timber.e("onCreate: arguments: SERVICE-AGENT-ID->%s", serviceAgentId);
        Timber.e("onCreate: arguments: DELEGATED-PRODUCT-ID->%s", delegatedProductId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delegated, container, false);
        agentNameTextView = view.findViewById(R.id.delegated_agent_name_text_view);
        agentStatusTextView = view.findViewById(R.id.delegated_agent_status_text_view);
        openChat = view.findViewById(R.id.open_chat_button);
        progressBar = view.findViewById(R.id.service_progress_bar);

        goToDelegationChat(view);

        agentNameTextView.setText(agentName);

        delegatedProductNameTextView = view.findViewById(R.id.delegated_service_header_name);
        delegatedProductDescriptionTextView = view.findViewById(R.id.description_text_view);
        delegatedProductCostTextView = view.findViewById(R.id.service_cost_text_view);
//        delegatedProductDurationTextView = view.findViewById(R.id.service_duration_text_view);

//        delegatedProductDocsLayout = view.findViewById(R.id.service_docs_linear_layout);
        delegatedProductStepsLayout = view.findViewById(R.id.delegated_service_steps_layout);

        Timber.e("onCreateView: New product id%s", delegatedProductId);

        detailedProductViewModel.getProductEntityLiveDataById(delegatedProductId).observe(getViewLifecycleOwner(), delegatedProductEntity -> {

            //TODO: BUG!
            productName = delegatedProductEntity.getProductName();

            Timber.e("onCreateView: DELEGATED PRODUCT->%s", delegatedProductEntity.getProductDuration());
            delegatedProductNameTextView.setText(delegatedProductEntity.getProductName());
            delegatedProductDescriptionTextView.setText(delegatedProductEntity.getProductDescription());
            delegatedProductCostTextView.setText(delegatedProductEntity.getProductCost());
//            delegatedProductDurationTextView.setText(delegatedProductEntity.getProductDuration());

//            docsList = new ArrayList<>(delegatedProductEntity.getProductDocuments());
            /*if (!docsList.isEmpty()) {
                delegatedProductDocsLayout.removeAllViews();
                for (int i = 0; i < docsList.size(); i++) {
                    delegatedServiceDocsTextView = new TextView(getContext());
                    delegatedServiceDocsTextView.setId(i);
                    delegatedServiceDocsTextView.setText(delegatedProductEntity.getProductDocuments().get(i));
                    delegatedServiceDocsTextView.setTextSize(14);
                    delegatedProductDocsLayout.addView(delegatedServiceDocsTextView);
                }
            }*/

            stepsList = new ArrayList<>(delegatedProductEntity.getProductSteps());
            if (!stepsList.isEmpty()) {
                delegatedProductStepsLayout.removeAllViews();
                stepsTV.clear();
                for (int i = 0; i < stepsList.size(); i++) {
                    delegatedServiceStepsTextView = new TextView(getContext());
                    delegatedServiceStepsTextView.setId(i);
                    delegatedServiceStepsTextView.setText(delegatedProductEntity.getProductSteps().get(i));
                    delegatedServiceStepsTextView.setTextSize(14);
                    delegatedProductStepsLayout.addView(delegatedServiceStepsTextView);
                    stepsTV.add(delegatedServiceStepsTextView);

                    delegatedServiceViewModel.getDelegatedServiceEntityLiveData().observe(getViewLifecycleOwner(), delegatedServiceEntity1 -> {
                        Timber.e("onCreateView: Steps " + delegatedServiceEntity1.getServiceProgress() + " " + delegatedServiceStepsTextView.getText().toString());

                        if (delegatedServiceEntity1.getServiceProgress().contains(delegatedServiceStepsTextView.getText().toString())) {
                            Timber.e("onCreateView: Cross");
                            delegatedServiceStepsTextView.setPaintFlags(delegatedServiceStepsTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }

                    });
                }
            }

            delegatedServiceViewModel.getDelegatedServiceById(serviceId).observe(getViewLifecycleOwner(), delegatedServiceEntity1 -> {
                ArrayList<String> progress = delegatedServiceEntity1.getServiceProgress();
                //Log.e(TAG, "onCreate: DELEGATED-SERVICE-ENTITY-LIVE-DATA->"+stepsTV.size()+" "+delegatedServiceEntity1.getServiceProgress().size());

                /**
                 *This below 'for-loop' undoes the striking out of service steps (which should not be a likely scenario to occur)
                 **/
                for (int i = 0; i < stepsTV.size(); i++) {
                    stepsTV.get(i).setPaintFlags(delegatedServiceStepsTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }

                /*
                 *This line below correlates the progress with the number of steps checked off
                 */

                int progressPercentage = (progress.size() / stepsTV.size()) * 100;
                progressBar.setProgress((progress.size() * 100 / stepsTV.size()));

                Timber.e("onCreateView: ->%s", progressPercentage);

                /*
                 *This below 'for-loop' strikes out service steps (according to the agent's progress)
                 */
                for (int i = 0; i < progress.size(); i++) {
                    for (int j = 0; j < stepsTV.size(); j++) {
                        if (progress.contains(stepsTV.get(j).getText().toString())) {
                            stepsTV.get(j).setPaintFlags(delegatedServiceStepsTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }

                        Timber.e("onCreateView: StepsTV.SIZE->" + stepsTV.size() + "Progress.SIZE->" + progress.size());
                    }
                }

                Timber.e("onCreateView: hasConfirmed->%s", hasConfirmed);
                if (progressPercentage == 100 && !hasConfirmed) {
                    getActivity().runOnUiThread(() -> {
                        Timber.e("onCreateView: StepsTV.SIZE->" + stepsTV.size() + "Progress.SIZE->" + progress.size());

                        confirmReceiptDialog.setTitle("Confirm Receipt");
                        confirmReceiptDialog.setMessage("Please confirm that you have received the product...");
                        confirmReceiptDialog.setButton(Dialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ConfirmService(getContext(), serviceId) {
                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                                    @Override
                                    public void done(@NotNull byte[] data, int code) {

//                                        Log.e(TAG, "onClick: Confirmation done-1->"+ Arrays.toString(data));

//                                        Log.e(TAG, "onCreateView: hasConfirmed->"+hasConfirmed);

                                        hasConfirmed = true;
                                        startActivity(new Intent(getContext(), Feedback.class));

                                    }
                                };
                                confirmReceiptDialog.dismiss();
                            }
                        });
                        confirmReceiptDialog.setButton(Dialog.BUTTON_NEGATIVE, "REJECT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getContext(), ServiceIssue.class));

                                confirmReceiptDialog.dismiss();
                            }
                        });
                        confirmReceiptDialog.show();
                    });
                }
            });
        });
        return view;
    }

    private void goToDelegationChat(View view) {
        openChat = view.findViewById(R.id.open_chat_button);

        delegatedServiceViewModel
                .getDelegatedServiceByProductId(delegatedProductId).observe(getViewLifecycleOwner(), delegatedServiceEntity1 -> {
//            Log.e(TAG, "goToDelegationChat: DelegatedServiceModel"+delegatedServiceEntity1.getDelegatedProductId());
            delegatedServiceEntity1.getServiceId();
        });

        openChat.setOnClickListener(v -> {

            MixpanelAPI mixpanel =
                    MixpanelAPI.getInstance(getContext(),
                            getResources().getString(R.string.mixpanelToken));

            JSONObject chatObject = new JSONObject();
            try {
                chatObject.put("agentName", agentName);
                chatObject.put("serviceId", serviceId);
                chatObject.put("serviceName", productName);

                if (mixpanel != null) {
                    mixpanel.track("openChatTapped", chatObject);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent chatIntent = new Intent(getActivity(), DelegationChat.class);
            chatIntent.putExtra("AGENT_NAME", agentName);
            chatIntent.putExtra("SERVICE_ID", serviceId);
            chatIntent.putExtra("SERVICE_NAME", productName);
            startActivity(chatIntent);
        });

        SharedPreferences delegatedServiceFragPrefs = Objects.requireNonNull(getActivity())
                .getSharedPreferences(ummoUserPreferences, mode);

        if (getArguments() != null) {
            serviceId = getArguments().getString("SERVICE_ID");
            agentName = delegatedServiceFragPrefs.getString("DELEGATED_AGENT", "");
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            Log.e(TAG, "onAttach: LISTENER->"+mListener);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
