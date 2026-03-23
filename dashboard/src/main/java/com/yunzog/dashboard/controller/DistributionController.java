package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.DistributionDao;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DistributionController {

    @FXML
    private Label ordersLabel;
    @FXML
    private Label shipmentsLabel;
    @FXML
    private Label deliveredLabel;
    @FXML
    private Label onTimeLabel;

    @FXML
    private BarChart<String, Number> shipmentChart;

    @FXML
    private TableView<DistributionDao.ShipmentRow> shipmentTable;
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> customerCol;
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> cityCol;
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> stateCol;
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> statusCol;
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> shipDateCol;
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> deliveryDateCol;

    private final DistributionDao dao = new DistributionDao();

    @FXML
    private void initialize() {
        setupTable();
        loadData();
    }

    public void loadData() {
        ordersLabel.setText(String.valueOf(dao.getTotalOrders()));
        shipmentsLabel.setText(String.valueOf(dao.getTotalShipments()));
        deliveredLabel.setText(String.valueOf(dao.getDelivered()));
        onTimeLabel.setText(String.format("%.1f%%", dao.getOnTimeRate()));

        shipmentTable.setItems(FXCollections.observableArrayList(dao.getShipments()));

        shipmentChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Shipments");

        dao.getShipments().forEach(s -> {
            String month = s.shipDate().substring(5, 7);
            series.getData().add(new XYChart.Data<>(month, 1));
        });

        shipmentChart.getData().add(series);
    }

    private void setupTable() {
        shipmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        customerCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().customer()));
        cityCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().city()));
        stateCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().state()));
        statusCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().status()));
        shipDateCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().shipDate()));
        deliveryDateCol.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().deliveryDate()));
    }
}
