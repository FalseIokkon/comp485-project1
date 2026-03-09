package com.yunzog.dashboard.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class DB {
    private static final String DB_FILE = "yunzog.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    public static void init() {
        boolean firstRun = !Files.exists(Path.of(DB_FILE));

        try (Connection conn = connect()) {
            try (Statement st = conn.createStatement()) {
                st.execute("""
                    CREATE TABLE IF NOT EXISTS division_kpi (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        division TEXT NOT NULL,
                        metric TEXT NOT NULL,
                        value REAL NOT NULL,
                        as_of TEXT NOT NULL
                    )
                """);
            }

            if (firstRun) seed(conn);

        } catch (Exception e) {
            throw new RuntimeException("DB init failed", e);
        }
    }

    private static void seed(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO division_kpi (division, metric, value, as_of)
            VALUES (?, ?, ?, date('now'))
        """)) {
            insert(ps, "HR", "Open Positions", 12);
            insert(ps, "Finance", "Monthly Revenue (M)", 4.8);
            insert(ps, "Administration", "Tickets Backlog", 31);
            insert(ps, "Marketing", "Leads This Month", 420);
            insert(ps, "Manufacturing", "Units Produced", 18500);
            insert(ps, "Distributing", "On-time Delivery %", 96.2);
        }
    }

    private static void insert(PreparedStatement ps, String division, String metric, double value) throws SQLException {
        ps.setString(1, division);
        ps.setString(2, metric);
        ps.setDouble(3, value);
        ps.executeUpdate();
    }
}