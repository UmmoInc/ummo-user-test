package xyz.ummo.user.rvItems

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_card.view.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.delegate.ServiceComment
import xyz.ummo.user.delegate.UpdateService
import xyz.ummo.user.models.Service
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import java.text.SimpleDateFormat
import java.util.*

class ServiceItem(private val service: Service,
                  val context: Context?,
                  savedUserActions: JSONObject) : Item<GroupieViewHolder>() {

    /** Shared Preferences for storing user actions **/
    private lateinit var serviceItemPrefs: SharedPreferences
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences: String = "UMMO_USER_PREFERENCES"
    private var upVote: Boolean = false
    private var downVote: Boolean = false
    private var commentedOn: Boolean = false

    init {
        upVote = savedUserActions.getBoolean("UP-VOTE")
        downVote = savedUserActions.getBoolean("DOWN-VOTE")
        commentedOn = savedUserActions.getBoolean("COMMENTED-ON")
    }

    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")

    /** Initializing ServiceViewModel **/
    private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
            .get(ServiceViewModel::class.java)

    private var serviceEntity = ServiceEntity()

    private var profileViewModel = ViewModelProvider(context as FragmentActivity)
            .get(ProfileViewModel::class.java)

    private var profileEntity = ProfileEntity()

    override fun getLayout(): Int {
        return R.layout.service_card
    }

    @SuppressLint("SimpleDateFormat")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        Timber.e("UP-VOTE -> $upVote")
        Timber.e("DOWN-VOTE -> $downVote")
        Timber.e("COMMENTED-ON -> $commentedOn")

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!

        viewHolder.itemView.service_title_text_view.text = service.serviceName //1
        viewHolder.itemView.service_description_text_view.text = service.serviceDescription //2
        viewHolder.itemView.service_eligibility_text_view.text = service.serviceEligibility //3
        viewHolder.itemView.service_centre_text_view.text = service.serviceCentre.toString() //4
        viewHolder.itemView.service_cost_text_view.text = service.serviceCost //6
        viewHolder.itemView.service_duration_text_view.text = service.serviceDuration //7
        viewHolder.itemView.service_requirements_text_view.text = service.serviceDocuments.toString() //8
        viewHolder.itemView.approve_count_text_view.text = service.usefulCount.toString() //9
        viewHolder.itemView.disapprove_count_text_view.text = service.notUsefulCount.toString() //10
        viewHolder.itemView.service_comments_count_text_view.text = service.serviceComments.size.toString() //11
        viewHolder.itemView.share_count_text_view.text = service.serviceShareCount.toString() //12

        /** Date chunk below is being used to capture a selection's time-stamp **/
        val simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        val currentDate = simpleDateFormat.format(Date())

        //TODO: Assign serviceEntity to serviceValues
        assignServiceEntity(serviceEntity)
        getUserProfile()

        /** The below two methods check for any actions the user might have taken before a
         * click-action is lodged:
         * 1. If a service-up-vote || service-down-vote exists, deactivate the UP-VOTE option
         * 2. else, leave things as they're... **/
        if (upVote) {
            upVoteTriggeredChangeStates(viewHolder)
        } else {

            viewHolder.itemView.approve_service_image.isClickable = true
            viewHolder.itemView.approve_service_image.isEnabled = true
            viewHolder.itemView.approve_service_relative_layout.isClickable = true
            viewHolder.itemView.approve_service_relative_layout.isEnabled = true
            viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_outline_thumb_up_blue_24)
            viewHolder.itemView.you_up_voted_this_text_view.visibility = View.INVISIBLE
        }

        if (downVote) {
            downVoteTriggeredChangeStates(viewHolder)
        } else {
            viewHolder.itemView.disapprove_service_image.isEnabled = true
            viewHolder.itemView.disapprove_service_image.isClickable = true
            viewHolder.itemView.disapprove_service_relative_layout.isEnabled = true
            viewHolder.itemView.disapprove_service_relative_layout.isClickable = true
            viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_outline_thumb_down_red_24)
            viewHolder.itemView.you_downvoted_this_text_view.visibility = View.INVISIBLE
        }

        if (commentedOn) {
            commentTriggeredChangeStates(viewHolder)
        } else {
            viewHolder.itemView.service_comments_count_text_view.text = serviceEntity.commentCount.toString()
            viewHolder.itemView.service_comments_image.setImageResource(R.drawable.ic_outline_chat_bubble_blue_24)
            viewHolder.itemView.you_commented_on_this_text_view.visibility = View.INVISIBLE
        }

        /** Expand Service Card to reveal more info - Layout-Click... **/
        viewHolder.itemView.action_layout.setOnClickListener {
            if (viewHolder.itemView.action_text_view.text == "MORE INFO") {
                viewHolder.itemView.expandable_relative_layout.visibility = View.VISIBLE
                viewHolder.itemView.expand_image_view.visibility = View.GONE
                viewHolder.itemView.collapse_image_view.visibility = View.VISIBLE
                viewHolder.itemView.action_text_view.text = "CLOSE"
            } else if (viewHolder.itemView.action_text_view.text == "CLOSE") {
                viewHolder.itemView.expandable_relative_layout.visibility = View.GONE
                viewHolder.itemView.expand_image_view.visibility = View.VISIBLE
                viewHolder.itemView.collapse_image_view.visibility = View.GONE
                viewHolder.itemView.action_text_view.text = "MORE INFO"
            }
        }

        /** Expand Service Card to reveal more info - Text-Click... **/
        viewHolder.itemView.action_text_view.setOnClickListener {
            if (viewHolder.itemView.action_text_view.text == "MORE INFO") {
                viewHolder.itemView.expandable_relative_layout.visibility = View.VISIBLE
                viewHolder.itemView.expand_image_view.visibility = View.GONE
                viewHolder.itemView.collapse_image_view.visibility = View.VISIBLE
                viewHolder.itemView.action_text_view.text = "CLOSE"
            } else if (viewHolder.itemView.action_text_view.text == "CLOSE") {
                viewHolder.itemView.expandable_relative_layout.visibility = View.GONE
                viewHolder.itemView.expand_image_view.visibility = View.VISIBLE
                viewHolder.itemView.collapse_image_view.visibility = View.GONE
                viewHolder.itemView.action_text_view.text = "MORE INFO"
            }
        }

        /** [1] Approve Service Click Handlers **/
        viewHolder.itemView.approve_service_relative_layout.setOnClickListener {
            viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
            upVoteService(currentDate)

            /** #UX Trigger icon-change on click **/
            upVoteTriggeredChangeStates(viewHolder)
        }
        viewHolder.itemView.approve_service_image.setOnClickListener {
            viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
            upVoteService(currentDate)

            /** #UX Trigger icon-change on click **/
            upVoteTriggeredChangeStates(viewHolder)
        }

        /** [2] Disapprove Service Click Handlers **/
        viewHolder.itemView.disapprove_service_relative_layout.setOnClickListener {
            viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()
            downVoteService(currentDate)

            /** #UX Trigger icon-change on click **/
            downVoteTriggeredChangeStates(viewHolder)
        }

        viewHolder.itemView.disapprove_service_image.setOnClickListener {
            viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()
            downVoteService(currentDate)

            /** #UX Trigger icon-change on click **/
            downVoteTriggeredChangeStates(viewHolder)
        }

        /** [3] Service Feedback Click Handlers **/
        viewHolder.itemView.service_comments_relative_layout.setOnClickListener {
            makeServiceComment(currentDate)
            commentTriggeredChangeStates(viewHolder)
        }

        viewHolder.itemView.service_comments_image.setOnClickListener {
            makeServiceComment(currentDate)
            commentTriggeredChangeStates(viewHolder)
        }

        /** [4] Service Feedback Click Handlers **/
        viewHolder.itemView.share_service_relative_layout.setOnClickListener {
            Timber.e("Service Share-Layout!")
        }
        viewHolder.itemView.share_service_image.setOnClickListener {
            Timber.e("Service Share-Image!")
        }
    }

    /** When the user Up-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $upVote option, to prevent them from up-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $downVote option, in case they want to change their minds
     * TODO: Allow the user to undo their upVote by reversing the action altogether **/
    private fun upVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.approve_service_image.isClickable = false
        viewHolder.itemView.approve_service_image.isEnabled = false
        viewHolder.itemView.approve_service_relative_layout.isClickable = false
        viewHolder.itemView.approve_service_relative_layout.isEnabled = false
        viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_thumb_up_filled_24)
        viewHolder.itemView.you_up_voted_this_text_view.visibility = View.VISIBLE

        viewHolder.itemView.disapprove_service_image.isClickable = true
        viewHolder.itemView.disapprove_service_image.isEnabled = true
        viewHolder.itemView.disapprove_service_relative_layout.isClickable = true
        viewHolder.itemView.disapprove_service_relative_layout.isEnabled = true
        viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_outline_thumb_down_red_24)
        viewHolder.itemView.you_downvoted_this_text_view.visibility = View.INVISIBLE

    }

    /** When the user Down-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $downVote option, to prevent them from down-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $upVote option, in case they want to change their minds
     *  TODO: Allow the user to undo their upVote by reversing the action altogether **/
    private fun downVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.disapprove_service_image.isEnabled = false
        viewHolder.itemView.disapprove_service_image.isClickable = false
        viewHolder.itemView.disapprove_service_relative_layout.isEnabled = false
        viewHolder.itemView.disapprove_service_relative_layout.isClickable = false
        viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_thumb_down_filled_24)
        viewHolder.itemView.you_downvoted_this_text_view.visibility = View.VISIBLE

        viewHolder.itemView.approve_service_image.isClickable = true
        viewHolder.itemView.approve_service_image.isEnabled = true
        viewHolder.itemView.approve_service_relative_layout.isClickable = true
        viewHolder.itemView.approve_service_relative_layout.isEnabled = true
        viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_outline_thumb_up_blue_24)
        viewHolder.itemView.you_up_voted_this_text_view.visibility = View.INVISIBLE
    }

    /** When the user makes a comment, we want them to immediately see a UI-state change that
     * lets them know that their action has been captured:
     * 1. We update the comment icon to a filled one
     * 2. We let them know that they've commented by showing them a text; **/
    private fun commentTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.service_comments_image.setImageResource(R.drawable.ic_chat_bubble_filled_24)
        viewHolder.itemView.you_commented_on_this_text_view.visibility = View.VISIBLE
    }

    private fun assignServiceEntity(mServiceEntity: ServiceEntity) {
        mServiceEntity.serviceId = service.serviceId //0
        mServiceEntity.serviceName = service.serviceName //1
        mServiceEntity.serviceDescription = service.serviceDescription //2
        mServiceEntity.serviceEligibility = service.serviceEligibility //3
        mServiceEntity.serviceCentres = service.serviceCentre //4
        mServiceEntity.presenceRequired = service.presenceRequired //5
        mServiceEntity.serviceCost = service.serviceCost //6
        mServiceEntity.serviceDocuments = service.serviceDocuments //7
        mServiceEntity.serviceDuration = service.serviceDuration //8
        mServiceEntity.usefulCount = service.usefulCount //9
        mServiceEntity.notUsefulCount = service.notUsefulCount //10
        mServiceEntity.serviceComments = service.serviceComments //11
        mServiceEntity.commentCount = service.serviceCommentCount //12
        mServiceEntity.serviceShares = service.serviceShareCount //13
        mServiceEntity.serviceViews = service.serviceViewCount //14
        mServiceEntity.serviceProvider = service.serviceProvider //15
    }

    private fun upVoteService(date: String) {

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val upVoteEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        upVoteEditor.putBoolean("UP-VOTE-${serviceEntity.serviceId}", true)
                .putBoolean("DOWN-VOTE-${serviceEntity.serviceId}", false).apply()

        serviceEntity.usefulCount = serviceEntity.usefulCount?.plus(1)
        Timber.e("SERVICE-USEFUL -> ${serviceEntity.usefulCount}")
        serviceViewModel.updateService(serviceEntity)

        makeServiceUpdate("THUMBS_UP", date)
    }

    private fun undoServiceUseful() {
        /** Check if the count is not zero - because we can't have a negative count **/
        if (serviceEntity.usefulCount != 0) {
            serviceEntity.usefulCount = serviceEntity.usefulCount?.minus(1)
        }

        Timber.e("SERVICE-USEFUL -> ${serviceEntity.usefulCount}")
        serviceViewModel.updateService(serviceEntity)

    }

    private fun downVoteService(date: String) {

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val downVoteEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        downVoteEditor.putBoolean("UP-VOTE-${serviceEntity.serviceId}", false)
                .putBoolean("DOWN-VOTE-${serviceEntity.serviceId}", true).apply()

        serviceEntity.notUsefulCount = serviceEntity.notUsefulCount?.plus(1)
        Timber.e("SERVICE-NOT-USEFUL -> ${serviceEntity.notUsefulCount}")
        serviceViewModel.updateService(serviceEntity)

        makeServiceUpdate("THUMBS_DOWN", date)
    }

    private fun undoServiceNotUseful() {
        /** Check if the count is not zero - because we can't have a negative count **/
        if (serviceEntity.notUsefulCount != 0) {
            serviceEntity.notUsefulCount = serviceEntity.notUsefulCount?.minus(1)
        }
        Timber.e("SERVICE-NOT-USEFUL -> ${serviceEntity.notUsefulCount}")
        serviceViewModel.updateService(serviceEntity)

    }

    private fun makeServiceUpdate(updateType: String, date: String) {
        val serviceUpdate = JSONObject()

        try {
            serviceUpdate.put("_id", serviceEntity.serviceId)
                    .put("update_time", date)
                    .put("update_type", updateType)
                    .put("user_contact", profileEntity.profileContact)

            Timber.e("SERVICE-UPDATE-OBJECT -> $serviceUpdate")

            object : UpdateService(context!!, serviceUpdate) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {
                        Timber.e("SERVICE UPDATED -> ${String(data)}")
                    } else {
                        Timber.e("SERVICE-UPDATE-ERROR-> $code")
                    }
                }
            }
        } catch (jse: JSONException) {
            Timber.e("JSONException ->$jse")
        }
    }

    private fun getUserProfile() {
        profileEntity = profileViewModel.profileEntityListData[0]
    }

    /** This lets us wrap it all up and plug it into the action-triggers that the user taps to
     * trigger the comment process **/
    private fun makeServiceComment(date: String) {

        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val serviceCommentEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        serviceCommentEditor.putBoolean("COMMENTED-ON-${serviceEntity.serviceId}", true).apply()

        assignServiceEntity(serviceEntity)
        showCommentDialog(date)
        makeServiceUpdate("SERVICE_COMMENT", date)
    }

    /** This is where we publish the comment captured by #captureServiceComment:
     * 1. $serviceCommentObject takes 4 values: a) serviceId; b) comment; c) date; d) userContact
     * 2. published to the server with the #ServiceComment object override
     * 3. TODO: let the user know that their comment has been published & congratulate them! **/
    private fun commentOnService(serviceComment: String, date: String) {
        val serviceCommentObject = JSONObject()

        try {
            serviceCommentObject.put("_id", serviceEntity.serviceId)
                    .put("service_comment", serviceComment)
                    .put("comment_date", date)
                    .put("user_contact", profileEntity.profileContact)

            object : ServiceComment(context!!, serviceCommentObject) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {
                        Timber.e("SERVICE COMMENT -> ${String(data)}")
                    } else {
                        Timber.e("SERVICE-COMMENT-ERROR-> $code")
                    }
                }
            }
        } catch (jse: JSONException) {
            Timber.e("JSONException ->$jse")
        }
    }

    /** With this function, we're simply displaying the commentDialog  && capturing the comment
     * before inserting it into #captureServiceComment **/
    private fun showCommentDialog(date: String) {
        val commentDialogView = LayoutInflater.from(context)
                .inflate(R.layout.service_comment_dialog, null)

        val commentDialogBuilder = MaterialAlertDialogBuilder(context!!)

        commentDialogBuilder
                .setTitle("Comment on ${service.serviceName}")
                .setIcon(R.drawable.logo)
                .setView(commentDialogView)

        commentDialogBuilder.setPositiveButton("Comment") { dialogInterface, i ->
            val serviceCommentEditText = commentDialogView
                    .findViewById<TextInputEditText>(R.id.serviceCommentEditText)

            val serviceComment = serviceCommentEditText.text?.trim().toString()
            captureServiceComment(serviceComment, date)
        }

        commentDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
            Timber.e("Feedback Cancelled")
        }

        commentDialogBuilder.show()
    }

    /** With this function, we're:
     * 1. doing the actual saving of the comment to RoomDB
     * 2. calling #commentOnService to publish the comment to the back-end/server **/
    private fun captureServiceComment(mServiceComment: String, date: String) {
        serviceEntity.serviceComments?.add(mServiceComment)
        serviceViewModel.updateService(serviceEntity)
        commentOnService(mServiceComment, date)
//        Timber.e("SERVICE COMMENT ${serviceEntity.serviceComments?.size} CAPTURED FOR -> ${serviceEntity.serviceName}!")
    }

    private fun showSnackBarBlue(viewHolder: GroupieViewHolder, message: String, length: Int) {
        val bottomNav = viewHolder.itemView.findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(viewHolder.itemView.findViewById(R.id.rootLayout), message, length)
        snackbar.setTextColor(context!!.resources.getColor(R.color.ummo_4))
        snackbar.anchorView = bottomNav
        snackbar.show()
    }

    private fun showSnackBarRed(viewHolder: GroupieViewHolder, message: String, length: Int) {
        val bottomNav = viewHolder.itemView.findViewById<View>(R.id.bottom_nav)
        val snackbar = Snackbar.make(viewHolder.itemView.findViewById(R.id.rootLayout), message, length)
        snackbar.setTextColor(context!!.resources.getColor(R.color.quantum_googred600))
        snackbar.anchorView = bottomNav
        snackbar.show()
    }
}