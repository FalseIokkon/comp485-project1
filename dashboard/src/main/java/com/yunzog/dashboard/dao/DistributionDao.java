package com.yunzog.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.yunzog.dashboard.db.DB;

/**
 * Data access object for distribution-related data.
 *
 * <p>This class provides methods to retrieve shipment and order metrics
 * from the database, including totals, delivery performance, and detailed
 * shipment records. It uses JDBC to execute SQL queries and map results
 * into domain objects.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class DistributionDao {

    /**
     * Retrieves the total number of orders.
     *
     * @return the total count of orders in the database
     */
    public int getTotalOrders() {
        return getInt("SELECT COUNT(*) FROM orders");
    }

    /**
     * Retrieves the total number of shipments.
     *
     * @return the total count of shipments in the database
     */
    public int getTotalShipments() {
        return getInt("SELECT COUNT(*) FROM shipments");
    }

    /**
     * Retrieves the number of delivered shipments.
     *
     * @return the count of shipments with a delivery status of DELIVERED
     */
    public int getDelivered() {
        return getInt("SELECT COUNT(*) FROM shipments WHERE delivery_status = 'DELIVERED'");
    }

    /**
     * Calculates the percentage of shipments delivered on time.
     *
     * <p>This value is computed as the average of the {@code on_time} field
     * multiplied by 100.</p>
     *
     * @return the on-time delivery rate as a percentage
     */
    public double getOnTimeRate() {
        return getDouble("SELECT AVG(on_time) * 100 FROM shipments");
    }

    /**
     * Retrieves a list of shipment records with associated customer and
     * destination details.
     *
     * <p>This method joins the shipments, orders, and customers tables to
     * produce a complete view of shipment data.</p>
     *
     * @return a list of {@link ShipmentRow} records representing shipments
     */
    public List<ShipmentRow> getShipments() {
        String sql = """
                    SELECT c.company_name, s.destination_city, s.destination_state,
                           s.delivery_status, s.ship_date, s.delivery_date
                    FROM shipments s
                    JOIN orders o ON s.order_id = o.order_id
                    JOIN customers c ON o.customer_id = c.customer_id
                """;

        List<ShipmentRow> list = new ArrayList<>();

        try (Connection conn = DB.connect();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ShipmentRow(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6)));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /**
     * Executes a SQL query that returns a single integer value.
     *
     * @param sql the SQL query to execute
     * @return the integer result of the query, or 0 if no result is found
     * @throws RuntimeException if a database error occurs
     */
    private int getInt(String sql) {
        try (Connection c = DB.connect();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a SQL query that returns a single double value.
     *
     * @param sql the SQL query to execute
     * @return the double result of the query, or 0 if no result is found
     * @throws RuntimeException if a database error occurs
     */
    private double getDouble(String sql) {
        try (Connection c = DB.connect();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Represents a shipment record with customer and delivery details.
     *
     * @param customer the customer name
     * @param city the destination city
     * @param state the destination state
     * @param status the delivery status
     * @param shipDate the shipment date
     * @param deliveryDate the delivery date
     */
    public record ShipmentRow(
            String customer,
            String city,
            String state,
            String status,
            String shipDate,
            String deliveryDate) {
    }
}