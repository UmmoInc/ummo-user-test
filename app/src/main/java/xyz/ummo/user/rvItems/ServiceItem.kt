package xyz.ummo.user.rvItems

import android.content.Context
import android.preference.PreferenceManager
import android.view.View
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_card.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.models.Service

class ServiceItem (private val service: Service, val context: Context?) : Item<GroupieViewHolder>() {

    var jwt = PreferenceManager.getDefaultSharedPreferences(context).getString("jwt", "")

    override fun getLayout(): Int {
        return R.layout.service_card
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.service_title_text_view.text = service.serviceName
        viewHolder.itemView.service_description_text_view.text = service.serviceDescription
        viewHolder.itemView.service_eligibility_text_view.text = service.serviceEligibility
        viewHolder.itemView.service_centre_text_view.text = service.serviceCentre
        viewHolder.itemView.service_cost_text_view.text = service.serviceCost
        viewHolder.itemView.service_duration_text_view.text = service.serviceDuration
        viewHolder.itemView.service_requirements_text_view.text = service.serviceRequirements
        viewHolder.itemView.disapprove_count_text_view.text = service.disapproveCount.toString()
        viewHolder.itemView.approve_count_text_view.text = service.approveCount.toString()
        viewHolder.itemView.service_comments_count_text_view.text = service.commentCount.toString()
        viewHolder.itemView.share_count_text_view.text = service.shares.toString()

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

        /** [1] Disapprove Service Click Handlers **/
        viewHolder.itemView.disapprove_service_relative_layout.setOnClickListener {
            Timber.e("Service Disapproved-Layout!")
        }
        viewHolder.itemView.disapprove_service_image.setOnClickListener {
            Timber.e("Service Disapproved-Image!")
        }

        /** [2] Approve Service Click Handlers **/
        viewHolder.itemView.approve_service_relative_layout.setOnClickListener {
            Timber.e("Service Approved-Layout!")
        }
        viewHolder.itemView.approve_service_image.setOnClickListener {
            Timber.e("Service Approved-Image!")
        }

        /** [3] Service Feedback Click Handlers **/
        viewHolder.itemView.service_comments_relative_layout.setOnClickListener {
            Timber.e("Service Comments-Layout!")
        }
        viewHolder.itemView.service_comments_image.setOnClickListener {
            Timber.e("Service Comments-Image!")
        }

        /** [4] Service Feedback Click Handlers **/
        viewHolder.itemView.share_service_relative_layout.setOnClickListener {
            Timber.e("Service Share-Layout!")
        }
        viewHolder.itemView.share_service_image.setOnClickListener {
            Timber.e("Service Share-Image!")
        }
    }
}