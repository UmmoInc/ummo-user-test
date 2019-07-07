package xyz.ummo.user.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;
import xyz.ummo.user.AllServices;
import xyz.ummo.user.Department;
import xyz.ummo.user.LinePagerIndicatorDecoration;
import xyz.ummo.user.R;
import xyz.ummo.user.Services;
import xyz.ummo.user.adapters.departmentsAdapter;
import xyz.ummo.user.adapters.servicesCarouselAdapter;
import xyz.ummo.user.delegate.PublicService;
import xyz.ummo.user.delegate.PublicServiceData;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<PublicServiceData> _data;

    private ArrayList<Department> departmentArrayList = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    private Button requestAgent;

    private servicesCarouselAdapter carouselAdapter;


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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        loadDepartments(_data);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_home, container, false);

        requestAgent = view.findViewById(R.id.request_agent_btn);

        requestAgent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent= new Intent(getContext(), AllServices.class);
                ((Activity) Objects.requireNonNull(getContext())).finish();
                getContext().startActivity(intent);

            }
        });


        //TODO get all departments and do the stuff below
        final FeatureCoverFlow coverFlow = view.findViewById(R.id.coverflow);
        coverFlow.setAdapter(new servicesCarouselAdapter(getContext(), _data));
        coverFlow.setOnScrollPositionListener(onScrollListener());



        return view;
    }

    private FeatureCoverFlow.OnScrollPositionListener onScrollListener() {
        return new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {
                Log.v("MainActiivty", "position: " + position);
            }

            @Override
            public void onScrolling() {
                Log.i("MainActivity", "scrolling");
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
    public void onAttach(Context context) {
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

    public void loadDepartments(List<PublicServiceData> data) {
        for(int i = 0; i<data.size();i++){
            departmentArrayList.add(new Department(data.get(i).getServiceName()));
        }
    }

    public void viewServices(View view){

        Intent i= new Intent(getContext(), Services.class);
        startActivity(i);

    }
}
