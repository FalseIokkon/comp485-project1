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

    public double getMonthlyRevenue() {
        String sql = """
            SELECT COALESCE(SUM(amount), 0)
            FROM invoices
            WHERE strftime('%Y-%m', invoice_date) = strftime('%Y-%m', 'now')
        """;

        return getDoubleValue(sql);
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
            WHERE strftime('%Y-%m', lead_date) = strftime('%Y-%m', 'now')
        """;

        return getIntValue(sql);
    }

    public int getUnitsProducedThisMonth() {
        String sql = """
            SELECT COALESCE(SUM(units_produced), 0)
            FROM production_batches
            WHERE strftime('%Y-%m', scheduled_date) = strftime('%Y-%m', 'now')
        """;

        return getIntValue(sql);
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
        String today = getTodayDate();

        rows.add(new DivisionKPI(
                "HR",
                "Open Positions",
                getOpenPositions(),
                today
        ));

        rows.add(new DivisionKPI(
                "Finance",
                "Monthly Revenue",
                getMonthlyRevenue(),
                today
        ));

        rows.add(new DivisionKPI(
                "Marketing",
                "Leads This Month",
                getLeadsThisMonth(),
                today
        ));

        rows.add(new DivisionKPI(
                "Manufacturing",
                "Units Produced This Month",
                getUnitsProducedThisMonth(),
                today
        ));

        rows.add(new DivisionKPI(
                "Distributing",
                "On-Time Delivery %",
                getOnTimeDeliveryRate(),
                today
        ));

        rows.add(new DivisionKPI(
                "Overview",
                "Active Customers",
                getActiveCustomers(),
                today
        ));

        return rows;
    }

    public List<MonthlyTrendPoint> getRevenueVsExpensesTrend() {
        Map<String, MonthlyTrendPoint> trendMap = new LinkedHashMap<>();

        String revenueSql = """
            SELECT strftime('%Y-%m', invoice_date) AS month,
                   COALESCE(SUM(amount_paid), 0) AS revenue
            FROM invoices
            GROUP BY strftime('%Y-%m', invoice_date)
            ORDER BY month
        """;

        String expenseSql = """
            SELECT strftime('%Y-%m', expense_date) AS month,
                   COALESCE(SUM(amount), 0) AS expenses
            FROM expenses
            GROUP BY strftime('%Y-%m', expense_date)
            ORDER BY month
        """;

        try (Connection conn = DB.connect()) {

            try (PreparedStatement ps = conn.prepareStatement(revenueSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String month = rs.getString("month");
                    double revenue = rs.getDouble("revenue");
                    trendMap.put(month, new MonthlyTrendPoint(month, revenue, 0));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(expenseSql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String month = rs.getString("month");
                    double expenses = rs.getDouble("expenses");

                    MonthlyTrendPoint existing = trendMap.get(month);
                    if (existing == null) {
                        trendMap.put(month, new MonthlyTrendPoint(month, 0, expenses));
                    } else {
                        trendMap.put(month, new MonthlyTrendPoint(month, existing.revenue(), expenses));
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load overview trend data", e);
        }

        return new ArrayList<>(trendMap.values());
    }

    private double getDoubleValue(String sql) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute overview query", e);
        }
    }

    private int getIntValue(String sql) {
        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute overview query", e);
        }
    }

    private String getTodayDate() {
        String sql = "SELECT date('now')";

        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString(1);
            }
            return "";

        } catch (SQLException e) {
            throw new RuntimeException("Failed to get current date", e);
        }
    }

    public record MonthlyTrendPoint(String month, double revenue, double expenses) {
    }
}