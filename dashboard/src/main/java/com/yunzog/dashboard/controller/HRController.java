package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.HRDao;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class HRController {

    @FXML private Label totalEmployeesLabel;
    @FXML private Label openPositionsLabel;
    @FXML private Label hiresThisMonthLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label avgPerformanceLabel;

    @FXML private BarChart<String, Number> hiresChart;

    @FXML private TableView<HRDao.EmployeeDirectoryRow> employeeTable;
    @FXML private TableColumn<HRDao.EmployeeDirectoryRow, String> nameCol;
    @FXML private TableColumn<HRDao.EmployeeDirectoryRow, String> titleCol;
    @FXML private TableColumn<HRDao.EmployeeDirectoryRow, String> departmentCol;
    @FXML private TableColumn<HRDao.EmployeeDirectoryRow, String> branchCol;
    @FXML private TableColumn<HRDao.EmployeeDirectoryRow, String> statusCol;
    @FXML private TableColumn<HRDao.EmployeeDirectoryRow, String> hireDateCol;

    private final HRDao hrDao = new HRDao();

    @FXML
    private void initialize() {
        setupEmployeeTable();
        loadData();
    }

    public void loadData() {
        loadCards();
        loadHiresChart();
        loadEmployeeTable();
    }

    private void loadCards() {
        totalEmployeesLabel.setText(String.valueOf(hrDao.getTotalEmployees()));
        openPositionsLabel.setText(String.valueOf(hrDao.getOpenPositions()));
        hiresThisMonthLabel.setText(String.valueOf(hrDao.getHiresThisMonth()));
        attendanceRateLabel.setText(String.format("%.1f%%", hrDao.getAttendanceRate()));
        avgPerformanceLabel.setText(String.format("%.1f", hrDao.getAveragePerformanceRating()));
    }

    private void loadHiresChart() {
        hiresChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hires");

        for (HRDao.HiresByMonthPoint point : hrDao.getHiresByMonth()) {
            series.getData().add(new XYChart.Data<>(point.month(), point.hires()));
        }

        hiresChart.getData().add(series);
    }

    private void loadEmployeeTable() {
        employeeTable.setItems(FXCollections.observableArrayList(hrDao.getEmployeeDirectory()));
    }

    private void setupEmployeeTable() {
        employeeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        nameCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().fullName()));

        titleCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().jobTitle()));

        departmentCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().departmentName()));

        branchCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().branchName()));

        statusCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().employmentStatus()));

        hireDateCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().hireDate()));
    }
}