package xyz.ummo.user.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.ummo.user.R
import xyz.ummo.user.models.PublicServiceData

class ProvidersAdapter(private val myDataset: List<PublicServiceData>): RecyclerView.Adapter<ProvidersAdapter.ProviderViewHolder>() {
    class ProviderViewHolder(val view:View):RecyclerView.ViewHolder(view){
        val title:TextView
        val more: TextView
        val image: ImageView
        val description:TextView
        val details:TextView
       // val docs:TextView
        val cost:TextView
        val duration:TextView
        val requestBtn:Button

        init {
            title = view.findViewById(R.id.service_title)
            more = view.findViewById(R.id.more_on_the_service_text)
            image = view.findViewById(R.id.service_image)
            description = view.findViewById(R.id.service_description)
            details = view.findViewById(R.id.service_form)
          //  docs = view.findViewById(R.id.service_documents)
            cost = view.findViewById(R.id.service_cost)
            duration = view.findViewById(R.id.service_duration)
            requestBtn = view.findViewById(R.id.request_agent_btn)
        }
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val item: PublicServiceData = myDataset.get(position)
       holder.title.text = item.serviceName
        holder.description.text = "Described"
        holder.details.text = "Details"
       // holder.docs.text = "Docs"
        holder.cost.text = "Arm and leg"
        holder.duration.text = "All Day"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        Log.e("Recycler","Oncreate")
        val providersLayout = LayoutInflater.from(parent.context).inflate(R.layout.service_content,parent,false)
        return ProviderViewHolder(providersLayout)
    }

    override fun getItemCount(): Int {
        return myDataset.size
    }

}