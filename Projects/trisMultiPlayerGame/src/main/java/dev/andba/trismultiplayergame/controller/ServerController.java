package dev.andba.trismultiplayergame.controller;

import dev.andba.trismultiplayergame.module.ServerTris;
import dev.andba.trismultiplayergame.util.DatabaseUtil;
import jakarta.persistence.EntityManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ServerController {
    @FXML
    private Label welcomeText;

    // EntityManager per operazioni JPA
    private EntityManager em;
    private ServerTris server;

    @FXML
    protected void initialize(){
        // Ottiene EntityManager per collegarsi al DB
        em = DatabaseUtil.getEntityManager();

        server = new ServerTris(50, em);
        server.StartServer();
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}