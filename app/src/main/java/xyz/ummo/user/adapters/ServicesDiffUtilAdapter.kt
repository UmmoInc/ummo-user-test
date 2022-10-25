package xyz.ummo.user.adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.service_slice.view.*
import org.json.JSONObject
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity
import xyz.ummo.user.ui.fragments.bottomSheets.IntroduceDelegate
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceOptionsMenuBottomSheet
import xyz.ummo.user.ui.fragments.bottomSheets.ServiceRequestBottomSheet
import xyz.ummo.user.utilities.*
import java.io.Serializable


class ServicesDiffUtilAdapter(private var optionsMenuClickListener: OptionsMenuClickListener) :
    RecyclerView.Adapter<ServicesDiffUtilAdapter.ServiceViewHolder>() {

    private lateinit var mContext: Context

    private lateinit var mixpanel: MixpanelAPI

    private lateinit var serviceItemSlicePreferences: SharedPreferences

    interface OptionsMenuClickListener {
        fun onOptionsMenuClicked(position: Int)
    }

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
        mixpanel = MixpanelAPI.getInstance(
            mContext,
            mContext.resources.getString(R.string.mixpanelToken)
        )

        serviceItemSlicePreferences = mContext.getSharedPreferences(ummoUserPreferences, mode)

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

            if (serviceEntity.delegatable!!) {
                //service_delegatable_tag_relative_layout.visibility = View.VISIBLE
                service_slice_request_agent_button.visibility = View.VISIBLE
            }

            options_menu_service_slice.setOnClickListener {
//                optionsMenuClickListener.onOptionsMenuClicked(position)
                val serviceBundle = Bundle()
                val serviceEntityObject = JSONObject()
                val serviceOptionsMenuBottomSheet = ServiceOptionsMenuBottomSheet()
                serviceBundle.putSerializable(SERVICE_ENTITY, serviceEntity)
                serviceOptionsMenuBottomSheet.arguments = serviceBundle
                serviceOptionsMenuBottomSheet.show(
                    (mContext as FragmentActivity).supportFragmentManager,
                    ServiceOptionsMenuBottomSheet.TAG
                )

                serviceEntityObject.put("service_name", serviceEntity.serviceName)
                mixpanel.track("Service Options Menu", serviceEntityObject)
            }

            /** Let's let the user tap on any item on the view to launch more info **/
            /*open_service_image_slice.setOnClickListener {
                showServiceDetails(serviceEntity)
            }*/
            mini_service_card_view.setOnClickListener {
                showServiceDetails(serviceEntity)
            }
            service_card_avatar_image_view_slice.setOnClickListener {
                showServiceDetails(serviceEntity)
            }
            service_title_text_view_slice.setOnClickListener {
                showServiceDetails(serviceEntity)
            }
            service_description_text_view_slice.setOnClickListener {
                showServiceDetails(serviceEntity)
            }

            service_slice_request_agent_button.setOnClickListener {
                requestServiceAgent(serviceEntity)
            }
        }
    }

    private fun requestServiceAgent(serviceEntity: ServiceEntity) {
        val requestBundle = Bundle()
        requestBundle.putSerializable(SERVICE_ENTITY, serviceEntity)

        val introduceDelegateBottomSheetDialog = IntroduceDelegate()
        introduceDelegateBottomSheetDialog.arguments = requestBundle

        val serviceRequestBottomSheetDialog = ServiceRequestBottomSheet()
        serviceRequestBottomSheetDialog.arguments = requestBundle

        val serviceRequested = JSONObject()
        serviceRequested.put(SERVICE_ENTITY, serviceEntity)

        if (serviceItemSlicePreferences.getBoolean(DELEGATION_INTRO_IS_CONFIRMED, false)) {
            serviceRequestBottomSheetDialog.show(
                (mContext as FragmentActivity).supportFragmentManager,
                ServiceRequestBottomSheet.TAG
            )
            mixpanel.track("All Services - Requesting Service", serviceRequested)
        } else {
            introduceDelegateBottomSheetDialog.show(
                (mContext as FragmentActivity).supportFragmentManager,
                IntroduceDelegate.TAG
            )
            mixpanel.track("All Services - Requesting Service (INTRO)", serviceRequested)
        }
    }

    /** When a service is tapped on, it should expand to a detailed view with more info. **/
    private fun showServiceDetails(serviceEntity: ServiceEntity) {
        val intent = Intent(mContext, DetailedServiceActivity::class.java)
        intent.putExtra(PARENT, "ServicesAdapter")
        val serviceResults = JSONObject()
        serviceResults.put("Service", intent.getStringExtra(SERVICE_ENTITY))
        mixpanel.track("Found Results", serviceResults)

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