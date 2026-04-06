package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.ManufacturingDao;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Controller for the manufacturing dashboard view.
 *
 * <p>This controller manages the manufacturing summary display, production
 * chart, and production batch table shown in the JavaFX dashboard. It
 * retrieves manufacturing-related data through the {@link ManufacturingDao}
 * and updates the interface components with current production metrics and
 * batch information.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class ManufacturingController {

    /** Displays the total number of products. */
    @FXML
    private Label totalProductsLabel;

    /** Displays the total number of batches scheduled. */
    @FXML
    private Label batchesScheduledLabel;

    /** Displays the total number of units produced. */
    @FXML
    private Label unitsProducedLabel;

    /** Displays the total defect count. */
    @FXML
    private Label defectCountLabel;

    /** Bar chart used to display planned and produced units by month. */
    @FXML
    private BarChart<String, Number> productionChart;

    /** Table used to display production batch records. */
    @FXML
    private TableView<ManufacturingDao.ProductionBatchRow> batchTable;

    /** Column displaying the product name. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> productCol;

    /** Column displaying the scheduled production date. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> scheduledDateCol;

    /** Column displaying the completed production date. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> completedDateCol;

    /** Column displaying the number of units planned. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> unitsPlannedCol;

    /** Column displaying the number of units produced. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> unitsProducedCol;

    /** Column displaying the defect count for a batch. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> defectCountCol;

    /** Column displaying the machine uptime value. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> machineUptimeCol;

    /** Column displaying the batch status. */
    @FXML
    private TableColumn<ManufacturingDao.ProductionBatchRow, String> statusCol;

    /** Data access object used to retrieve manufacturing data. */
    private final ManufacturingDao dao = new ManufacturingDao();

    /**
     * Initializes the controller after the FXML elements have been loaded.
     *
     * <p>This method configures the production batch table, prepares the
     * production chart, and loads the initial manufacturing data into the
     * dashboard.</p>
     */
    @FXML
    private void initialize() {
        setupBatchTable();
        setupChart();
        loadData();
    }

    /**
     * Loads all manufacturing dashboard data into the user interface.
     *
     * <p>This method refreshes the summary cards, updates the production
     * chart, and populates the production batch table.</p>
     */
    public void loadData() {
        loadCards();
        loadProductionChart();
        loadBatchTable();
    }

    /**
     * Loads and formats the manufacturing summary card values.
     *
     * <p>This method retrieves product totals, scheduled batch counts,
     * production totals, and defect counts from the data source and displays
     * them in the dashboard summary labels.</p>
     */
    private void loadCards() {
        totalProductsLabel.setText(String.valueOf(dao.getTotalProducts()));
        batchesScheduledLabel.setText(String.valueOf(dao.getBatchesScheduled()));
        unitsProducedLabel.setText(String.valueOf(dao.getUnitsProduced()));
        defectCountLabel.setText(String.valueOf(dao.getDefectCount()));
    }

    /**
     * Loads production data into the production chart.
     *
     * <p>This method clears existing chart data, creates separate series for
     * planned and produced units, and adds monthly production values retrieved
     * from the data source.</p>
     */
    private void loadProductionChart() {
        productionChart.getData().clear();

        XYChart.Series<String, Number> planned = new XYChart.Series<>();
        planned.setName("Units Planned");

        XYChart.Series<String, Number> produced = new XYChart.Series<>();
        produced.setName("Units Produced");

        dao.getProductionBatches().forEach(batch -> {
            String month = batch.scheduledDate().substring(5, 7);
            planned.getData().add(new XYChart.Data<>(month, batch.unitsPlanned()));
            produced.getData().add(new XYChart.Data<>(month, batch.unitsProduced()));
        });

        productionChart.getData().addAll(planned, produced);
    }

    /**
     * Loads production batch records into the batch table.
     *
     * <p>This method retrieves production batch data from the data source and
     * populates the table with the current batch records.</p>
     */
    private void loadBatchTable() {
        batchTable.setItems(FXCollections.observableArrayList(dao.getProductionBatches()));
    }

    /**
     * Configures the appearance and axis scaling of the production chart.
     *
     * <p>This method disables animation, adjusts spacing, configures legend
     * visibility, and sets the chart axes for readability. The vertical axis
     * is scaled dynamically based on the highest planned or produced unit
     * value and uses an appropriate tick interval for the displayed range.</p>
     */
    private void setupChart() {
        productionChart.setAnimated(false);
        productionChart.setLegendVisible(true);
        productionChart.setHorizontalGridLinesVisible(true);
        productionChart.setVerticalGridLinesVisible(false);
        productionChart.setAlternativeColumnFillVisible(false);
        productionChart.setAlternativeRowFillVisible(false);

        productionChart.setCategoryGap(24);
        productionChart.setBarGap(6);

        CategoryAxis xAxis = (CategoryAxis) productionChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) productionChart.getYAxis();

        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);

        int max = dao.getProductionBatches()
                .stream()
                .mapToInt(b -> Math.max(b.unitsPlanned(), b.unitsProduced()))
                .max()
                .orElse(0);

        int upper = (int) Math.ceil(max * 1.2);

        int tickUnit;
        if (upper <= 10) {
            tickUnit = 1;
        } else if (upper <= 50) {
            tickUnit = 5;
        } else if (upper <= 100) {
            tickUnit = 10;
        } else if (upper <= 500) {
            tickUnit = 50;
        } else if (upper <= 1000) {
            tickUnit = 100;
        } else if (upper <= 5000) {
            tickUnit = 500;
        } else if (upper <= 10000) {
            tickUnit = 1000;
        } else {
            tickUnit = 5000;
        }

        upper = ((upper + tickUnit - 1) / tickUnit) * tickUnit;

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(upper);
        yAxis.setTickUnit(tickUnit);
        yAxis.setTickLabelGap(10);
    }

    /**
     * Configures the production batch table and binds each column to its
     * corresponding value in a {@link ManufacturingDao.ProductionBatchRow}.
     *
     * <p>This method also applies a constrained resize policy so that the
     * available table width is distributed across all columns.</p>
     */
    private void setupBatchTable() {
        batchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        productCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().productName()));
        scheduledDateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().scheduledDate()));
        completedDateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().completedDate()));
        unitsPlannedCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().unitsPlanned())));
        unitsProducedCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().unitsProduced())));
        defectCountCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().defectCount())));
        machineUptimeCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.valueOf(cell.getValue().machineUptime())));
        statusCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status()));
    }
}