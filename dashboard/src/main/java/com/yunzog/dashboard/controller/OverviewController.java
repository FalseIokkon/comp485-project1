package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.OverviewDao;
import com.yunzog.dashboard.model.DivisionKPI;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

/**
 * Controller for the overview dashboard view.
 *
 * <p>This controller manages the executive summary display, division KPI table,
 * and performance trend chart shown in the JavaFX dashboard. It retrieves
 * organization-wide metrics through the {@link OverviewDao} and updates the
 * interface components with current revenue, staffing, delivery, lead,
 * production, and customer information.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class OverviewController {

    /** Displays the monthly revenue amount. */
    @FXML
    private Label revenueLabel;

    /** Displays the number of open positions. */
    @FXML
    private Label positionsLabel;

    /** Displays the on-time delivery rate. */
    @FXML
    private Label deliveryLabel;

    /** Displays the number of leads generated this month. */
    @FXML
    private Label leadsLabel;

    /** Displays the number of units produced this month. */
    @FXML
    private Label unitsLabel;

    /** Displays the number of active customers. */
    @FXML
    private Label customersLabel;

    /** Table used to display division KPI records. */
    @FXML
    private TableView<DivisionKPI> table;

    /** Column displaying the division name. */
    @FXML
    private TableColumn<DivisionKPI, String> divisionCol;

    /** Column displaying the KPI metric name. */
    @FXML
    private TableColumn<DivisionKPI, String> metricCol;

    /** Column displaying the KPI metric value. */
    @FXML
    private TableColumn<DivisionKPI, Double> valueCol;

    /** Column displaying the date associated with the KPI value. */
    @FXML
    private TableColumn<DivisionKPI, String> asOfCol;

    /** Line chart used to display revenue and expense trends by month. */
    @FXML
    private LineChart<String, Number> performanceChart;

    /** Data access object used to retrieve overview dashboard data. */
    private final OverviewDao overviewDao = new OverviewDao();

    /**
     * Initializes the controller after the FXML elements have been loaded.
     *
     * <p>This method configures the KPI table, prepares the performance chart,
     * and loads the initial overview data into the dashboard.</p>
     */
    @FXML
    private void initialize() {
        setupTable();
        setupChart();
        loadData();
    }

    /**
     * Loads all overview dashboard data into the user interface.
     *
     * <p>This method refreshes the KPI summary cards, populates the division
     * KPI table, and updates the revenue versus expenses trend chart.</p>
     */
    public void loadData() {
        loadCards();
        loadTable();
        loadChart();
    }

    /**
     * Loads and formats the overview summary card values.
     *
     * <p>This method retrieves organization-wide KPI values from the data
     * source and displays them in the dashboard summary labels.</p>
     */
    private void loadCards() {
        revenueLabel.setText(String.format("$%,.2f", overviewDao.getMonthlyRevenue()));
        positionsLabel.setText(String.valueOf(overviewDao.getOpenPositions()));
        deliveryLabel.setText(String.format("%.1f%%", overviewDao.getOnTimeDeliveryRate()));
        leadsLabel.setText(String.valueOf(overviewDao.getLeadsThisMonth()));
        unitsLabel.setText(String.valueOf(overviewDao.getUnitsProducedThisMonth()));
        customersLabel.setText(String.valueOf(overviewDao.getActiveCustomers()));
    }

    /**
     * Loads division KPI records into the table.
     *
     * <p>This method retrieves KPI entries from the data source and populates
     * the table with the current division metrics.</p>
     */
    private void loadTable() {
        table.setItems(FXCollections.observableArrayList(overviewDao.getDivisionKpis()));
    }

    /**
     * Loads revenue and expense trend data into the performance chart.
     *
     * <p>This method clears any existing chart data, creates separate series
     * for revenue and expenses, adds monthly trend points, and installs
     * interactive tooltips for each data point.</p>
     */
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

        installTooltips(revenueSeries, "Revenue");
        installTooltips(expensesSeries, "Expenses");
    }

    /**
     * Configures the appearance and axis settings of the performance chart.
     *
     * <p>This method disables animation, adjusts grid line settings, and
     * configures the horizontal and vertical axes for readability. The
     * vertical axis is scaled dynamically based on the highest revenue or
     * expense value and uses fixed increments for cleaner presentation.</p>
     */
    private void setupChart() {
        performanceChart.setAnimated(false);
        performanceChart.setLegendVisible(true);
        performanceChart.setHorizontalGridLinesVisible(true);
        performanceChart.setVerticalGridLinesVisible(false);
        performanceChart.setAlternativeColumnFillVisible(false);
        performanceChart.setAlternativeRowFillVisible(false);

        CategoryAxis xAxis = (CategoryAxis) performanceChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) performanceChart.getYAxis();

        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);

        double max = overviewDao.getRevenueVsExpensesTrend()
                .stream()
                .mapToDouble(p -> Math.max(p.revenue(), p.expenses()))
                .max()
                .orElse(0);

        double upperBound = Math.ceil(max / 10000.0) * 10000;

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(10000);
        yAxis.setTickLabelGap(10);
    }

    /**
     * Installs tooltips and hover effects for each data point in a chart series.
     *
     * <p>Each tooltip displays the provided label, the month associated with
     * the data point, and the formatted monetary value. A scaling effect is
     * also applied when the user hovers over a chart point.</p>
     *
     * @param series the chart series whose data points will receive tooltips
     * @param label the label displayed in each tooltip
     */
    private void installTooltips(XYChart.Series<String, Number> series, String label) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            data.nodeProperty().addListener((obs, oldNode, node) -> {
                if (node != null) {
                    Tooltip tooltip = new Tooltip(
                            label + "\n" +
                            "Month: " + data.getXValue() + "\n" +
                            "Amount: $" + String.format("%,.2f", data.getYValue().doubleValue())
                    );

                    Tooltip.install(node, tooltip);

                    node.setOnMouseEntered(e -> {
                        node.setStyle("-fx-scale-x: 1.3; -fx-scale-y: 1.3;");
                    });

                    node.setOnMouseExited(e -> {
                        node.setStyle("");
                    });
                }
            });
        }
    }

    /**
     * Configures the division KPI table and binds each column to its
     * corresponding value in a {@link DivisionKPI}.
     *
     * <p>This method also applies a constrained resize policy so that the
     * available table width is distributed across all columns. The value
     * column is formatted to display whole numbers without decimal places
     * and fractional values with one decimal place.</p>
     */
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
            /**
             * Updates the displayed text for a value cell.
             *
             * <p>Whole-number values are displayed without decimal places,
             * while fractional values are displayed with one decimal place.</p>
             *
             * @param value the value to display in the table cell
             * @param empty indicates whether this cell should be shown as empty
             */
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