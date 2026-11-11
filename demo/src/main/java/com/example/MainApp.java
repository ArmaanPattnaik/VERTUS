package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("/simulator.fxml"));

        Scene scene = new Scene(loader.load(), 1200, 760);

        stage.setTitle("CPU Scheduling Simulator");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}