package edu.univ.erp.auth;

import edu.univ.erp.data.DbConnector;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public LoginResult login(String username, String password) {
        String sql = "SELECT user_id, role, password_hash, status, failed_attempts, lockout_until FROM users_auth WHERE username = ?";

        try (Connection conn = DbConnector.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String storedHash = rs.getString("password_hash");
                    String status = rs.getString("status");
                    int failedAttempts = rs.getInt("failed_attempts");
                    java.sql.Timestamp lockoutUntil = rs.getTimestamp("lockout_until");

                    if ("Locked".equals(status)) {
                        return new LoginResult(false, "This account is locked.", 0, null);
                    }

                    if (lockoutUntil != null) {
                        long currentTime = System.currentTimeMillis();
                        long lockoutTime = lockoutUntil.getTime();
                        
                        if (currentTime < lockoutTime) {
                            long remainingSeconds = (lockoutTime - currentTime) / 1000;
                            return new LoginResult(false, 
                                "Account temporarily locked due to too many failed attempts. Please try again in " + 
                                remainingSeconds + " seconds.", 0, null);
                        } else {
                            resetLockout(conn, userId);
                            failedAttempts = 0;
                        }
                    }

                    if (BCrypt.checkpw(password, storedHash)) {
                        String role = rs.getString("role");

                        resetLockout(conn, userId);

                        UserSession.getInstance().startSession(userId, username, role);

                        return new LoginResult(true, "Login successful!", userId, role);
                    } else {
                        failedAttempts++;
                        
                        if (failedAttempts >= 5) {
                            java.sql.Timestamp lockoutTime = new java.sql.Timestamp(
                                System.currentTimeMillis() + (60 * 1000));
                            
                            String lockSql = "UPDATE users_auth SET failed_attempts = 0, lockout_until = ? WHERE user_id = ?";
                            try (PreparedStatement lockPstmt = conn.prepareStatement(lockSql)) {
                                lockPstmt.setTimestamp(1, lockoutTime);
                                lockPstmt.setInt(2, userId);
                                lockPstmt.executeUpdate();
                            }
                            
                            return new LoginResult(false, 
                                "Too many failed login attempts. Account locked for 1 minute.", 0, null);
                        } else {
                            String updateSql = "UPDATE users_auth SET failed_attempts = ? WHERE user_id = ?";
                            try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                                updatePstmt.setInt(1, failedAttempts);
                                updatePstmt.setInt(2, userId);
                                updatePstmt.executeUpdate();
                            }
                            
                            int remainingAttempts = 5 - failedAttempts;
                            return new LoginResult(false, 
                                "Incorrect username or password. " + remainingAttempts + " attempt(s) remaining.", 0, null);
                        }
                    }
                } else {
                    return new LoginResult(false, "Incorrect username or password.", 0, null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new LoginResult(false, "Database error. Please try again later.", 0, null);
        }
    }
    
    private void resetLockout(Connection conn, int userId) throws SQLException {
        String resetSql = "UPDATE users_auth SET failed_attempts = 0, lockout_until = NULL WHERE user_id = ?";
        try (PreparedStatement resetPstmt = conn.prepareStatement(resetSql)) {
            resetPstmt.setInt(1, userId);
            resetPstmt.executeUpdate();
        }
    }
    
    public String changePassword(int userId, String oldPassword, String newPassword) {
        String verifySql = "SELECT password_hash FROM users_auth WHERE user_id = ?";
        
        try (Connection conn = DbConnector.getAuthConnection();
             PreparedStatement verifyPstmt = conn.prepareStatement(verifySql)) {
            
            verifyPstmt.setInt(1, userId);
            try (ResultSet rs = verifyPstmt.executeQuery()) {
                if (!rs.next()) {
                    return "Error: User not found.";
                }
                
                String storedHash = rs.getString("password_hash");
                
                if (!BCrypt.checkpw(oldPassword, storedHash)) {
                    return "Error: Old password is incorrect.";
                }
                
                conn.setAutoCommit(false);
                
                try {
                    String historySql = "INSERT INTO auth_db.password_history (user_id, password, changed_at) VALUES (?, ?, NOW())";
                    try (PreparedStatement historyPstmt = conn.prepareStatement(historySql)) {
                        historyPstmt.setInt(1, userId);
                        historyPstmt.setString(2, oldPassword);
                        historyPstmt.executeUpdate();
                    }
                    
                    String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                    String updateSql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.setString(1, newHash);
                        updatePstmt.setInt(2, userId);
                        updatePstmt.executeUpdate();
                    }
                    
                    conn.commit();
                    return "Success! Password changed successfully.";
                    
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("password_history")) {
                return "Error: Password history table not found. Please create it first.";
            }
            return "Error: Database error during password change.";
        }
    }
    
    public java.util.List<String> getPasswordHistory(int userId) {
        java.util.List<String> history = new java.util.ArrayList<>();
        
        String sql = "SELECT password, changed_at FROM auth_db.password_history " +
                "WHERE user_id = ? ORDER BY changed_at DESC";
        
        try (Connection conn = DbConnector.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String password = rs.getString("password");
                    String timestamp = rs.getTimestamp("changed_at").toString();
                    history.add("Changed on: " + timestamp + " | Password: " + password);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        
        return history;
    }
}