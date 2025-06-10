package dev.andba.trismultiplayergame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.andba.trismultiplayergame.module.Client;
import dev.andba.trismultiplayergame.module.ClientOperation;
import dev.andba.trismultiplayergame.module.ServerOperation;
import dev.andba.trismultiplayergame.module.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.Arrays;
import java.util.List;

public class ClientHubController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Client client;
    private User user;
    private boolean readyToPlay = true;
    ObservableList<String> view;

    @FXML
    private ListView<String> listView;

    @FXML
    private Label messageLabel;


    private void handleMessage(String response) {
        Platform.runLater(() -> {
            try {
                ServerOperation serverOperation = objectMapper.readValue(response, ServerOperation.class);
                switch (serverOperation.getOperation()) {
                    case "GetOnlinePlayer" -> {
                        System.out.println(response);
                        List<String> players = serverOperation.getPlayerList();
                        if (players != null) {
                            view = FXCollections.observableArrayList(players);
                        } else {
                            view = FXCollections.observableArrayList(); // lista vuota
                        }
                        listView.setItems(view);
                    }
                    case "RequestGame" -> handleRequestGame(serverOperation);
                    default -> messageLabel.setText("Errore imprevisto");
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleRequestGame(ServerOperation serverOperation) {
        if(readyToPlay) {
            readyToPlay = false;
            messageLabel.setText("Sfida ricevuta da: " + serverOperation.getChallenger());
        }
        else
            messageLabel.setText("Sfida ricevuta da: " +  serverOperation.getChallenger() + "ma gia impegnato.");
    }

    @FXML
    void challenge(ActionEvent event) {
        String challengare = listView.getSelectionModel().getSelectedItem();
        ClientOperation operation = new ClientOperation("RequestGame",user,challengare);
        try {
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message);
            client.send(message);
            readyToPlay = false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void initialize(Client client) {
        this.client = client;
        this.client.setMessageListener(this::handleMessage);
        user = client.getUser();

        ClientOperation operation = new ClientOperation("GetOnlinePlayer",user);
        try {
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message);
            client.send(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
