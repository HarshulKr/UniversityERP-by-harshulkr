package edu.univ.erp.ui.admin;

import edu.univ.erp.domain.UserAccount;
import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class UserManagementPanel extends JPanel {

    private final AdminService adminService;
    private final DefaultTableModel tableModel;
    private final JTable userTable;

    public UserManagementPanel() {
        this.adminService = new AdminService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnNames = {"ID", "Username", "Role", "Roll No", "Program", "Department"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addUserButton = new JButton("Add New User");
        buttonPanel.add(addUserButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addUserButton.addActionListener(e -> showAddUserDialog());

        loadTableData();
    }

    private void loadTableData() {
        tableModel.setRowCount(0);

        List<UserAccount> users = adminService.getAllUserAccounts();
        for (UserAccount user : users) {
            Vector<Object> row = new Vector<>();
            row.add(user.getUserId());
            row.add(user.getUsername());
            row.add(user.getRole());
            row.add(user.getRollNo());
            row.add(user.getProgram());
            row.add(user.getDepartment());
            tableModel.addRow(row);
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Role:"), gbc);
        JRadioButton studentRadio = new JRadioButton("Student");
        studentRadio.setSelected(true);
        JRadioButton instructorRadio = new JRadioButton("Instructor");
        JRadioButton adminRadio = new JRadioButton("Admin");
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(studentRadio);
        roleGroup.add(instructorRadio);
        roleGroup.add(adminRadio);
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(studentRadio);
        radioPanel.add(instructorRadio);
        radioPanel.add(adminRadio);
        gbc.gridx = 1;
        formPanel.add(radioPanel, gbc);

        JLabel rollNoLabel = new JLabel("Roll No:");
        JTextField rollNoField = new JTextField(20);
        JLabel programLabel = new JLabel("Program:");
        JTextField programField = new JTextField(20);
        JLabel deptLabel = new JLabel("Department:");
        JTextField deptField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(rollNoLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(rollNoField, gbc);
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(programLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(programField, gbc);
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(deptLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(deptField, gbc);

        ActionListener radioListener = e -> {
            boolean isStudent = studentRadio.isSelected();
            boolean isInstructor = instructorRadio.isSelected();
            
            rollNoLabel.setEnabled(isStudent);
            rollNoField.setEnabled(isStudent);
            programLabel.setEnabled(isStudent);
            programField.setEnabled(isStudent);

            deptLabel.setEnabled(isInstructor);
            deptField.setEnabled(isInstructor);
        };
        studentRadio.addActionListener(radioListener);
        instructorRadio.addActionListener(radioListener);
        adminRadio.addActionListener(radioListener);
        radioListener.actionPerformed(null);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        bottomPanel.add(saveButton);
        bottomPanel.add(cancelButton);

        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            UserAccount user = new UserAccount();
            user.setUsername(usernameField.getText());
            
            String role = studentRadio.isSelected() ? "Student" : (instructorRadio.isSelected() ? "Instructor" : "Admin");
            user.setRole(role);
            user.setRollNo(rollNoField.getText());
            user.setProgram(programField.getText());
            user.setDepartment(deptField.getText());
            String password = new String(passwordField.getPassword());

            String message = adminService.createUserAccount(user, password);

            JOptionPane.showMessageDialog(dialog, message);
            if (message.startsWith("Success")) {
                loadTableData();
                dialog.dispose();
            }
        });

        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
}