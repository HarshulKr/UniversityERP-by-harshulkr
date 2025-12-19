package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.CourseDropdownItem;
import edu.univ.erp.domain.InstructorDropdownItem;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.SectionDetails;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class SectionManagementPanel extends JPanel {

    private final AdminService adminService;
    private final DefaultTableModel tableModel;
    private final JTable sectionTable;

    public SectionManagementPanel() {
        this.adminService = new AdminService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"ID", "Course Code", "Title", "Instructor", "Time", "Room", "Capacity", "Semester", "Year"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sectionTable = new JTable(tableModel);
        sectionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(sectionTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addSectionButton = new JButton("Add New Section");
        JButton deleteSectionButton = new JButton("Delete Selected Section");
        buttonPanel.add(addSectionButton);
        buttonPanel.add(deleteSectionButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addSectionButton.addActionListener(e -> {
            showAddSectionDialog();
        });

        deleteSectionButton.addActionListener(e -> {
            deleteSelectedSection();
        });

        loadTableData();
    }

    private void loadTableData() {
        tableModel.setRowCount(0);

        List<SectionDetails> sections = adminService.getAllSectionDetails();
        for (SectionDetails section : sections) {
            Vector<Object> row = new Vector<>();
            row.add(section.getSectionId());
            row.add(section.getCourseCode());
            row.add(section.getCourseTitle());
            row.add(section.getInstructorName());
            row.add(section.getDayTime());
            row.add(section.getRoom());
            row.add(section.getCapacity());
            row.add(section.getSemester());
            row.add(section.getYear());
            tableModel.addRow(row);
        }
    }

    private void showAddSectionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Section", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Course:"), gbc);
        gbc.gridx = 1;
        List<CourseDropdownItem> courses = adminService.getCoursesForDropdown();
        JComboBox<CourseDropdownItem> courseCombo = new JComboBox<>(new Vector<>(courses));
        formPanel.add(courseCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Instructor:"), gbc);
        gbc.gridx = 1;
        List<InstructorDropdownItem> instructors = adminService.getInstructorsForDropdown();
        JComboBox<InstructorDropdownItem> instructorCombo = new JComboBox<>(new Vector<>(instructors));
        formPanel.add(instructorCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Day/Time:"), gbc);
        gbc.gridx = 1;
        JTextField dayTimeField = new JTextField(20);
        dayTimeField.setToolTipText("e.g., Mon/Wed 10-11:30");
        formPanel.add(dayTimeField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1;
        JTextField roomField = new JTextField(20);
        formPanel.add(roomField, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        JTextField capacityField = new JTextField(20);
        formPanel.add(capacityField, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1;
        JTextField semesterField = new JTextField(20);
        semesterField.setToolTipText("e.g., Fall");
        formPanel.add(semesterField, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        JTextField yearField = new JTextField(20);
        yearField.setToolTipText("e.g., 2025");
        formPanel.add(yearField, gbc);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        bottomPanel.add(saveButton);
        bottomPanel.add(cancelButton);

        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            try {
                Section section = new Section();
                CourseDropdownItem selectedCourse = (CourseDropdownItem) courseCombo.getSelectedItem();
                InstructorDropdownItem selectedInstructor = (InstructorDropdownItem) instructorCombo.getSelectedItem();

                if (selectedCourse == null || selectedInstructor == null) {
                    JOptionPane.showMessageDialog(dialog, "Error: You must select a course and an instructor.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dayTimeField.getText().isEmpty() || roomField.getText().isEmpty() || semesterField.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Error: All text fields must be filled.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                section.setCourseId(selectedCourse.getCourseId());
                section.setInstructorId(selectedInstructor.getInstructorId());
                section.setDayTime(dayTimeField.getText());
                section.setRoom(roomField.getText());
                section.setCapacity(Integer.parseInt(capacityField.getText()));
                section.setSemester(semesterField.getText());
                section.setYear(Integer.parseInt(yearField.getText()));

                String message = adminService.createSection(section);

                JOptionPane.showMessageDialog(dialog, message);
                if (message.startsWith("Success")) {
                    loadTableData();
                    dialog.dispose();
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Error: Capacity and Year must be numbers.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void deleteSelectedSection() {
        int selectedRow = sectionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int sectionId = (int) tableModel.getValueAt(selectedRow, 0);
        String courseCode = (String) tableModel.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete section " + sectionId + " (" + courseCode + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                String message = adminService.deleteSection(sectionId);
                JOptionPane.showMessageDialog(this, message);
                if (message.startsWith("Success")) {
                    loadTableData();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}