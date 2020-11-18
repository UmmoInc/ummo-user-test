package xyz.ummo.user.ui.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.ummo.user.Product
import xyz.ummo.user.R
import xyz.ummo.user.adapters.AllProductsAdapter
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [GetAgent.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [GetAgent.newInstance] factory method to
 * create an instance of this fragment.
 */
class GetAgent : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var allProductsAdapter: AllProductsAdapter? = null
    private var productsRecyclerView: RecyclerView? = null
    private val products = ArrayList<Product>()
    private var mListener: OnFragmentInteractionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
        addProducts()
        allProductsAdapter = AllProductsAdapter(context, products)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_get_agent, container, false)
        productsRecyclerView = view.findViewById(R.id.all_products_rv)

        //Set productsRecyclerView
        productsRecyclerView?.adapter = allProductsAdapter
        productsRecyclerView?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        productsRecyclerView?.itemAnimator = DefaultItemAnimator()
        return view
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        //        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri?)
    }

    private fun addProducts() {
        var product = Product("Product 1", "Mbabane", "", "",
                "", "", "", "", "")
        products.add(product)
        product = Product("Product 2", "Mbabane", "", "",
                "", "", "", "", "")
        products.add(product)
        product = Product("Product 3", "Mbabane", "", "",
                "", "", "", "", "")
        products.add(product)
        product = Product("Product 4", "Mbabane", "", "",
                "", "", "", "", "")
        products.add(product)
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): GetAgent {
            val fragment = GetAgent()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}