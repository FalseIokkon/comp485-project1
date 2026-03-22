package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.OverviewDao;
import com.yunzog.dashboard.model.DivisionKPI;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class OverviewController {

    // ===== KPI cards =====
    @FXML private Label revenueLabel;
    @FXML private Label positionsLabel;
    @FXML private Label deliveryLabel;
    @FXML private Label leadsLabel;
    @FXML private Label unitsLabel;
    @FXML private Label customersLabel;

    // ===== Table =====
    @FXML private TableView<DivisionKPI> table;
    @FXML private TableColumn<DivisionKPI, String> divisionCol;
    @FXML private TableColumn<DivisionKPI, String> metricCol;
    @FXML private TableColumn<DivisionKPI, Double> valueCol;
    @FXML private TableColumn<DivisionKPI, String> asOfCol;

    // ===== Chart =====
    @FXML private LineChart<String, Number> performanceChart;

    private final OverviewDao overviewDao = new OverviewDao();

    @FXML
    private void initialize() {
        setupTable();
        loadData();
    }

    public void loadData() {
        loadCards();
        loadTable();
        loadChart();
    }

    private void loadCards() {
        revenueLabel.setText(String.format("$%,.2f", overviewDao.getMonthlyRevenue()));
        positionsLabel.setText(String.valueOf(overviewDao.getOpenPositions()));
        deliveryLabel.setText(String.format("%.1f%%", overviewDao.getOnTimeDeliveryRate()));
        leadsLabel.setText(String.valueOf(overviewDao.getLeadsThisMonth()));
        unitsLabel.setText(String.valueOf(overviewDao.getUnitsProducedThisMonth()));
        customersLabel.setText(String.valueOf(overviewDao.getActiveCustomers()));
    }

    private void loadTable() {
        table.setItems(FXCollections.observableArrayList(overviewDao.getDivisionKpis()));
    }

    private void loadChart() {
        performanceChart.getData().clear();

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");

        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenses");

        for (OverviewDao.MonthlyTrendPoint point : overviewDao.getRevenueVsExpensesTrend()) {
            revenueSeries.getData().add(new XYChart.Data<>(point.month(), point.revenue()));
            expensesSeries.getData().add(new XYChart.Data<>(point.month(), point.expenses()));
        }

        performanceChart.getData().addAll(revenueSeries, expensesSeries);
    }

    private void setupTable() {
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
}