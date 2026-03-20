package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class HRSeed {

    public static void seed(Connection conn) throws Exception {
        seedEmployees(conn);
        seedJobOpenings(conn);
    }

    private static void seedEmployees(Connection conn) throws Exception {
        String sql = """
            INSERT INTO employees
            (first_name, last_name, email, job_title, department_id, branch_id, hire_date, employment_status, performance_rating, attendance_rate, salary)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertEmployee(ps, "Michael", "Scott", "michael@yunzog.com", "Regional Manager", 7, 1, "2021-04-10", "ACTIVE", 3.8, 96.0, 78000);
            insertEmployee(ps, "Pam", "Beesly", "pam@yunzog.com", "Receptionist", 6, 1, "2022-01-15", "ACTIVE", 4.2, 98.5, 42000);
            insertEmployee(ps, "Jim", "Halpert", "jim@yunzog.com", "Sales Representative", 7, 1, "2021-07-01", "ACTIVE", 4.4, 97.2, 61000);
            insertEmployee(ps, "Dwight", "Schrute", "dwight@yunzog.com", "Sales Representative", 7, 1, "2020-03-20", "ACTIVE", 4.7, 99.0, 64000);
            insertEmployee(ps, "Angela", "Martin", "angela@yunzog.com", "Accountant", 2, 1, "2021-09-12", "ACTIVE", 4.3, 97.8, 59000);
        }
    }

    private static void seedJobOpenings(Connection conn) throws Exception {
        String sql = """
            INSERT INTO job_openings
            (title, department_id, branch_id, posted_date, status)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertOpening(ps, "HR Coordinator", 1, 1, "2026-03-01", "OPEN");
            insertOpening(ps, "Warehouse Associate", 5, 1, "2026-03-05", "OPEN");
            insertOpening(ps, "Marketing Analyst", 3, 2, "2026-02-20", "FILLED");
        }
    }

    private static void insertEmployee(
            PreparedStatement ps,
            String firstName,
            String lastName,
            String email,
            String jobTitle,
            int departmentId,
            int branchId,
            String hireDate,
            String status,
            double performanceRating,
            double attendanceRate,
            double salary
    ) throws Exception {
        ps.setString(1, firstName);
        ps.setString(2, lastName);
        ps.setString(3, email);
        ps.setString(4, jobTitle);
        ps.setInt(5, departmentId);
        ps.setInt(6, branchId);
        ps.setString(7, hireDate);
        ps.setString(8, status);
        ps.setDouble(9, performanceRating);
        ps.setDouble(10, attendanceRate);
        ps.setDouble(11, salary);
        ps.executeUpdate();
    }

    private static void insertOpening(
            PreparedStatement ps,
            String title,
            int departmentId,
            int branchId,
            String postedDate,
            String status
    ) throws Exception {
        ps.setString(1, title);
        ps.setInt(2, departmentId);
        ps.setInt(3, branchId);
        ps.setString(4, postedDate);
        ps.setString(5, status);
        ps.executeUpdate();
    }
}