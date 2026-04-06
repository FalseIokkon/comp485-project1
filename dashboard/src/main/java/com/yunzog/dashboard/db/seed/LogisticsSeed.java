package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Seeds logistics and distribution data including customers, orders,
 * and shipment records across the application reporting period.
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class LogisticsSeed {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END = LocalDate.of(2025, 12, 31);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            int totalRows = 36;
            long totalDays = END.toEpochDay() - START.toEpochDay();

            for (int i = 0; i < totalRows; i++) {
                long offsetDays = (i * totalDays) / (totalRows - 1);
                LocalDate orderDate = START.plusDays(offsetDays);

                int customerId = (i % 5) + 1;
                double totalAmount = 2400 + (i * 190);

                String status;
                if (orderDate.isBefore(LocalDate.of(2025, 10, 1))) {
                    status = "DELIVERED";
                } else if (orderDate.isBefore(LocalDate.of(2025, 11, 15))) {
                    status = "SHIPPED";
                } else {
                    status = (i % 2 == 0) ? "APPROVED" : "DELIVERED";
                }

                insertOrder(
                        ps,
                        customerId,
                        orderDate.format(FMT),
                        totalAmount,
                        status
                );
            }
        }
    }

    private static void seedShipments(Connection conn) throws Exception {
        String sql = """
            INSERT INTO shipments
            (order_id, destination_city, destination_state, ship_date, delivery_date, delivery_status, on_time, delivery_days)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String[][] destinations = {
                {"Scranton", "PA"},
                {"Wilkes-Barre", "PA"},
                {"Scranton", "PA"},
                {"Stamford", "CT"},
                {"Utica", "NY"}
            };

            int totalRows = 36;
            long totalDays = END.toEpochDay() - START.toEpochDay();

            for (int i = 0; i < totalRows; i++) {
                int orderId = i + 1;

                long offsetDays = (i * totalDays) / (totalRows - 1);
                LocalDate orderDate = START.plusDays(offsetDays);
                LocalDate shipDate = orderDate.plusDays(1);

                String city = destinations[i % destinations.length][0];
                String state = destinations[i % destinations.length][1];

                String deliveryStatus;
                Integer deliveryDays;
                String deliveryDate;
                int onTime;

                if (orderDate.isBefore(LocalDate.of(2025, 10, 1))) {
                    deliveryDays = 2 + (i % 3);
                    deliveryDate = shipDate.plusDays(deliveryDays).format(FMT);
                    deliveryStatus = (i % 6 == 0) ? "DELAYED" : "DELIVERED";
                    onTime = deliveryStatus.equals("DELIVERED") ? 1 : 0;
                } else if (orderDate.isBefore(LocalDate.of(2025, 11, 15))) {
                    deliveryDays = null;
                    deliveryDate = null;
                    deliveryStatus = "IN_TRANSIT";
                    onTime = 1;
                } else {
                    if (i % 2 == 0) {
                        deliveryDays = null;
                        deliveryDate = null;
                        deliveryStatus = "READY";
                        onTime = 1;
                    } else {
                        deliveryDays = 4;
                        deliveryDate = shipDate.plusDays(deliveryDays).format(FMT);
                        deliveryStatus = "DELIVERED";
                        onTime = 1;
                    }
                }

                insertShipment(
                        ps,
                        orderId,
                        city,
                        state,
                        shipDate.format(FMT),
                        deliveryDate,
                        deliveryStatus,
                        onTime,
                        deliveryDays
                );
            }
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