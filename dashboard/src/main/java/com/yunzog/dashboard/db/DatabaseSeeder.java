package com.yunzog.dashboard.db;

import com.yunzog.dashboard.db.seed.FinanceSeed;
import com.yunzog.dashboard.db.seed.HRSeed;
import com.yunzog.dashboard.db.seed.LogisticsSeed;
import com.yunzog.dashboard.db.seed.MarketingSeed;
import com.yunzog.dashboard.db.seed.OperationsSeed;
import com.yunzog.dashboard.db.seed.OrganizationSeed;

import java.sql.Connection;

public class DatabaseSeeder {

    public static void seed(Connection conn) {
        try {
            conn.setAutoCommit(false);

            OrganizationSeed.seed(conn);
            HRSeed.seed(conn);
            LogisticsSeed.seed(conn);
            MarketingSeed.seed(conn);
            OperationsSeed.seed(conn);
            FinanceSeed.seed(conn);

            conn.commit();
            System.out.println("Database seeded successfully.");

        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Database seeding failed", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }
    }
}