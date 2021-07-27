package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class Server extends Application {

    public ServerController uiController;


    @Override
    public void start(Stage primaryStage) throws Exception{


        FXMLLoader fxmlLoader = new FXMLLoader(Server.class.getResource("ServerPainterUI.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Server");
        primaryStage.setScene(new Scene(root, 700, 700));
        primaryStage.setOnHidden(e -> {
            try {
                uiController.shutdown();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        primaryStage.show();

        uiController = fxmlLoader.getController();

    }




    public static void main(String[] args) {
        launch(args);
    }

}