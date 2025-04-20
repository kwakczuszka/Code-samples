package com.example.casino.Controllers;

import com.example.casino.Main;
import com.example.casino.Packets.Packet;
import com.example.casino.Packets.RegisterPacket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class RegisterController implements Initializable {

    @FXML
    private Label Err;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private Button exit;

    @FXML
    private Button minimize;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private void register() throws NoSuchAlgorithmException, InvalidKeySpecException,
            ExecutionException, InterruptedException {
        String email = emailField.getText();
        String login = usernameField.getText();
        LocalDate dateTemp = birthDatePicker.getValue();


        String date = String.valueOf(dateTemp);

        String password = passwordField.getText();
        if (login.isEmpty() || password.isEmpty() || email.isEmpty() || date.isEmpty()) {
            enableErr("NIE PODANO WSZYSTKICH DANYCH");
        } else if (!isOver18(dateTemp)) {
            enableErr("Musisz mieć co najmniej 18 lat!");
        } else {
            Packet p = new RegisterPacket("REGISTER TRY", email, login, password, date, RegisterPacket.Status.REGISTER);
            Main.client.sendPacket(p);
        }
    }

    private boolean isOver18(LocalDate birthDate) {
        LocalDate today = LocalDate.now();
        Period age = Period.between(birthDate, today);
        return age.getYears() >= 18;
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/casino/login.fxml"));
            Parent root = loader.load();
            Main.stage.setScene(new Scene(root));
            Main.stage.sizeToScene();
            Main.stage.show();
            LoginController lc = loader.getController();
            Main.client.setLoginController(lc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void onEnter(ActionEvent event) throws NoSuchAlgorithmException,
            InvalidKeySpecException, ExecutionException, InterruptedException {
        register();
    }

    public void enableErr(String errMessage) {
        Err.setText(errMessage);
        Err.setVisible(true);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Err.setVisible(false);
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
