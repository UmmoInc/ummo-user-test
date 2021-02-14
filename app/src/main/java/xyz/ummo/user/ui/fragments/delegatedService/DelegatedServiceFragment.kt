package xyz.ummo.user.ui.fragments.delegatedService

import android.app.AlertDialog
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.kofigyan.stateprogressbar.StateProgressBar
import kotlinx.android.synthetic.main.confirm_service_delivered_view.*
import kotlinx.android.synthetic.main.confirm_service_delivered_view.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.DelegatedServiceEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.AppBarDelegatedScreenBinding
import xyz.ummo.user.databinding.ConfirmServiceDeliveredViewBinding
import xyz.ummo.user.databinding.FragmentDelegatedBinding
import xyz.ummo.user.delegate.DelegationFeedback
import xyz.ummo.user.delegate.User.Companion.SERVICE_STATE
import xyz.ummo.user.delegate.User.Companion.mode
import xyz.ummo.user.delegate.User.Companion.ummoUserPreferences
import xyz.ummo.user.ui.detailedService.DetailedProductViewModel
import xyz.ummo.user.ui.fragments.pagesFrags.PagesFragment
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.RatingSentEvent
import xyz.ummo.user.utilities.eventBusEvents.ServiceUpdateEvents

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [DelegatedServiceFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [DelegatedServiceFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DelegatedServiceFragment : Fragment {
    // TODO: Rename and change types of parameters
    private val mParam1: String? = null
    private val mParam2: String? = null
    private var agentName: String? = null
    private var productName: String? = null
    private var delegationId: String? = null
    private var serviceAgentId: String? = null

    //    private var delegatedProductId: String? = null
    private var delegatedServiceId: String? = null
    private var serviceState: Int = 0
    private var agentNameTextView: TextView? = null
    private var agentStatusTextView: TextView? = null
    private var delegatedProductNameTextView: TextView? = null
    private var delegatedProductDescriptionTextView: TextView? = null
    private var delegatedProductCostTextView: TextView? = null

    private val delegatedServiceDocsTextView: TextView? = null
    private var delegatedServiceStepsTextView: TextView? = null
    private var progressBar: ProgressBar? = null
    private val stepsTV = ArrayList<TextView>()
    var stepsList: ArrayList<String>? = null
    var docsList: ArrayList<String>? = null
    private val delegatedProductDocsLayout: LinearLayout? = null
    private var delegatedProductStepsLayout: LinearLayout? = null
    private var openChat: ImageView? = null
    private var mListener: OnFragmentInteractionListener? = null
    private var delegatedServiceViewModel: DelegatedServiceViewModel? = null
    private var detailedProductViewModel: DetailedProductViewModel? = null
    private var serviceViewModel: ServiceViewModel? = null
    private var delegatedServiceEntity = DelegatedServiceEntity()
    private var confirmReceiptDialog: AlertDialog? = null
    private var hasConfirmed = false
    private var toolbar: MaterialToolbar? = null
    private var countOfDelegatedServices: Int = 0
    private lateinit var stateProgressBar: StateProgressBar

    private lateinit var viewBinding: FragmentDelegatedBinding
    private lateinit var appBarBinding: AppBarDelegatedScreenBinding
    private lateinit var rateDelegationBinding: ConfirmServiceDeliveredViewBinding
    private lateinit var editor: SharedPreferences.Editor

    private var ratingSentEvent = RatingSentEvent()

    constructor(entity: DelegatedServiceEntity) {
        delegatedServiceEntity = entity
    }

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        confirmReceiptDialog = AlertDialog.Builder(context).create()
        appBarBinding = AppBarDelegatedScreenBinding.inflate(layoutInflater)
        rateDelegationBinding = ConfirmServiceDeliveredViewBinding.inflate(layoutInflater)

        delegatedServiceViewModel = ViewModelProvider(this)
                .get(DelegatedServiceViewModel::class.java)

        countOfDelegatedServices = delegatedServiceViewModel!!.getCountOfDelegatedServices()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        viewBinding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_delegated, container, false)

        rateDelegationBinding = DataBindingUtil
                .inflate(inflater, R.layout.confirm_service_delivered_view, container, false)

        val view = viewBinding.root

        val sharedPreferences = (activity)?.getSharedPreferences(ummoUserPreferences, mode)

        delegatedServiceId = sharedPreferences!!.getString("DELEGATED_SERVICE_ID", "")
        serviceState = sharedPreferences.getInt(SERVICE_STATE, 0)
        Timber.e("SERVICE - STATE => $serviceState")

        serviceViewModel = ViewModelProvider(this).get(ServiceViewModel::class.java)

        /** The commented block below will be used in a later version of the app **/
        /*agentNameTextView = viewBinding.delegatedAgentNameTextView
        agentStatusTextView = viewBinding.delegatedAgentStatusTextView
        openChat = viewBinding.openChatButton
        progressBar = viewBinding.serviceProgressBar*/

        delegatedProductNameTextView = viewBinding.delegatedServiceHeaderName
        delegatedProductDescriptionTextView = viewBinding.descriptionTextView
        delegatedProductCostTextView = viewBinding.serviceCostTextView
//        delegatedProductStepsLayout = view/**/Binding.delegatedServiceStepsLayout
        stateProgressBar = viewBinding.delegationStateProgressBar

        /** Defining the states to be shown on the [StateProgressBar].
         *  Then setting the animation duration of the state transitions. **/
        val delegationStates = arrayOf("PENDING", "IN-PROGRESS", "DONE", "DELIVERED")
        stateProgressBar.setStateDescriptionData(delegationStates)
        stateProgressBar.animationDuration = 1000

        agentNameTextView?.text = agentName

        updateServiceState()

//        goToDelegationChat()

        when {
            /** Displaying Delegated Service from the arguments passed through
             * from [ServiceItem] **/
            arguments != null -> {

                viewBinding.delegationLayout.visibility = View.VISIBLE
                viewBinding.noDelegationLayout.visibility = View.GONE

                delegationId = arguments!!.getString("DELEGATION_ID")
                serviceAgentId = arguments!!.getString("SERVICE_AGENT_ID")
                delegatedServiceId = arguments!!.getString("DELEGATED_SERVICE_ID")

                Timber.e("DELEGATION_ID -> $delegatedServiceId")
                Timber.e("DELEGATED_SERVICE_ID -> $delegationId")
                Timber.e("SERVICE_AGENT_ID -> $serviceAgentId")

                delegatedServiceViewModel = ViewModelProvider(this)
                        .get(DelegatedServiceViewModel::class.java)

                Timber.e("onCreate: Service id%s", delegationId)

//            delegatedServiceEntity = delegatedServiceViewModel.getDelegatedServiceEntityLiveData();//getDelegatedServiceById(serviceId).getValue();
                /*detailedProductViewModel = ViewModelProvider(this)
                        .get(DetailedProductViewModel::class.java)*/

                inflateDelegatedServiceView(delegatedServiceId)

            }

            /** Displaying Delegated Service from Room (local) **/
            countOfDelegatedServices > 0 -> {
                viewBinding.delegationLayout.visibility = View.VISIBLE
                viewBinding.noDelegationLayout.visibility = View.GONE
                Timber.e("DELEGATED---SERVICE---ID-> $delegatedServiceId")
                inflateDelegatedServiceView(delegatedServiceId)
            }
            /** Display the absence of any service **/
            else -> {
                viewBinding.noDelegationLayout.visibility = View.VISIBLE
                viewBinding.delegationLayout.visibility = View.GONE

                takeMeHome()
            }
        }

//        Timber.e("onCreateView: New SERVICE id%s", delegatedServiceId)
        /** {START} Filling in View with DetailedProduct elements **/
        /*detailedProductViewModel!!.getProductEntityLiveDataById(delegatedProductId)
                .observe(viewLifecycleOwner, { delegatedProductEntity: ProductEntity ->

                    //TODO: BUG!
                    productName = delegatedProductEntity.productName
                    //Timber.e("onCreateView: DELEGATED PRODUCT->%s", delegatedProductEntity.productDuration)
                    delegatedProductNameTextView?.text = delegatedProductEntity.productName
                    delegatedProductDescriptionTextView?.text = delegatedProductEntity.productDescription
                    delegatedProductCostTextView?.text = delegatedProductEntity.productCost
                    //            delegatedProductDurationTextView.setText(delegatedProductEntity.getProductDuration());

//            docsList = new ArrayList<>(delegatedProductEntity.getProductDocuments());
                    *//*if (!docsList.isEmpty()) {
                        delegatedProductDocsLayout.removeAllViews();
                        for (int i = 0; i < docsList.size(); i++) {
                            delegatedServiceDocsTextView = new TextView(getContext());
                            delegatedServiceDocsTextView.setId(i);
                            delegatedServiceDocsTextView.setText(delegatedProductEntity.getProductDocuments().get(i));
                            delegatedServiceDocsTextView.setTextSize(14);
                            delegatedProductDocsLayout.addView(delegatedServiceDocsTextView);
                        }
                    }*//*
                    stepsList = ArrayList(delegatedProductEntity.productSteps)
                    if (stepsList!!.isNotEmpty()) {
                        delegatedProductStepsLayout?.removeAllViews()
                        stepsTV.clear()
                        for (i in stepsList!!.indices) {
                            delegatedServiceStepsTextView = TextView(context)
                            delegatedServiceStepsTextView!!.id = i
                            delegatedServiceStepsTextView!!.text = delegatedProductEntity.productSteps[i]
                            delegatedServiceStepsTextView!!.textSize = 14f
                            delegatedProductStepsLayout?.addView(delegatedServiceStepsTextView)
                            stepsTV.add(delegatedServiceStepsTextView!!)
                            delegatedServiceViewModel!!.delegatedServiceEntityLiveData?.observe(viewLifecycleOwner, Observer { delegatedServiceEntity1: DelegatedServiceEntity ->
                                Timber.e("onCreateView: Steps ${delegatedServiceEntity1.serviceProgress}  ${delegatedServiceStepsTextView!!.text}")
                                if (delegatedServiceEntity1.serviceProgress.contains(delegatedServiceStepsTextView!!.text.toString())) {
                                    Timber.e("onCreateView: Cross")
                                    delegatedServiceStepsTextView!!.paintFlags = delegatedServiceStepsTextView!!.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                }
                            })
                        }
                    }

                    delegatedServiceViewModel!!.getDelegatedServiceById(serviceId)
                            .observe(viewLifecycleOwner, { delegatedServiceEntity1: DelegatedServiceEntity ->
                                val progress = delegatedServiceEntity1.serviceProgress
                                //Log.e(TAG, "onCreate: DELEGATED-SERVICE-ENTITY-LIVE-DATA->"+stepsTV.size()+" "+delegatedServiceEntity1.getServiceProgress().size());
                                */
        /**
         * This below 'for-loop' undoes the striking out of service steps (which should not be a likely scenario to occur)
         *//*
                                for (i in stepsTV.indices) {
                                    stepsTV[i].paintFlags = delegatedServiceStepsTextView!!.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                                }

                                */
        /**
         *This line below correlates the progress with the number of steps checked off
         **//*
                                val progressPercentage = progress.size / stepsTV.size * 100
                                progressBar?.progress = progress.size * 100 / stepsTV.size
                                Timber.e("onCreateView: ->%s", progressPercentage)

                                */
        /**
         *This below 'for-loop' strikes out service steps (according to the agent's progress)
         **//*
                                for (i in progress.indices) {
                                    for (j in stepsTV.indices) {
                                        if (progress.contains(stepsTV[j].text.toString())) {
                                            stepsTV[j].paintFlags = delegatedServiceStepsTextView!!.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                        }
                                        Timber.e("onCreateView: StepsTV.SIZE-> ${stepsTV.size} Progress.SIZE-> + ${progress.size}")
                                    }
                                }
                                Timber.e("onCreateView: hasConfirmed->%s", hasConfirmed)
                                if (progressPercentage == 100 && !hasConfirmed) {
                                    activity!!.runOnUiThread {
                                        Timber.e("onCreateView: StepsTV.SIZE-> ${stepsTV.size} Progress.SIZE-> ${progress.size}")
                                        confirmReceiptDialog!!.setTitle("Confirm Receipt")
                                        confirmReceiptDialog!!.setMessage("Please confirm that you have received the product...")
                                        confirmReceiptDialog!!.setButton(Dialog.BUTTON_POSITIVE, "Confirm", DialogInterface.OnClickListener { dialog, which ->
                                            object : ConfirmService((context)!!, (serviceId)!!) {
                                                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                                                override fun done(data: ByteArray, code: Int) {

//                                        Log.e(TAG, "onClick: Confirmation done-1->"+ Arrays.toString(data));

//                                        Log.e(TAG, "onCreateView: hasConfirmed->"+hasConfirmed);
                                                    hasConfirmed = true
                                                    startActivity(Intent(context, Feedback::class.java))
                                                }
                                            }
                                            confirmReceiptDialog!!.dismiss()
                                        })
                                        confirmReceiptDialog!!.setButton(Dialog.BUTTON_NEGATIVE, "REJECT") { dialog, which ->
                                            startActivity(Intent(context, ServiceIssue::class.java))
                                            confirmReceiptDialog!!.dismiss()
                                        }
                                        confirmReceiptDialog!!.show()
                                    }
                                }
                            })
                })*/
        /** {END} Filling in View with DetailedProduct elements **/

        //TODO: Review this code snippet
        /*serviceViewModel!!.getServiceEntityLiveDataById(delegatedServiceId!!)
                .observe(viewLifecycleOwner, {delegatedServiceEntity: ServiceEntity ->
                    productName = delegatedServiceEntity.serviceName

                    delegatedProductNameTextView?.text = delegatedServiceEntity.serviceName
                    delegatedProductDescriptionTextView?.text = delegatedServiceEntity.serviceDescription
                    delegatedProductCostTextView?.text = delegatedServiceEntity.serviceCost

                })*/

        return view
    }

    /** With this function, we're displaying the service-delivery confirmation.
     * Within it, we're passing the $rating && $review values to the [rateDelegation] function.
     * Then we transmit (via [EventBus]), the [ratingSentEvent] to MainScreen for displaying snackbar.
     * Finally, we then [clearDelegationCache] to remove the [DelegatedServiceEntity]. **/
    private fun confirmServiceDelivery() {
        val confirmServiceDeliveryDialogBuilder = MaterialAlertDialogBuilder(context!!)
        val confirmServiceDeliveryView = LayoutInflater
                .from(context).inflate(R.layout.confirm_service_delivered_view, null)

        confirmServiceDeliveryDialogBuilder.setTitle("Confirm Service Delivery")
                .setIcon(R.drawable.logo).setView(confirmServiceDeliveryView)
                .setPositiveButton("SEND") { dialogInterface, i ->

                    val delegationFeedbackText = confirmServiceDeliveryView
                            .findViewById<TextInputEditText>(R.id.delegation_feedback_text_field)
                    val delegationRatingBar = confirmServiceDeliveryView.findViewById<RatingBar>(R.id.delegation_rating_bar)
                    Timber.e("RATING VALUE -> ${delegationRatingBar.rating}")
                    Timber.e("CONFIRMING DELIVERY -> ${delegationFeedbackText.text?.trim()}")

                    rateDelegation("6028fe03c5f96eff464ab55b",
                            delegationRatingBar.rating.toInt(),
                            delegationFeedbackText.text?.trim().toString())

                    ratingSentEvent.ratingSent = true
                    EventBus.getDefault().post(ratingSentEvent)

                    clearDelegationCache()
                }
                .setNegativeButton("LATER") { dialogInterface, i ->
                    Timber.e("REJECTING DELIVERY")
                }

        confirmServiceDeliveryDialogBuilder.show()
    }

    /** [rateDelegation] captures the [delegationId], the delegation rating & reviews;
     * Sends them to the back-end via [DelegationFeedback]
     * TODO: needs to be moved to AppRepo **/
    private fun rateDelegation(mDelegationId: String, mRating: Int, mReview: String) {
        object : DelegationFeedback(mDelegationId, mRating, mReview) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200 || code == 201) {
                    Timber.e("DELEGATION RATED -> ${String(data)}!")
                } else {
                    Timber.e("ERROR RATING DELEGATION -> $code")
                    Timber.e("ERROR RATING DELEGATION -> ${String(data)}")
                }
            }
        }
    }

    /** In order to update the [serviceState], we check the sharedPreferences for the state and
     * make the necessary UI changes to reflect the current state (via [stateProgressBar].**/
    private fun updateServiceState() {
        val sharedPreferences = (activity)?.getSharedPreferences(ummoUserPreferences, mode)

        when (sharedPreferences?.getInt(SERVICE_STATE, 0)) {
            -1 -> {
//                viewBinding.delegationStateProgressBar.backgroundColor = context!!.resources.getColor(R.color.Grey)
//                viewBinding.delegationStateProgressBar.setMaxStateNumber(StateProgressBar.StateNumber.TWO)
                viewBinding.delegationStateProgressBar.isEnabled = false
                viewBinding.delegationStateProgressBar.isActivated = false
                viewBinding.delegationStateProgressBar.visibility = View.GONE
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.VISIBLE
                Timber.e("updatedServiceState DELAYED")
            }
            0 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.ONE)
                Timber.e("updatedServiceState PENDING")

            }
            1 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.TWO)
                Timber.e("updatedServiceState STARTED")

            }
            2 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.THREE)
                Timber.e("updatedServiceState DONE")
            }
            3 -> {
                viewBinding.serviceInterruptedRelativeLayout.visibility = View.GONE
                viewBinding.delegationStateProgressBar.visibility = View.VISIBLE
                viewBinding.delegationStateProgressBar.setCurrentStateNumber(StateProgressBar.StateNumber.FOUR)
                Timber.e("updatedServiceState DELIVERED")

                confirmServiceDelivery()

            }
        }
    }

    @Subscribe
    fun onServiceStateChange(serviceUpdateEvents: ServiceUpdateEvents) {

        val sharedPreferences = context!!.getSharedPreferences(ummoUserPreferences, mode)
        val editor = sharedPreferences!!.edit()

        when (serviceUpdateEvents.serviceObject.getString("status")) {
            "PENDING" -> {
                editor.putInt(SERVICE_STATE, 0).apply()
                Timber.e("onServiceState PENDING")
            }
            "STARTED" -> {
                editor.putInt(SERVICE_STATE, 1).apply()
                Timber.e("onServiceState STARTED")

            }
            "DELAYED" -> {
                editor.putInt(SERVICE_STATE, -1).apply()
                Timber.e("onServiceState DELAYED")

            }
            "DONE" -> {
                editor.putInt(SERVICE_STATE, 2).apply()
                Timber.e("onServiceState DONE")

            }
            "DELIVERED" -> {
                editor.putInt(SERVICE_STATE, 3).apply()
                Timber.e("onServiceState DELIVERED")

            }
        }
    }

    /** Once the User has rated & reviewed a service, we clear it off the phone's memory by removing
     * the value from RoomDB **/
    private fun clearDelegationCache() {

        delegatedServiceViewModel!!.deleteAllDelegatedServices()

        viewBinding.noDelegationLayout.visibility = View.VISIBLE
        viewBinding.delegationLayout.visibility = View.GONE

    }

    private fun inflateDelegatedServiceView(delegatedServiceId: String?) {
        serviceViewModel!!.getServiceEntityLiveDataById(delegatedServiceId!!)
                .observe(viewLifecycleOwner, { serviceEntity: ServiceEntity ->
                    delegatedProductNameTextView!!.text = serviceEntity.serviceName
                    delegatedProductDescriptionTextView!!.text = serviceEntity.serviceDescription
                    delegatedProductCostTextView!!.text = serviceEntity.serviceCost
                })
    }

    private fun takeMeHome() {
        viewBinding.homeButton.setOnClickListener {
            val pagesFragment: Fragment = PagesFragment()

            val fm: FragmentManager = activity?.supportFragmentManager!!
            val transaction: FragmentTransaction = fm.beginTransaction()
            transaction.replace(R.id.frame, pagesFragment)
            transaction.commit()
        }
    }

    /** The function below will be used for a later version of the app **/
    /*private fun goToDelegationChat() {
        openChat = viewBinding.openChatButton
        delegatedServiceViewModel
                ?.getDelegatedServiceByProductId(delegatedProductId)?.observe(viewLifecycleOwner, Observer { delegatedServiceEntity1: DelegatedServiceEntity ->
//            Log.e(TAG, "goToDelegationChat: DelegatedServiceModel"+delegatedServiceEntity1.getDelegatedProductId());
                    delegatedServiceEntity1.serviceId
                })
        openChat?.setOnClickListener(View.OnClickListener { v: View? ->
            val mixpanel = MixpanelAPI.getInstance(context,
                    resources.getString(R.string.mixpanelToken))
            val chatObject = JSONObject()

            try {
                chatObject.put("agentName", agentName)
                chatObject.put("serviceId", serviceId)
                chatObject.put("serviceName", productName)
                mixpanel?.track("openChatTapped", chatObject)

            } catch (e: JSONException) {
                e.printStackTrace()
            }
            val chatIntent = Intent(activity, DelegationChat::class.java)
            chatIntent.putExtra("AGENT_NAME", agentName)
            chatIntent.putExtra("SERVICE_ID", serviceId)
            chatIntent.putExtra("SERVICE_NAME", productName)
            startActivity(chatIntent)
        })
        val delegatedServiceFragPrefs = Objects.requireNonNull(activity)
                ?.getSharedPreferences(ummoUserPreferences, mode)
        if (arguments != null) {
            serviceId = arguments!!.getString("SERVICE_ID")
            agentName = delegatedServiceFragPrefs?.getString("DELEGATED_AGENT", "")
        }
    }*/

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri?) {
        if (mListener != null) {
            mListener!!.onFragmentInteraction(uri)
        }
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
         * @return A new instance of fragment DelegatedServiceFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): DelegatedServiceFragment {
            val fragment = DelegatedServiceFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            Timber.e("newInstance: SetArgument%s", args)
            fragment.arguments = args
            return fragment
        }
    }

}