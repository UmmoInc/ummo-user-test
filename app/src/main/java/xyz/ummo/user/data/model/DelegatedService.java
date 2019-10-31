package xyz.ummo.user.data.model;

import org.json.JSONArray;

import java.util.ArrayList;

public interface DelegatedService {
//    String getServiceName();
    String getServiceId();
    String getDelegatedProductId();
    ArrayList getServiceProgress();
    String getServiceAgentId();

}
