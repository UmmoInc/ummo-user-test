package xyz.ummo.user.rvItems

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_card.view.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.delegate.BookmarkService
import xyz.ummo.user.delegate.ServiceComment
import xyz.ummo.user.delegate.UpdateService
import xyz.ummo.user.models.Service
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.DownvoteServiceEvent
import xyz.ummo.user.utilities.eventBusEvents.ServiceBookmarkedEvent
import xyz.ummo.user.utilities.eventBusEvents.UpvoteServiceEvent
import java.text.SimpleDateFormat
import java.util.*

class ServiceItem(private val service: Service,
                  val context: Context?,
                  savedUserActions: JSONObject) : Item<GroupieViewHolder>() {

    /** Shared Preferences for storing user actions **/
    private lateinit var serviceItemPrefs: SharedPreferences
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences = "UMMO_USER_PREFERENCES"
    private var upVote: Boolean = false
    private var downVote: Boolean = false
    private var commentedOn: Boolean = false
    private var bookmarked: Boolean = false
    private var anonymousComment: Boolean = false
    private val isBookmarkedEvent = ServiceBookmarkedEvent()
    private val isUpvotedEvent = UpvoteServiceEvent()
    private val isDownvotedEvent = DownvoteServiceEvent()

    init {
        upVote = savedUserActions.getBoolean("UP-VOTE")
        downVote = savedUserActions.getBoolean("DOWN-VOTE")
        commentedOn = savedUserActions.getBoolean("COMMENTED-ON")
        bookmarked = savedUserActions.getBoolean("BOOKMARKED")
    }

//    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")

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
        Timber.e("BOOKMARKED -> $bookmarked")

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
//        viewHolder.itemView.save_count_text_view.text = service.serviceShareCount.toString() //12
//        viewHolder.itemView.share_count_text_view.text = service.serviceShareCount.toString() //12

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
            reverseUpVoteTriggeredChangeStates(viewHolder)
        }

        if (downVote) {
            downVoteTriggeredChangeStates(viewHolder)
        } else {
            reverseDownVoteTriggeredChangeStates(viewHolder)
        }

        if (commentedOn) {
            commentTriggeredChangeStates(viewHolder)
        } else {
            reverseCommentTriggeredChangeStates(viewHolder)
        }

        if (bookmarked) {
            bookmarkTriggeredChangeStates(viewHolder)
        } else {
            reverseBookmarkChangeStates(viewHolder)
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
            if (upVote) {
                /** #UX Trigger icon-change on click **/
                upVoteTriggeredChangeStates(viewHolder)
                viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
                upVoteService(currentDate)
                Timber.e("UPVOTE-CHECK-1: $upVote")

            } else {
                reverseUpVoteTriggeredChangeStates(viewHolder)
                viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
                undoServiceUpvote(currentDate)
                Timber.e("UPVOTE-CHECK-2: $upVote")

                //TODO: reverse Up-vote - server-side
            }

        }
        viewHolder.itemView.approve_service_image.setOnClickListener {
            if (upVote) {
                /** #UX Trigger icon-change on click **/
                upVoteTriggeredChangeStates(viewHolder)
                viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
                upVoteService(currentDate)
            } else {
                reverseUpVoteTriggeredChangeStates(viewHolder)
                viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
                undoServiceUpvote(currentDate)
            }
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

        /** [4] Save Service Click Handlers **/
        viewHolder.itemView.save_service_relative_layout.setOnClickListener {
            Timber.e("Save Service-Layout!")

            if (!bookmarked) {
                bookmarkService(currentDate)
                bookmarkTriggeredChangeStates(viewHolder)
            } else {
                removeBookmark(currentDate)
                reverseBookmarkChangeStates(viewHolder)
            }
        }
        viewHolder.itemView.save_service_image.setOnClickListener {
            Timber.e("Save Service-Image!")

            if (!bookmarked) {
                bookmarkService(currentDate)
                bookmarkTriggeredChangeStates(viewHolder)
            } else {
                removeBookmark(currentDate)
                reverseBookmarkChangeStates(viewHolder)
            }
        }
    }

    /** When the user Up-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $upVote option, to prevent them from up-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $downVote option, in case they want to change their minds
     * TODO: Allow the user to undo their upVote by reversing the action altogether **/
    private fun upVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
        viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_thumb_up_filled_24)
        viewHolder.itemView.you_up_voted_this_text_view.visibility = View.VISIBLE

        reverseDownVoteTriggeredChangeStates(viewHolder)

    }

    private fun reverseUpVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()
        viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_outline_thumb_up_blue_24)
        viewHolder.itemView.you_up_voted_this_text_view.visibility = View.INVISIBLE
    }

    /** When the user Down-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $downVote option, to prevent them from down-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $upVote option, in case they want to change their minds
     *  TODO: Allow the user to undo their upVote by reversing the action altogether **/
    private fun downVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()
        viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_thumb_down_filled_24)
        viewHolder.itemView.you_downvoted_this_text_view.visibility = View.VISIBLE

        reverseUpVoteTriggeredChangeStates(viewHolder)
    }

    private fun reverseDownVoteTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()
        viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_outline_thumb_down_red_24)
        viewHolder.itemView.you_downvoted_this_text_view.visibility = View.INVISIBLE
    }

    /** When the user Down-Votes, we want them to immediately see a UI-state change that lets them
     * know that their action has been captured:
     * 1. We deactivate the $downVote option, to prevent them from down-voting again;
     * 2. We let them know that they've down-voted by showing them a text;
     * 3. We activate the $upVote option, in case they want to change their minds
     *  TODO: Allow the user to undo their upVote by reversing the action altogether **/
    private fun bookmarkTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.save_service_image.setImageResource(R.drawable.ic_filled_bookmark_24)
        viewHolder.itemView.you_saved_this_text_view.visibility = View.VISIBLE
    }

    private fun reverseBookmarkChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.save_service_image.setImageResource(R.drawable.ic_outline_bookmark_border_24)
        viewHolder.itemView.you_saved_this_text_view.visibility = View.INVISIBLE
    }

    /** When the user makes a comment, we want them to immediately see a UI-state change that
     * lets them know that their action has been captured:
     * 1. We update the comment icon to a filled one
     * 2. We let them know that they've commented by showing them a text; **/
    private fun commentTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.service_comments_image.setImageResource(R.drawable.ic_chat_bubble_filled_24)
        viewHolder.itemView.you_commented_on_this_text_view.visibility = View.VISIBLE
    }

    private fun reverseCommentTriggeredChangeStates(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.service_comments_image.setImageResource(R.drawable.ic_outline_chat_bubble_blue_24)
        viewHolder.itemView.you_commented_on_this_text_view.visibility = View.INVISIBLE
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

        /** Publish Upvote Event via EventBus **/
        isUpvotedEvent.serviceUpvote = true
        isUpvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isUpvotedEvent)

        isDownvotedEvent.serviceDownvote = false
        isDownvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isDownvotedEvent)
    }

    private fun undoServiceUpvote(date: String) {
        /** Check if the count is not zero - because we can't have a negative count **/
        if (serviceEntity.usefulCount != 0) {
            serviceEntity.usefulCount = serviceEntity.usefulCount?.minus(1)
        }

        Timber.e("SERVICE-USEFUL -> ${serviceEntity.usefulCount}")
        serviceViewModel.updateService(serviceEntity)
        //TODO: undo service upvote - server side

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

        /** Publish Downvote Event via EventBus & flip Upvote **/
        isDownvotedEvent.serviceDownvote = true
        isDownvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isDownvotedEvent)

        isUpvotedEvent.serviceUpvote = false
        isUpvotedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isUpvotedEvent)
    }

    private fun undoServiceNotDownvote() {
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
                    .put("anonymous_comment", anonymousComment)
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

        val anonymousCheckBox = commentDialogView
                .findViewById<CheckBox>(R.id.anonymous_comment_check_box)

        anonymousCheckBox.setOnClickListener {
            if (anonymousCheckBox.isChecked) {
                Timber.e("GOING ANONYMOUS!")
                //TODO: change the color of the comment dialog
                /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                commentDialogView.setBackgroundColor(R.color.greyProfile)
                }*/

                anonymousComment = true
            } else {
                anonymousComment = false
                Timber.e("NOT GOING ANONYMOUS!")
            }
        }

        commentDialogBuilder.setPositiveButton("Comment") { dialogInterface, i ->
            val serviceCommentEditText = commentDialogView
                    .findViewById<TextInputEditText>(R.id.service_comment_edit_text)

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

    private fun bookmarkService(date: String) {
        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val serviceBookmarkEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        serviceBookmarkEditor.putBoolean("BOOKMARKED-${serviceEntity.serviceId}", true).apply()

        serviceEntity.bookmarked = true
        serviceViewModel.updateService(serviceEntity)
        Timber.e("BOOK-MARKING SERVICE -> ${serviceEntity.serviceName}")

        publishServiceBookmark(date)

        isBookmarkedEvent.serviceBookmarked = true
        isBookmarkedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isBookmarkedEvent)
    }

    private fun removeBookmark(date: String) {
        /** Initializing sharedPreferences **/
        serviceItemPrefs = context?.getSharedPreferences(ummoUserPreferences, mode)!!
        /** Capture actions with $editor below && store them in $sharedPrefs for UX purposes; i.e.,:
         * 1. switch action-icon based on user's previous action
         * 2. toggle b/n icons based on user changing their mind on an action **/
        val serviceBookmarkEditor: SharedPreferences.Editor = serviceItemPrefs.edit()
        serviceBookmarkEditor.putBoolean("BOOKMARKED-${serviceEntity.serviceId}", false).apply()

        serviceEntity.bookmarked = false
        serviceViewModel.updateService(serviceEntity)
        Timber.e("REMOVING SERVICE BOOK-MARK -> ${serviceEntity.serviceName}")

        isBookmarkedEvent.serviceBookmarked = false
        isBookmarkedEvent.serviceId = serviceEntity.serviceId
        EventBus.getDefault().post(isBookmarkedEvent)

        //TODO: remove bookmark from user-profile (backend)
    }

    private fun publishServiceBookmark(date: String) {
        val bookmarkObject = JSONObject()
        try {
            bookmarkObject
                    .put("bookmark_date", date)
                    .put("user_contact", profileEntity.profileContact)
                    .put("bookmarked_service", serviceEntity.serviceId)

            object : BookmarkService(context!!, bookmarkObject) {
                override fun done(data: ByteArray, code: Number) {
                    if (code == 200) {
                        Timber.e("BOOKMARKING SERVICE -> ${String(data)}")
                    } else {
                        Timber.e("SERVICE-BOOKMARK-ERROR-> $code")
                    }
                }
            }
        } catch (jse: JSONException) {
            Timber.e("JSONException ->$jse")
        }
    }
}