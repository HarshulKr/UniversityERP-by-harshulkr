package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.univ.erp.domain.CatalogSection;
import edu.univ.erp.domain.EnrolledCourse;
import edu.univ.erp.domain.StudentGrade;
import edu.univ.erp.service.SettingsService;

public class StudentDAO {

    private final SettingsService settingsService;

    public StudentDAO() {
        this.settingsService = new SettingsService();
    }

    public List<EnrolledCourse> getEnrolledCourses(int studentId) {
        List<EnrolledCourse> enrolledCourses = new ArrayList<>();

        String sql = "SELECT " +
                "    e.enrollment_id, " +
                "    c.code AS course_code, " +
                "    c.title AS course_title, " +
                "    s.day_time, " +
                "    s.room, " +
                "    i.department AS instructor_department " +
                "FROM " +
                "    enrollments e " +
                "JOIN " +
                "    sections s ON e.section_id = s.section_id " +
                "JOIN " +
                "    courses c ON s.course_id = c.course_id " +
                "JOIN " +
                "    instructors i ON s.instructor_id = i.user_id " +
                "WHERE " +
                "    e.student_id = ?";

        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EnrolledCourse course = new EnrolledCourse(
                            rs.getInt("enrollment_id"),
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getString("instructor_department")
                    );
                    enrolledCourses.add(course);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return enrolledCourses;
    }

    public static void main(String[] args) {
        System.out.println("--- Testing StudentDAO ---");

        StudentDAO dao = new StudentDAO();

        List<EnrolledCourse> courses = dao.getEnrolledCourses(1001);

        if (courses.isEmpty()) {
            System.out.println("Test failed: No courses found. Check database connection or sample data.");
        } else {
            System.out.println("Test successful! Found " + courses.size() + " course(s):");
            for (EnrolledCourse c : courses) {
                System.out.println("  -> " + c.getCourseCode() + ": " + c.getCourseTitle() +
                        " on " + c.getDayTime() + " in " + c.getRoom());
            }
        }
    }

    public List<CatalogSection> getAvailableCatalog() {
        List<CatalogSection> catalog = new ArrayList<>();

        String sql = "SELECT " +
                "    s.section_id, " +
                "    c.code AS course_code, " +
                "    c.title AS course_title, " +
                "    i.department AS instructor_department, " +
                "    s.day_time, " +
                "    s.room, " +
                "    s.capacity, " +
                "    COUNT(e.enrollment_id) AS current_enrollment " +
                "FROM " +
                "    sections s " +
                "JOIN " +
                "    courses c ON s.course_id = c.course_id " +
                "JOIN " +
                "    instructors i ON s.instructor_id = i.user_id " +
                "LEFT JOIN " +
                "    enrollments e ON s.section_id = e.section_id " +
                "GROUP BY " +
                "    s.section_id";

        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                CatalogSection section = new CatalogSection(
                        rs.getInt("section_id"),
                        rs.getString("course_code"),
                        rs.getString("course_title"),
                        rs.getString("instructor_department"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("current_enrollment"),
                        rs.getInt("capacity")
                );
                catalog.add(section);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return catalog;
    }

    public String registerForSection(int studentId, int sectionId) {
        if (settingsService.isMaintenanceModeOn()) {
            return "Error: The system is in maintenance mode. Registration is temporarily disabled.";
        }

        try (Connection conn = DbConnector.getErpConnection()) {

            String checkSql = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND section_id = ?";
            try (PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {
                checkPstmt.setInt(1, studentId);
                checkPstmt.setInt(2, sectionId);
                try (ResultSet rs = checkPstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return "Error: You are already enrolled in this section.";
                    }
                }
            }

            String capSql = "SELECT COUNT(e.enrollment_id) AS current, s.capacity FROM sections s " +
                    "LEFT JOIN enrollments e ON s.section_id = e.section_id " +
                    "WHERE s.section_id = ? GROUP BY s.section_id";
            try (PreparedStatement capPstmt = conn.prepareStatement(capSql)) {
                capPstmt.setInt(1, sectionId);
                try (ResultSet rs = capPstmt.executeQuery()) {
                    if (rs.next()) {
                        int current = rs.getInt("current");
                        int capacity = rs.getInt("capacity");
                        if (current >= capacity) {
                            return "Error: This section is full (" + current + "/" + capacity + ").";
                        }
                    } else {
                        String capOnlySql = "SELECT capacity FROM sections WHERE section_id = ?";
                        try (PreparedStatement capOnlyPstmt = conn.prepareStatement(capOnlySql)) {
                            capOnlyPstmt.setInt(1, sectionId);
                            try (ResultSet capRs = capOnlyPstmt.executeQuery()) {
                                if (capRs.next()) {
                                    if (capRs.getInt("capacity") <= 0) {
                                        return "Error: This section is full (0/0).";
                                    }
                                }
                            }
                        }
                    }
                }
            }

            String insertSql = "INSERT INTO enrollments (student_id, section_id) VALUES (?, ?)";
            try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                insertPstmt.setInt(1, studentId);
                insertPstmt.setInt(2, sectionId);
                int rowsAffected = insertPstmt.executeUpdate();

                if (rowsAffected > 0) {
                    return "Success! Registered for the course!";
                } else {
                    return "Error: Registration failed for an unknown reason.";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database error during registration.";
        }
    }

    public String dropSection(int enrollmentId) {
        if (settingsService.isMaintenanceModeOn()) {
            return "Error: The system is in maintenance mode. Dropping courses is temporarily disabled.";
        }

        String deadlineCheckSql = "SELECT s.drop_deadline FROM erp_db.sections s " +
                "JOIN erp_db.enrollments e ON s.section_id = e.section_id " +
                "WHERE e.enrollment_id = ?";
        
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(deadlineCheckSql)) {
            
            checkPstmt.setInt(1, enrollmentId);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next()) {
                    java.sql.Date deadline = rs.getDate("drop_deadline");
                    if (deadline != null) {
                        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
                        if (today.after(deadline)) {
                            return "Error: The drop deadline (" + deadline.toString() + ") has passed. You cannot drop this course now.";
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Could not check drop deadline.";
        }

        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";

        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, enrollmentId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return "Course dropped successfully.";
            } else {
                return "Error: Could not find the course to drop.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: Database error during drop.";
        }
    }

    public List<StudentGrade> getStudentGrades(int studentId) {
        List<StudentGrade> grades = new ArrayList<>();

        String sql = "SELECT " +
                "    c.code AS course_code, " +
                "    c.title AS course_title, " +
                "    g.component, " +
                "    g.score, " +
                "    g.final_grade " +
                "FROM " +
                "    grades g " +
                "JOIN " +
                "    enrollments e ON g.enrollment_id = e.enrollment_id " +
                "JOIN " +
                "    sections s ON e.section_id = s.section_id " +
                "JOIN " +
                "    courses c ON s.course_id = c.course_id " +
                "WHERE " +
                "    e.student_id = ? " +
                "ORDER BY " +
                "    c.code, g.component";

        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    StudentGrade grade = new StudentGrade(
                            rs.getString("course_code"),
                            rs.getString("course_title"),
                            rs.getString("component"),
                            rs.getDouble("score"),
                            rs.getString("final_grade")
                    );
                    grades.add(grade);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }
}