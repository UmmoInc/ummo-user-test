package xyz.ummo.user.rvItems

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_slice.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.SERVICE_OBJECT
import java.io.Serializable

class ServiceSliceItem(
    private val serviceSliceObject: ServiceObject,
    val context: Context?
) : Item<GroupieViewHolder>() {

    private val bundle = Bundle()

    /** Initializing ServiceViewModel **/
    private var serviceViewModel = ViewModelProvider(context as FragmentActivity)
        .get(ServiceViewModel::class.java)
    private lateinit var serviceEntity: ServiceEntity

    override fun getLayout(): Int {
        return R.layout.service_slice
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        assignServiceEntity(serviceEntity)

        serviceViewModel.addService(serviceEntity)

        Timber.e("INCOMING SERVICE NAME -> ${serviceSliceObject.serviceName}")
        viewHolder.itemView.service_title_text_view_slice.text = serviceSliceObject.serviceName
        viewHolder.itemView.service_description_text_view_slice.text =
            serviceSliceObject.serviceDescription

        /** Implement [showServiceDetails] on the following clickable items **/
        viewHolder.itemView.mini_service_card_view.setOnClickListener {
            showServiceDetails()
        }
        viewHolder.itemView.mini_service_root_relative_layout.setOnClickListener {
            showServiceDetails()
        }
        viewHolder.itemView.service_header_relative_layout_slice.setOnClickListener {
            showServiceDetails()
        }
        viewHolder.itemView.service_info_title_relative_layout_slice.setOnClickListener {
            showServiceDetails()
        }
        viewHolder.itemView.service_card_avatar_image_view_slice.setOnClickListener {
            showServiceDetails()
        }
        viewHolder.itemView.service_title_text_view_slice.setOnClickListener {
            showServiceDetails()
        }
        /*viewHolder.itemView.open_service_image_slice.setOnClickListener {
            showServiceDetails()
        }*/
    }

    /** When a service is tapped on, it should expand to a detailed view with more info. **/
    private fun showServiceDetails() {
        val intent = Intent(context, DetailedServiceActivity::class.java)

        /** Passing Service to [DetailedServiceActivity] via [Serializable] object **/
        intent.putExtra(SERVICE_OBJECT, serviceSliceObject as Serializable)
//        intent.putExtra(SERVICE_IMAGE, )
        Timber.e("SHOWING SERVICE DETAILS -> $serviceSliceObject")
//        serviceViewModel.addService(serviceEntity)
        context!!.startActivity(intent)
    }

    private fun assignServiceEntity(mServiceEntity: ServiceEntity) {
        mServiceEntity.serviceId = serviceSliceObject.serviceId //0
        mServiceEntity.serviceName = serviceSliceObject.serviceName //1
        mServiceEntity.serviceDescription = serviceSliceObject.serviceDescription //2
        mServiceEntity.serviceEligibility = serviceSliceObject.serviceEligibility //3
        mServiceEntity.serviceCentres = serviceSliceObject.serviceCentres //4
        mServiceEntity.delegatable = serviceSliceObject.delegatable //5
//        mServiceEntity.serviceCost = serviceSliceObject.serviceCost //6
        mServiceEntity.serviceDocuments = serviceSliceObject.serviceDocuments //7
        mServiceEntity.serviceDuration = serviceSliceObject.serviceDuration //8
        mServiceEntity.usefulCount = serviceSliceObject.usefulCount //9
        mServiceEntity.notUsefulCount = serviceSliceObject.notUsefulCount //10
        mServiceEntity.serviceComments = serviceSliceObject.serviceComments //11
        mServiceEntity.commentCount = serviceSliceObject.serviceCommentCount //12
        mServiceEntity.serviceShares = serviceSliceObject.serviceShareCount //13
        mServiceEntity.serviceViews = serviceSliceObject.serviceViewCount //14
        mServiceEntity.serviceProvider = serviceSliceObject.serviceProvider //15
        mServiceEntity.serviceCategory = serviceSliceObject.serviceCategory //16
    }
}