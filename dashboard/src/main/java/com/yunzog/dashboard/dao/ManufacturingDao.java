package com.yunzog.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.yunzog.dashboard.db.DB;

/**
 * Data access object for manufacturing-related data.
 *
 * <p>This class provides methods to retrieve production metrics and batch
 * information from the database. It supports queries for product counts,
 * production activity, defect tracking, and detailed batch records.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class ManufacturingDao {

    /**
     * Retrieves the total number of active products.
     *
     * @return the count of active products
     */
    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = 1";
        return getIntValue(sql);
    }

    /**
     * Retrieves the total number of scheduled production batches.
     *
     * @return the count of production batches
     */
    public int getBatchesScheduled() {
        String sql = "SELECT COUNT(*) FROM production_batches";
        return getIntValue(sql);
    }

    /**
     * Retrieves the total number of units produced across all batches.
     *
     * @return the sum of units produced
     */
    public int getUnitsProduced() {
        String sql = "SELECT COALESCE(SUM(units_produced),0) FROM production_batches";
        return getIntValue(sql);
    }

    /**
     * Retrieves the total number of defective units across all batches.
     *
     * @return the sum of defect counts
     */
    public int getDefectCount() {
        String sql = "SELECT COALESCE(SUM(defect_count),0) FROM production_batches";
        return getIntValue(sql);
    }

    /**
     * Retrieves a list of production batch records with associated product details.
     *
     * <p>This method joins production batches with product data to provide
     * a complete view of each batch, including scheduling, output, quality,
     * and operational metrics.</p>
     *
     * @return a list of {@link ProductionBatchRow} records
     */
    public List<ProductionBatchRow> getProductionBatches() {
        String sql = """
                    SELECT pb.*, p.name AS product_name
                    FROM production_batches pb
                    JOIN products p ON pb.product_id = p.product_id
                    ORDER BY scheduled_date
                """;

        List<ProductionBatchRow> rows = new ArrayList<>();

        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new ProductionBatchRow(
                        rs.getString("product_name"),
                        rs.getString("scheduled_date"),
                        rs.getString("completed_date"),
                        rs.getInt("units_planned"),
                        rs.getInt("units_produced"),
                        rs.getInt("defect_count"),
                        rs.getDouble("machine_uptime"),
                        rs.getString("status")));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load production batches", e);
        }

        return rows;
    }

    /**
     * Executes a SQL query that returns a single integer value.
     *
     * @param sql the SQL query to execute
     * @return the integer result of the query, or 0 if no result is found
     * @throws RuntimeException if a database error occurs
     */
    private int getIntValue(String sql) {
        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next())
                return rs.getInt(1);
            return 0;

        } catch (Exception e) {
            throw new RuntimeException("Query failed", e);
        }
    }

    /**
     * Represents a production batch record with product and operational details.
     *
     * @param productName the name of the product being manufactured
     * @param scheduledDate the scheduled production date
     * @param completedDate the actual completion date
     * @param unitsPlanned the number of units planned for production
     * @param unitsProduced the number of units actually produced
     * @param defectCount the number of defective units produced
     * @param machineUptime the machine uptime percentage during production
     * @param status the current status of the production batch
     */
    public record ProductionBatchRow(
            String productName,
            String scheduledDate,
            String completedDate,
            int unitsPlanned,
            int unitsProduced,
            int defectCount,
            double machineUptime,
            String status) {
    }
}