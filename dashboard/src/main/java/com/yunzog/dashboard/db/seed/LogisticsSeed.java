package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class LogisticsSeed {

    public static void seed(Connection conn) throws Exception {
        seedCustomers(conn);
        seedOrders(conn);
        seedShipments(conn);
    }

    private static void seedCustomers(Connection conn) throws Exception {
        String sql = """
            INSERT INTO customers
            (company_name, contact_name, city, state, status)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertCustomer(ps, "Dunder Valley School District", "Alice Monroe", "Scranton", "PA", "ACTIVE");
            insertCustomer(ps, "Penn Paper Supply Co.", "Robert James", "Wilkes-Barre", "PA", "ACTIVE");
            insertCustomer(ps, "Lackawanna Legal Group", "Sandra Miles", "Scranton", "PA", "ACTIVE");
            insertCustomer(ps, "Hudson Office Retail", "Ethan Cole", "Stamford", "CT", "PROSPECT");
            insertCustomer(ps, "Utica Business Solutions", "Maya Turner", "Utica", "NY", "ACTIVE");
        }
    }

    private static void seedOrders(Connection conn) throws Exception {
        String sql = """
            INSERT INTO orders
            (customer_id, order_date, total_amount, status)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertOrder(ps, 1, "2026-01-06", 4200.00, "DELIVERED");
            insertOrder(ps, 2, "2026-01-13", 3100.00, "DELIVERED");
            insertOrder(ps, 3, "2026-02-02", 5800.00, "SHIPPED");
            insertOrder(ps, 4, "2026-02-16", 2600.00, "DELIVERED");
            insertOrder(ps, 5, "2026-03-01", 4900.00, "APPROVED");
        }
    }

    private static void seedShipments(Connection conn) throws Exception {
        String sql = """
            INSERT INTO shipments
            (order_id, destination_city, destination_state, ship_date, delivery_date, delivery_status, on_time, delivery_days)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertShipment(ps, 1, "Scranton", "PA", "2026-01-07", "2026-01-09", "DELIVERED", 1, 2);
            insertShipment(ps, 2, "Wilkes-Barre", "PA", "2026-01-14", "2026-01-17", "DELIVERED", 1, 3);
            insertShipment(ps, 3, "Scranton", "PA", "2026-02-03", null, "IN_TRANSIT", 1, null);
            insertShipment(ps, 4, "Stamford", "CT", "2026-02-17", "2026-02-22", "DELAYED", 0, 5);
            insertShipment(ps, 5, "Utica", "NY", "2026-03-03", null, "READY", 1, null);
        }
    }

    private static void insertCustomer(
            PreparedStatement ps,
            String companyName,
            String contactName,
            String city,
            String state,
            String status
    ) throws Exception {
        ps.setString(1, companyName);
        ps.setString(2, contactName);
        ps.setString(3, city);
        ps.setString(4, state);
        ps.setString(5, status);
        ps.executeUpdate();
    }

    private static void insertOrder(
            PreparedStatement ps,
            int customerId,
            String orderDate,
            double totalAmount,
            String status
    ) throws Exception {
        ps.setInt(1, customerId);
        ps.setString(2, orderDate);
        ps.setDouble(3, totalAmount);
        ps.setString(4, status);
        ps.executeUpdate();
    }

    private static void insertShipment(
            PreparedStatement ps,
            int orderId,
            String destinationCity,
            String destinationState,
            String shipDate,
            String deliveryDate,
            String deliveryStatus,
            int onTime,
            Integer deliveryDays
    ) throws Exception {
        ps.setInt(1, orderId);
        ps.setString(2, destinationCity);
        ps.setString(3, destinationState);
        ps.setString(4, shipDate);
        ps.setString(5, deliveryDate);
        ps.setString(6, deliveryStatus);
        ps.setInt(7, onTime);

        if (deliveryDays == null) {
            ps.setNull(8, java.sql.Types.INTEGER);
        } else {
            ps.setInt(8, deliveryDays);
        }

        ps.executeUpdate();
    }
}