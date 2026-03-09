package com.yunzog.dashboard.dao;

import com.yunzog.dashboard.db.DB;
import com.yunzog.dashboard.model.DivisionKPI;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DivisionDAO {

    public List<DivisionKPI> getAllKpis() {
        List<DivisionKPI> out = new ArrayList<>();

        try (Connection conn = DB.connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("""
                 SELECT division, metric, value, as_of
                 FROM division_kpi
                 ORDER BY division, metric
             """)) {

            while (rs.next()) {
                out.add(new DivisionKPI(
                        rs.getString("division"),
                        rs.getString("metric"),
                        rs.getDouble("value"),
                        rs.getString("as_of")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Query failed", e);
        }

        return out;
    }
}