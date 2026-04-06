package com.yunzog.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.yunzog.dashboard.db.DB;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Data access object for finance-related data.
 *
 * <p>This class provides methods to retrieve financial summary metrics,
 * monthly expense trends, and invoice records from the database. It uses
 * JDBC queries to access expense, payroll, and invoice data and maps the
 * results into dashboard-friendly record types.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class FinanceDao {

    /** Reporting year used for monthly expense aggregation. */
    private static final String REPORT_YEAR = "2025";

    /**
     * Retrieves the total amount of all recorded expenses.
     *
     * @return the sum of all expense amounts
     */
    public double getTotalExpenses() {
        String sql = "SELECT SUM(amount) FROM expenses";
        return getDoubleValue(sql);
    }

    /**
     * Retrieves the total payroll amount for the current reporting period.
     *
     * @return the sum of all net payroll values
     */
    public double getPayrollThisMonth() {
        String sql = "SELECT SUM(net_pay) FROM payroll";
        return getDoubleValue(sql);
    }

    /**
     * Retrieves the total number of invoices sent.
     *
     * @return the count of invoices in the database
     */
    public int getInvoicesSent() {
        String sql = "SELECT COUNT(*) FROM invoices";
        return getIntValue(sql);
    }

    /**
     * Retrieves the total amount of payments received for paid invoices.
     *
     * @return the total amount paid on invoices with a PAID status
     */
    public double getPaymentsReceived() {
        String sql = "SELECT SUM(amount_paid) FROM invoices WHERE payment_status='PAID'";
        return getDoubleValue(sql);
    }

    /**
     * Retrieves the total outstanding amount across all invoices.
     *
     * <p>This value is calculated as the sum of the remaining unpaid balance
     * for each invoice.</p>
     *
     * @return the total outstanding invoice amount
     */
    public double getOutstandingAmount() {
        String sql = "SELECT SUM(amount - amount_paid) FROM invoices";
        return getDoubleValue(sql);
    }

    /**
     * Retrieves monthly expense totals for the configured reporting year.
     *
     * <p>This method initializes entries for all twelve months of the report
     * year so that months with no recorded expenses are still included with
     * a value of zero.</p>
     *
     * @return a list of {@link ExpensesByMonthPoint} objects ordered by month
     * @throws RuntimeException if the expense data cannot be loaded
     */
    public List<ExpensesByMonthPoint> getExpensesByMonth() {
        java.util.Map<String, ExpensesByMonthPoint> points = new java.util.LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%s-%02d", REPORT_YEAR, month);
            points.put(monthKey, new ExpensesByMonthPoint(monthKey, 0));
        }

        String sql = """
                    SELECT strftime('%Y-%m', expense_date) AS month, SUM(amount) AS total
                    FROM expenses
                    WHERE strftime('%Y', expense_date) = ?
                    GROUP BY month
                    ORDER BY month
                """;

        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, REPORT_YEAR);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String month = rs.getString("month");
                    double total = rs.getDouble("total");
                    points.put(month, new ExpensesByMonthPoint(month, total));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load expenses by month", e);
        }

        return new ArrayList<>(points.values());
    }

    /**
     * Retrieves invoice records for display in the finance dashboard.
     *
     * <p>Each invoice row includes invoice details, payment amounts, status,
     * and a generated customer display name based on the customer identifier.</p>
     *
     * @return an observable list of {@link InvoiceRow} records
     * @throws RuntimeException if invoice data cannot be loaded
     */
    public ObservableList<InvoiceRow> getInvoiceRows() {
        ObservableList<InvoiceRow> rows = FXCollections.observableArrayList();
        String sql = "SELECT invoice_number, customer_id, amount, amount_paid, payment_status, invoice_date FROM invoices";

        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new InvoiceRow(
                        rs.getString("invoice_number"),
                        "Customer " + rs.getInt("customer_id"),
                        rs.getDouble("amount"),
                        rs.getDouble("amount_paid"),
                        rs.getString("payment_status"),
                        rs.getString("invoice_date")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load invoices", e);
        }

        return rows;
    }

    /**
     * Executes a SQL query that returns a single integer value.
     *
     * @param sql the SQL query to execute
     * @return the integer result of the query, or 0 if no result is found
     * @throws RuntimeException if the query execution fails
     */
    private int getIntValue(String sql) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    /**
     * Executes a SQL query that returns a single double value.
     *
     * @param sql the SQL query to execute
     * @return the double result of the query, or 0 if no result is found
     * @throws RuntimeException if the query execution fails
     */
    private double getDoubleValue(String sql) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    /**
     * Represents a monthly expense total for a specific month.
     *
     * @param month the month in {@code YYYY-MM} format
     * @param totalAmount the total expense amount for the month
     */
    public record ExpensesByMonthPoint(String month, double totalAmount) {
    }

    /**
     * Represents an invoice record for display in the finance dashboard.
     *
     * @param invoiceNumber the invoice number
     * @param customerName the customer display name
     * @param amount the total invoice amount
     * @param amountPaid the amount paid toward the invoice
     * @param status the payment status of the invoice
     * @param invoiceDate the invoice issue date
     */
    public record InvoiceRow(
            String invoiceNumber,
            String customerName,
            double amount,
            double amountPaid,
            String status,
            String invoiceDate) {
    }
}