package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.MarketingDao;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MarketingController {

    @FXML
    private Label totalBudgetLabel;
    @FXML
    private Label totalSpendLabel;
    @FXML
    private Label activeCampaignsLabel;
    @FXML
    private Label leadsThisMonthLabel;

    @FXML
    private BarChart<String, Number> spendChart;

    @FXML
    private TableView<MarketingDao.CampaignRow> campaignTable;
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> nameCol;
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> channelCol;
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> budgetCol;
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> spendCol;
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> statusCol;
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> startDateCol;
    @FXML
    private TableColumn<MarketingDao.CampaignRow, String> endDateCol;

    private final MarketingDao marketingDao = new MarketingDao();

    @FXML
    private void initialize() {
        setupCampaignTable();
        loadData();
    }

    public void loadData() {
        loadCards();
        loadSpendChart();
        loadCampaignTable();
    }

    private void loadCards() {
        totalBudgetLabel.setText(String.format("$%,.2f", marketingDao.getTotalBudget()));
        totalSpendLabel.setText(String.format("$%,.2f", marketingDao.getTotalSpend()));
        activeCampaignsLabel.setText(String.valueOf(marketingDao.getActiveCampaigns()));
        leadsThisMonthLabel.setText(String.valueOf(marketingDao.getLeadsThisMonth()));
    }

    private void loadSpendChart() {
        spendChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Spend");

        for (MarketingDao.CampaignByMonthPoint point : marketingDao.getCampaignSpendByMonth()) {
            series.getData().add(new XYChart.Data<>(point.month(), point.spend()));
        }

        spendChart.getData().add(series);
    }

    private void loadCampaignTable() {
        campaignTable.setItems(FXCollections.observableArrayList(marketingDao.getCampaignTable()));
    }

    private void setupCampaignTable() {
        campaignTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        nameCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().name()));
        channelCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().channel()));
        budgetCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.format("$%,.2f", cell.getValue().budget())));
        spendCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.format("$%,.2f", cell.getValue().spend())));
        statusCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status()));
        startDateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().startDate()));
        endDateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().endDate()));
    }
}