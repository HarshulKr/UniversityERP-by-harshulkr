package edu.univ.erp.service;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.DbConnector;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.InstructorSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstructorService {

    private final SettingsService settingsService;

    public InstructorService() {
        this.settingsService = new SettingsService();
    }

    public boolean isMaintenanceModeOn() {
        return settingsService.isMaintenanceModeOn();
    }

    public List<InstructorSection> getMySections() {
        List<InstructorSection> sections = new ArrayList<>();
        int instructorId = UserSession.getInstance().getUserId();

        String sql = "SELECT s.section_id, c.code, c.title, s.day_time, s.room, s.semester, s.year " +
                "FROM erp_db.sections s " +
                "JOIN erp_db.courses c ON s.course_id = c.course_id " +
                "WHERE s.instructor_id = ?";

        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, instructorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String semesterYear = rs.getString("semester") + " " + rs.getInt("year");
                    sections.add(new InstructorSection(
                            rs.getInt("section_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            semesterYear,
                            rs.getString("room")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }

    public List<GradeComponent> getGradebookData(int sectionId) {
        List<GradeComponent> gradebook = new ArrayList<>();

        String studentSql = "SELECT e.enrollment_id, s.roll_no, u.username " +
                "FROM erp_db.enrollments e " +
                "JOIN erp_db.students s ON e.student_id = s.user_id " +
                "JOIN auth_db.users_auth u ON s.user_id = u.user_id " +
                "WHERE e.section_id = ? " +
                "ORDER BY s.roll_no";

        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(studentSql)) {

            pstmt.setInt(1, sectionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    GradeComponent studentGrade = new GradeComponent(
                            rs.getInt("enrollment_id"),
                            rs.getString("roll_no"),
                            rs.getString("username")
                    );
                    gradebook.add(studentGrade);
                }
            }

            loadScoresAndGrades(conn, gradebook);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return gradebook;
    }

    private void loadScoresAndGrades(Connection conn, List<GradeComponent> gradebook) throws SQLException {
        if (gradebook.isEmpty()) return;
        StringBuilder enrollmentIds = new StringBuilder();
        for (int i = 0; i < gradebook.size(); i++) {
            enrollmentIds.append(gradebook.get(i).getEnrollmentId());
            if (i < gradebook.size() - 1) {
                enrollmentIds.append(",");
            }
        }

        String gradesSql = "SELECT enrollment_id, component, score, final_grade FROM erp_db.grades " +
                "WHERE enrollment_id IN (" + enrollmentIds.toString() + ")";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(gradesSql)) {

            java.util.Map<Integer, GradeComponent> gradeMap = new java.util.HashMap<>();
            for (GradeComponent gc : gradebook) {
                gradeMap.put(gc.getEnrollmentId(), gc);
            }

            while (rs.next()) {
                int enrollmentId = rs.getInt("enrollment_id");
                String component = rs.getString("component");
                Double score = rs.getDouble("score");
                if (rs.wasNull()) {
                    score = null;
                }
                String finalGrade = rs.getString("final_grade");

                GradeComponent gc = gradeMap.get(enrollmentId);
                if (gc != null) {
                    if (component != null) {
                        gc.setScore(component, score);
                    }
                    if (finalGrade != null && !finalGrade.isEmpty()) {
                        gc.setFinalGrade(finalGrade);
                    }
                }
            }
        }
    }

    public List<String> getComponentNamesForSection(int sectionId) {
        List<String> components = new ArrayList<>();
        
        String sql = "SELECT component_name FROM erp_db.section_components " +
                "WHERE section_id = ? ORDER BY display_order";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String component = rs.getString("component_name");
                    if (component != null && !component.trim().isEmpty()) {
                        components.add(component);
                    }
                }
            }
        } catch (SQLException e) {
        }
        
        return components;
    }
    
    public java.util.Map<String, Integer> getComponentWeightsForSection(int sectionId) {
        java.util.Map<String, Integer> weights = new java.util.LinkedHashMap<>();
        
        String sql = "SELECT component_name, weight FROM erp_db.section_components " +
                "WHERE section_id = ? ORDER BY display_order";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    weights.put(rs.getString("component_name"), rs.getInt("weight"));
                }
            }
        } catch (SQLException e) {
        }
        
        return weights;
    }
    
    public String saveComponentWeights(int sectionId, java.util.Map<String, Integer> componentWeights) {
        if (settingsService.isMaintenanceModeOn()) {
            return "Error: Cannot save components. System is in Maintenance Mode.";
        }
        
        try (Connection conn = DbConnector.getErpConnection()) {
            conn.setAutoCommit(false);
            
            String deleteSql = "DELETE FROM erp_db.section_components WHERE section_id = ?";
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setInt(1, sectionId);
                delStmt.executeUpdate();
            }
            
            String insertSql = "INSERT INTO erp_db.section_components (section_id, component_name, weight, display_order) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                int order = 0;
                for (java.util.Map.Entry<String, Integer> entry : componentWeights.entrySet()) {
                    insStmt.setInt(1, sectionId);
                    insStmt.setString(2, entry.getKey());
                    insStmt.setInt(3, entry.getValue());
                    insStmt.setInt(4, order++);
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
            }
            
            conn.commit();
            return "Success! Components saved.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error saving components: " + e.getMessage();
        }
    }
    
    public String saveScores(List<GradeComponent> gradebook, int sectionId, List<String> componentNames) {
        if (settingsService.isMaintenanceModeOn()) {
            return "Error: Cannot save scores. System is in Maintenance Mode.";
        }
        if (gradebook.isEmpty()) return "Error: No students to grade.";
        if (componentNames == null || componentNames.isEmpty()) {
            return "Error: No components defined.";
        }

        try (Connection conn = DbConnector.getErpConnection()) {
            conn.setAutoCommit(false);

            String deleteSql = "DELETE FROM erp_db.grades WHERE enrollment_id = ? AND component = ?";
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                for (GradeComponent student : gradebook) {
                    for (String component : componentNames) {
                        delStmt.setInt(1, student.getEnrollmentId());
                        delStmt.setString(2, component);
                        delStmt.addBatch();
                    }
                }
                delStmt.executeBatch();
            }

            String insertSql = "INSERT INTO erp_db.grades (enrollment_id, component, score) VALUES (?, ?, ?)";
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                for (GradeComponent student : gradebook) {
                    for (String component : componentNames) {
                        Double score = student.getScore(component);

                        if (score != null) {
                            if (score < 0 || score > 100) {
                                conn.rollback();
                                return "Error: Score for " + student.getUsername() + " (" + component + ") must be between 0 and 100.";
                            }
                            insStmt.setInt(1, student.getEnrollmentId());
                            insStmt.setString(2, component);
                            insStmt.setDouble(3, score);
                            insStmt.addBatch();
                        }
                    }
                }
                insStmt.executeBatch();
            }

            conn.commit();
            
            createGradeUpdateNotifications(conn, gradebook, sectionId);
            
            return "Success! All component scores saved.";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error saving scores: " + e.getMessage();
        }
    }
    
    private void createGradeUpdateNotifications(Connection conn, List<GradeComponent> gradebook, int sectionId) {
        try {
            String courseInfoSql = "SELECT c.code, c.title FROM erp_db.sections s " +
                    "JOIN erp_db.courses c ON s.course_id = c.course_id " +
                    "WHERE s.section_id = ?";
            String courseCode = "";
            String courseTitle = "";
            
            try (PreparedStatement pstmt = conn.prepareStatement(courseInfoSql)) {
                pstmt.setInt(1, sectionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        courseCode = rs.getString("code");
                        courseTitle = rs.getString("title");
                    }
                }
            }
            
            Set<Integer> notifiedStudents = new HashSet<>();
            String notificationSql = "INSERT INTO erp_db.notifications (student_id, message, created_at, is_read) " +
                    "SELECT e.student_id, ?, NOW(), 0 " +
                    "FROM erp_db.enrollments e " +
                    "WHERE e.enrollment_id = ?";
            
            try (PreparedStatement notifPstmt = conn.prepareStatement(notificationSql)) {
                for (GradeComponent student : gradebook) {
                    int enrollmentId = student.getEnrollmentId();
                    
                    String studentIdSql = "SELECT student_id FROM erp_db.enrollments WHERE enrollment_id = ?";
                    try (PreparedStatement studentPstmt = conn.prepareStatement(studentIdSql)) {
                        studentPstmt.setInt(1, enrollmentId);
                        try (ResultSet rs = studentPstmt.executeQuery()) {
                            if (rs.next()) {
                                int studentId = rs.getInt("student_id");
                                
                                if (!notifiedStudents.contains(studentId)) {
                                    String message = "Your grades for " + courseCode + " (" + courseTitle + ") have been updated.";
                                    notifPstmt.setString(1, message);
                                    notifPstmt.setInt(2, enrollmentId);
                                    notifPstmt.addBatch();
                                    notifiedStudents.add(studentId);
                                }
                            }
                        }
                    }
                }
                if (!notifiedStudents.isEmpty()) {
                    notifPstmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String computeAndSaveFinalGrades(List<GradeComponent> gradebook, java.util.Map<String, Integer> componentWeights, int sectionId) {
        if (settingsService.isMaintenanceModeOn()) {
            return "Error: Cannot compute final grades. System is in Maintenance Mode.";
        }

        String updateSql = "UPDATE erp_db.grades SET final_grade = ? WHERE enrollment_id = ?";

        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            int updateCount = 0;
            for (GradeComponent student : gradebook) {
                String finalGrade = student.calculateFinalGrade(componentWeights);
                student.setFinalGrade(finalGrade);

                if (!"Incomplete".equals(finalGrade)) {
                    pstmt.setString(1, finalGrade);
                    pstmt.setInt(2, student.getEnrollmentId());
                    pstmt.addBatch();
                    updateCount++;
                }
            }

            if (updateCount > 0) {
                pstmt.executeBatch();
                
                createFinalGradeNotifications(conn, gradebook, sectionId);
                
                return "Success! Final grades computed and saved for " + updateCount + " student(s).";
            } else {
                return "Warning: No final grades calculated. Check if all required scores are entered.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error saving final grades: " + e.getMessage();
        }
    }
    
    private void createFinalGradeNotifications(Connection conn, List<GradeComponent> gradebook, int sectionId) {
        try {
            String courseInfoSql = "SELECT c.code, c.title FROM erp_db.sections s " +
                    "JOIN erp_db.courses c ON s.course_id = c.course_id " +
                    "WHERE s.section_id = ?";
            String courseCode = "";
            String courseTitle = "";
            
            try (PreparedStatement pstmt = conn.prepareStatement(courseInfoSql)) {
                pstmt.setInt(1, sectionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        courseCode = rs.getString("code");
                        courseTitle = rs.getString("title");
                    }
                }
            }
            
            Set<Integer> notifiedStudents = new HashSet<>();
            String notificationSql = "INSERT INTO erp_db.notifications (student_id, message, created_at, is_read) " +
                    "SELECT e.student_id, ?, NOW(), 0 " +
                    "FROM erp_db.enrollments e " +
                    "WHERE e.enrollment_id = ?";
            
            try (PreparedStatement notifPstmt = conn.prepareStatement(notificationSql)) {
                for (GradeComponent student : gradebook) {
                    int enrollmentId = student.getEnrollmentId();
                    String finalGrade = student.getFinalGrade();
                    
                    if (!"Incomplete".equals(finalGrade)) {
                        String studentIdSql = "SELECT student_id FROM erp_db.enrollments WHERE enrollment_id = ?";
                        try (PreparedStatement studentPstmt = conn.prepareStatement(studentIdSql)) {
                            studentPstmt.setInt(1, enrollmentId);
                            try (ResultSet rs = studentPstmt.executeQuery()) {
                                if (rs.next()) {
                                    int studentId = rs.getInt("student_id");
                                    
                                    if (!notifiedStudents.contains(studentId)) {
                                        String message = "Your final grade for " + courseCode + " (" + courseTitle + ") has been computed: " + finalGrade;
                                        notifPstmt.setString(1, message);
                                        notifPstmt.setInt(2, enrollmentId);
                                        notifPstmt.addBatch();
                                        notifiedStudents.add(studentId);
                                    }
                                }
                            }
                        }
                    }
                }
                if (!notifiedStudents.isEmpty()) {
                    notifPstmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public String setDropDeadline(int sectionId, java.sql.Date deadlineDate) {
        if (settingsService.isMaintenanceModeOn()) {
            return "Error: Cannot set deadline. System is in Maintenance Mode.";
        }
        
        String sql = "UPDATE erp_db.sections SET drop_deadline = ? WHERE section_id = ?";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, deadlineDate);
            pstmt.setInt(2, sectionId);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return "Success! Drop deadline set to " + deadlineDate.toString() + ".";
            } else {
                return "Error: Section not found.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error setting deadline: " + e.getMessage();
        }
    }
    
    public java.sql.Date getDropDeadline(int sectionId) {
        String sql = "SELECT drop_deadline FROM erp_db.sections WHERE section_id = ?";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, sectionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDate("drop_deadline");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}