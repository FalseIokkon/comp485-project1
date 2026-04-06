package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Seeds finance-related database tables with sample expense, payroll,
 * and invoice data for the application reporting period.
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class FinanceSeed {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END = LocalDate.of(2025, 12, 31);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            String[] categories = {
                "Office Supplies",
                "Advertising",
                "Equipment",
                "Utilities",
                "Shipping"
            };

            String[] descriptions = {
                "Accounting office supplies",
                "Regional ad campaign",
                "Production equipment maintenance",
                "Branch utility bill",
                "Distribution support costs"
            };

            int totalRows = 36;
            long totalDays = END.toEpochDay() - START.toEpochDay();

            for (int i = 0; i < totalRows; i++) {
                long offsetDays = (i * totalDays) / (totalRows - 1);
                LocalDate expenseDate = START.plusDays(offsetDays);

                int departmentId = switch (i % 5) {
                    case 0 -> 2;
                    case 1 -> 3;
                    case 2 -> 4;
                    case 3 -> 6;
                    default -> 5;
                };

                String category = categories[i % categories.length];
                String description = descriptions[i % descriptions.length];

                double amount = 2200 + (i * 180);

                insertExpense(
                        ps,
                        expenseDate.format(FMT),
                        departmentId,
                        category,
                        description,
                        amount
                );
            }
        }
    }

    private static void seedPayroll(Connection conn) throws Exception {
        String sql = """
            INSERT INTO payroll
            (employee_id, pay_period_start, pay_period_end, gross_pay, bonus, deductions, net_pay, paid_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int employeeCount = 5;

            for (int month = 1; month <= 12; month++) {
                YearMonth ym = YearMonth.of(2025, month);
                LocalDate payPeriodStart = ym.atDay(1);
                LocalDate payPeriodEnd = ym.atEndOfMonth();
                LocalDate paidDate = payPeriodEnd;

                for (int employeeId = 1; employeeId <= employeeCount; employeeId++) {
                    double grossPay = 3200 + (employeeId * 550) + (month * 25);
                    double bonus = ((employeeId + month) % 3 == 0) ? 250.00 : 0.00;
                    double deductions = round2(grossPay * 0.18);
                    double netPay = round2(grossPay + bonus - deductions);

                    insertPayroll(
                            ps,
                            employeeId,
                            payPeriodStart.format(FMT),
                            payPeriodEnd.format(FMT),
                            grossPay,
                            bonus,
                            deductions,
                            netPay,
                            paidDate.format(FMT)
                    );
                }
            }
        }
    }

    private static void seedInvoices(Connection conn) throws Exception {
        String sql = """
            INSERT INTO invoices
            (invoice_number, customer_id, invoice_date, due_date, amount, amount_paid, payment_status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int totalRows = 60;
            long totalDays = END.toEpochDay() - START.toEpochDay();

            for (int i = 0; i < totalRows; i++) {
                int invoiceNumber = 1001 + i;

                long offsetDays = (i * totalDays) / (totalRows - 1);
                LocalDate invoiceDate = START.plusDays(offsetDays);
                LocalDate dueDate = invoiceDate.plusDays(30);

                int customerId = (i % 5) + 1;
                double amount = 2800 + (i * 165);

                String paymentStatus = "PAID";
                double amountPaid = amount;

                insertInvoice(
                        ps,
                        "INV-" + invoiceNumber,
                        customerId,
                        invoiceDate.format(FMT),
                        dueDate.format(FMT),
                        amount,
                        amountPaid,
                        paymentStatus
                );
            }
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

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}