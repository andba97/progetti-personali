package dev.andba.trismultiplayergame.module;

import java.util.ArrayList;

public class ServerOperation {

    private String operation;
    private ArrayList<String> playerList;
    private String challenger;

    public ServerOperation(String operation, ArrayList<String> playerList) {
        this.operation = operation;
        this.playerList = playerList;
    }

    public ServerOperation(String operation, String challenger) {
        this.operation = operation;
        this.challenger = challenger;
    }

    public ServerOperation() {
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public ArrayList<String> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(ArrayList<String> playerList) {
        this.playerList = playerList;
    }

    public String getChallenger() {
        return challenger;
    }

    public void setChallenger(String challenger) {
        this.challenger = challenger;
    }
}
