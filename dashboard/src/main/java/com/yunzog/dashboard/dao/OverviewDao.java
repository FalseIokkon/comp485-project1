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

/**
 * Data access object for overview dashboard data.
 *
 * <p>This class provides methods for retrieving organization-wide KPI values
 * and monthly financial trends used in the overview dashboard. It queries the
 * database for summary metrics such as revenue, staffing, delivery performance,
 * lead generation, production output, and active customers.</p>
 *
 * <p>The class also builds aggregated division KPI records and monthly trend
 * data for revenue and expenses based on a fixed reporting period.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class OverviewDao {

    /** Reporting month used for month-based dashboard metrics. */
    private static final String REPORT_MONTH = "2025-12";

    /** Reporting year used for year-based trend analysis. */
    private static final String REPORT_YEAR = "2025";

    /** Reference date displayed for overview KPI records. */
    private static final String REPORT_AS_OF = "2025-12-31";

    /**
     * Retrieves the total revenue collected during the reporting month.
     *
     * <p>This value is calculated as the sum of all invoice payments whose
     * invoice date falls within the configured reporting month.</p>
     *
     * @return the total monthly revenue
     */
    public double getMonthlyRevenue() {
        String sql = """
            SELECT COALESCE(SUM(amount_paid), 0)
            FROM invoices
            WHERE strftime('%Y-%m', invoice_date) = ?
        """;

        return getDoubleValue(sql, REPORT_MONTH);
    }

    /**
     * Retrieves the total number of currently open job positions.
     *
     * @return the count of job openings with status {@code OPEN}
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
     * Calculates the on-time delivery rate for completed shipments.
     *
     * <p>This value is computed as the percentage of shipments marked as
     * on time among shipments whose delivery status is either
     * {@code DELIVERED} or {@code DELAYED}.</p>
     *
     * @return the on-time delivery rate as a percentage
     */
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

    /**
     * Retrieves the number of leads generated during the reporting month.
     *
     * @return the number of leads created in the reporting month
     */
    public int getLeadsThisMonth() {
        String sql = """
            SELECT COUNT(*)
            FROM leads
            WHERE strftime('%Y-%m', lead_date) = ?
        """;

        return getIntValue(sql, REPORT_MONTH);
    }

    /**
     * Retrieves the total number of units produced during the reporting month.
     *
     * @return the sum of units produced in the reporting month
     */
    public int getUnitsProducedThisMonth() {
        String sql = """
            SELECT COALESCE(SUM(units_produced), 0)
            FROM production_batches
            WHERE strftime('%Y-%m', scheduled_date) = ?
        """;

        return getIntValue(sql, REPORT_MONTH);
    }

    /**
     * Retrieves the total number of active customers.
     *
     * @return the count of customers whose status is {@code ACTIVE}
     */
    public int getActiveCustomers() {
        String sql = """
            SELECT COUNT(*)
            FROM customers
            WHERE status = 'ACTIVE'
        """;

        return getIntValue(sql);
    }

    /**
     * Builds a list of division KPI records for the overview dashboard.
     *
     * <p>Each record represents a key metric associated with a division and
     * includes a label, numeric value, and reporting date.</p>
     *
     * @return a list of {@link DivisionKPI} records for dashboard display
     */
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

    /**
     * Retrieves monthly revenue and expense totals for the reporting year.
     *
     * <p>This method builds a complete twelve-month trend series, ensuring
     * that each month in the reporting year is represented even if no data
     * exists for that month. Revenue and expense data are loaded separately
     * and merged into a single ordered result.</p>
     *
     * @return a list of {@link MonthlyTrendPoint} values for the reporting year
     * @throws RuntimeException if the trend data cannot be loaded
     */
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

    /**
     * Executes a query that returns a single double value.
     *
     * <p>This method prepares the statement, binds any provided parameters,
     * executes the query, and returns the first column of the first row.
     * If no result is found, {@code 0} is returned.</p>
     *
     * @param sql the SQL query to execute
     * @param params optional query parameters to bind
     * @return the resulting double value, or {@code 0} if no result exists
     * @throws RuntimeException if a database error occurs
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
            throw new RuntimeException("Failed to execute overview query", e);
        }
    }

    /**
     * Executes a query that returns a single integer value.
     *
     * <p>This method prepares the statement, binds any provided parameters,
     * executes the query, and returns the first column of the first row.
     * If no result is found, {@code 0} is returned.</p>
     *
     * @param sql the SQL query to execute
     * @param params optional query parameters to bind
     * @return the resulting integer value, or {@code 0} if no result exists
     * @throws RuntimeException if a database error occurs
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
            throw new RuntimeException("Failed to execute overview query", e);
        }
    }

    /**
     * Binds string parameters to a prepared statement in order.
     *
     * @param ps the prepared statement receiving the parameters
     * @param params the parameter values to bind
     * @throws SQLException if parameter binding fails
     */
    private void bindParams(PreparedStatement ps, String... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setString(i + 1, params[i]);
        }
    }

    /**
     * Retrieves the reporting date associated with overview KPI records.
     *
     * @return the reporting date string
     */
    private String getAsOfDate() {
        return REPORT_AS_OF;
    }

    /**
     * Represents a monthly revenue and expense trend record.
     *
     * @param month the month in {@code YYYY-MM} format
     * @param revenue the total revenue for the month
     * @param expenses the total expenses for the month
     */
    public record MonthlyTrendPoint(String month, double revenue, double expenses) {
    }
}