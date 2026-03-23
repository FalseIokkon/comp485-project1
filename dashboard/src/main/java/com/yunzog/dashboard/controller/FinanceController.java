package com.yunzog.dashboard.controller;

import com.yunzog.dashboard.dao.FinanceDao;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class FinanceController {

    @FXML
    private Label totalExpensesLabel;
    @FXML
    private Label payrollLabel;
    @FXML
    private Label invoicesSentLabel;
    @FXML
    private Label paymentsReceivedLabel;
    @FXML
    private Label outstandingLabel;

    @FXML
    private BarChart<String, Number> expensesChart;

    @FXML
    private TableView<FinanceDao.InvoiceRow> invoiceTable;
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> invoiceNumberCol;
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> customerCol;
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> amountCol;
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> amountPaidCol;
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> statusCol;
    @FXML
    private TableColumn<FinanceDao.InvoiceRow, String> invoiceDateCol;

    private final FinanceDao financeDao = new FinanceDao();

    @FXML
    private void initialize() {
        setupInvoiceTable();
        loadData();
    }

    public void loadData() {
        loadCards();
        loadExpensesChart();
        loadInvoiceTable();
    }

    private void loadCards() {
        totalExpensesLabel.setText(String.format("$%,.2f", financeDao.getTotalExpenses()));
        payrollLabel.setText(String.format("$%,.2f", financeDao.getPayrollThisMonth()));
        invoicesSentLabel.setText(String.valueOf(financeDao.getInvoicesSent()));
        paymentsReceivedLabel.setText(String.format("$%,.2f", financeDao.getPaymentsReceived()));
        outstandingLabel.setText(String.format("$%,.2f", financeDao.getOutstandingAmount()));
    }

    private void loadExpensesChart() {
        expensesChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (FinanceDao.ExpensesByMonthPoint point : financeDao.getExpensesByMonth()) {
            series.getData().add(new XYChart.Data<>(point.month(), point.totalAmount()));
        }

        expensesChart.getData().add(series);
    }

    private void loadInvoiceTable() {
        invoiceTable.setItems(FXCollections.observableArrayList(financeDao.getInvoiceRows()));
    }

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
}