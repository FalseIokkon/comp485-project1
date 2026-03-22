package com.yunzog.dashboard;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class MainController {

    @FXML private Button refreshButton;

    @FXML
    private void initialize() {
        // App shell only for now
    }

    @FXML
    private void onRefresh() {
        refreshButton.setDisable(true);
        refreshButton.setText("Refreshing...");

        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(event -> {
            refreshButton.setText("Updated");

            PauseTransition reset = new PauseTransition(Duration.seconds(1));
            reset.setOnFinished(e -> {
                refreshButton.setText("Refresh");
                refreshButton.setDisable(false);
            });
            reset.play();
        });
        pause.play();
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }
}