package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.FinanceDao;

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
 * Controller for the finance dashboard view.
 *
 * <p>This controller manages the financial summary display, monthly expenses
 * chart, and invoice table shown in the JavaFX dashboard. It retrieves
 * financial data through the {@link FinanceDao} and updates the interface
 * components to reflect current expense and invoice information.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class FinanceController {

    /** Displays the total recorded expenses. */
    @FXML
    private Label totalExpensesLabel;

    /** Displays the payroll amount for the current month. */
    @FXML
    private Label payrollLabel;

    /** Displays the total number of invoices sent. */
    @FXML
    private Label invoicesSentLabel;

    /** Displays the total amount of payments received. */
    @FXML
    private Label paymentsReceivedLabel;

    /** Displays the total outstanding unpaid amount. */
    @FXML
    private Label outstandingLabel;

    /** Bar chart used to display monthly expense totals. */
    @FXML
    private BarChart<String, Number> expensesChart;

    /** Table used to display invoice records. */
    @FXML
    private TableView<FinanceDao.InvoiceRow> invoiceTable;

    /** Column displaying the invoice number. */
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> invoiceNumberCol;

    /** Column displaying the customer name. */
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> customerCol;

    /** Column displaying the invoice amount. */
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> amountCol;

    /** Column displaying the amount paid toward the invoice. */
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> amountPaidCol;

    /** Column displaying the invoice status. */
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> statusCol;

    /** Column displaying the invoice date. */
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> invoiceDateCol;

    /** Data access object used to retrieve finance-related data. */
    private final FinanceDao financeDao = new FinanceDao();

    /**
     * Initializes the controller after the FXML elements have been loaded.
     *
     * <p>This method configures the invoice table, prepares the expenses chart,
     * and loads the initial finance data into the dashboard.</p>
     */
    @FXML
    private void initialize() {
        setupInvoiceTable();
        setupChart();
        loadData();
    }

    /**
     * Loads all finance dashboard data into the user interface.
     *
     * <p>This method refreshes the summary cards, updates the monthly expenses
     * chart, and populates the invoice table.</p>
     */
    public void loadData() {
        loadCards();
        loadExpensesChart();
        loadInvoiceTable();
    }

    /**
     * Loads and formats the financial summary card values.
     *
     * <p>This method retrieves expense, payroll, invoice, payment, and
     * outstanding balance values from the data source and displays them
     * in the dashboard summary labels.</p>
     */
    private void loadCards() {
        totalExpensesLabel.setText(String.format("$%,.2f", financeDao.getTotalExpenses()));
        payrollLabel.setText(String.format("$%,.2f", financeDao.getPayrollThisMonth()));
        invoicesSentLabel.setText(String.valueOf(financeDao.getInvoicesSent()));
        paymentsReceivedLabel.setText(String.format("$%,.2f", financeDao.getPaymentsReceived()));
        outstandingLabel.setText(String.format("$%,.2f", financeDao.getOutstandingAmount()));
    }

    /**
     * Loads monthly expense data into the expenses chart.
     *
     * <p>This method clears any existing chart data, creates a new data series,
     * and adds monthly expense totals retrieved from the data source.</p>
     */
    private void loadExpensesChart() {
        expensesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (FinanceDao.ExpensesByMonthPoint point : financeDao.getExpensesByMonth()) {
            series.getData().add(new XYChart.Data<>(point.month(), point.totalAmount()));
        }

        expensesChart.getData().add(series);
    }

    /**
     * Loads invoice records into the invoice table.
     *
     * <p>This method retrieves invoice row data from the data source and
     * populates the table with the current invoice records.</p>
     */
    private void loadInvoiceTable() {
        invoiceTable.setItems(FXCollections.observableArrayList(financeDao.getInvoiceRows()));
    }

    /**
     * Configures the invoice table and binds each column to its corresponding
     * value in a {@link FinanceDao.InvoiceRow}.
     *
     * <p>This method also applies a constrained resize policy so that the
     * available table width is distributed across all columns.</p>
     */
    private void setupInvoiceTable() {
        invoiceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        invoiceNumberCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().invoiceNumber()));
        customerCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().customerName()));
        amountCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.format("$%,.2f", cell.getValue().amount())));
        amountPaidCol.setCellValueFactory(
                cell -> new ReadOnlyStringWrapper(String.format("$%,.2f", cell.getValue().amountPaid())));
        statusCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().status()));
        invoiceDateCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().invoiceDate()));
    }

    /**
     * Configures the appearance and axis scaling of the expenses chart.
     *
     * <p>This method disables animation, hides the legend, adjusts spacing,
     * and configures the horizontal and vertical axes for readability.
     * The vertical axis is scaled dynamically based on the highest monthly
     * expense value and uses fixed increments for cleaner presentation.</p>
     */
    private void setupChart() {
        expensesChart.setAnimated(false);
        expensesChart.setLegendVisible(false);
        expensesChart.setHorizontalGridLinesVisible(true);
        expensesChart.setVerticalGridLinesVisible(false);
        expensesChart.setCategoryGap(24);
        expensesChart.setBarGap(8);

        CategoryAxis xAxis = (CategoryAxis) expensesChart.getXAxis();
        NumberAxis yAxis = (NumberAxis) expensesChart.getYAxis();

        xAxis.setTickLabelRotation(0);
        xAxis.setTickLabelGap(10);

        double max = financeDao.getExpensesByMonth()
                .stream()
                .mapToDouble(p -> p.totalAmount())
                .max()
                .orElse(0);

        double upper = Math.ceil(max / 5000.0) * 5000;
        upper += 5000;

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(upper);
        yAxis.setTickUnit(5000);
        yAxis.setTickLabelGap(12);
    }
}