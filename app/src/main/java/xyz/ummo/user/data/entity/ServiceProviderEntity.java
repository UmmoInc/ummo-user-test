package xyz.ummo.user.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

import xyz.ummo.user.data.model.ServiceProviderModel;

@Entity(tableName = "service_provider")
public class ServiceProviderEntity implements ServiceProviderModel {
    @NonNull
    @PrimaryKey()
    @ColumnInfo(name = "service_provider_id")
    private String serviceProviderId;

    @ColumnInfo(name = "service_provider_province")
    private String serviceProviderProvince;

    @ColumnInfo(name = "service_provider_municipality")
    private String serviceProviderMunicipality;

    @ColumnInfo(name = "service_provider_town")
    private String serviceProviderTown;

    public String getServiceProviderProvince() {
        return serviceProviderProvince;
    }

    public void setServiceProviderProvince(String serviceProviderProvince) {
        this.serviceProviderProvince = serviceProviderProvince;
    }

    public String getServiceProviderMunicipality() {
        return serviceProviderMunicipality;
    }

    public void setServiceProviderMunicipality(String serviceProviderMunicipality) {
        this.serviceProviderMunicipality = serviceProviderMunicipality;
    }

    public String getServiceProviderTown() {
        return serviceProviderTown;
    }

    public void setServiceProviderTown(String serviceProviderTown) {
        this.serviceProviderTown = serviceProviderTown;
    }

    @Override
    @NonNull
    public String getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(@NonNull String serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    @Override
    @NonNull
    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(@NonNull String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    /*@Override
    @NonNull
    public ArrayList<String> getServiceProviderLocation() {
        return serviceProviderLocation;
    }

    public void setServiceProviderLocation(@NonNull ArrayList<String> serviceProviderLocation) {
        this.serviceProviderLocation = serviceProviderLocation;
    }*/

    @NonNull
    @ColumnInfo(name = "service_provider_name")
    private String serviceProviderName;

    /*@NonNull
    @ColumnInfo(name = "service_provider_location")
    private ArrayList<String> serviceProviderLocation;*/

    public ServiceProviderEntity(){}

    public ServiceProviderEntity(@NonNull String _serviceProviderId,
                                 @NonNull String _serviceProviderName,
                                 String _serviceProviderProvince,
                                 String _serviceProviderMunicipality,
                                 String _serviceProviderTown){
        this.serviceProviderId = _serviceProviderId;
        this.serviceProviderName = _serviceProviderName;
        this.serviceProviderMunicipality = _serviceProviderMunicipality;
        this.serviceProviderTown = _serviceProviderTown;
        this.serviceProviderProvince = _serviceProviderProvince;
    }
}
