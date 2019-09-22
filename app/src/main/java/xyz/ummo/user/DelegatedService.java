package xyz.ummo.user;

public class DelegatedService {

    String serviceName;
    String agentName;
    String serviceId;

    public DelegatedService(String serviceName, String agentName, String serviceId) {
        this.serviceName = serviceName;
        this.agentName = agentName;
        this.serviceId=serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getServiceId(){return serviceId;}
}
