package edu.univ.erp.service;

import edu.univ.erp.data.DbConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SettingsService {

    public boolean isMaintenanceModeOn() {
        String sql = "SELECT setting_value FROM erp_db.settings WHERE setting_key = 'maintenance_on'";
        try (Connection conn = DbConnector.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return "true".equalsIgnoreCase(rs.getString("setting_value"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String setMaintenanceMode(boolean isEnabled) {
        String sql = "UPDATE erp_db.settings SET setting_value = ? WHERE setting_key = 'maintenance_on'";
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, isEnabled ? "true" : "false");
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return "Success! Maintenance mode is now " + (isEnabled ? "ON" : "OFF");
            } else {
                return "Error: Could not find maintenance mode setting.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}