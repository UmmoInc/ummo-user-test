package xyz.ummo.user.rvItems

import android.content.Context
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentActivity
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_category.view.*
import xyz.ummo.user.R
import xyz.ummo.user.models.ServiceCategoryModel
import xyz.ummo.user.ui.fragments.pagesFrags.PagesFragment
import xyz.ummo.user.utilities.SERVICE_CATEGORY

class ServiceCategoryItem(
    private val serviceCategoryModel: ServiceCategoryModel,
    val context: Context
) : Item<GroupieViewHolder>() {

    private val bundle = Bundle()

    override fun getLayout(): Int {
        return R.layout.service_category
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.category_title_text_view.text = serviceCategoryModel.serviceCategoryName
        viewHolder.itemView.category_service_count_text_view.text =
            "${serviceCategoryModel.serviceCount} services"

        addThumbnailsToCategories(viewHolder)
        openCategory(viewHolder)

    }

    private fun addThumbnailsToCategories(viewHolder: GroupieViewHolder) {
        /** Applying appropriate artwork for each [ServiceCategoryItem] **/
        when {
            serviceCategoryModel.serviceCategoryName.contains("HEALTH", true) -> {
                viewHolder.itemView.category_image_view
                    .setImageDrawable(
                        AppCompatResources
                            .getDrawable(context, R.drawable.ic_twotone_health_and_safety_24)
                    )
            }
            serviceCategoryModel.serviceCategoryName.contains("EDUCATION", true) -> {
                viewHolder.itemView.category_image_view.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_twotone_school_24
                    )
                )
            }
            serviceCategoryModel.serviceCategoryName.contains("BUSINESS", true) -> {
                viewHolder.itemView.category_image_view.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_twotone_business_center_24
                    )
                )
            }
            serviceCategoryModel.serviceCategoryName.contains("TRAVEL", true) -> {
                viewHolder.itemView.category_image_view.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_twotone_flight_takeoff_24
                    )
                )
            }

            serviceCategoryModel.serviceCategoryName.contains("VEHICLES", true) -> {
                viewHolder.itemView.category_image_view.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_twotone_car_24
                    )
                )
            }

            serviceCategoryModel.serviceCategoryName.contains("IDENTITY", true) -> {
                viewHolder.itemView.category_image_view.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_twotone_identity_24
                    )
                )
            }

            serviceCategoryModel.serviceCategoryName.contains("AGRICULTURE", true) -> {
                viewHolder.itemView.category_image_view.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_twotone_agriculture_24
                    )
                )
            }

            serviceCategoryModel.serviceCategoryName.contains("IDEAS", true) -> {
                viewHolder.itemView.category_image_view.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_twotone_creatives_24
                    )
                )
            }
        }
    }

    private fun openCategory(viewHolder: GroupieViewHolder) {
        viewHolder.itemView.service_category_card_view.setOnClickListener {
            openFragment()
        }
    }

    private fun openFragment() {
        val fragmentActivity = context as FragmentActivity
        val fragmentManager = fragmentActivity.supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val pagesFragment = PagesFragment()

        bundle.putString(SERVICE_CATEGORY, serviceCategoryModel.serviceCategoryName)
        pagesFragment.arguments = bundle

        fragmentTransaction.replace(R.id.frame, pagesFragment)
        fragmentTransaction.commit()
    }
}