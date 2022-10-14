package xyz.ummo.user.ui.fragments.bottomSheets.serviceComments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.service_comment_bottom_sheet.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.db.ServiceCommentsDatabase
import xyz.ummo.user.data.entity.ServiceCommentEntity
import xyz.ummo.user.data.repo.serviceSomments.ServiceCommentsRepo
import xyz.ummo.user.databinding.ServiceCommentBottomSheetBinding
import xyz.ummo.user.models.ServiceCommentObject
import xyz.ummo.user.ui.detailedService.serviceComments.ServiceCommentsViewModel
import xyz.ummo.user.ui.detailedService.serviceComments.ServiceCommentsViewModelFactory
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.utilities.*
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class ServiceComments : BottomSheetDialogFragment() {
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewBinding: ServiceCommentBottomSheetBinding
    private lateinit var rootView: View
    private lateinit var serviceCommentObject: ServiceCommentObject
    private lateinit var serviceId: String
    private var serviceCommentsArrayList = ArrayList<ServiceCommentObject>()
    private var serviceCommentObjectSerializable: Serializable? = null
    private lateinit var simpleDateFormat: SimpleDateFormat
    private lateinit var serviceCommentPrefs: SharedPreferences
    private lateinit var serviceName: String
    private lateinit var mixpanelAPI: MixpanelAPI
    private lateinit var serviceCommentsViewModel: ServiceCommentsViewModel
    private val coroutineScope = CoroutineScope((Dispatchers.Main + parentJob))

    private var profileViewModel: ProfileViewModel? = null
    private var serviceCommentsViewModelOld: ServiceCommentsViewModelOld? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            serviceId = arguments?.getString(SERVICE_ID)!!
        }

        /** [START] Instantiating [serviceCommentsViewModel] to save [serviceComments] **/
        val serviceCommentsRepo =
            ServiceCommentsRepo(ServiceCommentsDatabase(requireContext()), requireActivity())
        val serviceCommentsViewModelProviderFactory =
            ServiceCommentsViewModelFactory(serviceId, serviceCommentsRepo)

        serviceCommentsViewModel = ViewModelProvider(
            this,
            serviceCommentsViewModelProviderFactory
        )[ServiceCommentsViewModel::class.java]
        /** [END] Instantiating [serviceCommentsViewModel] to save [serviceComments] **/

        mixpanelAPI = MixpanelAPI
            .getInstance(context, context?.resources?.getString(R.string.mixpanelToken))

        /*serviceCommentsViewModel = ViewModelProvider(this)
            .get(ServiceCommentsViewModel::class.java)*/

        simpleDateFormat = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

        serviceCommentPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        Timber.e("ARGUMENTS -> $arguments")
        viewBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.service_comment_bottom_sheet,
                container,
                false
            )
        rootView = viewBinding.root

        /** Below, we're:
         * 1. Initializing the Title with the Service Name,
         * 2. Checking for comments and hiding the progress bar,
         * 3. Populating the view with service comments returned,
         * 4. Submitting a service comment with the service's ID **/
        /*initializeServiceCommentsBottomSheetTitle()
        checkForCommentsAndStopProgressBar()*/
//        populateServiceCommentsRecyclerView()
        submitServiceComment(serviceId)

        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        return rootView
    }

    private fun submitServiceComment(serviceId: String) {
        viewBinding.serviceCommentTextInputLayout.setEndIconOnClickListener {
            val serviceCommentJSONObject = JSONObject()
            val serviceEventJSONObject = JSONObject()
            val serviceCommentText: String = viewBinding.serviceCommentEditText.text.toString()
            val date = simpleDateFormat.format(Date())
            val userContact: String = serviceCommentPrefs.getString(USER_CONTACT, "").toString()
            val userName: String = serviceCommentPrefs.getString(USER_NAME, "").toString()
            val newServiceCommentEntity: ServiceCommentEntity

            if (serviceCommentText.isNotEmpty()) {
                serviceCommentJSONObject.put("_id", serviceId)
                    .put("service_comment", serviceCommentText)
                    .put("comment_date", date)
                    .put("user_contact", userContact)
                Timber.e("SERVICE COMMENT -> $serviceCommentJSONObject")

                newServiceCommentEntity =
                    ServiceCommentEntity(serviceCommentText, serviceId, date, userName, userContact)

                coroutineScope.launch(Dispatchers.IO) {
                    serviceCommentsViewModel.saveServiceCommentFromUserToRoom(
                        newServiceCommentEntity
                    )

                    /** Hiding Soft Input Keyboard Window below **/
                    val inputMethodManager: InputMethodManager =
                        requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)

                    this@ServiceComments.dismiss()
                    viewBinding.serviceCommentEditText.clearComposingText()
                }
            } else {
                viewBinding.serviceCommentEditText.error = "Your comment is missing..."
            }
        }
    }

    companion object {
        const val TAG = "ServiceCommentsBottomSheet"
        private val parentJob = Job()

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ServiceComments().apply {
                arguments = Bundle().apply {

                }
            }
    }
}