package com.yunzog.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.yunzog.dashboard.db.DB;

/**
 * Data access object for marketing-related data.
 *
 * <p>This class provides methods to retrieve campaign budgets, spending,
 * campaign counts, lead counts, and monthly campaign spending trends from
 * the database. It uses JDBC to execute SQL queries and maps the results
 * into record-based data structures for use by the dashboard.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class MarketingDao {

    /** Reporting month used for month-specific lead calculations. */
    private static final String REPORT_MONTH = "2025-12";

    /** Reporting year used for year-based campaign trend calculations. */
    private static final String REPORT_YEAR = "2025";

    /**
     * Retrieves the total allocated marketing budget across all campaigns.
     *
     * @return the sum of all campaign budgets
     */
    public double getTotalBudget() {
        String sql = "SELECT COALESCE(SUM(budget),0) FROM campaigns";
        return getDoubleValue(sql);
    }

    /**
     * Retrieves the total marketing spend across all campaigns.
     *
     * @return the sum of all campaign spending values
     */
    public double getTotalSpend() {
        String sql = "SELECT COALESCE(SUM(spend),0) FROM campaigns";
        return getDoubleValue(sql);
    }

    /**
     * Retrieves the number of currently active campaigns.
     *
     * @return the count of campaigns with an ACTIVE status
     */
    public int getActiveCampaigns() {
        String sql = "SELECT COUNT(*) FROM campaigns WHERE status='ACTIVE'";
        return getIntValue(sql);
    }

    /**
     * Retrieves the number of leads recorded during the reporting month.
     *
     * @return the count of leads for the configured reporting month
     */
    public int getLeadsThisMonth() {
        String sql = "SELECT COUNT(*) FROM leads WHERE strftime('%Y-%m', lead_date)=?";
        return getIntValue(sql, REPORT_MONTH);
    }

    /**
     * Retrieves campaign records for display in the marketing dashboard table.
     *
     * <p>The returned records include campaign name, channel, budget, spend,
     * status, start date, and end date, ordered by start date.</p>
     *
     * @return a list of {@link CampaignRow} records representing campaigns
     * @throws RuntimeException if the campaign data cannot be loaded
     */
    public List<CampaignRow> getCampaignTable() {
        String sql = """
                    SELECT name, channel, budget, spend, status, start_date, end_date
                    FROM campaigns
                    ORDER BY start_date
                """;

        List<CampaignRow> rows = new ArrayList<>();

        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new CampaignRow(
                        rs.getString("name"),
                        rs.getString("channel"),
                        rs.getDouble("budget"),
                        rs.getDouble("spend"),
                        rs.getString("status"),
                        rs.getString("start_date"),
                        rs.getString("end_date")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load campaigns", e);
        }

        return rows;
    }

    /**
     * Retrieves campaign spending totals grouped by month for the reporting year.
     *
     * <p>This method initializes all twelve months of the reporting year with
     * zero values so that missing months still appear in the result. It then
     * replaces those defaults with actual spending totals for any months that
     * contain campaign data.</p>
     *
     * @return a list of {@link CampaignByMonthPoint} records representing
     *         monthly spending totals for the reporting year
     * @throws RuntimeException if the monthly campaign spending data cannot be loaded
     */
    public List<CampaignByMonthPoint> getCampaignSpendByMonth() {
        java.util.Map<String, CampaignByMonthPoint> points = new java.util.LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%s-%02d", REPORT_YEAR, month);
            points.put(monthKey, new CampaignByMonthPoint(monthKey, 0));
        }

        String sql = """
                    SELECT strftime('%Y-%m', start_date) AS month, COALESCE(SUM(spend),0) AS spend
                    FROM campaigns
                    WHERE strftime('%Y', start_date)=?
                    GROUP BY strftime('%Y-%m', start_date)
                    ORDER BY month
                """;

        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, REPORT_YEAR);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String month = rs.getString("month");
                    double spend = rs.getDouble("spend");
                    points.put(month, new CampaignByMonthPoint(month, spend));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load campaign spend by month", e);
        }

        return new ArrayList<>(points.values());
    }

    /**
     * Executes a SQL query that returns a single integer value.
     *
     * <p>This helper method supports optional string parameters for prepared
     * statement placeholders.</p>
     *
     * @param sql the SQL query to execute
     * @param params optional parameter values to bind to the query
     * @return the integer result of the query, or 0 if no result is found
     * @throws RuntimeException if the query fails to execute
     */
    private int getIntValue(String sql, String... params) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute Marketing query", e);
        }
    }

    /**
     * Executes a SQL query that returns a single double value.
     *
     * <p>This helper method supports optional string parameters for prepared
     * statement placeholders.</p>
     *
     * @param sql the SQL query to execute
     * @param params optional parameter values to bind to the query
     * @return the double result of the query, or 0 if no result is found
     * @throws RuntimeException if the query fails to execute
     */
    private double getDoubleValue(String sql, String... params) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute Marketing query", e);
        }
    }

    /**
     * Represents a campaign record used in the marketing campaign table.
     *
     * @param name the campaign name
     * @param channel the marketing channel used by the campaign
     * @param budget the allocated campaign budget
     * @param spend the amount spent by the campaign
     * @param status the current campaign status
     * @param startDate the campaign start date
     * @param endDate the campaign end date
     */
    public record CampaignRow(
            String name,
            String channel,
            double budget,
            double spend,
            String status,
            String startDate,
            String endDate) {
    }

    /**
     * Represents the total campaign spending for a specific month.
     *
     * @param month the month identifier in {@code YYYY-MM} format
     * @param spend the total campaign spend for the month
     */
    public record CampaignByMonthPoint(String month, double spend) {
    }
}