package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Application {
    public ClientController uiController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ClientPainterUI.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Client");
        primaryStage.setScene(new Scene(root, 700, 700));


        uiController = fxmlLoader.getController();
        primaryStage.setOnHidden(e -> {
            try {
                uiController.shutdown();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        primaryStage.show();
        //uiController.drawArea

        //GraphicsContext gc = uiController.canvas.getGraphicsContext2D();
        //gc.setFill(Color.BLUE);
        //gc.fillRect(0, 0, uiController.canvas.getWidth(), uiController.canvas.getHeight());


    }

    public static void main(String[] args) {
        launch(args);
    }
}
