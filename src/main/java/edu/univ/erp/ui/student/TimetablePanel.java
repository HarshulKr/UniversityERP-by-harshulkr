package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.EnrolledCourse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class TimetablePanel extends JPanel {

    private JTable timetableTable;
    private DefaultTableModel tableModel;
    private StudentDAO studentDAO;
    private UserSession session;
    private List<EnrolledCourse> currentEnrolledCourses;

    private final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color TEXT_COLOR = new Color(44, 62, 80);
    private final Font MAIN_FONT = new Font("SansSerif", Font.PLAIN, 14);
    private final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 22);
    private final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 15);

    public TimetablePanel() {
        this.studentDAO = new StudentDAO();
        this.session = UserSession.getInstance();

        setLayout(new BorderLayout(15, 15));
        setBackground(Color.WHITE);

        initUI();
        loadEnrolledCourses();
    }

    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel titleLabel = new JLabel("🗓️ My Enrolled Courses / Timetable");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel studentInfoLabel = new JLabel("ID: " + session.getUserId() + " | " + session.getUsername());
        studentInfoLabel.setFont(MAIN_FONT);
        studentInfoLabel.setForeground(new Color(200, 200, 200));
        headerPanel.add(studentInfoLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        String[] columnNames = {"Course Code", "Course Title", "Day/Time", "Room", "Instructor Dept."};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        timetableTable = new JTable(tableModel);
        timetableTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        timetableTable.setFont(MAIN_FONT);
        timetableTable.setRowHeight(28);

        timetableTable.getTableHeader().setFont(HEADER_FONT);
        timetableTable.getTableHeader().setBackground(new Color(230, 230, 230));
        timetableTable.getTableHeader().setForeground(TEXT_COLOR);
        timetableTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        timetableTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scrollPane = new JScrollPane(timetableTable);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 15, 0, 15),
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.setBackground(Color.WHITE);
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));

        JButton dropButton = new JButton("Drop Selected Course");
        dropButton.setFont(new Font("SansSerif", Font.BOLD, 15));

        dropButton.setBackground(new Color(192, 57, 43));
        dropButton.setForeground(Color.WHITE);
        dropButton.setFocusPainted(false);
        dropButton.setBorderPainted(false);
        dropButton.setPreferredSize(new Dimension(250, 40));
        dropButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        dropButton.addActionListener(e -> handleDropCourse());

        southPanel.add(dropButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void loadEnrolledCourses() {
        int studentId = session.getUserId();
        currentEnrolledCourses = studentDAO.getEnrolledCourses(studentId);
        tableModel.setRowCount(0);

        if (currentEnrolledCourses == null || currentEnrolledCourses.isEmpty()) {
            tableModel.addRow(new Object[]{"No courses found.", "Please enroll using the Course Catalog.", "", "", ""});
        } else {
            for (EnrolledCourse course : currentEnrolledCourses) {
                tableModel.addRow(new Object[]{
                        course.getCourseCode(),
                        course.getCourseTitle(),
                        course.getDayTime(),
                        course.getRoom(),
                        course.getInstructorDepartment()
                });
            }
        }
    }

    private void handleDropCourse() {
        int selectedRow = timetableTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course from the table to drop.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentEnrolledCourses == null || (currentEnrolledCourses.isEmpty() && selectedRow == 0)) {
            JOptionPane.showMessageDialog(this,
                    "There are no actual courses to drop.",
                    "No Courses",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        EnrolledCourse selectedCourse = currentEnrolledCourses.get(selectedRow);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to drop:\n" +
                        selectedCourse.getCourseCode() + " - " + selectedCourse.getCourseTitle() + "?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            String resultMessage = studentDAO.dropSection(selectedCourse.getEnrollmentId());

            if (resultMessage != null && resultMessage.startsWith("Course dropped")) {
                JOptionPane.showMessageDialog(this,
                        resultMessage,
                        "Drop Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                loadEnrolledCourses();
            } else {
                JOptionPane.showMessageDialog(this,
                        resultMessage,
                        "Drop Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshEnrolledCourses() {
        SwingUtilities.invokeLater(this::loadEnrolledCourses);
    }
}