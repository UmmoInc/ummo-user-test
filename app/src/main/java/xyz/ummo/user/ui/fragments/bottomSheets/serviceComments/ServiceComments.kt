package xyz.ummo.user.ui.fragments.bottomSheets.serviceComments

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_service_comments.*
import kotlinx.android.synthetic.main.fragment_service_comments.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.api.ServiceComment
import xyz.ummo.user.api.ViewServiceComments
import xyz.ummo.user.databinding.FragmentServiceCommentsBinding
import xyz.ummo.user.models.ServiceCommentObject
import xyz.ummo.user.rvItems.ServiceCommentItem
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.utilities.*
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ServiceComments : BottomSheetDialogFragment() {
    private lateinit var gAdapter: GroupAdapter<GroupieViewHolder>
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewBinding: FragmentServiceCommentsBinding
    private lateinit var rootView: View
    private lateinit var serviceCommentObject: ServiceCommentObject
    private lateinit var serviceId: String
    private var serviceCommentsArrayList = ArrayList<ServiceCommentObject>()
    private var serviceCommentObjectSerializable: Serializable? = null
    private lateinit var simpleDateFormat: SimpleDateFormat
    private lateinit var serviceCommentPrefs: SharedPreferences
    private lateinit var serviceName: String
    private lateinit var mixpanelAPI: MixpanelAPI

    private var profileViewModel: ProfileViewModel? = null
    private var serviceCommentsViewModel: ServiceCommentsViewModel? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gAdapter = GroupAdapter()

        mixpanelAPI = MixpanelAPI
            .getInstance(context, context?.resources?.getString(R.string.mixpanelToken))

        /*serviceCommentsViewModel = ViewModelProvider(this)
            .get(ServiceCommentsViewModel::class.java)*/

        simpleDateFormat = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

        serviceCommentPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        Timber.e("ARGUMENTS -> $arguments")
        viewBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_service_comments, container, false)
        rootView = viewBinding.root

        /** Scaffolding the [recyclerView] **/
        recyclerView = rootView.service_comment_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = rootView.service_comment_recycler_view.layoutManager
        recyclerView.adapter = gAdapter

        populateServiceCommentsRecyclerView()

        submitServiceComment(serviceId)

        initializeServiceCommentsBottomSheetTitle()

        checkForCommentsAndStopProgressBar()

        return rootView
    }

    private fun initializeServiceCommentsBottomSheetTitle() {
        /*val serviceCommentsSubtitleText =
            String.format(
                resources.getString(R.string.service_comments_subtitle),
                serviceCommentObject.serviceName
            )*/

        serviceName = arguments?.getString(SERVICE_NAME).toString()
        viewBinding.serviceCommentsHeaderSubtitleTextView.text = serviceName
    }

    private fun checkForCommentsAndStopProgressBar() {
        val timer = object : CountDownTimer(5000, 1000) {
            override fun onTick(p0: Long) {
                viewBinding.loadServiceCommentsProgressBar.visibility = View.VISIBLE
                viewBinding.noCommentsRelativeLayout.visibility = View.GONE
                viewBinding.serviceCommentNestedScrollView.visibility = View.GONE
            }

            override fun onFinish() {
                if (gAdapter.itemCount == 0) {
                    viewBinding.loadServiceCommentsProgressBar.visibility = View.GONE
                    viewBinding.noCommentsRelativeLayout.visibility = View.VISIBLE
                    viewBinding.serviceCommentNestedScrollView.visibility = View.GONE
                } else {
                    viewBinding.loadServiceCommentsProgressBar.visibility = View.GONE
                    viewBinding.noCommentsRelativeLayout.visibility = View.GONE
                    viewBinding.serviceCommentNestedScrollView.visibility = View.VISIBLE
                }
            }
        }
        timer.start()
    }

    private fun populateServiceCommentsRecyclerView() {
        if (isAdded) {
            serviceId = arguments?.get(SERVICE_ID) as String

            Timber.e("SERVICE COMMENTS ARRAY LIST -> $serviceCommentsArrayList")

            object : ViewServiceComments(serviceId) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {
                        serviceCommentsArrayList = parseServiceCommentsPayload(data)
                        Timber.e("INTERNAL SERVICE COMMENTS -> $serviceCommentsArrayList")

                        gAdapter.clear()

                        for (i in 0 until serviceCommentsArrayList.size) {
                            serviceCommentObject = serviceCommentsArrayList[i]
                            gAdapter.add(ServiceCommentItem(serviceCommentObject, context))
                            Timber.e("SERVICE COMMENTS BEING ADAPTER -> $serviceCommentObject")
                            /** Hiding the progress bar and displaying the list of comments b/c the [gAdapter]
                             * now has comments to show **/
                            viewBinding.serviceCommentNestedScrollView.visibility = View.VISIBLE
                            viewBinding.loadServiceCommentsProgressBar.visibility = View.GONE

                            viewBinding.serviceCommentNestedScrollView.smoothScrollTo(
                                0,
                                recyclerView.bottom
                            )
                        }
                    }
                }
            }
        }
    }

    private fun parseServiceCommentsPayload(data: ByteArray): ArrayList<ServiceCommentObject> {
        val serviceCommentObjects = ArrayList<ServiceCommentObject>()
        val serviceCommentsJSONArray: JSONArray
        var serviceCommentJSONObject: JSONObject
        var serviceCommentObject: ServiceCommentObject

        try {
            serviceCommentsJSONArray = JSONObject(String(data)).getJSONArray("payload")

            for (i in 0 until serviceCommentsJSONArray.length()) {
                serviceCommentJSONObject = serviceCommentsJSONArray[i] as JSONObject
//                Timber.e("SERVICE COMMENT JSON OBJ -> $serviceCommentJSONObject")

                serviceCommentObject = if (serviceCommentJSONObject.has("user_contact")) {
                    ServiceCommentObject(
                        serviceCommentJSONObject.getString("_id"),
                        serviceCommentJSONObject.getString("service_comment"),
                        serviceCommentJSONObject.getString("comment_date"),
                        serviceCommentJSONObject
                            .getJSONObject("user_contact").getString("name")
                    )
                } else {
//                    Timber.e("NO USER OBJECT FOUND!")
                    ServiceCommentObject(
                        serviceCommentJSONObject.getString("_id"),
                        serviceCommentJSONObject.getString("service_comment"),
                        serviceCommentJSONObject
                            .getString("comment_date"), "Anonymous"
                    )
                }
//                Timber.e("SERVICE COMMENT OBJECT -> $serviceCommentObject")

                serviceCommentObjects.add(serviceCommentObject)
            }
//            Timber.e("SERVICE COMMENT OBJECT [2]-> $serviceCommentObjects")

            return serviceCommentObjects
        } catch (jsonArrayException: JSONException) {
            Timber.e("Unable to parse Service Comment Payload data")
            throw jsonArrayException
        }

    }

    private fun submitServiceComment(serviceId: String) {
        viewBinding.serviceCommentTextInputLayout.setEndIconOnClickListener {
            val serviceCommentJSONObject = JSONObject()
            val serviceEventJSONObject = JSONObject()
            val serviceCommentText: String = viewBinding.serviceCommentEditText.text.toString()
            val date = simpleDateFormat.format(Date())
            val userContact: String = serviceCommentPrefs.getString(USER_CONTACT, "").toString()
            val userName: String = serviceCommentPrefs.getString(USER_NAME, "").toString()
            var newServiceCommentObject: ServiceCommentObject

            if (serviceCommentText.isNotEmpty()) {
                serviceCommentJSONObject.put("_id", serviceId)
                    .put("service_comment", serviceCommentText)
                    .put("comment_date", date)
                    .put("user_contact", userContact)

                object : ServiceComment(requireContext(), serviceCommentJSONObject) {
                    override fun done(data: ByteArray, code: Number) {
                        if (code == 200) {
                            Timber.e("SERVICE COMMENT MADE!")

                            service_comment_edit_text.text?.clear()

                            /** New ServiceComment Object; updates the recycler view **/
                            newServiceCommentObject =
                                ServiceCommentObject(
                                    serviceId,
                                    serviceCommentText,
                                    date,
                                    userName
                                )
                            gAdapter.add(ServiceCommentItem(newServiceCommentObject, context))
                            viewBinding.noCommentsRelativeLayout.visibility = View.GONE
                            gAdapter.notifyItemInserted(serviceCommentsArrayList.size + 1)

                            recyclerView.smoothScrollToPosition(gAdapter.itemCount + 1)
                            viewBinding.serviceCommentNestedScrollView.smoothScrollTo(
                                0,
                                recyclerView.bottom
                            )

                            /** Storing the comment state in Shared Preferences **/
                            val serviceCommentPrefEditor = serviceCommentPrefs.edit()
                            serviceCommentPrefEditor.putBoolean("COMMENTED-ON-$serviceId", true)
                                .apply()

                            /** Tracking [MixpanelAPI] event for service comments **/
                            serviceEventJSONObject.put(SERVICE_NAME, serviceName)
                            mixpanelAPI.track("commentedOnService", serviceEventJSONObject)

                        } else {
                            Timber.e("NO SERVICE COMMENT MADE!")
                        }
                    }
                }
            } else {
                viewBinding.serviceCommentEditText.error = "Your comment is missing..."
            }
        }
    }

    companion object {
        const val TAG = "ServiceCommentsBottomSheet"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ServiceComments().apply {
                arguments = Bundle().apply {

                }
            }
    }
}