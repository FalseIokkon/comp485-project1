package com.yunzog.dashboard;

import com.yunzog.dashboard.db.DB;
import com.yunzog.dashboard.db.DatabaseInitializer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

/**
 * Entry point for the Yunzog Dashboard application.
 *
 * <p>This class initializes the database and launches the JavaFX UI,
 * loading the main dashboard view and applying application styling.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Create DB + tables + seed sample data 
        DatabaseInitializer.initialize();
        
        
        FXMLLoader loader = new FXMLLoader(App.class.getResource("main.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 720);

        // Apply JMetro
        JMetro jMetro = new JMetro(Style.DARK); 
        jMetro.setScene(scene);

        stage.setTitle("Yunzog CEO Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}