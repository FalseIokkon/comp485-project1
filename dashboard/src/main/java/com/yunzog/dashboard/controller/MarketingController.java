package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.MarketingDao;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;

/**
 * Controller for the marketing dashboard view.
 *
 * <p>This controller manages the marketing summary display, campaign spend
 * chart, and campaign table shown in the JavaFX dashboard. It retrieves
 * marketing data through the {@link MarketingDao} and updates the interface
 * components with current budget, spending, campaign, and lead information.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class MarketingController {

    /** Displays the total allocated marketing budget. */
    @FXML
    private Label totalBudgetLabel;

    /** Displays the total marketing spend. */
    @FXML
    private Label totalSpendLabel;

    /** Displays the number of currently active campaigns. */
    @FXML
    private Label activeCampaignsLabel;

    /** Displays the number of leads generated this month. */
    @FXML
    private Label leadsThisMonthLabel;

    /** Bar chart used to display campaign spending by month. */
    @FXML
    private BarChart<String, Number> spendChart;

    /** Table used to display campaign records. */
    @FXML
    private TableView<MarketingDao.CampaignRow> campaignTable;

    /** Column displaying the campaign name. */
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> nameCol;

    /** Column displaying the campaign channel. */
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> channelCol;

    /** Column displaying the campaign budget. */
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> budgetCol;

    /** Column displaying the campaign spend amount. */
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> spendCol;

    /** Column displaying the campaign status. */
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> statusCol;

    /** Column displaying the campaign start date. */
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> startDateCol;

    /** Column displaying the campaign end date. */
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> endDateCol;

    /** Data access object used to retrieve marketing-related data. */
    private final MarketingDao marketingDao = new MarketingDao();

    /**
     * Initializes the controller after the FXML elements have been loaded.
     *
     * <p>This method configures the campaign table, prepares the spending chart,
     * and loads the initial marketing data into the dashboard.</p>
     */
    @FXML
    private void initialize() {
        setupCampaignTable();
        setupChart();
        loadData();
    }

    /**
     * Loads all marketing dashboard data into the user interface.
     *
     * <p>This method refreshes the summary cards, populates the campaign table,
     * and updates the monthly spending chart.</p>
     */
    public void loadData() {
        loadCards();
        loadCampaignTable();
        loadSpendChart();
    }

    /**
     * Loads and formats the marketing summary card values.
     *
     * <p>This method retrieves budget, spending, active campaign, and lead
     * values from the data source and displays them in the dashboard
     * summary labels.</p>
     */
    private void loadCards() {
        totalBudgetLabel.setText(String.format("$%,.2f", marketingDao.getTotalBudget()));
        totalSpendLabel.setText(String.format("$%,.2f", marketingDao.getTotalSpend()));
        activeCampaignsLabel.setText(String.valueOf(marketingDao.getActiveCampaigns()));
        leadsThisMonthLabel.setText(String.valueOf(marketingDao.getLeadsThisMonth()));
    }

    /**
     * Loads campaign records into the campaign table.
     *
     * <p>This method retrieves campaign row data from the data source and
     * populates the table with the current campaign records.</p>
     */
    private void loadCampaignTable() {
        campaignTable.setItems(
                FXCollections.observableArrayList(marketingDao.getCampaignTable())
        );
    }

    /**
     * Loads monthly campaign spending data into the chart.
     *
     * <p>This method clears any existing chart data, creates a new data series,
     * adds monthly campaign spending totals, and installs interactive tooltips
     * for each chart bar.</p>
     */
    private void loadSpendChart() {
        spendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Spend");

        for (MarketingDao.CampaignByMonthPoint point : marketingDao.getCampaignSpendByMonth()) {
            series.getData().add(new XYChart.Data<>(point.month(), point.spend()));
        }

        spendChart.getData().add(series);

        installTooltips(series, "Spend");
    }

    /**
     * Configures the appearance and axis settings of the spending chart.
     *
     * <p>This method disables animation, adjusts grid line and spacing
     * settings, and prepares the chart axes for improved readability.
     * The vertical axis is initially configured using monthly spend values
     * and then returned to automatic ranging.</p>
     */
    private void setupChart() {
        spendChart.setAnimated(false);
        spendChart.setLegendVisible(false);
        spendChart.setHorizontalGridLinesVisible(true);
        spendChart.setVerticalGridLinesVisible(true);
        spendChart.setAlternativeColumnFillVisible(false);
        spendChart.setAlternativeRowFillVisible(false);
        spendChart.setCategoryGap(24);
        spendChart.setBarGap(8);

        CategoryAxis xAxis = (CategoryAxis) spendChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) spendChart.getYAxis();

        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);

        double max = marketingDao.getCampaignSpendByMonth()
                .stream()
                .mapToDouble(p -> p.spend())
                .max()
                .orElse(0);

        double upperBound = Math.ceil(max / 10000.0) * 10000;

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(10000);
        yAxis.setTickLabelGap(10);
        yAxis.setAutoRanging(true);
        yAxis.setTickLabelFormatter(null);
        yAxis.setMinorTickVisible(false);
        yAxis.setTickMarkVisible(true);
        yAxis.setTickLabelsVisible(true);
    }

    /**
     * Installs tooltips and hover effects for each data point in a chart series.
     *
     * <p>Each tooltip displays the provided label, the month associated with
     * the data point, and the formatted monetary value. A scaling effect is
     * also applied when the user hovers over a chart bar.</p>
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
                        node.setStyle("-fx-scale-x: 1.15; -fx-scale-y: 1.15;");
                    });

                    node.setOnMouseExited(e -> {
                        node.setStyle("");
                    });
                }
            });
        }
    }

    /**
     * Configures the campaign table and binds each column to its corresponding
     * value in a {@link MarketingDao.CampaignRow}.
     *
     * <p>This method also applies a constrained resize policy so that the
     * available table width is distributed across all columns.</p>
     */
    private void setupCampaignTable() {
        campaignTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        nameCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().name()));

        channelCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().channel()));

        budgetCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(String.format("$%,.2f", cell.getValue().budget())));

        spendCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(String.format("$%,.2f", cell.getValue().spend())));

        statusCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().status()));

        startDateCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().startDate()));

        endDateCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().endDate()));
    }
}