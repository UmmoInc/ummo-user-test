package xyz.ummo.user.ui.fragments.services

import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONException
import timber.log.Timber
import xyz.ummo.user.DelegatedService
import xyz.ummo.user.R
import xyz.ummo.user.delegate.Service
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DelegatedServicesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DelegatedServicesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DelegatedServicesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private val delegatedServiceArrayList = ArrayList<DelegatedService>()
    private var recyclerView: RecyclerView? = null
    var delegatedServiceAdapter: DelegatedServiceAdapter? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var loadDelegatedServicesProgressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments!!.getString(ARG_PARAM1)
            mParam2 = arguments!!.getString(ARG_PARAM2)
        }
        addDelegatedServices()
        delegatedServiceAdapter = DelegatedServiceAdapter(delegatedServiceArrayList)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_delegated_services, container, false)
        recyclerView = view.findViewById(R.id.delegated_services_rv)
        loadDelegatedServicesProgressBar = view.findViewById(R.id.load_delegated_services_progress_bar)
        val linearLayoutManager = LinearLayoutManager(context)
        val ATTRS = intArrayOf(android.R.attr.listDivider)
        val a = context!!.obtainStyledAttributes(ATTRS)
        val divider = a.getDrawable(0)
        val insetRight = resources.getDimensionPixelSize(R.dimen.divider_item_right)
        val insetLeft = resources.getDimensionPixelSize(R.dimen.divider_item_left)
        val insetDivider = InsetDrawable(divider, insetLeft, 0, insetRight, 0)
        a.recycle()

        //Set categoryRecyclerView
        recyclerView?.adapter = delegatedServiceAdapter
        recyclerView?.layoutManager = linearLayoutManager
        recyclerView?.itemAnimator = DefaultItemAnimator()
        val dividerItemDecoration = DividerItemDecoration(recyclerView?.context,
                linearLayoutManager.orientation)
        dividerItemDecoration.setDrawable(insetDivider)
        recyclerView?.addItemDecoration(dividerItemDecoration)
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
        //if (context instanceof OnFragmentInteractionListener) {
        //  mListener = (OnFragmentInteractionListener) context;
        //} else {
        //  throw new RuntimeException(context.toString()
        //        + " must implement OnFragmentInteractionListener");
        //}
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

    private fun addDelegatedServices() {
        object : Service(activity!!) {
            override fun done(data: ByteArray, code: Number) {
                activity!!.runOnUiThread {
                    try {
                        val jsonArray = JSONArray(String(data))
                        delegatedServiceArrayList.clear()
                        Timber.e("run: %s", String(data))
                        for (i in 0 until jsonArray.length()) {
                            val jsonObject = jsonArray.getJSONObject(i)
                            val agentObject = jsonObject.getJSONObject("agent")
                            val productObject = jsonObject.getJSONObject("product")
                            Timber.e(jsonObject.getString("_id"))
                            val delegatedService = DelegatedService(
                                    productObject.getString("product_name"),
                                    agentObject.getString("name"),
                                    jsonObject.getString("_id"),
                                    agentObject.getString("_id"),
                                    jsonObject.getString("user"),
                                    productObject.getString("_id")
                            )
                            delegatedServiceArrayList.add(delegatedService)
                            Timber.e(jsonObject.toString())
                        }
                        loadDelegatedServicesProgressBar!!.visibility = View.GONE
                        delegatedServiceAdapter!!.notifyDataSetChanged()
                    } catch (e: JSONException) {
                        Timber.e("addDelegatedServices: newService JSE -> %s", e.toString())
                    }
                }
            }
        }
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DelegatedServicesFragment.
         */
        // TODO: Rename and change types and number of parameters
        private fun newInstance(param1: String, param2: String): DelegatedServicesFragment {
            val fragment = DelegatedServicesFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}