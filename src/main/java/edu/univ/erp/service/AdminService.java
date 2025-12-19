package edu.univ.erp.service;

import edu.univ.erp.data.DbConnector;
import edu.univ.erp.domain.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminService {

    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY code";

        try (Connection conn = DbConnector.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(new Course(
                        rs.getInt("course_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return courses;
    }

    public String createCourse(Course course) {
        String sql = "INSERT INTO courses (code, title, credits) VALUES (?, ?, ?)";
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setInt(3, course.getCredits());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return "Success! Course '" + course.getCode() + "' created.";
            } else {
                return "Error: Could not create course.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String updateCourse(Course course) {
        String sql = "UPDATE courses SET code = ?, title = ?, credits = ? WHERE course_id = ?";
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, course.getCode());
            pstmt.setString(2, course.getTitle());
            pstmt.setInt(3, course.getCredits());
            pstmt.setInt(4, course.getCourseId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return "Success! Course '" + course.getCode() + "' updated.";
            } else {
                return "Error: Course not found or no changes made.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String deleteCourse(int courseId) {
        String checkSql = "SELECT COUNT(*) FROM sections WHERE course_id = ?";
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {

            checkPstmt.setInt(1, courseId);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Error: Cannot delete course. It is in use by " + rs.getInt(1) + " section(s).";
                }
            }

            String deleteSql = "DELETE FROM courses WHERE course_id = ?";
            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                deletePstmt.setInt(1, courseId);
                int rowsAffected = deletePstmt.executeUpdate();

                if (rowsAffected > 0) {
                    return "Success! Course deleted.";
                } else {
                    return "Error: Course not found.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public List<UserAccount> getAllUserAccounts() {
        List<UserAccount> users = new ArrayList<>();
        String sql = "SELECT a.user_id, a.username, a.role, s.roll_no, s.program, i.department " +
                "FROM auth_db.users_auth a " +
                "LEFT JOIN erp_db.students s ON a.user_id = s.user_id " +
                "LEFT JOIN erp_db.instructors i ON a.user_id = i.user_id " +
                "WHERE a.role IN ('Student', 'Instructor', 'Admin') " +
                "ORDER BY a.role, a.username";

        try (Connection conn = DbConnector.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UserAccount user = new UserAccount();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role"));
                user.setRollNo(rs.getString("roll_no"));
                user.setProgram(rs.getString("program"));
                user.setDepartment(rs.getString("department"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public String createUserAccount(UserAccount user, String password) {
        int newUserId = -1;
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        String authSql = "INSERT INTO users_auth (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection authConn = DbConnector.getAuthConnection();
             PreparedStatement pstmtAuth = authConn.prepareStatement(authSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmtAuth.setString(1, user.getUsername());
            pstmtAuth.setString(2, hashedPassword);
            pstmtAuth.setString(3, user.getRole());
            pstmtAuth.executeUpdate();

            try (ResultSet rs = pstmtAuth.getGeneratedKeys()) {
                if (rs.next()) {
                    newUserId = rs.getInt(1);
                } else {
                    return "Error: Could not retrieve new user ID.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error creating auth user: " + e.getMessage();
        }

        if ("Admin".equals(user.getRole())) {
            return "Success! Admin user '" + user.getUsername() + "' created.";
        }

        String erpSql;
        if ("Student".equals(user.getRole())) {
            erpSql = "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)";
        } else {
            erpSql = "INSERT INTO instructors (user_id, department) VALUES (?, ?)";
        }

        try (Connection erpConn = DbConnector.getErpConnection();
             PreparedStatement pstmtErp = erpConn.prepareStatement(erpSql)) {

            if ("Student".equals(user.getRole())) {
                pstmtErp.setInt(1, newUserId);
                pstmtErp.setString(2, user.getRollNo());
                pstmtErp.setString(3, user.getProgram());
                pstmtErp.setInt(4, 1);
            } else {
                pstmtErp.setInt(1, newUserId);
                pstmtErp.setString(2, user.getDepartment());
            }
            pstmtErp.executeUpdate();

            return "Success! User '" + user.getUsername() + "' created.";

        } catch (SQLException e) {
            e.printStackTrace();
            try (Connection authConn = DbConnector.getAuthConnection();
                 PreparedStatement pstmtDel = authConn.prepareStatement("DELETE FROM users_auth WHERE user_id = ?")) {
                pstmtDel.setInt(1, newUserId);
                pstmtDel.executeUpdate();
                return "Error: Could not create profile (e.g., duplicate Roll No). User creation was rolled back.";
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
                return "CRITICAL ERROR: Profile creation failed AND auth user could not be rolled back.";
            }
        }
    }

    public List<SectionDetails> getAllSectionDetails() {
        List<SectionDetails> sections = new ArrayList<>();
        String sql = "SELECT " +
                "    s.section_id, c.code, c.title, u.username, " +
                "    s.day_time, s.room, s.capacity, s.semester, s.year " +
                "FROM erp_db.sections s " +
                "JOIN erp_db.courses c ON s.course_id = c.course_id " +
                "LEFT JOIN auth_db.users_auth u ON s.instructor_id = u.user_id";

        try (Connection conn = DbConnector.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                SectionDetails section = new SectionDetails();
                section.setSectionId(rs.getInt("section_id"));
                section.setCourseCode(rs.getString("code"));
                section.setCourseTitle(rs.getString("title"));
                section.setInstructorName(rs.getString("username"));
                section.setDayTime(rs.getString("day_time"));
                section.setRoom(rs.getString("room"));
                section.setCapacity(rs.getInt("capacity"));
                section.setSemester(rs.getString("semester"));
                section.setYear(rs.getInt("year"));
                sections.add(section);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sections;
    }

    public List<CourseDropdownItem> getCoursesForDropdown() {
        List<CourseDropdownItem> items = new ArrayList<>();
        String sql = "SELECT course_id, code FROM erp_db.courses ORDER BY code";
        try (Connection conn = DbConnector.getErpConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new CourseDropdownItem(rs.getInt("course_id"), rs.getString("code")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<InstructorDropdownItem> getInstructorsForDropdown() {
        List<InstructorDropdownItem> items = new ArrayList<>();
        String sql = "SELECT user_id, username FROM auth_db.users_auth WHERE role = 'Instructor' ORDER BY username";
        try (Connection conn = DbConnector.getAuthConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                items.add(new InstructorDropdownItem(rs.getInt("user_id"), rs.getString("username")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public String createSection(Section section) {
        String sql = "INSERT INTO erp_db.sections (course_id, instructor_id, day_time, room, capacity, semester, year) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, section.getCourseId());
            pstmt.setInt(2, section.getInstructorId());
            pstmt.setString(3, section.getDayTime());
            pstmt.setString(4, section.getRoom());
            pstmt.setInt(5, section.getCapacity());
            pstmt.setString(6, section.getSemester());
            pstmt.setInt(7, section.getYear());

            int rowsAffected = pstmt.executeUpdate();
            return (rowsAffected > 0) ? "Success! Section created." : "Error: Section not created.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public String deleteSection(int sectionId) {
        String checkSql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
        try (Connection conn = DbConnector.getErpConnection();
             PreparedStatement checkPstmt = conn.prepareStatement(checkSql)) {

            checkPstmt.setInt(1, sectionId);
            try (ResultSet rs = checkPstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "Error: Cannot delete section. It has " + rs.getInt(1) + " student(s) enrolled.";
                }
            }

            String deleteSql = "DELETE FROM sections WHERE section_id = ?";
            try (PreparedStatement deletePstmt = conn.prepareStatement(deleteSql)) {
                deletePstmt.setInt(1, sectionId);
                int rowsAffected = deletePstmt.executeUpdate();

                if (rowsAffected > 0) {
                    return "Success! Section deleted.";
                } else {
                    return "Error: Section not found.";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}