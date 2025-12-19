package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.Course;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class CourseManagementPanel extends JPanel {

    private final AdminService adminService;
    private JTable courseTable;
    private DefaultTableModel tableModel;

    public CourseManagementPanel() {
        this.adminService = new AdminService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initTable();

        initButtonPanel();

        loadCourseData();
    }

    private void initTable() {
        String[] columnNames = {"ID", "Course Code", "Title", "Credits"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        courseTable = new JTable(tableModel);
        courseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(courseTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addButton = new JButton("Add New Course");
        JButton editButton = new JButton("Edit Selected");
        JButton deleteButton = new JButton("Delete Selected");

        addButton.addActionListener(e -> showCourseForm(null));
        editButton.addActionListener(e -> onEditClicked());
        deleteButton.addActionListener(e -> onDeleteClicked());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadCourseData() {
        tableModel.setRowCount(0);

        List<Course> courses = adminService.getAllCourses();

        for (Course course : courses) {
            Vector<Object> row = new Vector<>();
            row.add(course.getCourseId());
            row.add(course.getCode());
            row.add(course.getTitle());
            row.add(course.getCredits());
            tableModel.addRow(row);
        }
    }

    private void onEditClicked() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        String code = (String) tableModel.getValueAt(selectedRow, 1);
        String title = (String) tableModel.getValueAt(selectedRow, 2);
        int credits = (int) tableModel.getValueAt(selectedRow, 3);

        Course courseToEdit = new Course(courseId, code, title, credits);

        showCourseForm(courseToEdit);
    }

    private void onDeleteClicked() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int courseId = (int) tableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) tableModel.getValueAt(selectedRow, 1);

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete course " + courseCode + "?\nThis cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            String message = adminService.deleteCourse(courseId);
            JOptionPane.showMessageDialog(this, message, "Delete Status", JOptionPane.INFORMATION_MESSAGE);
            loadCourseData();
        }
    }

    private void showCourseForm(Course course) {
        JTextField codeField = new JTextField(10);
        JTextField titleField = new JTextField(20);
        JTextField creditsField = new JTextField(5);

        String dialogTitle;
        if (course == null) {
            dialogTitle = "Add New Course";
        } else {
            dialogTitle = "Edit Course: " + course.getCode();
            codeField.setText(course.getCode());
            titleField.setText(course.getTitle());
            creditsField.setText(String.valueOf(course.getCredits()));
        }

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.add(new JLabel("Course Code:"));
        formPanel.add(codeField);
        formPanel.add(new JLabel("Course Title:"));
        formPanel.add(titleField);
        formPanel.add(new JLabel("Credits:"));
        formPanel.add(creditsField);

        int result = JOptionPane.showConfirmDialog(this, formPanel, dialogTitle,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String code = codeField.getText();
                String title = titleField.getText();
                int credits = Integer.parseInt(creditsField.getText());

                if (code.isEmpty() || title.isEmpty()) {
                    throw new Exception("Code and Title cannot be empty.");
                }

                String message;
                if (course == null) {
                    Course newCourse = new Course(code, title, credits);
                    message = adminService.createCourse(newCourse);
                } else {
                    course.setCode(code);
                    course.setTitle(title);
                    course.setCredits(credits);
                    message = adminService.updateCourse(course);
                }

                JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCourseData();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Error: Credits must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}