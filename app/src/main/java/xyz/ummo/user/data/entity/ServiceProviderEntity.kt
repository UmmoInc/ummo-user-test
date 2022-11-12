package xyz.ummo.user.data.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import xyz.ummo.user.data.model.ServiceProviderModel

@Entity(tableName = "service_provider")
class ServiceProviderEntity : ServiceProviderModel {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "service_provider_id")
    override lateinit var serviceProviderId: String

    @ColumnInfo(name = "service_provider_name")
    override var serviceProviderName: String? = null

    @ColumnInfo(name = "service_provider_description")
    override var serviceProviderDescription: String? = null

    @ColumnInfo(name = "service_provider_contact")
    override var serviceProviderContact: String? = null

    @ColumnInfo(name = "service_provider_email")
    override var serviceProviderEmail: String? = null

    @ColumnInfo(name = "service_provider_address")
    override var serviceProviderAddress: String? = null

}