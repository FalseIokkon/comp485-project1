package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.DistributionDao;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the distribution dashboard view.
 *
 * <p>This controller is responsible for initializing and updating the
 * distribution-related user interface components, including summary labels,
 * the shipment table, and the shipment activity chart. It retrieves shipment
 * and delivery data from the {@link DistributionDao} and presents that data
 * in a JavaFX dashboard layout.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class DistributionController {

    /** Displays the total number of orders. */
    @FXML
    private Label ordersLabel;

    /** Displays the total number of shipments. */
    @FXML
    private Label shipmentsLabel;

    /** Displays the total number of delivered shipments. */
    @FXML
    private Label deliveredLabel;

    /** Displays the percentage of shipments delivered on time. */
    @FXML
    private Label onTimeLabel;

    /** Bar chart used to display shipment counts by month. */
    @FXML
    private BarChart<String, Number> shipmentChart;

    /** Table used to display shipment records. */
    @FXML
    private TableView<DistributionDao.ShipmentRow> shipmentTable;

    /** Column displaying the customer name. */
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> customerCol;

    /** Column displaying the shipment destination city. */
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> cityCol;

    /** Column displaying the shipment destination state. */
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> stateCol;

    /** Column displaying the shipment status. */
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> statusCol;

    /** Column displaying the shipment date. */
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> shipDateCol;

    /** Column displaying the delivery date. */
    @FXML
    private TableColumn<DistributionDao.ShipmentRow, String> deliveryDateCol;

    /** Data access object used to retrieve distribution metrics and shipment records. */
    private final DistributionDao dao = new DistributionDao();

    /**
     * Initializes the controller after its FXML fields have been loaded.
     *
     * <p>This method configures the shipment table, prepares the chart, and
     * loads the initial dashboard data.</p>
     */
    @FXML
    private void initialize() {
        setupTable();
        setupChart();
        loadData();
    }

    /**
     * Loads distribution data from the data source and updates the dashboard.
     *
     * <p>This method refreshes the summary labels, populates the shipment table,
     * and rebuilds the shipment chart using monthly shipment totals.</p>
     */
    public void loadData() {
        ordersLabel.setText(String.valueOf(dao.getTotalOrders()));
        shipmentsLabel.setText(String.valueOf(dao.getTotalShipments()));
        deliveredLabel.setText(String.valueOf(dao.getDelivered()));
        onTimeLabel.setText(String.format("%.1f%%", dao.getOnTimeRate()));

        shipmentTable.setItems(FXCollections.observableArrayList(dao.getShipments()));

        shipmentChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Shipments");

        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();

        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            counts.put(month, 0);
        }

        dao.getShipments().forEach(s -> {
            String month = s.shipDate().substring(5, 7);
            counts.put(month, counts.getOrDefault(month, 0) + 1);
        });

        counts.forEach((month, count) ->
            series.getData().add(new XYChart.Data<>(month, count))
        );

        shipmentChart.getData().add(series);
    }

    /**
     * Configures the appearance and scale of the shipment chart.
     *
     * <p>This method disables animation, adjusts grid line visibility,
     * hides the legend, and configures the vertical axis to display
     * whole-number shipment counts based on the highest monthly total.</p>
     */
    private void setupChart() {
        shipmentChart.setAnimated(false);
        shipmentChart.setLegendVisible(false);
        shipmentChart.setHorizontalGridLinesVisible(true);
        shipmentChart.setVerticalGridLinesVisible(false);

        NumberAxis yAxis = (NumberAxis) shipmentChart.getYAxis();

        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        dao.getShipments().forEach(s -> {
            String month = s.shipDate().substring(5, 7);
            counts.put(month, counts.getOrDefault(month, 0) + 1);
        });

        int maxPerMonth = counts.values().stream().max(Integer::compare).orElse(1);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(maxPerMonth + 1);
        yAxis.setTickUnit(1);
    }

    /**
     * Configures the shipment table and binds each column to its corresponding
     * value in a {@link DistributionDao.ShipmentRow}.
     *
     * <p>This method also applies a constrained resize policy so that all
     * columns share the available table width.</p>
     */
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