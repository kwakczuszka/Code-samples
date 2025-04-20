package com.example.casino.Controllers;

import com.example.casino.Main;
import com.example.casino.Packets.LoginPacket;
import com.example.casino.Packets.Packet;
import com.example.casino.Server.GameServer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class LoginController implements Initializable {

    @FXML
    private Label Err;

    @FXML
    private Button exit;

    @FXML
    private Button minimize;

    @FXML
    private TextField loginField;

    @FXML
    private PasswordField passwordField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Err.setVisible(false);
    }

    public void login() {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty()) {
            enableErr("NIE PODANO WSZYSTKICH DANYCH");
        } else {
            Packet p = new LoginPacket("Próba logowania", login, password, LoginPacket.Status.LOGIN);
            Main.client.sendPacket(p);
        }
    }

    public void goToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/casino/Register.fxml"));
            Parent root = loader.load();
            Main.stage.setScene(new Scene(root));
            Main.stage.sizeToScene();
            Main.stage.show();
            RegisterController rc = loader.getController();
            Main.client.setRegisterController(rc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void enableErr(String errMessage) {
        Err.setText(errMessage);
        Err.setVisible(true);
    }

    @FXML
    public void onEnter() {
        login();
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

