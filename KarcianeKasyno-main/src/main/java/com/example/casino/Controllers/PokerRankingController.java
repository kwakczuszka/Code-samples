package com.example.casino.Controllers;

import com.example.casino.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PokerRankingController {
    private HashMap<String, Integer> pokerRankingMap = new HashMap<>();

    @FXML
    ListView top10poker;

    @FXML
    Button backButton;

    @FXML
    private Button exit;
    @FXML
    private Button minimize;

    public void initTop10List() {
        top10poker.getItems().clear();
        ArrayList<Map.Entry<String, Integer>> entryList = new ArrayList<>(pokerRankingMap.entrySet());
        entryList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        for (Map.Entry<String, Integer> entry : entryList) {
            String entryString = entry.getKey() + ", punkty: " + entry.getValue();
            top10poker.getItems().add(entryString);
        }

        top10poker.setCellFactory(lv -> {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(item);
                    if (getIndex() == 0) {
                        setId("first-cell");
                    } else if (getIndex() == 1) {
                        setId("second-cell");
                    } else if (getIndex() == 2) {
                        setId("third-cell");
                    } else if (getIndex() % 2 == 0) {
                        setId("even-cell");
                    } else if (getIndex() % 2 == 1) {
                        setId("odd-cell");
                    } else {
                        setId(null);
                    }

                }
            };
        });
    }

    public void setPokerRankingMap(HashMap<String, Integer> pokerRankingMap) {
        this.pokerRankingMap = pokerRankingMap;
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

    @FXML
    public void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/casino/Menu.fxml"));
            Parent root = loader.load();
            Main.stage.setScene(new Scene(root));
            Main.stage.sizeToScene();
            Main.stage.show();
            MenuController mc = loader.getController();
            Main.client.setMenuController(mc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
