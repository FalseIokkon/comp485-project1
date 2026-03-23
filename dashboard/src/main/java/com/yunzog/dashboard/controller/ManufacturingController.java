package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.ManufacturingDao;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ManufacturingController {

    @FXML
    private Label totalProductsLabel;
    @FXML
    private Label batchesScheduledLabel;
    @FXML
    private Label unitsProducedLabel;
    @FXML
    private Label defectCountLabel;

    @FXML
    private BarChart<String, Number> productionChart;

    @FXML
    private TableView<ManufacturingDao.ProductionBatchRow> batchTable;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> productCol;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> scheduledDateCol;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> completedDateCol;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> unitsPlannedCol;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> unitsProducedCol;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> defectCountCol;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> machineUptimeCol;
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> statusCol;

    private final ManufacturingDao dao = new ManufacturingDao();

    @FXML
    private void initialize() {
        setupBatchTable();
        loadData();
    }

    public void loadData() {
        loadCards();
        loadProductionChart();
        loadBatchTable();
    }

    private void loadCards() {
        totalProductsLabel.setText(String.valueOf(dao.getTotalProducts()));
        batchesScheduledLabel.setText(String.valueOf(dao.getBatchesScheduled()));
        unitsProducedLabel.setText(String.valueOf(dao.getUnitsProduced()));
        defectCountLabel.setText(String.valueOf(dao.getDefectCount()));
    }

    private void loadProductionChart() {
        productionChart.getData().clear();

        XYChart.Series<String, Number> planned = new XYChart.Series<>();
        planned.setName("Units Planned");

        XYChart.Series<String, Number> produced = new XYChart.Series<>();
        produced.setName("Units Produced");

        dao.getProductionBatches().forEach(batch -> {
            String month = batch.scheduledDate().substring(5, 7); // "MM"
            planned.getData().add(new XYChart.Data<>(month, batch.unitsPlanned()));
            produced.getData().add(new XYChart.Data<>(month, batch.unitsProduced()));
        });

        productionChart.getData().addAll(planned, produced);
    }

    private void loadBatchTable() {
        batchTable.setItems(FXCollections.observableArrayList(dao.getProductionBatches()));
    }

    private void setupBatchTable() {
        batchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        productCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().productName()));
        scheduledDateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().scheduledDate()));
        completedDateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().completedDate()));
        unitsPlannedCol
                .setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().unitsPlanned())));
        unitsProducedCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().unitsProduced())));
        defectCountCol
                .setCellValueFactory(cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().defectCount())));
        machineUptimeCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().machineUptime())));
        statusCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status()));
    }
}
