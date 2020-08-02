package xyz.ummo.user.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import xyz.ummo.user.Product;
import xyz.ummo.user.R;
import xyz.ummo.user.adapters.AllProductsAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GetAgent.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GetAgent#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GetAgent extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private AllProductsAdapter allProductsAdapter;
    private RecyclerView productsRecyclerView;
    private ArrayList<Product> products = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    public GetAgent() {
        // Required empty public constructor
    }

    public static GetAgent newInstance(String param1, String param2) {
        GetAgent fragment = new GetAgent();
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

        addProducts();

        allProductsAdapter = new AllProductsAdapter(getContext(), products);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_get_agent, container, false);

        productsRecyclerView = view.findViewById(R.id.all_products_rv);

        //Set productsRecyclerView
        productsRecyclerView.setAdapter(allProductsAdapter);
        productsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        productsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        return view;
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
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
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

    public void addProducts(){

        Product product = new Product("Product 1", "Mbabane", "", "",
        "", "", "", "", "");
        products.add(product);

        product = new Product("Product 2", "Mbabane", "", "",
                "", "", "", "", "");
        products.add(product);

        product = new Product("Product 3", "Mbabane", "", "",
                "", "", "", "", "");
        products.add(product);

        product = new Product("Product 4", "Mbabane", "", "",
                "", "", "", "", "");
        products.add(product);
    }
}
