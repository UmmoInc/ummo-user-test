package xyz.ummo.user;

import java.util.ArrayList;

public class Product {

    private String providerName;
    private String location;
    private String contact;
    private String description;
    private String cost;
    private String  docs;
    private String  steps;
    private String duration;
    String id;

    public Product(String providerName, String location, String contact, String id, String description, String steps, String duration, String docs, String cost){
        this.providerName= providerName;
        this.location= location;
        this.contact= contact;
        this.id= id;
        this.docs = docs;
        this.steps = steps;
        this.description = description;
        this.duration = duration;
        this.cost = cost;
    }

    public String  getDocs() {
        return docs;
    }

    public String getDescription() {
        return description;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getSteps() {
        return steps;
    }

    public String getDuration() {
        return duration;
    }

    public String getLocation() {
        return location;
    }

    public String getContact() {
        return contact;
    }

    public String getId() {
        return id;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }
}
