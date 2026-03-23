package com.yunzog.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.yunzog.dashboard.db.DB;

public class ManufacturingDao {

    public int getTotalProducts() {
        String sql = "SELECT COUNT(*) FROM products WHERE is_active = 1";
        return getIntValue(sql);
    }

    public int getBatchesScheduled() {
        String sql = "SELECT COUNT(*) FROM production_batches";
        return getIntValue(sql);
    }

    public int getUnitsProduced() {
        String sql = "SELECT COALESCE(SUM(units_produced),0) FROM production_batches";
        return getIntValue(sql);
    }

    public int getDefectCount() {
        String sql = "SELECT COALESCE(SUM(defect_count),0) FROM production_batches";
        return getIntValue(sql);
    }

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