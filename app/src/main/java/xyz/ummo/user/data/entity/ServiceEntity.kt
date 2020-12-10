package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.ummo.user.data.model.ServiceModel

@Entity(tableName = "service")
class ServiceEntity : ServiceModel {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "service_id") //0
    override var serviceId: String? = null

    @ColumnInfo(name = "service_name") //1
    override var serviceName: String? = null

    @ColumnInfo(name = "service_description") //2
    override var serviceDescription: String? = null

    @ColumnInfo(name = "service_eligibility") //3
    override var serviceEligibility: String? = null

    @ColumnInfo(name = "service_centres") //4
    override var serviceCentres: ArrayList<String>? = null
//    override var serviceCentres: String? = null

    @ColumnInfo(name = "presence_required") //5
    override var presenceRequired: Boolean? = null

    @ColumnInfo(name = "service_cost") //6
    override var serviceCost: String? = null

    @ColumnInfo(name = "service_documents") //7
    override var serviceDocuments: ArrayList<String>? = null

    @ColumnInfo(name = "service_duration") //8
    override var serviceDuration: String? = null

    @ColumnInfo(name = "useful_count") //9
    override var usefulCount: Int? = null

    @ColumnInfo(name = "not_useful_count") //10
    override var notUsefulCount: Int? = null

    @ColumnInfo(name = "service_comment") //11
    override var serviceComments: ArrayList<String>? = null

    @ColumnInfo(name = "comment_count") //12
    override var commentCount: Int? = null

    @ColumnInfo(name = "service_shares") //13
    override var serviceShares: Int? = null

    @ColumnInfo(name = "service_views") //14
    override var serviceViews: Int? = null

    @ColumnInfo(name = "service_provider") //15
    override var serviceProvider: String? = null

    @ColumnInfo(name = "bookmarked") //16
    override var bookmarked: Boolean? = false

    @ColumnInfo(name = "is_delegated")
    override var isDelegated: Boolean? = false
}