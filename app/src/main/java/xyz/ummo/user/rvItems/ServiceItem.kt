package xyz.ummo.user.rvItems

import android.annotation.SuppressLint
import android.content.Context
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_card.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.models.Service
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import java.text.SimpleDateFormat
import java.util.*

class ServiceItem(private val service: Service, val context: Context?) : Item<GroupieViewHolder>() {

    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")

    /** Initializing ServiceViewModel **/
    private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
            .get(ServiceViewModel::class.java)

    private var serviceEntity = ServiceEntity()

    override fun getLayout(): Int {
        return R.layout.service_card
    }

    @SuppressLint("SimpleDateFormat")
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.service_title_text_view.text = service.serviceName //1
        viewHolder.itemView.service_description_text_view.text = service.serviceDescription //2
        viewHolder.itemView.service_eligibility_text_view.text = service.serviceEligibility //3
        viewHolder.itemView.service_centre_text_view.text = service.serviceCentre.toString() //4
        viewHolder.itemView.service_cost_text_view.text = service.serviceCost //6
        viewHolder.itemView.service_duration_text_view.text = service.serviceDuration //7
        viewHolder.itemView.service_requirements_text_view.text = service.serviceDocuments.toString() //8
        viewHolder.itemView.approve_count_text_view.text = service.usefulCount.toString() //9
        viewHolder.itemView.disapprove_count_text_view.text = service.notUsefulCount.toString() //10
        viewHolder.itemView.service_comments_count_text_view.text = service.commentCount.toString() //11
        viewHolder.itemView.share_count_text_view.text = service.shares.toString() //12

        /**Date chunk below is being used to capture a selection's time-stamp**/
        val simpleDateFormat = SimpleDateFormat("dd/M/yyy hh:mm:ss")
        val currentDate = simpleDateFormat.format(Date())

        //TODO: Assign serviceEntity to serviceValues
        assignServiceEntity(serviceEntity)

        /**Expand Service Card to reveal more info - Layout-Click...**/
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
            Timber.e("Service Approved-Layout!")
            serviceUseful(service.serviceId, currentDate)
            viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()

            viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_thumb_up_filled_24)

            viewHolder.itemView.approve_service_image.isClickable = false
            viewHolder.itemView.approve_service_image.isEnabled = false
            viewHolder.itemView.approve_service_relative_layout.isClickable = false
            viewHolder.itemView.approve_service_relative_layout.isEnabled = false

//            showSnackBarBlue(viewHolder, "Service useful", -1)

            /**Undo #approveOperation and subtract the user-generated #NotUseful value set**/
            viewHolder.itemView.disapprove_service_image.isClickable = true
            viewHolder.itemView.disapprove_service_image.isEnabled = true
            viewHolder.itemView.disapprove_service_relative_layout.isClickable = true
            viewHolder.itemView.disapprove_service_relative_layout.isEnabled = true
            viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_outline_thumb_down_red_24)
            undoServiceNotUseful(service.serviceId, currentDate)
            viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()

            /**TODO: Toggle the image-icon**/
            /*if (viewHolder.itemView.approve_service_image.resources == getDrawable(context!!, R.drawable.ic_outline_thumb_up_blue_24)) {
                viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_thumb_up_filled_24)
                Timber.e("Toggling Image Resource [ON]!")
            } else if (viewHolder.itemView.approve_service_image.resources == getDrawable(context, R.drawable.ic_thumb_up_filled_24)){
                viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_outline_thumb_up_blue_24)
                Timber.e("Toggling Image Resource [OFF]!")
            }*/
        }

        viewHolder.itemView.approve_service_image.setOnClickListener {
            Timber.e("Service Approved-Image!")
            serviceUseful(service.serviceId, currentDate)
            viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()

            viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_thumb_up_filled_24)

            viewHolder.itemView.approve_service_image.isClickable = false
            viewHolder.itemView.approve_service_image.isEnabled = false

            viewHolder.itemView.approve_service_relative_layout.isClickable = false
            viewHolder.itemView.approve_service_relative_layout.isEnabled = false

//            showSnackBarBlue(viewHolder, "Service useful", -1)

            /**Undo #approveOperation and subtract the user-generated #NotUseful value set**/
            viewHolder.itemView.disapprove_service_image.isClickable = true
            viewHolder.itemView.disapprove_service_image.isEnabled = true
            viewHolder.itemView.disapprove_service_relative_layout.isClickable = true
            viewHolder.itemView.disapprove_service_relative_layout.isEnabled = true
            viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_outline_thumb_down_red_24)
            undoServiceNotUseful(service.serviceId, currentDate)
            viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()

            /**TODO: Toggle the image-icon**/
            /*if (viewHolder.itemView.approve_service_image.resources == getDrawable(context!!, R.drawable.ic_outline_thumb_up_blue_24)) {
                viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_thumb_up_filled_24)
                Timber.e("Toggling Image Resource [ON]!")
            } else if (viewHolder.itemView.approve_service_image.resources == getDrawable(context, R.drawable.ic_thumb_up_filled_24)){
                viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_outline_thumb_up_blue_24)
                Timber.e("Toggling Image Resource [OFF]!")
            }*/
        }

        /** [2] Disapprove Service Click Handlers **/
        viewHolder.itemView.disapprove_service_relative_layout.setOnClickListener {
            Timber.e("Service Disapproved-Layout!")
            serviceNotUseful(service.serviceId, currentDate)
            viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()

            viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_thumb_down_filled_24)

            viewHolder.itemView.disapprove_service_image.isEnabled = false
            viewHolder.itemView.disapprove_service_image.isClickable = false

            viewHolder.itemView.disapprove_service_relative_layout.isEnabled = false
            viewHolder.itemView.disapprove_service_relative_layout.isClickable = false

