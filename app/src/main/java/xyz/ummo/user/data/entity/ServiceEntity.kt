package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import xyz.ummo.user.data.utils.ServiceBenefitsTypeConverter
import xyz.ummo.user.data.utils.ServiceCostTypeConverter
import xyz.ummo.user.models.ServiceBenefit
import xyz.ummo.user.models.ServiceCostModel
import java.io.Serializable

@Entity(tableName = "service")
data class ServiceEntity(
    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "service_id") //0
    var serviceId: String,

    @NonNull
    @ColumnInfo(name = "service_name") //1
    var serviceName: String? = null,

    @NonNull
    @ColumnInfo(name = "service_description") //2
    var serviceDescription: String? = null,

    @NonNull
    @ColumnInfo(name = "service_eligibility") //3
    var serviceEligibility: String? = null,

    @ColumnInfo(name = "service_centres") //4
    var serviceCentres: ArrayList<String>? = null,
//    override var serviceCentres: String? = null

    @NonNull
    @ColumnInfo(name = "delegatable") //5
    var delegatable: Boolean? = null,

    @NonNull
    @ColumnInfo(name = "service_cost") //6
    var serviceCost: ArrayList<ServiceCostModel>? = null,

    @ColumnInfo(name = "service_documents") //7
    var serviceDocuments: ArrayList<String>? = null,

    @ColumnInfo(name = "service_duration") //8
    var serviceDuration: String? = null,

    @ColumnInfo(name = "useful_count") //9
    var usefulCount: Int? = null,

    @ColumnInfo(name = "not_useful_count") //10
    var notUsefulCount: Int? = null,

    @TypeConverters(ServiceCostTypeConverter::class)
    @ColumnInfo(name = "service_comment") //11
    var serviceComments: ArrayList<String>? = null,

    @ColumnInfo(name = "service_steps") //12
    var serviceSteps: ArrayList<String>? = null,

    @ColumnInfo(name = "comment_count") //13
    var commentCount: Int? = null,

    @ColumnInfo(name = "service_shares") //14
    var serviceShares: Int? = null,

    @ColumnInfo(name = "service_views") //15
    var serviceViews: Int? = null,

    @ColumnInfo(name = "service_provider") //16
    var serviceProvider: String? = null,

    @ColumnInfo(name = "bookmarked") //17
    var bookmarked: Boolean? = false,

    @ColumnInfo(name = "is_delegated") //18
    var isDelegated: Boolean? = false,

    @ColumnInfo(name = "service_category") //19
    var serviceCategory: String? = null,

    @ColumnInfo(name = "service_link") //20
    var serviceLink: String? = null,

    @ColumnInfo(name = "service_attachment_name") //21
    var serviceAttachmentName: String? = null,

    @ColumnInfo(name = "service_attachment_size") //22
    var serviceAttachmentSize: String? = null,

    @ColumnInfo(name = "service_attachment_url") //23
    var serviceAttachmentURL: String? = null,

    @TypeConverters(ServiceBenefitsTypeConverter::class)
    @ColumnInfo(name = "service_benefits") //24
    var serviceBenefits: ArrayList<ServiceBenefit>
) : Serializable