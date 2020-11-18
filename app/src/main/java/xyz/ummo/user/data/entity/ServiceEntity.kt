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

    @ColumnInfo(name = "presence_required") //5
    override var presenceRequired: Boolean? = null

    @ColumnInfo(name = "service_cost") //6
    override var serviceCost: Int? = null

    @ColumnInfo(name = "service_documents") //7
    override var serviceDocuments: ArrayList<String>? = null

    @ColumnInfo(name = "service_duration") //8
    override var serviceDuration: String? = null

    @ColumnInfo(name = "disapproval_count") //9
    override var disapprovalCount: Int? = null

    @ColumnInfo(name = "approval_count") //10
    override var approvalCount: Int? = null

    @ColumnInfo(name = "comments") //11
    override var comments: ArrayList<String>? = null

    @ColumnInfo(name = "service_shares") //12
    override var serviceShares: Int? = null

    @ColumnInfo(name = "service_views") //13
    override var serviceViews: Int? = null

    @ColumnInfo(name = "service_provider") //14
    override var serviceProvider: String? = null
}