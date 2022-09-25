package xyz.ummo.user.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.service_comment.view.*
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceCommentEntity

class ServiceCommentsDiffUtilAdapter :
    RecyclerView.Adapter<ServiceCommentsDiffUtilAdapter.ServiceCommentViewHolder>() {
    private lateinit var mContext: Context

    inner class ServiceCommentViewHolder(serviceCommentView: View) :
        RecyclerView.ViewHolder(serviceCommentView)

    private val differCallback = object : DiffUtil.ItemCallback<ServiceCommentEntity>() {
        override fun areItemsTheSame(
            oldItem: ServiceCommentEntity,
            newItem: ServiceCommentEntity
        ): Boolean {
            return oldItem.serviceId == newItem.serviceId
        }

        override fun areContentsTheSame(
            oldItem: ServiceCommentEntity,
            newItem: ServiceCommentEntity
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCommentViewHolder {
        mContext = parent.context
        return ServiceCommentViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.service_comment,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ServiceCommentViewHolder, position: Int) {
        val serviceCommentEntity = differ.currentList[position]

        holder.itemView.apply {
            user_name_text_view.text = serviceCommentEntity.userName
            comment_datetime_text_view.text = serviceCommentEntity.commentDateTime
            service_comment_text_view.text = serviceCommentEntity.serviceComment
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}