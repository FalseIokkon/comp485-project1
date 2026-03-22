package com.yunzog.dashboard.dao;

import com.yunzog.dashboard.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HRDao {

    private static final String REPORT_MONTH = "2025-12";
    private static final String REPORT_YEAR = "2025";

    public int getTotalEmployees() {
        String sql = """
            SELECT COUNT(*)
            FROM employees
            WHERE employment_status = 'ACTIVE'
        """;

        return getIntValue(sql);
    }

    public int getOpenPositions() {
        String sql = """
            SELECT COUNT(*)
            FROM job_openings
            WHERE status = 'OPEN'
        """;

        return getIntValue(sql);
    }

    public int getHiresThisMonth() {
        String sql = """
            SELECT COUNT(*)
            FROM employees
            WHERE strftime('%Y-%m', hire_date) = ?
        """;

        return getIntValue(sql, REPORT_MONTH);
    }

    public double getAttendanceRate() {
        String sql = """
            SELECT COALESCE(ROUND(AVG(attendance_rate), 1), 0)
            FROM employees
            WHERE employment_status = 'ACTIVE'
        """;

        return getDoubleValue(sql);
    }

    public double getAveragePerformanceRating() {
        String sql = """
            SELECT COALESCE(ROUND(AVG(performance_rating), 1), 0)
            FROM employees
            WHERE employment_status = 'ACTIVE'
        """;

        return getDoubleValue(sql);
    }

    public List<EmployeeDirectoryRow> getEmployeeDirectory() {
        String sql = """
            SELECT
                e.first_name || ' ' || e.last_name AS full_name,
                e.job_title,
                d.name AS department_name,
                b.name AS branch_name,
                e.employment_status,
                e.hire_date
            FROM employees e
            JOIN departments d ON e.department_id = d.department_id
            JOIN branches b ON e.branch_id = b.branch_id
            ORDER BY e.last_name, e.first_name
        """;

        List<EmployeeDirectoryRow> rows = new ArrayList<>();

        try (Connection conn = DB.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new EmployeeDirectoryRow(
                        rs.getString("full_name"),
                        rs.getString("job_title"),
                        rs.getString("department_name"),
                        rs.getString("branch_name"),
                        rs.getString("employment_status"),
                        rs.getString("hire_date")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load employee directory", e);
        }

        return rows;
    }

    public List<HiresByMonthPoint> getHiresByMonth() {
        java.util.Map<String, HiresByMonthPoint> points = new java.util.LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            String monthKey = String.format("%s-%02d", REPORT_YEAR, month);
            points.put(monthKey, new HiresByMonthPoint(monthKey, 0));
        }

        String sql = """
            SELECT
                strftime('%Y-%m', hire_date) AS month,
                COUNT(*) AS hires
            FROM employees
            WHERE strftime('%Y', hire_date) = ?
            GROUP BY strftime('%Y-%m', hire_date)
            ORDER BY month
        """;

        try (Connection conn = DB.connect();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, REPORT_YEAR);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String month = rs.getString("month");
                    int hires = rs.getInt("hires");
                    points.put(month, new HiresByMonthPoint(month, hires));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load hires-by-month data", e);
        }

        return new ArrayList<>(points.values());
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
            throw new RuntimeException("Failed to execute HR query", e);
        }
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
            throw new RuntimeException("Failed to execute HR query", e);
        }
    }

    private void bindParams(PreparedStatement ps, String... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setString(i + 1, params[i]);
        }
    }

    public record EmployeeDirectoryRow(
            String fullName,
            String jobTitle,
            String departmentName,
            String branchName,
            String employmentStatus,
            String hireDate
    ) {}

    public record HiresByMonthPoint(
            String month,
            int hires
    ) {}
}