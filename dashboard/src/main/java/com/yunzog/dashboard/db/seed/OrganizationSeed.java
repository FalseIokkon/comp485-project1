package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OrganizationSeed {

    public static void seed(Connection conn) throws Exception {
        seedDepartments(conn);
        seedBranches(conn);
    }

    private static void seedDepartments(Connection conn) throws Exception {
        String sql = "INSERT INTO departments (name) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertDepartment(ps, "HR");
            insertDepartment(ps, "Finance");
            insertDepartment(ps, "Marketing");
            insertDepartment(ps, "Manufacturing");
            insertDepartment(ps, "Distributing");
            insertDepartment(ps, "Administration");
            insertDepartment(ps, "Sales");
        }
    }

    private static void seedBranches(Connection conn) throws Exception {
        String sql = "INSERT INTO branches (name, city, state) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertBranch(ps, "Scranton", "Scranton", "PA");
            insertBranch(ps, "Stamford", "Stamford", "CT");
            insertBranch(ps, "Utica", "Utica", "NY");
        }
    }

    private static void insertDepartment(PreparedStatement ps, String name) throws Exception {
        ps.setString(1, name);
        ps.executeUpdate();
    }

    private static void insertBranch(PreparedStatement ps, String name, String city, String state) throws Exception {
        ps.setString(1, name);
        ps.setString(2, city);
        ps.setString(3, state);
        ps.executeUpdate();
    }
}