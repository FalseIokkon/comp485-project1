package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.HRDao;

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
 * Controller for the human resources dashboard view.
 *
 * <p>This controller manages the human resources summary display, monthly
 * hiring chart, and employee directory table shown in the JavaFX dashboard.
 * It retrieves HR-related data through the {@link HRDao} and updates the
 * interface components with current employee, hiring, attendance, and
 * performance information.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class HRController {

    /** Displays the total number of employees. */
    @FXML
    private Label totalEmployeesLabel;

    /** Displays the number of currently open positions. */
    @FXML
    private Label openPositionsLabel;

    /** Displays the number of hires made this month. */
    @FXML
    private Label hiresThisMonthLabel;

    /** Displays the employee attendance rate as a percentage. */
    @FXML
    private Label attendanceRateLabel;

    /** Displays the average employee performance rating. */
    @FXML
    private Label avgPerformanceLabel;

    /** Bar chart used to display hires by month. */
    @FXML
    private BarChart<String, Number> hiresChart;

    /** Table used to display employee directory records. */
    @FXML
    private TableView<HRDao.EmployeeDirectoryRow> employeeTable;

    /** Column displaying the employee full name. */
    @FXML
    private TableColumn<HRDao.EmployeeDirectoryRow, String> nameCol;

    /** Column displaying the employee job title. */
    @FXML
    private TableColumn<HRDao.EmployeeDirectoryRow, String> titleCol;

    /** Column displaying the employee department name. */
    @FXML
    private TableColumn<HRDao.EmployeeDirectoryRow, String> departmentCol;

    /** Column displaying the employee branch name. */
    @FXML
    private TableColumn<HRDao.EmployeeDirectoryRow, String> branchCol;

    /** Column displaying the employee employment status. */
    @FXML
    private TableColumn<HRDao.EmployeeDirectoryRow, String> statusCol;

    /** Column displaying the employee hire date. */
    @FXML
    private TableColumn<HRDao.EmployeeDirectoryRow, String> hireDateCol;

    /** Data access object used to retrieve human resources data. */
    private final HRDao hrDao = new HRDao();

    /**
     * Initializes the controller after the FXML elements have been loaded.
     *
     * <p>This method configures the employee table, prepares the hiring chart,
     * and loads the initial human resources data into the dashboard.</p>
     */
    @FXML
    private void initialize() {
        setupEmployeeTable();
        setupChart();
        loadData();
    }

    /**
     * Loads all human resources dashboard data into the user interface.
     *
     * <p>This method refreshes the summary cards, updates the monthly hires
     * chart, and populates the employee directory table.</p>
     */
    public void loadData() {
        loadCards();
        loadHiresChart();
        loadEmployeeTable();
    }

    /**
     * Loads and formats the human resources summary card values.
     *
     * <p>This method retrieves employee totals, hiring information,
     * attendance rate, and average performance values from the data source
     * and displays them in the dashboard summary labels.</p>
     */
    private void loadCards() {
        totalEmployeesLabel.setText(String.valueOf(hrDao.getTotalEmployees()));
        openPositionsLabel.setText(String.valueOf(hrDao.getOpenPositions()));
        hiresThisMonthLabel.setText(String.valueOf(hrDao.getHiresThisMonth()));
        attendanceRateLabel.setText(String.format("%.1f%%", hrDao.getAttendanceRate()));
        avgPerformanceLabel.setText(String.format("%.1f", hrDao.getAveragePerformanceRating()));
    }

    /**
     * Loads monthly hiring data into the hires chart.
     *
     * <p>This method clears any existing chart data, creates a new data series,
     * and adds monthly hiring totals retrieved from the data source.</p>
     */
    private void loadHiresChart() {
        hiresChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hires");

        for (HRDao.HiresByMonthPoint point : hrDao.getHiresByMonth()) {
            series.getData().add(new XYChart.Data<>(point.month(), point.hires()));
        }

        hiresChart.getData().add(series);
    }

    /**
     * Loads employee directory records into the employee table.
     *
     * <p>This method retrieves employee directory data from the data source
     * and populates the table with the current employee records.</p>
     */
    private void loadEmployeeTable() {
        employeeTable.setItems(FXCollections.observableArrayList(hrDao.getEmployeeDirectory()));
    }

    /**
     * Configures the appearance and axis scaling of the hires chart.
     *
     * <p>This method disables animation, hides the legend, adjusts chart
     * spacing, and configures the horizontal and vertical axes for improved
     * readability. The vertical axis is scaled dynamically based on the
     * highest monthly hiring value and uses whole-number increments.</p>
     */
    private void setupChart() {
        hiresChart.setAnimated(false);
        hiresChart.setLegendVisible(false);
        hiresChart.setHorizontalGridLinesVisible(true);
        hiresChart.setVerticalGridLinesVisible(false);
        hiresChart.setAlternativeColumnFillVisible(false);
        hiresChart.setAlternativeRowFillVisible(false);
        hiresChart.setCategoryGap(24);
        hiresChart.setBarGap(8);

        CategoryAxis xAxis = (CategoryAxis) hiresChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) hiresChart.getYAxis();

        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);

        int max = hrDao.getHiresByMonth()
                .stream()
                .mapToInt(p -> p.hires())
                .max()
                .orElse(0);

        int upper = (int) Math.ceil(max * 1.2);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(Math.max(upper, 1));
        yAxis.setTickUnit(1);
        yAxis.setTickLabelGap(10);
    }

    /**
     * Configures the employee table and binds each column to its corresponding
     * value in an {@link HRDao.EmployeeDirectoryRow}.
     *
     * <p>This method also applies a constrained resize policy so that the
     * available table width is distributed across all columns.</p>
     */
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