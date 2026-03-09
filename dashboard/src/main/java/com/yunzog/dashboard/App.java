package com.yunzog.dashboard;

import com.yunzog.dashboard.db.DB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Create DB + tables + seed sample data (first run)
        DB.init();

        FXMLLoader loader = new FXMLLoader(App.class.getResource("main.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);

        stage.setTitle("Yunzog CEO Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}