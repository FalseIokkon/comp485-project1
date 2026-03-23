package com.yunzog.dashboard.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.yunzog.dashboard.db.DB;

public class DistributionDao {

    public int getTotalOrders() {
        return getInt("SELECT COUNT(*) FROM orders");
    }

    public int getTotalShipments() {
        return getInt("SELECT COUNT(*) FROM shipments");
    }

    public int getDelivered() {
        return getInt("SELECT COUNT(*) FROM shipments WHERE delivery_status = 'DELIVERED'");
    }

    public double getOnTimeRate() {
        return getDouble("SELECT AVG(on_time) * 100 FROM shipments");
    }

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

    private int getInt(String sql) {
        try (Connection c = DB.connect();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private double getDouble(String sql) {
        try (Connection c = DB.connect();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public record ShipmentRow(
            String customer,
            String city,
            String state,
            String status,
            String shipDate,
            String deliveryDate) {
    }
}