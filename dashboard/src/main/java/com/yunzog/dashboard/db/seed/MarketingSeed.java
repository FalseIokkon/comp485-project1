package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class MarketingSeed {

    public static void seed(Connection conn) throws Exception {
        seedCampaigns(conn);
        seedLeads(conn);
    }

    private static void seedCampaigns(Connection conn) throws Exception {
        String sql = """
            INSERT INTO campaigns
            (name, channel, budget, spend, start_date, end_date, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertCampaign(ps, "Winter Office Restock", "Email", 2500.00, 2200.00, "2026-01-01", "2026-01-31", "COMPLETED");
            insertCampaign(ps, "Northeast Print Promo", "Print", 3200.00, 3000.00, "2026-02-01", "2026-02-28", "COMPLETED");
            insertCampaign(ps, "Spring Business Outreach", "Web", 4000.00, 1800.00, "2026-03-01", "2026-03-31", "ACTIVE");
            insertCampaign(ps, "Referral Growth Push", "Referral", 1500.00, 900.00, "2026-03-05", null, "ACTIVE");
        }
    }

    private static void seedLeads(Connection conn) throws Exception {
        String sql = """
            INSERT INTO leads
            (campaign_id, lead_date, source, status, customer_id)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            insertLead(ps, 1, "2026-01-08", "Email", "CONVERTED", 1);
            insertLead(ps, 1, "2026-01-14", "Email", "QUALIFIED", null);
            insertLead(ps, 2, "2026-02-07", "Print", "CONVERTED", 2);
            insertLead(ps, 2, "2026-02-18", "Print", "LOST", null);
            insertLead(ps, 3, "2026-03-04", "Web", "NEW", null);
            insertLead(ps, 3, "2026-03-06", "Web", "CONTACTED", null);
            insertLead(ps, 4, "2026-03-10", "Referral", "CONVERTED", 5);
        }
    }

    private static void insertCampaign(
            PreparedStatement ps,
            String name,
            String channel,
            double budget,
            double spend,
            String startDate,
            String endDate,
            String status
    ) throws Exception {
        ps.setString(1, name);
        ps.setString(2, channel);
        ps.setDouble(3, budget);
        ps.setDouble(4, spend);
        ps.setString(5, startDate);
        ps.setString(6, endDate);
        ps.setString(7, status);
        ps.executeUpdate();
    }

    private static void insertLead(
            PreparedStatement ps,
            int campaignId,
            String leadDate,
            String source,
            String status,
            Integer customerId
    ) throws Exception {
        ps.setInt(1, campaignId);
        ps.setString(2, leadDate);
        ps.setString(3, source);
        ps.setString(4, status);

        if (customerId == null) {
            ps.setNull(5, java.sql.Types.INTEGER);
        } else {
            ps.setInt(5, customerId);
        }

        ps.executeUpdate();
    }
}