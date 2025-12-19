package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.CatalogSection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CatalogPanel extends JPanel {

    private JTable catalogTable;
    private DefaultTableModel tableModel;
    private StudentDAO studentDAO;
    private UserSession session;

    private List<CatalogSection> currentCatalog;

    private TimetablePanel timetablePanel;

    public CatalogPanel(TimetablePanel timetablePanel) {
        this.timetablePanel = timetablePanel;
        this.studentDAO = new StudentDAO();
        this.session = UserSession.getInstance();

        setLayout(new BorderLayout(10, 10));

        initUI();
        loadCatalogData();
    }

    private void initUI() {
        JLabel titleLabel = new JLabel("Course Catalog & Registration");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Course Code", "Title", "Instructor Dept.",
                "Day/Time", "Room", "Enrollment"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        catalogTable = new JTable(tableModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.setRowHeight(25);
        catalogTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(catalogTable);
        add(scrollPane, BorderLayout.CENTER);

        JButton registerButton = new JButton("Register for Selected Course");
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        registerButton.setBackground(new Color(60, 179, 113));
        registerButton.setForeground(Color.WHITE);

        registerButton.addActionListener(e -> handleRegisterCourse());

        add(registerButton, BorderLayout.SOUTH);
    }

    private void loadCatalogData() {
        currentCatalog = studentDAO.getAvailableCatalog();

        tableModel.setRowCount(0);

        if (currentCatalog == null || currentCatalog.isEmpty()) {
            tableModel.addRow(new Object[]{"No courses available.", "", "", "", "", ""});
        } else {
            for (CatalogSection section : currentCatalog) {
                tableModel.addRow(new Object[]{
                        section.getCourseCode(),
                        section.getCourseTitle(),
                        section.getInstructorDepartment(),
                        section.getDayTime(),
                        section.getRoom(),
                        section.getEnrollmentStatus()
                });
            }
        }
    }

    private void handleRegisterCourse() {
        int selectedRow = catalogTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course from the table first.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CatalogSection selectedSection = currentCatalog.get(selectedRow);

        int studentId = session.getUserId();

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to register for:\n" +
                        selectedSection.getCourseCode() + " - " + selectedSection.getCourseTitle() + "?",
                "Confirm Registration",
                JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            String resultMessage = studentDAO.registerForSection(studentId, selectedSection.getSectionId());

            if (resultMessage != null && resultMessage.startsWith("Success")) {
                JOptionPane.showMessageDialog(this,
                        resultMessage,
                        "Registration Successful",
                        JOptionPane.INFORMATION_MESSAGE);
                loadCatalogData();

                if (timetablePanel != null) {
                    timetablePanel.refreshEnrolledCourses();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        resultMessage,
                        "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}