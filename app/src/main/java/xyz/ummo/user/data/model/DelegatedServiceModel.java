package xyz.ummo.user.data.model;

import org.json.JSONArray;

import java.util.ArrayList;

public interface DelegatedServiceModel {
//    String getServiceName();
//    String getId();
    String getDelegationId();
    String getDelegatedProductId();
    ArrayList getServiceProgress();
    String getServiceAgentId();

}
