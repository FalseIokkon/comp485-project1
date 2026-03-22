package com.yunzog.dashboard.dao;

import com.yunzog.dashboard.db.DB;
import com.yunzog.dashboard.model.DivisionKPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OverviewDao {

    private static final String REPORT_MONTH = "2025-12";
    private static final String REPORT_YEAR = "2025";
    private static final String REPORT_AS_OF = "2025-12-31";

    public double getMonthlyRevenue() {
        String sql = """
            SELECT COALESCE(SUM(amount_paid), 0)
            FROM invoices
            WHERE strftime('%Y-%m', invoice_date) = ?
        """;

        return getDoubleValue(sql, REPORT_MONTH);
    }

    public int getOpenPositions() {
        String sql = """
            SELECT COUNT(*)
            FROM job_openings
            WHERE status = 'OPEN'
        """;

        return getIntValue(sql);
    }

    public double getOnTimeDeliveryRate() {
        String sql = """
            SELECT COALESCE(
                ROUND(
                    100.0 * SUM(CASE WHEN on_time = 1 THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0),
                    1
                ),
                0
            )
            FROM shipments
            WHERE delivery_status IN ('DELIVERED', 'DELAYED')
        """;

        return getDoubleValue(sql);
    }

    public int getLeadsThisMonth() {
        String sql = """
            SELECT COUNT(*)
            FROM leads
            WHERE strftime('%Y-%m', lead_date) = ?
        """;

        return getIntValue(sql, REPORT_MONTH);
    }

    public int getUnitsProducedThisMonth() {
        String sql = """
            SELECT COALESCE(SUM(units_produced), 0)
            FROM production_batches
            WHERE strftime('%Y-%m', scheduled_date) = ?
        """;

        return getIntValue(sql, REPORT_MONTH);
    }

    public int getActiveCustomers() {
        String sql = """
            SELECT COUNT(*)
            FROM customers
            WHERE status = 'ACTIVE'
        """;

        return getIntValue(sql);
    }

    public List<DivisionKPI> getDivisionKpis() {
        List<DivisionKPI> rows = new ArrayList<>();
        String asOf = getAsOfDate();

        rows.add(new DivisionKPI(
                "HR",
                "Open Positions",
                getOpenPositions(),
                asOf
        ));

        rows.add(new DivisionKPI(
                "Finance",
                "Monthly Revenue",
                getMonthlyRevenue(),
                asOf
        ));

        rows.add(new DivisionKPI(
                "Marketing",
                "Leads This Month",
                getLeadsThisMonth(),
                asOf
        ));

        rows.add(new DivisionKPI(
                "Manufacturing",
                "Units Produced This Month",
                getUnitsProducedThisMonth(),
                asOf
        ));

        rows.add(new DivisionKPI(
                "Distributing",
                "On-Time Delivery %",
                getOnTimeDeliveryRate(),
                asOf
        ));

        rows.add(new DivisionKPI(
                "Overview",
                "Active Customers",
                getActiveCustomers(),
                asOf
        ));

        return rows;
    }

    public List<MonthlyTrendPoint> getRevenueVsExpensesTrend() {
        Map<String, MonthlyTrendPoint> trendMap = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%s-%02d", REPORT_YEAR, month);
            trendMap.put(monthKey, new MonthlyTrendPoint(monthKey, 0, 0));
        }

        String revenueSql = """
            SELECT strftime('%Y-%m', invoice_date) AS month,
                   COALESCE(SUM(amount_paid), 0) AS revenue
            FROM invoices
            WHERE strftime('%Y', invoice_date) = ?
            GROUP BY strftime('%Y-%m', invoice_date)
            ORDER BY month
        """;

        String expenseSql = """
            SELECT strftime('%Y-%m', expense_date) AS month,
                   COALESCE(SUM(amount), 0) AS expenses
            FROM expenses
            WHERE strftime('%Y', expense_date) = ?
            GROUP BY strftime('%Y-%m', expense_date)
            ORDER BY month
        """;

        try (Connection conn = DB.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(revenueSql)) {
                ps.setString(1, REPORT_YEAR);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String month = rs.getString("month");
                        double revenue = rs.getDouble("revenue");

                        MonthlyTrendPoint existing = trendMap.get(month);
                        if (existing != null) {
                            trendMap.put(month, new MonthlyTrendPoint(month, revenue, existing.expenses()));
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(expenseSql)) {
                ps.setString(1, REPORT_YEAR);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String month = rs.getString("month");
                        double expenses = rs.getDouble("expenses");

                        MonthlyTrendPoint existing = trendMap.get(month);
                        if (existing != null) {
                            trendMap.put(month, new MonthlyTrendPoint(month, existing.revenue(), expenses));
                        }
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load overview trend data", e);
        }

        return new ArrayList<>(trendMap.values());
    }

    private double getDoubleValue(String sql, String... params) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute overview query", e);
        }
    }

    private int getIntValue(String sql, String... params) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            bindParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute overview query", e);
        }
    }

    private void bindParams(PreparedStatement ps, String... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setString(i + 1, params[i]);
        }
    }

    private String getAsOfDate() {
        return REPORT_AS_OF;
    }

    public record MonthlyTrendPoint(String month, double revenue, double expenses) {
    }
}