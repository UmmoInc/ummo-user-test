package xyz.ummo.user.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.service_slice.view.*
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity
import xyz.ummo.user.utilities.SERVICE_ENTITY
import java.io.Serializable

class ServicesAdapter : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    private lateinit var mContext: Context

    inner class ServiceViewHolder(serviceView: View) : RecyclerView.ViewHolder(serviceView)

    private val differCallback = object : DiffUtil.ItemCallback<ServiceEntity>() {
        override fun areItemsTheSame(oldItem: ServiceEntity, newItem: ServiceEntity): Boolean {
            return oldItem.serviceId == newItem.serviceId
        }

        override fun areContentsTheSame(oldItem: ServiceEntity, newItem: ServiceEntity): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        mContext = parent.context

        return ServiceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.service_slice,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val serviceEntity = differ.currentList[position]

        holder.itemView.apply {
            service_title_text_view_slice.text = serviceEntity.serviceName
            service_description_text_view_slice.text = serviceEntity.serviceDescription
            setOnClickListener {
                onItemClickListener?.let {
                    it(serviceEntity)
                }
            }

            open_service_image_slice.setOnClickListener {
                showServiceDetails(serviceEntity)
            }
        }
    }

    /** When a service is tapped on, it should expand to a detailed view with more info. **/
    private fun showServiceDetails(serviceEntity: ServiceEntity) {
        val intent = Intent(mContext, DetailedServiceActivity::class.java)

        /** Passing Service to [DetailedServiceActivity] via [Serializable] object **/
        intent.putExtra(SERVICE_ENTITY, serviceEntity)

        mContext.startActivity(intent)
    }

    private var onItemClickListener: ((ServiceEntity) -> Unit)? = null

    fun setOnItemClickListener(listener: (ServiceEntity) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}