package dev.andba.trismultiplayergame.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.andba.trismultiplayergame.module.Client;
import dev.andba.trismultiplayergame.module.ClientOperation;
import dev.andba.trismultiplayergame.module.ServerResponse;
import dev.andba.trismultiplayergame.module.User;
import dev.andba.trismultiplayergame.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.List;

public class ClientHubController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Client client;
    private User user;
    private boolean readyToPlay = true;
    ObservableList<String> view;

    String challenger;

    @FXML
    private ListView<String> listView;

    @FXML
    private Label messageLabel;

    @FXML
    private Button acceptButton;

    @FXML
    private Button declineButton;

    @FXML
    private Button challengeButton;


    @FXML
    void challenge(ActionEvent event) {
        // Ottiene il nome del giocatore selezionato nella lista
        challenger = listView.getSelectionModel().getSelectedItem();

        // Crea un'operazione per richiedere una sfida al giocatore selezionato
        ClientOperation operation = new ClientOperation("RequestGame", user, challenger);
        try {
            // Converte l'operazione in JSON e la invia al server
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message); // Debug
            client.send(message);

            // Disabilita il bottone per evitare sfide multiple
            challengeButton.setDisable(true);
            readyToPlay = false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e); // Gestione dell'errore di serializzazione
        }
    }

    @FXML
    void decline(ActionEvent event) {
        // Crea un'operazione per rifiutare la sfida ricevuta
        ClientOperation operation = new ClientOperation("Decline", user, challenger);
        try {
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message);
            client.send(message);

            // Resetta l’interfaccia utente
            challengeButton.setDisable(false);
            acceptButton.setDisable(true);
            declineButton.setDisable(true);
            readyToPlay = true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void accept(ActionEvent event) {
        // Crea un'operazione per accettare la sfida
        ClientOperation operation = new ClientOperation("Accept", user, challenger);
        try {
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message);
            client.send(message);
            challengeButton.setDisable(false);

            // Cambia scena alla schermata di gioco, impostando il client e il nome del challenger
            GameController gameController = SceneManager.switchScene("Game-view", 600, 450);
            gameController.initialize(client, challenger, true); // true -> è stato sfidato
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(String response) {
        Platform.runLater(() -> {
            try {
                // Deserializza il messaggio JSON ricevuto
                ServerResponse<?> serverResponse = objectMapper.readValue(response, new TypeReference<ServerResponse<?>>() {});

                // Smista la risposta in base all'operazione
                switch (serverResponse.getOperation()) {
                    case "GetOnlinePlayer" -> {
                        // Aggiorna la lista dei giocatori online
                        ServerResponse<List<String>> typedResponse =
                                objectMapper.readValue(response, new TypeReference<ServerResponse<List<String>>>() {});
                        List<String> players = typedResponse.getData();
                        view = FXCollections.observableArrayList(players != null ? players : List.of());
                        listView.setItems(view);
                    }
                    case "RequestGame" -> {
                        // Gestisce una richiesta di sfida ricevuta
                        ServerResponse<String> typedResponse =
                                objectMapper.readValue(response, new TypeReference<ServerResponse<String>>() {});
                        handleRequestGame(typedResponse);
                    }
                    case "RequestResponse" -> {
                        // Gestisce la risposta alla richiesta di sfida
                        ServerResponse<String> typedResponse =
                                objectMapper.readValue(response, new TypeReference<ServerResponse<String>>() {});
                        handleRequestResponse(typedResponse);
                    }
                    default -> messageLabel.setText("Errore imprevisto");
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleRequestGame(ServerResponse<String> serverResponse) {
        if (readyToPlay) {
            // L'utente è disponibile: mostra i pulsanti per accettare o rifiutare la sfida
            readyToPlay = false;
            challenger = serverResponse.getData(); // Nome di chi ha lanciato la sfida
            messageLabel.setText("Sfida ricevuta da: " + challenger);
            acceptButton.setDisable(false);
            declineButton.setDisable(false);
            challengeButton.setDisable(true);
        } else {
            // L'utente è occupato: invia messaggio "Occupied" al server
            ClientOperation operation = new ClientOperation("Occupied", user, serverResponse.getData());
            try {
                String message = objectMapper.writeValueAsString(operation);
                System.out.println(message);
                client.send(message);
                readyToPlay = false;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleRequestResponse(ServerResponse<String> serverResponse) {
        if (serverResponse.getData().equals("Accepted")) {
            // La sfida è stata accettata: avvia la partita
            GameController gameController = SceneManager.switchScene("Game-view", 600, 450);
            gameController.initialize(client, challenger, false); // false -> ha sfidato
        } else {
            // La sfida è stata rifiutata o fallita
            messageLabel.setText(serverResponse.getData());
            readyToPlay = true;
            challengeButton.setDisable(false);
        }
    }

    /**
     * Inizializza il controller: disabilita pulsanti e richiede i giocatori online.
     */
    public void initialize(Client client) {
        acceptButton.setDisable(true);
        declineButton.setDisable(true);
        this.client = client;
        this.client.setMessageListener(this::handleMessage);
        user = client.getUser();

        // Invia al server la richiesta per ottenere la lista dei giocatori online
        ClientOperation operation = new ClientOperation("GetOnlinePlayer", user);
        try {
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message);
            client.send(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inizializza con un messaggio extra (es. da passare dopo una risposta precedente).
     */
    public void initialize(Client client, String message) {
        messageLabel.setText(message);
        initialize(client);
    }

}
