package com.yunzog.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.yunzog.dashboard.db.DB;

public class MarketingDao {

    private static final String REPORT_MONTH = "2025-12";
    private static final String REPORT_YEAR = "2025";

    public double getTotalBudget() {
        String sql = "SELECT COALESCE(SUM(budget),0) FROM campaigns";
        return getDoubleValue(sql);
    }

    public double getTotalSpend() {
        String sql = "SELECT COALESCE(SUM(spend),0) FROM campaigns";
        return getDoubleValue(sql);
    }

    public int getActiveCampaigns() {
        String sql = "SELECT COUNT(*) FROM campaigns WHERE status='ACTIVE'";
        return getIntValue(sql);
    }

    public int getLeadsThisMonth() {
        String sql = "SELECT COUNT(*) FROM leads WHERE strftime('%Y-%m', lead_date)=?";
        return getIntValue(sql, REPORT_MONTH);
    }

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

    private int getIntValue(String sql, String... params) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++)
                ps.setString(i + 1, params[i]);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute Marketing query", e);
        }
    }

    private double getDoubleValue(String sql, String... params) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++)
                ps.setString(i + 1, params[i]);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute Marketing query", e);
        }
    }

    public record CampaignRow(String name, String channel, double budget, double spend, String status, String startDate,
            String endDate) {
    }

    public record CampaignByMonthPoint(String month, double spend) {
    }
}