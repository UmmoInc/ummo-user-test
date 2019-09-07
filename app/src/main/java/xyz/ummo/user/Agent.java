package xyz.ummo.user;

public class Agent {

    private String agentName;
    private String agentContact;
    private String publicRating;


    public Agent(String agentName, String agentContact, String publicRating){

        this.agentName = agentName;
        this.agentContact = agentContact;
        this.publicRating = publicRating;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getAgentContact() {
        return agentContact;
    }

    public String getPublicRating() {
        return publicRating;
    }
}
