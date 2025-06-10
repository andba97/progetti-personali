package dev.andba.trismultiplayergame.module;

public class ClientOperation {
    private String action;
    User user;
    String competitor;
    public ClientOperation() {}


    public ClientOperation(String operation, User user) {
        this.action = operation;
        this.user = user;
    }
    public ClientOperation(String operation, User user, String competitor) {
        this.action = operation;
        this.user = user;
        this.competitor = competitor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCompetitor() {
        return competitor;
    }

    public void setCompetitor(String competitor) {
        this.competitor = competitor;
    }
}
