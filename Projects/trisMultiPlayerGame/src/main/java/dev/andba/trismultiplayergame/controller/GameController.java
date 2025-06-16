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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;


public class GameController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Client client;
    private User user;
    private String challenger;
    private char symbol;
    private final char[][] gameStatus = new char[3][3];
    private final int lastChoiche[] = new int[2];

    private final Image xImage = new Image(getClass().getResourceAsStream("/img/X.png"));
    private final Image oImage = new Image(getClass().getResourceAsStream("/img/Circle.png"));

    @FXML
    private Button confirmButton;

    @FXML
    private GridPane grid;

    @FXML
    private Label messageLabel;

    /**
     * Inizializza il controller per la partita.
     * @param client oggetto client per la comunicazione
     * @param challenger avversario
     * @param firstTurn true se questo giocatore inizia
     */
    public void initialize(Client client, String challenger, boolean firstTurn) {
        this.client = client;
        this.client.setMessageListener(this::handleMessage);
        this.challenger = challenger;
        this.user = client.getUser();

        // Se non è il primo turno, disabilita l'interfaccia utente
        if (!firstTurn) {
            confirmButton.setDisable(true);
            grid.setDisable(true);
            symbol = 'X'; // Chi non inizia gioca con X
        } else {
            symbol = 'O'; // Chi inizia gioca con O
        }

        // Inizializza la scelta a "nessuna"
        lastChoiche[0] = -1;
        lastChoiche[1] = -1;
    }

    /**
     * Gestione dei messaggi ricevuti dal server.
     */
    private void handleMessage(String response) {
        Platform.runLater(() -> {
            try {
                ServerResponse<?> serverResponse = objectMapper.readValue(response, new TypeReference<>() {});
                switch (serverResponse.getOperation()) {
                    case "RequestGame" -> handleRequestGame(objectMapper.readValue(response, new TypeReference<ServerResponse<String>>() {}));
                    case "Moves" -> handleMoves(objectMapper.readValue(response, new TypeReference<ServerResponse<String>>() {}));
                    case "Lose" -> handleLose();
                    default -> messageLabel.setText("");
                }
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Se ricevi una nuova richiesta mentre sei in partita, rispondi come "Occupato".
     */
    private void handleRequestGame(ServerResponse<String> serverResponse) {
        ClientOperation operation = new ClientOperation("Occupied", user, serverResponse.getData());
        try {
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message);
            client.send(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Elabora la mossa dell'avversario.
     */
    private void handleMoves(ServerResponse<String> serverResponse) {
        String moves = serverResponse.getData().replaceAll("[\\[\\]\\s]", "");
        String[] parts = moves.split(",");
        int row = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);

        // Determina il simbolo dell'avversario
        char enemySymbol = (symbol == 'X') ? 'O' : 'X';
        gameStatus[row][col] = enemySymbol;

        // Aggiorna la griglia visivamente
        for (Node node : grid.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);
            int r = nodeRow == null ? 0 : nodeRow;
            int c = nodeCol == null ? 0 : nodeCol;

            if (r == row && c == col && node instanceof ImageView imageView) {
                imageView.setImage(enemySymbol == 'X' ? xImage : oImage);
                break;
            }
        }

        // Resetta stato e riabilita l’interfaccia utente per la tua mossa
        lastChoiche[0] = -1;
        lastChoiche[1] = -1;
        confirmButton.setDisable(false);
        grid.setDisable(false);

        // Controlla pareggio
        if (checkDraw()) {
            ClientHubController controller = SceneManager.switchScene("ClientHub-view", 600, 450);
            controller.initialize(client, "Peccato hai pareggiato contro: " + challenger);
        }
    }

    /**
     * Gestisce la perdita della partita.
     */
    private void handleLose() {
        ClientHubController controller = SceneManager.switchScene("ClientHub-view", 600, 450);
        controller.initialize(client, "Peccato hai perso contro: " + challenger);
    }

    /**
     * Gestione della selezione di una cella nella griglia.
     */
    @FXML
    void moves(javafx.scene.input.MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        int cellWidth = 100, cellHeight = 100;

        int column = (int) (x / cellWidth);
        int row = (int) (y / cellHeight);

        if (column < 0 || column >= 3 || row < 0 || row >= 3) return;

        // Se la cella è vuota
        if (gameStatus[row][column] == '\0') {
            for (Node node : grid.getChildren()) {
                Integer nodeRow = GridPane.getRowIndex(node);
                Integer nodeCol = GridPane.getColumnIndex(node);
                int r = nodeRow == null ? 0 : nodeRow;
                int c = nodeCol == null ? 0 : nodeCol;

                // Imposta simbolo selezionato
                if (r == row && c == column && node instanceof ImageView imageView)
                    imageView.setImage(symbol == 'X' ? xImage : oImage);

                // Rimuove eventuale simbolo dalla scelta precedente
                if (r == lastChoiche[0] && c == lastChoiche[1] && node instanceof ImageView imageView) {
                    imageView.setImage(null);
                    gameStatus[r][c] = '\0'; // Reset scelta precedente (attenzione: può essere un bug, vedi nota sotto)
                }
            }

            // Aggiorna ultima scelta e stato del gioco
            lastChoiche[0] = row;
            lastChoiche[1] = column;
            gameStatus[row][column] = symbol;
        }
    }

    /**
     * Conferma la mossa e invia al server.
     */
    @FXML
    public void confirm(ActionEvent event) {
        ClientOperation operation;

        if (!checkWin()) {
            operation = new ClientOperation("Moves", user, challenger);
            operation.setMoves(lastChoiche);
            confirmButton.setDisable(true);
            grid.setDisable(true);
        } else {
            operation = new ClientOperation("Win", user, challenger);
        }

        try {
            String message = objectMapper.writeValueAsString(operation);
            System.out.println(message);
            client.send(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // Controlla se la mossa ha portato a una vittoria o pareggio
        if (operation.getAction().equals("Win")) {
            ClientHubController controller = SceneManager.switchScene("ClientHub-view", 600, 450);
            controller.initialize(client, "Complimenti hai vinto contro: " + challenger);
        } else if (checkDraw()) {
            ClientHubController controller = SceneManager.switchScene("ClientHub-view", 600, 450);
            controller.initialize(client, "Peccato hai pareggiato contro: " + challenger);
        }
    }

    /**
     * Controlla se c'è una combinazione vincente.
     */
    public boolean checkWin() {
        // Righe e colonne
        for (int i = 0; i < 3; i++) {
            if (gameStatus[i][0] != '\0' && gameStatus[i][0] == gameStatus[i][1] && gameStatus[i][1] == gameStatus[i][2])
                return true;
            if (gameStatus[0][i] != '\0' && gameStatus[0][i] == gameStatus[1][i] && gameStatus[1][i] == gameStatus[2][i])
                return true;
        }

        // Diagonali
        if (gameStatus[0][0] != '\0' && gameStatus[0][0] == gameStatus[1][1] && gameStatus[1][1] == gameStatus[2][2])
            return true;

        if (gameStatus[0][2] != '\0' && gameStatus[0][2] == gameStatus[1][1] && gameStatus[1][1] == gameStatus[2][0])
            return true;

        return false;
    }

    /**
     * Controlla se tutte le celle sono piene senza vincitore.
     */
    public boolean checkDraw() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (gameStatus[row][col] == '\0') {
                    return false; // C'è almeno una cella vuota
                }
            }
        }
        return true; // Nessuna cella vuota, ma nessuno ha vinto
    }
}

