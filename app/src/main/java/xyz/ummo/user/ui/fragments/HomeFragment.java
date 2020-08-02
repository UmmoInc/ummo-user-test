package xyz.ummo.user.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.snackbar.Snackbar;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.jetbrains.annotations.NotNull;

import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;
import xyz.ummo.user.Department;
import xyz.ummo.user.R;
import xyz.ummo.user.Services;
import xyz.ummo.user.adapters.ServiceProviderAdapter;
import xyz.ummo.user.data.entity.ServiceProviderEntity;
import xyz.ummo.user.delegate.PublicService;
import xyz.ummo.user.models.PublicServiceData;
import xyz.ummo.user.delegate.SocketIO;
import xyz.ummo.user.utilities.ServiceProviderViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "HomeFragment";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<PublicServiceData> _data;

    private ArrayList<Department> departmentArrayList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    private Button requestAgent, reloadServicesButton;

    private ServiceProviderAdapter serviceProviderAdapter;

    private ArrayList<PublicServiceData> serviceProviderList= new ArrayList<>();
    private RelativeLayout offlineLayout;

    private volatile boolean stopThread;
    Emitter connectEmitter = null;
    Emitter disconnectEmitter = null;
    RecyclerView recyclerView;

    private Handler homeHandler = new Handler();
    private ServiceProviderEntity serviceProviderEntity = new ServiceProviderEntity();
    private ServiceProviderViewModel serviceProviderViewModel;
    private ProgressBar loadServicesProgressBar;

    public HomeFragment(){}

    public HomeFragment(List<PublicServiceData> data) {
        // Required empty public constructor
        _data = data;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(List<PublicServiceData> data, String param2) {
        HomeFragment fragment = new HomeFragment(data);
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, "param1");
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MixpanelAPI mixpanel =
                MixpanelAPI.getInstance(getContext(),
                        getResources().getString(R.string.mixpanelToken));

        if (mixpanel != null) {
            mixpanel.track("homeFragment");
        }

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        serviceProviderViewModel = ViewModelProviders.of(this).get(ServiceProviderViewModel.class);

        serviceProviderAdapter = new ServiceProviderAdapter(serviceProviderList,getActivity());
//        loadDepartments(_data);
        addServiceProviders();
//        serviceProviderAdapter.addProduct();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        final FragmentActivity c = getActivity(); // TODO: 10/16/19 -> rename `c`
        recyclerView = view.findViewById(R.id.service_provider_rv);
        loadServicesProgressBar = view.findViewById(R.id.load_service_progress_bar);
        reloadServicesButton = view.findViewById(R.id.reloadServicesButton);
        offlineLayout = view.findViewById(R.id.offlineLayout);
//        serviceProviderAdapter.addProduct();
        LinearLayoutManager layoutManager = new LinearLayoutManager(c, LinearLayoutManager.VERTICAL, true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(serviceProviderAdapter);

        reloadServiceProviders(view);

//        requestAgent = view.findViewById(R.id.request_agent_btn);
//
//        requestAgent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent= new Intent(getContext(), AllServices.class);
//                ((Activity) Objects.requireNonNull(getContext())).finish();
//                getContext().startActivity(intent);
//
//            }
//        });
//
//        //TODO get all departments and do the stuff below
//        final FeatureCoverFlow coverFlow = view.findViewById(R.id.coverflow);
//        coverFlow.setAdapter(new servicesCarouselAdapter(getContext(), _data));
//        coverFlow.setOnScrollPositionListener(onScrollListener());

        return view;
    }

    private FeatureCoverFlow.OnScrollPositionListener onScrollListener() {
        return new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {
                Log.e(TAG, "FeatureCoverFlow position: " + position);
            }

            @Override
            public void onScrolling() {
                Log.e(TAG, "onScrolling");
            }
        };
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
        //if (context instanceof OnFragmentInteractionListener) {
          //  mListener = (OnFragmentInteractionListener) context;
        //} else {
          //  throw new RuntimeException(context.toString()
            //        + " must implement OnFragmentInteractionListener");
        //}
    }

    @Override
    public void onDetach() {
        connectEmitter.off(Socket.EVENT_CONNECT);
        disconnectEmitter.off(Socket.EVENT_DISCONNECT);
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

    private void loadDepartments(List<PublicServiceData> data) {
        for(int i = 0; i<data.size();i++){
            departmentArrayList.add(new Department(data.get(i).getServiceName()));
        }
    }

    public void viewServices(View view){

        Intent i= new Intent(getContext(), Services.class);
        startActivity(i);
    }

    private void addServiceProviders(){

        final String[] serviceProviderName = new String[1];
        final String[] serviceProviderId = new String[1];
        final String[] serviceProviderMunicipality = new String[1];
        final String[] serviceProviderProvince = new String[1];
        final String[] serviceProviderTown = new String[1];
        reloadData();

        Log.e(TAG, "addService: ADAPTER-COUNT [before SOCKET]->"+serviceProviderAdapter.getItemCount());

        connectEmitter = SocketIO.INSTANCE.getMSocket().on("connect", args -> { // TODO: 11/3/19 -> NullObjectReference on appInit

            Log.e(TAG, "addService: ADAPTER-COUNT [after SOCKET]->"+serviceProviderAdapter.getItemCount());

            stopTimerThread();

                serviceProviderList.clear();
                if(getActivity()==null){
                    Log.e(TAG, "addServiceProviders: Weird, getAtivity returns null here ");
                }

                reloadData();
        });

         disconnectEmitter = SocketIO.INSTANCE.getMSocket().on(Socket.EVENT_DISCONNECT, args -> {

            if(getActivity()==null){
                Log.e(TAG, "addServiceProviders: Weird two, getAtivity returns null here ");
            }

            Log.e(TAG, "addService: ADAPTER-COUNT [after ERR-SOCKET]->"+serviceProviderAdapter.getItemCount());

            startTimerThread();

            serviceProviderList.clear();

            for (int i = 0; i < serviceProviderViewModel.getServiceProviders().size(); i++) {
                serviceProviderId[0] = serviceProviderViewModel.getServiceProviders().get(i).getServiceProviderId();
                serviceProviderName[0] = serviceProviderViewModel.getServiceProviders().get(i).getServiceProviderName();
                serviceProviderProvince[0] = serviceProviderViewModel.getServiceProviders().get(i).getServiceProviderProvince();
                serviceProviderMunicipality[0] = serviceProviderViewModel.getServiceProviders().get(i).getServiceProviderMunicipality();
                serviceProviderTown[0] = serviceProviderViewModel.getServiceProviders().get(i).getServiceProviderTown();
                Log.e(TAG, "addServiceProviders (" + i + ")=>" + serviceProviderName[0]);
                PublicServiceData publicServiceData = new PublicServiceData(serviceProviderName[0], serviceProviderProvince[0], serviceProviderMunicipality[0], serviceProviderTown[0], serviceProviderId[0]);
                serviceProviderList.add(publicServiceData);
            }

            (getActivity()).runOnUiThread(() -> {
                loadServicesProgressBar.setVisibility(View.INVISIBLE);
                serviceProviderAdapter.notifyDataSetChanged();
                Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content), "Showing offline data", Snackbar.LENGTH_SHORT).show();
            });

        });
    }

    private void reloadServiceProviders(View view){

        reloadServicesButton = view.findViewById(R.id.reloadServicesButton);
        reloadServicesButton.setOnClickListener(v -> {
            loadServicesProgressBar.setVisibility(View.VISIBLE);
            offlineLayout.setVisibility(View.GONE);
            reloadData();
        });
    }

    private void startTimerThread(){
        Log.e(TAG, "startTimerThread");

        stopThread = false;
        TimerRunnable timerRunnable = new TimerRunnable(20);
        new Thread(timerRunnable).start();
    }

    private void stopTimerThread(){
        Log.e(TAG, "stopTimerThread");

        homeHandler.post(new Runnable() {
            @Override
            public void run() {
                offlineLayout.setVisibility(View.GONE);
                loadServicesProgressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        stopThread = true;
    }

    class TimerRunnable implements Runnable{
        int seconds;

        TimerRunnable(int seconds){
            this.seconds = seconds;
        }

        @Override
        public void run() {
            for (int i = 0; i < seconds; i++) {

                homeHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        serviceProviderList.clear();
                        loadServicesProgressBar.setVisibility(View.VISIBLE);
                    }
                });

                if (stopThread)
                    return;

                if (i == seconds-1){
                    homeHandler.post(() -> {
                        loadServicesProgressBar.setVisibility(View.INVISIBLE);
                        offlineLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);

                        Snackbar.make(Objects.requireNonNull(getActivity()).findViewById(android.R.id.content),
                                "Connection lost...", Snackbar.LENGTH_SHORT).show();
                        /*Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
//                                loadServicesProgressBar.setVisibility(View.VISIBLE);

                        });*/
                    });

                }
                Log.e(TAG, "run: seconds->("+i+")");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void reloadData(){
        new PublicService(getActivity()){
            @SuppressLint("LogNotTimber")
            @Override
            public void done(@NotNull List<PublicServiceData> data, @NotNull Number code) {
                serviceProviderList.clear();
                serviceProviderList.addAll(data);
                serviceProviderAdapter.notifyDataSetChanged();
//                Log.e(TAG, "done: ADAPTER-COUNT->"+"serviceProviderAdapter.getItemCount()");
                Log.e(TAG, "PUBLIC SERVICE DATA -> "+ data);

                getActivity().runOnUiThread(() -> loadServicesProgressBar.setVisibility(View.INVISIBLE));
//                    serviceProviderList.addAll(data);
            }
        };
    }
}
