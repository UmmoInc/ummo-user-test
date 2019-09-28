package xyz.ummo.user;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class ServiceProvider {

        public String serviceProviderName;
        public String province;
        public String municipality;
        public String town;
        public ServiceProvider(String serviceProviderName){
            this.serviceProviderName= serviceProviderName;
        }

        public ServiceProvider(JSONObject jsonObject){
            try{
                serviceProviderName = jsonObject.getString("service_name");
                province = jsonObject.getJSONObject("location").getString("province");
                municipality = jsonObject.getJSONObject("location").getString("municipality");
                town = jsonObject.getJSONObject("location").getString("town");
            }catch (JSONException jse){
                Log.e("ServiceProvider.java",jse.toString());
            }
        }

    }

    /*
  location: {
    province: String,
    municipality: String,
    town: String
  },
     */