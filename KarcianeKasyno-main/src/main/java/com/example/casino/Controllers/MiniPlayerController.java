package com.example.casino.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class MiniPlayerController {
    @FXML
    private ImageView status;

    @FXML
    private Label statusLabel;

    @FXML
    private Label username;


    public void setData(String username, boolean ready) {
        this.username.setText(username);
        if (ready) {
            status.setImage(new Image(getClass().getResourceAsStream("/images/ready_status.png")));
            statusLabel.setText("Ready");
            statusLabel.setTextFill(Color.DARKGREEN);
        } else {
            status.setImage(new Image(getClass().getResourceAsStream("/images/not_ready_status.png")));
            statusLabel.setText("Not Ready");
            statusLabel.setTextFill(Color.RED);
        }
    }

}
