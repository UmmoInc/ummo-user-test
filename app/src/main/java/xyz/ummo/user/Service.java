package xyz.ummo.user;

public class Service {

    private String serviceName;
    private String serviceDescription;
    private String form;
    private String personalDocs;
    private String cost;
    private String duraion;


    public Service(String serviceName, String serviceDescription, String form, String personalDocs,
                   String cost, String duraion){

        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.form = form;
        this.personalDocs = personalDocs;
        this.cost = cost;
        this.duraion = duraion;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public String getForm() {
        return form;
    }

    public String getPersonalDocs() {
        return personalDocs;
    }

    public String getCost() {
        return cost;
    }

    public String getDuraion() {
        return duraion;
    }
}
