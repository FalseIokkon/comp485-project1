package com.yunzog.dashboard.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    private static final String DB_FILE = "yunzog.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    public static Connection connect() throws SQLException {
        System.out.println("DB path: " + new java.io.File(DB_FILE).getAbsolutePath());
        return DriverManager.getConnection(JDBC_URL);
    }
}