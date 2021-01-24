package xyz.ummo.user.data.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

import xyz.ummo.user.data.model.DelegatedServiceModel;

@Entity(tableName = "delegated_service")
public class DelegatedServiceEntity implements DelegatedServiceModel {

    /*@PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private String id;

    */
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "service_id")
    private String serviceId;

    /*@NonNull
    @ColumnInfo(name = "service_name")
    private String serviceName;*/

    @NonNull
    @ColumnInfo(name = "service_progress")
    private ArrayList serviceProgress;

    @NonNull
    @ColumnInfo(name = "delegated_product_id")
    private String delegatedProductId;

    @ColumnInfo(name = "service_agent_id")
    private String serviceAgentId;

    public DelegatedServiceEntity(@NonNull String _serviceId,
//                                  @NonNull String _serviceName,
                                  @NonNull String _delegatedProductId,
//                                  @NonNull ArrayList _serviceProgress,
                                  @NonNull String _serviceAgentId){
        this.serviceId = _serviceId;
//        this.serviceName = _serviceName;
        this.delegatedProductId = _delegatedProductId;
//        this.serviceProgress = _serviceProgress;
        this.serviceAgentId = _serviceAgentId;
    }

    public DelegatedServiceEntity(){
        serviceId = null;
    }

    /*@Override
    public String getId() {
        return null;
    }*/

    @Override
    @NonNull
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(@NonNull String serviceId) {
        this.serviceId = serviceId;
    }

    /*@Override
    @NonNull
    public String getServiceName() {
        return serviceName;
    }*/

//    public void setServiceName(@NonNull String serviceName) {
//        this.serviceName = serviceName;
//    }

    @Override
    @NonNull
    public ArrayList getServiceProgress() {
        return serviceProgress;
    }

    public void setServiceProgress(@NonNull ArrayList serviceProgress) {
        this.serviceProgress = serviceProgress;
    }

    @Override
    @NonNull
    public String getDelegatedProductId() {
        return delegatedProductId;
    }

    public void setDelegatedProductId(@NonNull String delegatedProductId) {
        this.delegatedProductId = delegatedProductId;
    }

    @Override
    public String getServiceAgentId() {
        return serviceAgentId;
    }

    public void setServiceAgentId(String serviceAgentId) {
        this.serviceAgentId = serviceAgentId;
    }
}
