package com.example.casino.Controllers;


import com.example.casino.Main;
import com.example.casino.Packets.GameReadyPacket;
import com.example.casino.Packets.JoinGamePacket;
import com.example.casino.Player;
import com.example.casino.Server.RummyGame;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RummyLobbyController {

    private boolean isReady;
    @FXML
    private Button LogoutButton;

    @FXML
    private Label uuid;

    @FXML
    private HBox playersContainer;

    @FXML
    private Button ReadyButton;
    private RummyGame rummyGame;

    private Map<Integer, Player> players = new HashMap<>();

    public void setUuid(String uuid) {
        this.uuid.setText(uuid);
    }

    public void setRummyGame(RummyGame RummyGame) {
        this.rummyGame = rummyGame;
    }

    public void refreshPlayerContainer() {
        playersContainer.getChildren().clear();
        for (Map.Entry<Integer, Player> p : this.players.entrySet()) {
            Integer playerID = p.getKey();
            Player player = p.getValue();
            try {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/com/example/casino/MiniPlayer.fxml"));
                VBox playerBox = fxmlLoader.load();
                MiniPlayerController miniPlayerController = fxmlLoader.getController();
                miniPlayerController.setData(player.getPlayerData(), player.isReady());
                playersContainer.getChildren().add(playerBox);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FXML
    public void copyToClipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(uuid.getText());
        clipboard.setContent(content);
    }

    @FXML
    public void leaveGame() {
        Main.client.sendPacket(new JoinGamePacket("leave", uuid.getText(), JoinGamePacket.GameType.RUMMY, JoinGamePacket.Status.LEAVE));
    }

    public void addPlayer(Player player) {
        this.players.put(player.getPlayerID(), player);
    }


    public void removePlayer(Player player) {
        this.players.remove(player.getPlayerID());
    }

    public void changeStatus(boolean isReady, Player player) {
        if (isReady) {
            this.players.get(player.getPlayerID()).setReady(true);
        } else {
            this.players.get(player.getPlayerID()).setReady(false);
        }
    }


    public void ready() {
        if (!isReady) {
            Main.client.sendPacket(new GameReadyPacket("ready", GameReadyPacket.GameType.RUMMY, uuid.getText(), GameReadyPacket.Status.READY));
            isReady = true;
            ReadyButton.setText("Not Ready");
        } else {
            Main.client.sendPacket(new GameReadyPacket("notready", GameReadyPacket.GameType.RUMMY, uuid.getText(), GameReadyPacket.Status.NOT_READY));
            isReady = false;
            ReadyButton.setText("Ready");
        }
    }

}
