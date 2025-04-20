package com.example.casino;

import com.example.casino.Controllers.LoginController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Main extends Application {
    public static Stage stage;
    public static Player player;
    public static Client client;
    private double xOffset = 0;
    private double yOffset = 0;

    public void start(Stage primaryStage) throws Exception {

        client = new Client();
        this.stage = primaryStage;
        primaryStage.initStyle(StageStyle.UNDECORATED);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });

        LoginController lc = loader.getController();
        client.setLoginController(lc);

        stage.setTitle("Login");
        stage.setScene(new Scene(root));
        stage.show();
        client.start();

    }

    public static void main(String[] args) {
        launch();
    }

}
