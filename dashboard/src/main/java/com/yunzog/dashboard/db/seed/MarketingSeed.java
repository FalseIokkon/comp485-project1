package com.yunzog.dashboard.db.seed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MarketingSeed {

    private static final LocalDate START = LocalDate.of(2025, 1, 1);
    private static final LocalDate END = LocalDate.of(2025, 12, 31);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
            insertCampaign(ps, "Winter Office Restock", "Email", 3000.00, 2850.00, "2025-01-01", "2025-01-31", "COMPLETED");
            insertCampaign(ps, "Spring Northeast Print Push", "Print", 4200.00, 3980.00, "2025-03-01", "2025-04-15", "COMPLETED");
            insertCampaign(ps, "Summer Web Lead Funnel", "Web", 5000.00, 4725.00, "2025-06-01", "2025-07-31", "COMPLETED");
            insertCampaign(ps, "Fall Referral Growth", "Referral", 2200.00, 2050.00, "2025-09-01", "2025-10-31", "COMPLETED");
            insertCampaign(ps, "Year-End Business Outreach", "Web", 4500.00, 2600.00, "2025-12-01", "2025-12-31", "ACTIVE");
            insertCampaign(ps, "Holiday Email Retargeting", "Email", 1800.00, 1100.00, "2025-12-10", null, "ACTIVE");
        }
    }

    private static void seedLeads(Connection conn) throws Exception {
        String sql = """
            INSERT INTO leads
            (campaign_id, lead_date, source, status, customer_id)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int totalRows = 30;
            long totalDays = END.toEpochDay() - START.toEpochDay();

            for (int i = 0; i < totalRows; i++) {
                long offsetDays = (i * totalDays) / (totalRows - 1);
                LocalDate leadDate = START.plusDays(offsetDays);

                int campaignId = switch (leadDate.getMonthValue()) {
                    case 1, 2 -> 1;
                    case 3, 4, 5 -> 2;
                    case 6, 7, 8 -> 3;
                    case 9, 10, 11 -> 4;
                    default -> (i % 2 == 0 ? 5 : 6);
                };

                String source = switch (campaignId) {
                    case 1, 6 -> "Email";
                    case 2 -> "Print";
                    case 3, 5 -> "Web";
                    default -> "Referral";
                };

                String status;
                Integer customerId = null;

                switch (i % 5) {
                    case 0 -> {
                        status = "CONVERTED";
                        customerId = ((i / 5) % 5) + 1;
                    }
                    case 1 -> status = "QUALIFIED";
                    case 2 -> status = "CONTACTED";
                    case 3 -> status = "NEW";
                    default -> status = "LOST";
                }

                insertLead(
                        ps,
                        campaignId,
                        leadDate.format(FMT),
                        source,
                        status,
                        customerId
                );
            }

            // extra December leads so REPORT_MONTH=2025-12 looks active
            insertLead(ps, 5, "2025-12-05", "Web", "NEW", null);
            insertLead(ps, 5, "2025-12-12", "Web", "CONTACTED", null);
            insertLead(ps, 6, "2025-12-18", "Email", "QUALIFIED", null);
            insertLead(ps, 6, "2025-12-22", "Email", "CONVERTED", 5);
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