//            showSnackBarRed(viewHolder, "Service NOT useful", -1)

            /**Undo #approveOperation and subtract the user-generated #Useful value set**/
            viewHolder.itemView.approve_service_image.isClickable = true
            viewHolder.itemView.approve_service_image.isEnabled = true
            viewHolder.itemView.approve_service_relative_layout.isClickable = true
            viewHolder.itemView.approve_service_relative_layout.isEnabled = true
            viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_outline_thumb_up_blue_24)
            undoServiceUseful(service.serviceId, currentDate)
            viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()

        }

        viewHolder.itemView.disapprove_service_image.setOnClickListener {
            Timber.e("Service Disapproved-Image!")
            serviceNotUseful(service.serviceId, currentDate)
            viewHolder.itemView.disapprove_count_text_view.text = serviceEntity.notUsefulCount.toString()

            viewHolder.itemView.disapprove_service_image.setImageResource(R.drawable.ic_thumb_down_filled_24)

            viewHolder.itemView.disapprove_service_image.isEnabled = false
            viewHolder.itemView.disapprove_service_image.isClickable = false

            viewHolder.itemView.disapprove_service_relative_layout.isEnabled = false
            viewHolder.itemView.disapprove_service_relative_layout.isClickable = false

//            showSnackBarRed(viewHolder, "Service NOT useful", -1)

            /**Undo #approveOperation and subtract the user-generated #Useful value set**/
            viewHolder.itemView.approve_service_image.isClickable = true
            viewHolder.itemView.approve_service_image.isEnabled = true
            viewHolder.itemView.approve_service_relative_layout.isClickable = true
            viewHolder.itemView.approve_service_relative_layout.isEnabled = true
            viewHolder.itemView.approve_service_image.setImageResource(R.drawable.ic_outline_thumb_up_blue_24)
            undoServiceUseful(service.serviceId, currentDate)
            viewHolder.itemView.approve_count_text_view.text = serviceEntity.usefulCount.toString()

        }

        /** [3] Service Feedback Click Handlers **/
        viewHolder.itemView.service_comments_relative_layout.setOnClickListener {
            Timber.e("Service Comments-Layout!")

            serviceComment(service.serviceId, currentDate)
            viewHolder.itemView.service_comments_count_text_view.text = serviceEntity.commentCount.toString()

//            showSnackBarRed(viewHolder, "Thanks for your feedback...", -1)
//            showCommentDialog()
        }

        viewHolder.itemView.service_comments_image.setOnClickListener {
            Timber.e("Service Comments-Image!")
            serviceComment(service.serviceId, currentDate)
            viewHolder.itemView.service_comments_count_text_view.text = serviceEntity.commentCount.toString()

//            showSnackBarRed(viewHolder, "Thanks for your feedback...", -1)

//            showCommentDialog()
        }

        /** [4] Service Feedback Click Handlers **/
        viewHolder.itemView.share_service_relative_layout.setOnClickListener {
            Timber.e("Service Share-Layout!")
        }
        viewHolder.itemView.share_service_image.setOnClickListener {
            Timber.e("Service Share-Image!")
        }
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
        mServiceEntity.commentCount = service.commentCount //11
        mServiceEntity.serviceShares = service.shares //12
        mServiceEntity.serviceViews = service.views //13
        mServiceEntity.serviceProvider = service.serviceProvider //14
    }

    private fun serviceUseful(serviceId: String, date: String) {
        serviceEntity.usefulCount = serviceEntity.usefulCount?.plus(1)
        Timber.e("SERVICE-USEFUL -> ${serviceEntity.usefulCount}")
        serviceViewModel.updateService(serviceEntity)

    }

    private fun undoServiceUseful(serviceId: String, date: String) {
        serviceEntity.usefulCount = serviceEntity.usefulCount?.minus(1)
        Timber.e("SERVICE-USEFUL -> ${serviceEntity.usefulCount}")
        serviceViewModel.updateService(serviceEntity)

    }

    private fun serviceNotUseful(serviceId: String, date: String) {
        serviceEntity.notUsefulCount = serviceEntity.notUsefulCount?.plus(1)
        Timber.e("SERVICE-NOT-USEFUL -> ${serviceEntity.notUsefulCount}")
        serviceViewModel.updateService(serviceEntity)

    }

    private fun undoServiceNotUseful(serviceId: String, date: String) {
        serviceEntity.notUsefulCount = serviceEntity.notUsefulCount?.minus(1)
        Timber.e("SERVICE-NOT-USEFUL -> ${serviceEntity.notUsefulCount}")
        serviceViewModel.updateService(serviceEntity)

    }

    private fun serviceComment(serviceId: String, date: String) {
        showCommentDialog()
    }

    private fun showCommentDialog() {
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
            Timber.e("SERVICE-COMMENT -> $serviceComment")
            serviceEntity.commentCount = 1
        }

        commentDialogBuilder.setNegativeButton("Cancel") { dialogInterface, i ->
            Timber.e("Feedback Cancelled")
        }

        commentDialogBuilder.show()
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