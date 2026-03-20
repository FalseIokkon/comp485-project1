package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OperationsSeed {

    public static void seed(Connection conn) throws Exception {
        seedProducts(conn);
        seedProductionBatches(conn);
    }

    private static void seedProducts(Connection conn) throws Exception {
        String sql = """
            INSERT INTO products
            (sku, name, category, unit_price, unit_cost, is_active)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertProduct(ps, "PAPER-A4-01", "Standard Copy Paper", "Copy Paper", 8.99, 4.20, 1);
            insertProduct(ps, "PAPER-LTR-02", "Premium Letter Paper", "Office Paper", 11.49, 5.10, 1);
            insertProduct(ps, "CARD-001", "White Cardstock Pack", "Cardstock", 14.99, 7.25, 1);
            insertProduct(ps, "ENV-010", "Business Envelope Box", "Supplies", 6.49, 2.90, 1);
        }
    }

    private static void seedProductionBatches(Connection conn) throws Exception {
        String sql = """
            INSERT INTO production_batches
            (product_id, scheduled_date, completed_date, units_planned, units_produced, defect_count, machine_uptime, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertBatch(ps, 1, "2026-03-01", "2026-03-01", 5000, 4920, 35, 97.5, "COMPLETED");
            insertBatch(ps, 2, "2026-03-03", "2026-03-03", 3500, 3440, 22, 96.8, "COMPLETED");
            insertBatch(ps, 3, "2026-03-05", null, 2800, 1200, 12, 94.3, "IN_PROGRESS");
            insertBatch(ps, 4, "2026-03-07", "2026-03-08", 2000, 1910, 18, 92.7, "DELAYED");
            insertBatch(ps, 1, "2026-03-10", null, 5200, 0, 0, 100.0, "PLANNED");
        }
    }

    private static void insertProduct(
            PreparedStatement ps,
            String sku,
            String name,
            String category,
            double unitPrice,
            double unitCost,
            int isActive
    ) throws Exception {
        ps.setString(1, sku);
        ps.setString(2, name);
        ps.setString(3, category);
        ps.setDouble(4, unitPrice);
        ps.setDouble(5, unitCost);
        ps.setInt(6, isActive);
        ps.executeUpdate();
    }

    private static void insertBatch(
            PreparedStatement ps,
            int productId,
            String scheduledDate,
            String completedDate,
            int unitsPlanned,
            int unitsProduced,
            int defectCount,
            double machineUptime,
            String status
    ) throws Exception {
        ps.setInt(1, productId);
        ps.setString(2, scheduledDate);
        ps.setString(3, completedDate);
        ps.setInt(4, unitsPlanned);
        ps.setInt(5, unitsProduced);
        ps.setInt(6, defectCount);
        ps.setDouble(7, machineUptime);
        ps.setString(8, status);
        ps.executeUpdate();
    }
}