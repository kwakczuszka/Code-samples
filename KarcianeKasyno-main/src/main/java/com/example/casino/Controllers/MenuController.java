package com.example.casino.Controllers;

import com.example.casino.Main;
import com.example.casino.Packets.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.UUID;

public class MenuController {

    @FXML
    private Button exit;
    @FXML
    private Button minimize;
    @FXML
    private Button CreateButton;

    @FXML
    private Button JoinButton;

    @FXML
    private Button LogoutButton;

    @FXML
    private Button goToPokerRankingButton, goToRemikRankingButton;

    @FXML
    private TextField gameID;

    @FXML
    private TextField gameID1;

    @FXML
    public void createPokerGame() {
        String uniqueID = UUID.randomUUID().toString();
        Packet p = new CreateGamePacket("game creation", uniqueID, CreateGamePacket.GameType.POKER);
        Main.client.sendPacket(p);
    }

    @FXML
    public void createRummyGame() {
        String uniqueID = UUID.randomUUID().toString();
        Packet p = new CreateGamePacket("game creation", uniqueID, CreateGamePacket.GameType.RUMMY);
        Main.client.sendPacket(p);
    }

    @FXML
    public void joinPokerGame() {
        String uuid = gameID.getText();
        if (uuid.isEmpty()) {
            System.out.println("Nie podano kodu");
        } else {
            JoinGamePacket packet = new JoinGamePacket("JOIN", uuid, JoinGamePacket.GameType.POKER, JoinGamePacket.Status.JOIN);
            Main.client.sendPacket(packet);
        }
    }

    @FXML
    public void joinRummyGame() {
        String uuid = gameID1.getText();
        if (uuid.isEmpty()) {
            System.out.println("Nie podano kodu");
        } else {
            JoinGamePacket packet = new JoinGamePacket("JOIN", uuid, JoinGamePacket.GameType.RUMMY, JoinGamePacket.Status.JOIN);
            Main.client.sendPacket(packet);
        }
    }

    @FXML
    public void LogOut() {
        Main.client.sendPacket(new LoginPacket("Loging out", LoginPacket.Status.LOGOUT));
    }

    @FXML
    public void goToPokerRanking() {
        Main.client.sendPacket(new RankingPacket("Switching to Poker Ranking", RankingPacket.Status.POKER));
    }

    @FXML
    public void goToRemikRanking() {
        Main.client.sendPacket(new RankingPacket("Switching to Remik Ranking", RankingPacket.Status.REMIK));

    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) exit.getScene().getWindow();
        //stage.setIconified(true);
        try {
            Main.client.stopConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Main.client.interrupt();
        System.out.println("zamykanie");
        stage.close();
        System.exit(0);
    }

    @FXML
    public void minimizeWindow() {
        Stage stage = (Stage) minimize.getScene().getWindow();
        stage.setIconified(true);
    }
}
