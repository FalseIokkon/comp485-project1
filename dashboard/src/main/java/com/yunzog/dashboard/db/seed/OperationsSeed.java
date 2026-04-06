package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
/**
 * Seeds operations and manufacturing data including product records
 * and production batch activity for the application reporting period.
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class OperationsSeed {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END = LocalDate.of(2025, 12, 31);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            int totalRows = 24;
            long totalDays = END.toEpochDay() - START.toEpochDay();

            for (int i = 0; i < totalRows; i++) {
                long offsetDays = (i * totalDays) / (totalRows - 1);
                LocalDate scheduledDate = START.plusDays(offsetDays);

                int productId = (i % 4) + 1;
                int unitsPlanned = 3200 + (i * 180);
                int unitsProduced;
                int defectCount;
                double machineUptime;
                String status;
                String completedDate;

                if (scheduledDate.isBefore(LocalDate.of(2025, 11, 15))) {
                    status = (i % 7 == 0) ? "DELAYED" : "COMPLETED";
                    defectCount = 18 + (i % 20);
                    unitsProduced = unitsPlanned - (50 + (i % 60));
                    machineUptime = 93.5 + (i % 6);

                    if ("COMPLETED".equals(status)) {
                        completedDate = scheduledDate.plusDays(1 + (i % 2)).format(FMT);
                    } else {
                        completedDate = scheduledDate.plusDays(3).format(FMT);
                    }
                } else if (scheduledDate.isBefore(LocalDate.of(2025, 12, 20))) {
                    status = "IN_PROGRESS";
                    defectCount = 8 + (i % 10);
                    unitsProduced = unitsPlanned / 2;
                    machineUptime = 95.0 + (i % 4);
                    completedDate = null;
                } else {
                    status = "PLANNED";
                    defectCount = 0;
                    unitsProduced = 0;
                    machineUptime = 100.0;
                    completedDate = null;
                }

                insertBatch(
                        ps,
                        productId,
                        scheduledDate.format(FMT),
                        completedDate,
                        unitsPlanned,
                        unitsProduced,
                        defectCount,
                        machineUptime,
                        status
                );
            }

            // extra completed December batches so REPORT_MONTH=2025-12 has meaningful units
            insertBatch(ps, 1, "2025-12-03", "2025-12-04", 5200, 5080, 26, 97.8, "COMPLETED");
            insertBatch(ps, 2, "2025-12-10", "2025-12-11", 4300, 4215, 21, 96.9, "COMPLETED");
            insertBatch(ps, 3, "2025-12-17", null, 3900, 1800, 10, 95.4, "IN_PROGRESS");
            insertBatch(ps, 4, "2025-12-27", null, 3600, 0, 0, 100.0, "PLANNED");
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