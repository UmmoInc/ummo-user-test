package xyz.ummo.user;

public class DelegatedService {

    String serviceName;
    String agentName;
    String serviceId;
    String agentId;
    String userId;
    String productId;

    public DelegatedService(
            String serviceName,
            String agentName,
            String serviceId,
            String agentId,
            String userId,
            String productId) {
        this.serviceName = serviceName;
        this.agentName = agentName;
        this.serviceId=serviceId;
        this.agentId = agentId;
        this.userId = userId;
        this.productId = productId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getServiceId(){return serviceId;}

    public String getAgentId() {
        return agentId;
    }

    public String getProductId() {
        return productId;
    }

    public String getUserId() {
        return userId;
    }
}
