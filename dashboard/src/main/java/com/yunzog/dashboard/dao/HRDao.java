package com.yunzog.dashboard.dao;

import com.yunzog.dashboard.db.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for human resources-related data.
 *
 * <p>This class provides methods to retrieve employee, hiring, attendance,
 * and performance information from the database. It also supplies employee
 * directory records and monthly hiring trend data for use in dashboard
 * views and reports.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class HRDao {

    /** Reporting month used for month-based HR queries in {@code YYYY-MM} format. */
    private static final String REPORT_MONTH = "2025-12";

    /** Reporting year used for year-based HR queries in {@code YYYY} format. */
    private static final String REPORT_YEAR = "2025";

    /**
     * Retrieves the total number of active employees.
     *
     * @return the count of employees whose employment status is ACTIVE
     */
    public int getTotalEmployees() {
        String sql = """
            SELECT COUNT(*)
            FROM employees
            WHERE employment_status = 'ACTIVE'
        """;

        return getIntValue(sql);
    }

    /**
     * Retrieves the total number of open job positions.
     *
     * @return the count of job openings whose status is OPEN
     */
    public int getOpenPositions() {
        String sql = """
            SELECT COUNT(*)
            FROM job_openings
            WHERE status = 'OPEN'
        """;

        return getIntValue(sql);
    }

    /**
     * Retrieves the number of employees hired during the reporting month.
     *
     * @return the count of employees hired in the configured reporting month
     */
    public int getHiresThisMonth() {
        String sql = """
            SELECT COUNT(*)
            FROM employees
            WHERE strftime('%Y-%m', hire_date) = ?
        """;

        return getIntValue(sql, REPORT_MONTH);
    }

    /**
     * Retrieves the average attendance rate for active employees.
     *
     * <p>The returned value is rounded to one decimal place. If no matching
     * records are found, this method returns {@code 0}.</p>
     *
     * @return the average attendance rate for active employees
     */
    public double getAttendanceRate() {
        String sql = """
            SELECT COALESCE(ROUND(AVG(attendance_rate), 1), 0)
            FROM employees
            WHERE employment_status = 'ACTIVE'
        """;

        return getDoubleValue(sql);
    }

    /**
     * Retrieves the average performance rating for active employees.
     *
     * <p>The returned value is rounded to one decimal place. If no matching
     * records are found, this method returns {@code 0}.</p>
     *
     * @return the average performance rating for active employees
     */
    public double getAveragePerformanceRating() {
        String sql = """
            SELECT COALESCE(ROUND(AVG(performance_rating), 1), 0)
            FROM employees
            WHERE employment_status = 'ACTIVE'
        """;

        return getDoubleValue(sql);
    }

    /**
     * Retrieves the employee directory with department and branch details.
     *
     * <p>This method joins employee, department, and branch data to produce
     * a complete list of employee directory records ordered by last name and
     * first name.</p>
     *
     * @return a list of {@link EmployeeDirectoryRow} records
     * @throws RuntimeException if the employee directory cannot be loaded
     */
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

    /**
     * Retrieves hiring totals for each month of the reporting year.
     *
     * <p>This method returns one entry for every month in the configured
     * reporting year. Months with no hires are included with a value of
     * {@code 0} to preserve chronological completeness in charts.</p>
     *
     * @return a list of {@link HiresByMonthPoint} records for the reporting year
     * @throws RuntimeException if the monthly hiring data cannot be loaded
     */
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

    /**
     * Executes a SQL query that returns a single integer value.
     *
     * @param sql the SQL query to execute
     * @param params optional parameter values bound to the prepared statement
     * @return the integer result of the query, or {@code 0} if no result is found
     * @throws RuntimeException if the query execution fails
     */
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

    /**
     * Executes a SQL query that returns a single double value.
     *
     * @param sql the SQL query to execute
     * @param params optional parameter values bound to the prepared statement
     * @return the double result of the query, or {@code 0} if no result is found
     * @throws RuntimeException if the query execution fails
     */
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

    /**
     * Binds string parameters to a prepared statement in order.
     *
     * @param ps the prepared statement to configure
     * @param params the parameter values to bind
     * @throws SQLException if a database access error occurs while binding values
     */
    private void bindParams(PreparedStatement ps, String... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setString(i + 1, params[i]);
        }
    }

    /**
     * Represents a row in the employee directory.
     *
     * @param fullName the employee's full name
     * @param jobTitle the employee's job title
     * @param departmentName the department name
     * @param branchName the branch name
     * @param employmentStatus the employee's employment status
     * @param hireDate the employee's hire date
     */
    public record EmployeeDirectoryRow(
            String fullName,
            String jobTitle,
            String departmentName,
            String branchName,
            String employmentStatus,
            String hireDate
    ) {}

    /**
     * Represents a monthly hiring data point.
     *
     * @param month the month in {@code YYYY-MM} format
     * @param hires the number of hires recorded for that month
     */
    public record HiresByMonthPoint(
            String month,
            int hires
    ) {}
}