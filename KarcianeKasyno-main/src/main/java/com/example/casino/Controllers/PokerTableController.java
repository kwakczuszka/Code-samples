package com.example.casino.Controllers;

import com.example.casino.Main;
import com.example.casino.Packets.GamePacket;
import com.example.casino.Player;
import com.example.casino.Server.Karta;
import com.example.casino.Server.Rank;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class PokerTableController implements Initializable {
    @FXML
    private Button CallButton;

    @FXML
    private Button CheckButton;

    @FXML
    private Button FoldButton;

    @FXML
    private Button RaiseButton;

    @FXML
    private ImageView TableCard1;

    @FXML
    private ImageView TableCard2;

    @FXML
    private ImageView TableCard3;

    @FXML
    private ImageView TableCard4;

    @FXML
    private ImageView TableCard5;

    @FXML
    private ImageView yourCard1;
    @FXML
    private ImageView yourCard2;

    @FXML
    private Label yourMove;

    @FXML
    private ImageView player1Card1;

    @FXML
    private ImageView player1Card2;

    @FXML
    private ImageView player2Card1;

    @FXML
    private ImageView player2Card2;

    @FXML
    private ImageView player3Card1;

    @FXML
    private ImageView player3Card2;

    @FXML
    private ImageView player4Card1;

    @FXML
    private ImageView player4Card2;
    @FXML
    private Label pool;
    @FXML
    private Label player1Money;

    @FXML
    private Label player1Username;
    @FXML
    private Label player2Money;

    @FXML
    private Label player2Username;

    @FXML
    private Label player3Money;

    @FXML
    private Label player3Username;
    @FXML
    private Label player4Money;

    @FXML
    private Label player4Username;
    @FXML
    private Label yourMoney;

    @FXML
    private Label yourUsername;

    private Map<Player, PlayerFields> playersFields;
    private Integer currentBid;


    @FXML
    private Label player1Move;

    @FXML
    private Label player2Move;

    @FXML
    private Label player3Move;

    @FXML
    private Label player4Move;

    @FXML
    private ImageView winnerImage;

    @FXML
    private Label winnerLabel;

    @FXML
    Rectangle blackRectangle;

    @FXML
    public void callFunc() {
        if (Integer.valueOf(yourMoney.getText()) < currentBid) {
            pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + Integer.valueOf(yourMoney.getText())));
            yourMoney.setText("0");
        } else {

            pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + currentBid));
            yourMoney.setText(String.valueOf(Integer.valueOf(yourMoney.getText()) - currentBid));
        }
        Main.client.sendPacket(new GamePacket("next", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.CALL));
        disableButtonCall();
        disableButtonFold();
        disableButtonRaise();
        disableButtonCheck();
        showUserMoveText("CALL");
        hideAllUsersMoves();
    }


    @FXML
    public void checkFunc() {
        Main.client.sendPacket(new GamePacket("next", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.CALL));
        disableButtonCall();
        disableButtonFold();
        disableButtonRaise();
        disableButtonCheck();
        showUserMoveText("CHECK");
        hideAllUsersMoves();
    }

    @FXML
    public void foldFunc() {
        Main.client.sendPacket(new GamePacket("next", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.FOLD));
        disableButtonCall();
        disableButtonFold();
        disableButtonRaise();
        disableButtonCheck();
        showUserMoveText("FOLD");
        hideAllUsersMoves();
    }

    @FXML
    public void raiseFunc() {
        if (Integer.valueOf(yourMoney.getText()) < (currentBid + 50)) {
            pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + currentBid + Integer.valueOf(yourMoney.getText())));
            yourMoney.setText("0");
        } else {
            pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + currentBid + 50));
            yourMoney.setText(String.valueOf(Integer.valueOf(yourMoney.getText()) - (currentBid + 50)));
        }
        Main.client.sendPacket(new GamePacket("next", GamePacket.Status.MOVE, GamePacket.MOVE_TYPE.RAISE));
        disableButtonCall();
        disableButtonFold();
        disableButtonRaise();
        disableButtonCheck();
        showUserMoveText("RAISE");
        hideAllUsersMoves();
    }

    @FXML
    public void nextPlayer() {
        Main.client.sendPacket(new GamePacket("next", GamePacket.Status.MOVE));
        disableButtonCall();
        disableButtonFold();
        disableButtonRaise();
        disableButtonCheck();
    }

    public void setYourData(String username, Integer money) {
        this.yourUsername.setText(username);
        this.yourMoney.setText(String.valueOf(money));
    }

    public void setYourCard1(Karta yourCard1) {
        String rank = Rank.rank.get(yourCard1.rank.toString());
        String color = yourCard1.kolor.toString();
        this.yourCard1.setImage(new Image(getClass().getResourceAsStream("/images/cards/" + rank + color + ".png")));
    }

    public void setYourCard2(Karta yourCard2) {
        String rank = Rank.rank.get(yourCard2.rank.toString());
        String color = yourCard2.kolor.toString();
        this.yourCard2.setImage(new Image(getClass().getResourceAsStream("/images/cards/" + rank + color + ".png")));
    }

    public void assignPlayers(List<Player> plyerList) {
        // TODO : popraw to prosze
        Player player1 = plyerList.get(0);
        PlayerFields playerFields = new PlayerFields(player1Money, player1Username, player1Card1, player1Card2, player1Move);
        playerFields.setPlayer(player1, true);
        playersFields.put(player1, playerFields);
        if (plyerList.size() == 1) return;
        Player player2 = plyerList.get(1);
        playerFields = new PlayerFields(player2Money, player2Username, player2Card1, player2Card2, player2Move);
        playerFields.setPlayer(player2, true);
        playersFields.put(player2, playerFields);
        if (plyerList.size() == 2) return;
        Player player3 = plyerList.get(2);
        playerFields = new PlayerFields(player3Money, player3Username, player3Card1, player3Card2, player3Move);
        playerFields.setPlayer(player3, true);
        playersFields.put(player3, playerFields);
        if (plyerList.size() == 3) return;
        Player player4 = plyerList.get(3);
        playerFields = new PlayerFields(player4Money, player4Username, player4Card1, player4Card2, player4Move);
        playerFields.setPlayer(player4, true);
        playersFields.put(player4, playerFields);
    }


    public void smallBlind() {
        pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + 5));
        yourMoney.setText(String.valueOf(Integer.valueOf(yourMoney.getText()) - 5));
        currentBid = 5;
        hideAllUsersMoves();
        showUserMoveText("SMALL BLIND");
    }

    public void otherSmallBlind(Player player) {
        pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + 5));
        playersFields.get(player).setMoney(String.valueOf(Integer.valueOf(playersFields.get(player).getMoney()) - 5));
        hideUserText();
        hideAllUsersMoves();
        playersFields.get(player).showUserMoveText("SMALL BLIND");
    }

    public void otherBigBlind(Player player) {
        pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + 10));
        playersFields.get(player).setMoney(String.valueOf(Integer.valueOf(playersFields.get(player).getMoney()) - 10));
        hideUserText();
        hideAllUsersMoves();
        playersFields.get(player).showUserMoveText("BIG BLIND");
    }

    public void bigBlind() {
        pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + 10));
        yourMoney.setText(String.valueOf(Integer.valueOf(yourMoney.getText()) - 10));
        currentBid = 10;
        hideAllUsersMoves();
        showUserMoveText("BIG BLIND");
    }

    public void setTableCard(Integer cardIndex, Karta card) {
        String rank = Rank.rank.get(card.rank.toString());
        String color = card.kolor.toString();
        switch (cardIndex) {
            case 1: {
                TableCard1.setImage(new Image(getClass().getResourceAsStream("/images/cards/" + rank + color + ".png")));
                break;
            }
            case 2: {
                TableCard2.setImage(new Image(getClass().getResourceAsStream("/images/cards/" + rank + color + ".png")));
                break;
            }
            case 3: {
                TableCard3.setImage(new Image(getClass().getResourceAsStream("/images/cards/" + rank + color + ".png")));
                break;
            }
            case 4: {
                TableCard4.setImage(new Image(getClass().getResourceAsStream("/images/cards/" + rank + color + ".png")));
                break;
            }
            case 5: {
                TableCard5.setImage(new Image(getClass().getResourceAsStream("/images/cards/" + rank + color + ".png")));
                break;
            }
        }

    }

    public void makeMove(GamePacket.MOVE_TYPE moveType, Integer currentBid) {
        this.currentBid = currentBid;
        if (moveType.equals(GamePacket.MOVE_TYPE.CHECK)) {
            enableButtonFold();
            enableButtonRaise();
            disableButtonCall();
            enableButtonCheck();
            hideButtonCall();
            showButtonCheck();
        } else {
            enableButtonFold();
            enableButtonRaise();
            disableButtonCheck();
            enableButtonCall();
            hideButtonCheck();
            showButtonCall();
        }
    }

    public void otherMakeMove(Player player, GamePacket.MOVE_TYPE moveType, Integer money) {
        pool.setText(String.valueOf(Integer.valueOf(pool.getText()) + (Integer.valueOf(playersFields.get(player).getMoney()) - money)));
        playersFields.get(player).setMoney(String.valueOf(money));
        hideUserText();
        hideAllUsersMoves();
        switch (moveType) {
            case CALL -> playersFields.get(player).showUserMoveText("CALL");
            case FOLD -> userFolded(player);
            case CHECK -> playersFields.get(player).showUserMoveText("CHECK");
            case RAISE -> playersFields.get(player).showUserMoveText("RAISE");
        }
    }

    private void userFolded(Player player) {
        playersFields.get(player).showUserMoveText("FOLD");
        playersFields.get(player).folded();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        hideUserText();
        disableButtonFold();
        disableButtonCheck();
        disableButtonRaise();
        disableButtonCall();
        hideButtonCheck();
        hideWinner();
        this.pool.setText("0");
        this.playersFields = new HashMap<>();
        this.player1Move.setVisible(false);
        this.player2Move.setVisible(false);
        this.player3Move.setVisible(false);
        this.player4Move.setVisible(false);
    }

    private void disableButtonCall() {
        this.CallButton.setDisable(true);
    }

    private void disableButtonCheck() {
        this.CheckButton.setDisable(true);
    }

    private void disableButtonRaise() {
        this.RaiseButton.setDisable(true);
    }

    private void disableButtonFold() {
        this.FoldButton.setDisable(true);
    }

    private void enableButtonCall() {
        this.CallButton.setDisable(false);
    }

    private void enableButtonCheck() {
        this.CheckButton.setDisable(false);
    }

    private void enableButtonRaise() {
        this.RaiseButton.setDisable(false);
    }

    private void enableButtonFold() {
        this.FoldButton.setDisable(false);
    }

    private void hideButtonCheck() {
        this.CheckButton.setVisible(false);
    }

    private void showButtonCheck() {
        this.CheckButton.setVisible(true);
    }

    private void hideButtonCall() {
        this.CallButton.setVisible(false);
    }

    private void showButtonCall() {
        this.CallButton.setVisible(true);
    }

    private void showUserMoveText(String text) {
        this.yourMove.setText(text);
        this.yourMove.setVisible(true);
    }

    private void hideUserText() {
        this.yourMove.setVisible(false);
    }

    private void hideAllUsersMoves() {
        for (Map.Entry<Player, PlayerFields> set : playersFields.entrySet()) {
            set.getValue().hideUserText();
        }
    }

    public void hideWinner() {
        this.blackRectangle.setVisible(false);
        this.winnerImage.setVisible(false);
        this.winnerLabel.setVisible(false);
    }

    public void showWinner(String username, Player player, Karta card1, Karta card2) {
        this.blackRectangle.setVisible(true);
        this.winnerLabel.setText(username + " wins");
        this.winnerImage.setVisible(true);
        this.winnerLabel.setVisible(true);
        try {
            playersFields.get(player).setFirstCard(card1);
            playersFields.get(player).setSecondCard(card2);
            playersFields.get(player).turnOver();
        } catch (NullPointerException e) {
            System.out.println("problem przy identyfikacji kart uzytkownika");
        }
    }

}
