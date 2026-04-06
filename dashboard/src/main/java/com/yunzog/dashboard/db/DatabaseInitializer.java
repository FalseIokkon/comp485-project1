package com.yunzog.dashboard.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Handles initial database setup for the application.
 *
 * <p>This class loads and executes the database schema, enables required
 * database settings, and seeds initial data if the database is empty.</p>
 *
 * @author Yun, Jonathan
 * @author Zoghlami, Amin
 */
public class DatabaseInitializer {

    private static final String SCHEMA_PATH = "/com/yunzog/dashboard/schema.sql";

    public static void initialize() {
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            InputStream is = DatabaseInitializer.class.getResourceAsStream(SCHEMA_PATH);

            if (is == null) {
                throw new RuntimeException("schema.sql not found at: " + SCHEMA_PATH);
            }

            String sql = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8)
            ).lines().collect(Collectors.joining("\n"));

            String[] statements = sql.split(";");

            for (String statement : statements) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

            if (isDatabaseEmpty(conn)) {
                DatabaseSeeder.seed(conn);
            }

            System.out.println("Database initialized successfully.");

        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    private static boolean isDatabaseEmpty(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM departments";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() && rs.getInt(1) == 0;
        }
    }
}