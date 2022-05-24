package xyz.ummo.user.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.android.synthetic.main.service_category.view.*
import org.json.JSONObject
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ServiceCategoryEntity
import xyz.ummo.user.ui.fragments.pagesFrags.PagesFragment
import xyz.ummo.user.utilities.CATEGORY
import xyz.ummo.user.utilities.SERVICE_CATEGORY

class ServiceCategoriesAdapter :
    RecyclerView.Adapter<ServiceCategoriesAdapter.ServiceCategoryViewHolder>() {
    private lateinit var mContext: Context
    private lateinit var mixpanelAPI: MixpanelAPI

    inner class ServiceCategoryViewHolder(serviceCategoryView: View) :
        RecyclerView.ViewHolder(serviceCategoryView)

    private val differCallback = object : DiffUtil.ItemCallback<ServiceCategoryEntity>() {
        override fun areItemsTheSame(
            oldItem: ServiceCategoryEntity,
            newItem: ServiceCategoryEntity
        ): Boolean {
            return oldItem.serviceCategory == newItem.serviceCategory
        }

        override fun areContentsTheSame(
            oldItem: ServiceCategoryEntity,
            newItem: ServiceCategoryEntity
        ): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceCategoryViewHolder {
        mContext = parent.context
        mixpanelAPI = MixpanelAPI.getInstance(
            mContext,
            mContext.resources.getString(R.string.mixpanelToken)
        )

        return ServiceCategoryViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.service_category, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ServiceCategoryViewHolder, position: Int) {
        val serviceCategoryEntity = differ.currentList[position]

        holder.itemView.apply {
            category_title_text_view.text = serviceCategoryEntity.serviceCategory
            category_service_count_text_view.text = "${serviceCategoryEntity.serviceCount} services"
            setOnClickListener {
                onItemClickListener?.let {
                    it(serviceCategoryEntity)
                }
            }

            service_category_card_view.setOnClickListener {
                openCategory(position)
            }
            category_main_relative_layout.setOnClickListener {
                openCategory(position)
            }
            category_image_view.setOnClickListener {
                openCategory(position)
            }
            category_title_text_view.setOnClickListener {
                openCategory(position)
            }
            category_service_count_text_view.setOnClickListener {
                openCategory(position)
            }
        }

        addThumbnailsToCategories(holder, position)
    }

    private fun addThumbnailsToCategories(viewHolder: ServiceCategoryViewHolder, position: Int) {
        val serviceCategoryEntity = differ.currentList[position]

        when {
            serviceCategoryEntity.serviceCategory.contains("HEALTH", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_health_and_safety_24)
                    )
            }

            serviceCategoryEntity.serviceCategory.contains("EDUCATION", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_school_24)
                    )
            }

            serviceCategoryEntity.serviceCategory.contains("BUSINESS", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_business_center_24)
                    )
            }

            serviceCategoryEntity.serviceCategory.contains("TRAVEL", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_flight_takeoff_24)
                    )
            }

            serviceCategoryEntity.serviceCategory.contains("VEHICLES", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_car_24)
                    )
            }

            serviceCategoryEntity.serviceCategory.contains("IDENTITY", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_identity_24)
                    )
            }

            serviceCategoryEntity.serviceCategory.contains("AGRICULTURE", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_agriculture_24)
                    )
            }

            serviceCategoryEntity.serviceCategory.contains("IDEAS", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(mContext, R.drawable.ic_twotone_creatives_24)
                    )
            }

        }
    }

    private var onItemClickListener: ((ServiceCategoryEntity) -> Unit)? = null

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun openCategory(position: Int) {
        val serviceCategoryEntity = differ.currentList[position]

        val category = JSONObject()
        mixpanelAPI =
            MixpanelAPI.getInstance(mContext, mContext.resources.getString(R.string.mixpanelToken))
        openFragment(serviceCategoryEntity)
        category.put("CATEGORY_NAME", serviceCategoryEntity.serviceCategory)
        mixpanelAPI.track("serviceCategory_selected", category)
    }

    private fun openFragment(serviceCategoryEntity: ServiceCategoryEntity) {
        val bundle = Bundle()
        val fragmentActivity = mContext as FragmentActivity
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val pagesFragment = PagesFragment()
        pagesFragment.arguments?.putString(CATEGORY, serviceCategoryEntity.serviceCategory)

        bundle.putString(SERVICE_CATEGORY, serviceCategoryEntity.serviceCategory)
        pagesFragment.arguments = bundle

        fragmentTransaction.replace(R.id.frame, pagesFragment)
        fragmentTransaction.commit()
    }
}