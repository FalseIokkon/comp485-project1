package com.yunzog.dashboard;

import com.yunzog.dashboard.dao.OverviewDao;
import com.yunzog.dashboard.model.DivisionKPI;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.animation.PauseTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class MainController {

    // ===== Overview tab table =====
    @FXML private TableView<DivisionKPI> table;
    @FXML private TableColumn<DivisionKPI, String> divisionCol;
    @FXML private TableColumn<DivisionKPI, String> metricCol;
    @FXML private TableColumn<DivisionKPI, Double> valueCol;
    @FXML private TableColumn<DivisionKPI, String> asOfCol;

    // ===== Overview KPI cards =====
    @FXML private Label revenueLabel;
    @FXML private Label positionsLabel;
    @FXML private Label deliveryLabel;
    @FXML private Label leadsLabel;
    @FXML private Label unitsLabel;
    @FXML private Label customersLabel;
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private ProgressIndicator refreshSpinner;


    // ===== DAO layer =====
    private final OverviewDao overviewDao = new OverviewDao();

    // Later:
    // private final HRDao hrDao = new HRDao();
    // private final FinanceDao financeDao = new FinanceDao();
    // private final MarketingDao marketingDao = new MarketingDao();
    // private final ManufacturingDao manufacturingDao = new ManufacturingDao();
    // private final LogisticsDao logisticsDao = new LogisticsDao();

    @FXML
    private void initialize() {
        setupOverviewTable();
        loadOverview();
    }

    @FXML
    private void onRefresh() {
        refreshButton.setDisable(true);
        refreshButton.setText("Refreshing...");

        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(event -> {
            loadOverview();

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

    private void setupOverviewTable() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        divisionCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().division()));

        metricCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().metric()));

        valueCol.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().value()));

        asOfCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().asOf()));

        valueCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);

                if (empty || value == null) {
                    setText(null);
                    return;
                }

                if (value % 1 == 0) {
                    setText(String.format("%.0f", value));
                } else {
                    setText(String.format("%.1f", value));
                }
            }
        });
    }

    private void loadOverview() {
        loadOverviewCards();
        loadOverviewTable();
    }

    private void loadOverviewCards() {
        revenueLabel.setText(String.format("$%,.2f", overviewDao.getMonthlyRevenue()));
        positionsLabel.setText(String.valueOf(overviewDao.getOpenPositions()));
        deliveryLabel.setText(String.format("%.1f%%", overviewDao.getOnTimeDeliveryRate()));
        leadsLabel.setText(String.valueOf(overviewDao.getLeadsThisMonth()));
        unitsLabel.setText(String.valueOf(overviewDao.getUnitsProducedThisMonth()));
        customersLabel.setText(String.valueOf(overviewDao.getActiveCustomers()));
    }

    private void loadOverviewTable() {
        table.setItems(FXCollections.observableArrayList(overviewDao.getDivisionKpis()));
    }

    // ===== Future tab loaders =====

    // private void loadHrTab() {
    //     // Use hrDao here
    // }

    // private void loadFinanceTab() {
    //     // Use financeDao here
    // }

    // private void loadMarketingTab() {
    //     // Use marketingDao here
    // }

    // private void loadManufacturingTab() {
    //     // Use manufacturingDao here
    // }

    // private void loadLogisticsTab() {
    //     // Use logisticsDao here
    // }
}