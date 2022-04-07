package xyz.ummo.user.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.service_slice.view.*
import xyz.ummo.user.R
import xyz.ummo.user.models.ServiceObject

class ServicesAdapter : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(serviceView: View): RecyclerView.ViewHolder(serviceView)

    private val differCallback = object : DiffUtil.ItemCallback<ServiceObject>() {
        override fun areItemsTheSame(oldItem: ServiceObject, newItem: ServiceObject): Boolean {
            return oldItem.serviceId == newItem.serviceId
        }

        override fun areContentsTheSame(oldItem: ServiceObject, newItem: ServiceObject): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        return ServiceViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.service_slice,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val serviceObject = differ.currentList[position]

        holder.itemView.apply {
            service_title_text_view_slice.text = serviceObject.serviceName
            service_description_text_view_slice.text = serviceObject.serviceDescription
            setOnClickListener {
                onItemClickListener?.let {
                    it(serviceObject)
                }
            }
            open_service_image_slice.setOnClickListener {

            }
        }
    }

    private var onItemClickListener: ((ServiceObject) -> Unit)? = null

    fun setOnItemClickListener(listener: (ServiceObject) -> Unit) {
        onItemClickListener = listener
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}