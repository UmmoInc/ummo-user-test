package xyz.ummo.user.ui.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import xyz.ummo.user.DelegatedService;
import xyz.ummo.user.R;
import xyz.ummo.user.adapters.DelegatedServiceAdapter;
import xyz.ummo.user.delegate.Service;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DelegatedServicesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DelegatedServicesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DelegatedServicesFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "DelegatedServicesFrag.";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<DelegatedService> delegatedServiceArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    DelegatedServiceAdapter delegatedServiceAdapter;

    private OnFragmentInteractionListener mListener;

    public DelegatedServicesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DelegatedServicesFragment.
     */
    // TODO: Rename and change types and number of parameters
    private static DelegatedServicesFragment newInstance(String param1, String param2) {
        DelegatedServicesFragment fragment = new DelegatedServicesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
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

        addDelegatedServices();
        delegatedServiceAdapter = new DelegatedServiceAdapter(delegatedServiceArrayList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_delegated_services, container, false);
        recyclerView = view.findViewById(R.id.delegated_services_rv);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext());

        int[] ATTRS = new int[]{android.R.attr.listDivider};

        TypedArray a = getContext().obtainStyledAttributes(ATTRS);
        Drawable divider = a.getDrawable(0);
        int insetRight = getResources().getDimensionPixelSize(R.dimen.divider_item_right);
        int insetLeft = getResources().getDimensionPixelSize(R.dimen.divider_item_left);
        InsetDrawable insetDivider = new InsetDrawable(divider, insetLeft, 0, insetRight, 0);
        a.recycle();

        //Set catergoryRecyclerView
        recyclerView.setAdapter(delegatedServiceAdapter);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(insetDivider  );
        recyclerView.addItemDecoration(dividerItemDecoration);

        return  view;
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

    private void addDelegatedServices(){

        new Service(getActivity()){
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray jsonArray = new JSONArray(new String(data));
                            delegatedServiceArrayList.clear();
                            for (int i= 0; i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                JSONObject agentObject = jsonObject.getJSONObject("agent");
                                JSONObject productObject = jsonObject.getJSONObject("product");

                                Log.e("_ID",jsonObject.getString("_id"));

                                DelegatedService delegatedService = new DelegatedService(productObject.
                                        getString("product_name"), agentObject.getString("name"), jsonObject.getString("_id"));
                                delegatedServiceArrayList.add(delegatedService);

                                Log.e("tag", jsonObject.toString());

                            }

                            delegatedServiceAdapter.notifyDataSetChanged();

                        }catch (JSONException e){
                            Log.e(TAG, "addDelegatedServices: newService JSE -> "+e.toString());
                        }
                    }
                });
            }
        };

    }

}
