package xyz.ummo.user;

public class Product {

    String providerName;
    String location;
    String contact;
    String description;

    public String getDocs() {
        return docs;
    }

    String docs;

    public String getSteps() {
        return steps;
    }

    public String getDuration() {
        return duration;
    }

    String steps;
    String duration;
    String id;

    public Product(String providerName, String location, String contact, String id, String description, String steps, String duration, String docs){
        this.providerName= providerName;
        this.location= location;
        this.contact= contact;
        this.id= id;
        this.docs = docs;
        this.steps = steps;
        this.description = description;
        this.duration = duration;
    }

    public String getDescription() {
        return description;
    }

    public String getProviderName() {
        return providerName;
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
}
