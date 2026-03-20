package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class FinanceSeed {

    public static void seed(Connection conn) throws Exception {
        seedExpenses(conn);
        seedPayroll(conn);
        seedInvoices(conn);
    }

    private static void seedExpenses(Connection conn) throws Exception {
        String sql = """
            INSERT INTO expenses
            (expense_date, department_id, category, description, amount)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertExpense(ps, "2026-01-10", 2, "Office Supplies", "Accounting office supplies", 450.00);
            insertExpense(ps, "2026-01-15", 3, "Advertising", "Regional print ad campaign", 1800.00);
            insertExpense(ps, "2026-02-05", 4, "Equipment", "Production equipment maintenance", 2200.00);
            insertExpense(ps, "2026-02-14", 6, "Utilities", "Admin office utilities", 950.00);
            insertExpense(ps, "2026-03-03", 5, "Shipping", "Distribution support costs", 1400.00);
        }
    }

    private static void seedPayroll(Connection conn) throws Exception {
        String sql = """
            INSERT INTO payroll
            (employee_id, pay_period_start, pay_period_end, gross_pay, bonus, deductions, net_pay, paid_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertPayroll(ps, 1, "2026-02-01", "2026-02-28", 6500.00, 500.00, 1200.00, 5800.00, "2026-03-01");
            insertPayroll(ps, 2, "2026-02-01", "2026-02-28", 3500.00, 0.00, 700.00, 2800.00, "2026-03-01");
            insertPayroll(ps, 3, "2026-02-01", "2026-02-28", 5000.00, 300.00, 950.00, 4350.00, "2026-03-01");
            insertPayroll(ps, 4, "2026-02-01", "2026-02-28", 5200.00, 450.00, 1000.00, 4650.00, "2026-03-01");
            insertPayroll(ps, 5, "2026-02-01", "2026-02-28", 4800.00, 0.00, 900.00, 3900.00, "2026-03-01");
        }
    }

    private static void seedInvoices(Connection conn) throws Exception {
        String sql = """
            INSERT INTO invoices
            (invoice_number, customer_id, invoice_date, due_date, amount, amount_paid, payment_status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertInvoice(ps, "INV-1001", 1, "2026-01-08", "2026-02-08", 4200.00, 4200.00, "PAID");
            insertInvoice(ps, "INV-1002", 2, "2026-01-15", "2026-02-15", 3100.00, 3100.00, "PAID");
            insertInvoice(ps, "INV-1003", 3, "2026-02-04", "2026-03-04", 5800.00, 3000.00, "PARTIAL");
            insertInvoice(ps, "INV-1004", 4, "2026-02-18", "2026-03-18", 2600.00, 0.00, "OVERDUE");
            insertInvoice(ps, "INV-1005", 5, "2026-03-02", "2026-04-02", 4900.00, 0.00, "UNPAID");
        }
    }

    private static void insertExpense(
            PreparedStatement ps,
            String expenseDate,
            int departmentId,
            String category,
            String description,
            double amount
    ) throws Exception {
        ps.setString(1, expenseDate);
        ps.setInt(2, departmentId);
        ps.setString(3, category);
        ps.setString(4, description);
        ps.setDouble(5, amount);
        ps.executeUpdate();
    }

    private static void insertPayroll(
            PreparedStatement ps,
            int employeeId,
            String payPeriodStart,
            String payPeriodEnd,
            double grossPay,
            double bonus,
            double deductions,
            double netPay,
            String paidDate
    ) throws Exception {
        ps.setInt(1, employeeId);
        ps.setString(2, payPeriodStart);
        ps.setString(3, payPeriodEnd);
        ps.setDouble(4, grossPay);
        ps.setDouble(5, bonus);
        ps.setDouble(6, deductions);
        ps.setDouble(7, netPay);
        ps.setString(8, paidDate);
        ps.executeUpdate();
    }

    private static void insertInvoice(
            PreparedStatement ps,
            String invoiceNumber,
            int customerId,
            String invoiceDate,
            String dueDate,
            double amount,
            double amountPaid,
            String paymentStatus
    ) throws Exception {
        ps.setString(1, invoiceNumber);
        ps.setInt(2, customerId);
        ps.setString(3, invoiceDate);
        ps.setString(4, dueDate);
        ps.setDouble(5, amount);
        ps.setDouble(6, amountPaid);
        ps.setString(7, paymentStatus);
        ps.executeUpdate();
    }
}