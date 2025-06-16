package dev.andba.trismultiplayergame.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.andba.trismultiplayergame.module.Client;
import dev.andba.trismultiplayergame.module.ClientOperation;
import dev.andba.trismultiplayergame.module.User;
import dev.andba.trismultiplayergame.util.SceneManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ClientRegisterController {
    @FXML
    private PasswordField confirmPasswordLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField usernameField;

    private final ObjectMapper objectMapper  = new ObjectMapper();;
    private Client client;

    /**
     * Metodo chiamato quando l'utente clicca il pulsante "Registrati".
     */
    @FXML
    public void register(ActionEvent event) {
        // Recupera i valori dai campi di input
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Controllo input utente: username vuoto
        if (username.isBlank()) {
            messageLabel.setText("Per favore inserire un username");
            return;
        }
        // Controllo input utente: password vuota
        else if (password.isBlank()) {
            messageLabel.setText("Per favore inserire una password");
            return;
        }
        // Controllo input utente: password di conferma vuota o non coincidente
        else if (confirmPassword.isBlank() || !confirmPassword.equals(password)) {
            messageLabel.setText("Per favore confermare la password in modo corretto");
            return;
        }

        // Crea un oggetto utente e una richiesta di registrazione
        User user = new User(username, password);
        ClientOperation operation = new ClientOperation("Register", user);

        try {
            // Converte l'oggetto in JSON e lo invia al server
            String message = objectMapper.writeValueAsString(operation);
            client.send(message);
        } catch (JsonProcessingException e) {
            // Se la conversione JSON fallisce, genera un'eccezione
            throw new RuntimeException(e);
        }
    }

    /**
     * Metodo chiamato quando l'utente clicca il pulsante "Torna al Login".
     */
    @FXML
    public void backToLogin(ActionEvent event) {
        // Cambia scena tornando alla schermata di login e inizializza il relativo controller
        ClientLoginController clientLoginController = SceneManager.switchScene("ClientLogin-view", 400, 400);
        clientLoginController.initialize(client);
    }

    /**
     * Metodo chiamato quando il server invia una risposta alla registrazione.
     */
    private void handleMessage(String response) {
        // Esegui sul thread dell'interfaccia grafica
        Platform.runLater(() -> {
            // Gestione delle varie risposte dal server
            switch (response) {
                case "OK":
                    messageLabel.setText("Registrazione effettuato con successo");
                    break;
                case "ERROR4":
                    messageLabel.setText("Username già presente");
                    break;
                default:
                    messageLabel.setText("Default error");
            }

            // Pulisce i campi di input dopo la risposta
            usernameField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
        });
    }

    /**
     * Inizializza il controller con l'oggetto client e imposta il listener per i messaggi.
     */
    public void initialize(Client client) {
        this.client = client;
        this.client.setMessageListener(this::handleMessage);
    }
}
