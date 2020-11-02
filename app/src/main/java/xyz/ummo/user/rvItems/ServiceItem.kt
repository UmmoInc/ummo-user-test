package xyz.ummo.user.rvItems

import android.content.Context
import android.preference.PreferenceManager
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_card.view.*
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
    }
}