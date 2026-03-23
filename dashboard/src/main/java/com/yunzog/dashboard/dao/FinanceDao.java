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

public class FinanceDao {

    private static final String REPORT_YEAR = "2025";

    public double getTotalExpenses() {
        String sql = "SELECT SUM(amount) FROM expenses";
        return getDoubleValue(sql);
    }

    public double getPayrollThisMonth() {
        String sql = "SELECT SUM(net_pay) FROM payroll";
        return getDoubleValue(sql);
    }

    public int getInvoicesSent() {
        String sql = "SELECT COUNT(*) FROM invoices";
        return getIntValue(sql);
    }

    public double getPaymentsReceived() {
        String sql = "SELECT SUM(amount_paid) FROM invoices WHERE payment_status='PAID'";
        return getDoubleValue(sql);
    }

    public double getOutstandingAmount() {
        String sql = "SELECT SUM(amount - amount_paid) FROM invoices";
        return getDoubleValue(sql);
    }

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

    private int getIntValue(String sql) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    private double getDoubleValue(String sql) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }

    public record ExpensesByMonthPoint(String month, double totalAmount) {
    }

    public record InvoiceRow(String invoiceNumber, String customerName, double amount, double amountPaid, String status,
            String invoiceDate) {
    }
}