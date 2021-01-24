package xyz.ummo.user.ui.fragments.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import xyz.ummo.user.models.DelegatedService
import xyz.ummo.user.R
import xyz.ummo.user.ui.MainScreen.Companion.supportFM
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment

class DelegatedServiceAdapter(private val delegatedServicesList: List<DelegatedService>) : RecyclerView.Adapter<DelegatedServiceAdapter.MyViewHolder>() {
    private var agentName: String? = null
    var serviceName: String? = null

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var serviceName: TextView = view.findViewById(R.id.service_name)
        var agentName: TextView = view.findViewById(R.id.agent_name)
        var service: DelegatedService? = null

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.delegated_services_list, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val delegatedService = delegatedServicesList[position]
        agentName = delegatedService.agentName
        serviceName = delegatedService.serviceName
        holder.serviceName.text = delegatedService.serviceName
        holder.agentName.text = delegatedService.agentName

        holder.serviceName.setOnClickListener {
            val delegatedServiceFragment = DelegatedServiceFragment()
            val bundle = Bundle()
            bundle.putString("SERVICE_ID", delegatedService.serviceId)
            bundle.putString("SERVICE_AGENT_ID", delegatedService.agentId)
            bundle.putString("DELEGATED_PRODUCT_ID", delegatedService.productId)

            delegatedServiceFragment.arguments = bundle

            Timber.e("onClick: AGENT_ID ${delegatedService.agentId} | PRODUCT_ID ${delegatedService.productId}")

            val fragmentTransaction = supportFM.beginTransaction()
            fragmentTransaction.replace(R.id.rootLayout, delegatedServiceFragment)
            fragmentTransaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return delegatedServicesList.size
    }
}