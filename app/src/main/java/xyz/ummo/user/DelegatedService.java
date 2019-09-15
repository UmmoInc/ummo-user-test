package xyz.ummo.user;

public class DelegatedService {

    String serviceName;
    String agentName;

    public DelegatedService(String serviceName, String agentName) {
        this.serviceName = serviceName;
        this.agentName = agentName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getAgentName() {
        return agentName;
    }
}
