package edu.univ.erp.service;

import edu.univ.erp.data.DbConnector;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing student notifications.
 */
public class NotificationService {
    
    /**
     * Creates a notification for a student when their grade is updated.
     * 
     * @param studentId The student's user ID
     * @param message The notification message
     * @return Success or error message
     */
    public String createNotification(int studentId, String message) {
        String sql = "INSERT INTO erp_db.notifications (student_id, message, created_at, is_read) VALUES (?, ?, NOW(), 0)";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
            
            return "Success";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Gets all notifications for a student.
     * 
     * @param studentId The student's user ID
     * @return List of notification messages with timestamps
     */
    public List<Notification> getNotifications(int studentId) {
        List<Notification> notifications = new ArrayList<>();
        
        String sql = "SELECT notification_id, message, created_at, is_read " +
                "FROM erp_db.notifications " +
                "WHERE student_id = ? " +
                "ORDER BY created_at DESC";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(new Notification(
                        rs.getInt("notification_id"),
                        rs.getString("message"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("is_read") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return notifications;
    }
    
    /**
     * Marks a notification as read.
     * 
     * @param notificationId The notification ID
     */
    public void markAsRead(int notificationId) {
        String sql = "UPDATE erp_db.notifications SET is_read = 1 WHERE notification_id = ?";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificationId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gets count of unread notifications for a student.
     * 
     * @param studentId The student's user ID
     * @return Number of unread notifications
     */
    public int getUnreadCount(int studentId) {
        String sql = "SELECT COUNT(*) FROM erp_db.notifications WHERE student_id = ? AND is_read = 0";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    /**
     * Simple notification data class.
     */
    public static class Notification {
        private final int notificationId;
        private final String message;
        private final Timestamp createdAt;
        private final boolean isRead;
        
        public Notification(int notificationId, String message, Timestamp createdAt, boolean isRead) {
            this.notificationId = notificationId;
            this.message = message;
            this.createdAt = createdAt;
            this.isRead = isRead;
        }
        
        public int getNotificationId() { return notificationId; }
        public String getMessage() { return message; }
        public Timestamp getCreatedAt() { return createdAt; }
        public boolean isRead() { return isRead; }
    }
}




