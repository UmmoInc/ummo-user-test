package xyz.ummo.user;

public class Product {

    String providerName;
    String location;
    String contact;
    String website;

    public Product(String providerName, String location, String contact, String website){
        this.providerName= providerName;
        this.location= location;
        this.contact= contact;
        this.website= website;

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

    public String getWebsite() {
        return website;
    }
}
