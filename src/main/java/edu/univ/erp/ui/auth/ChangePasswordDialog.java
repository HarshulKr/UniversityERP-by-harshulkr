package edu.univ.erp.ui.auth;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.UserSession;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ChangePasswordDialog extends JDialog {
    
    private final AuthService authService;
    private final int userId;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JLabel messageLabel;

    public ChangePasswordDialog(Frame parent) {
        super(parent, "Change Password", true);
        this.authService = new AuthService();
        this.userId = UserSession.getInstance().getUserId();
        
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        initUI();
    }
    
    private void initUI() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.LINE_START;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Old Password:"), gbc);
        gbc.gridx = 1;
        oldPasswordField = new JPasswordField(20);
        oldPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(oldPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        newPasswordField = new JPasswordField(20);
        newPasswordField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        formPanel.add(newPasswordField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        messageLabel = new JLabel(" ");
        messageLabel.setForeground(Color.RED);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        formPanel.add(messageLabel, gbc);
        
        add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        JButton showHistoryButton = new JButton("Show Password History");
        JButton changeButton = new JButton("Change Password");
        JButton cancelButton = new JButton("Cancel");
        
        showHistoryButton.addActionListener(e -> showPasswordHistory());
        changeButton.addActionListener(e -> handleChangePassword());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(showHistoryButton);
        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void handleChangePassword() {
        String oldPassword = new String(oldPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        
        if (oldPassword.isEmpty()) {
            messageLabel.setText("Please enter your old password.");
            return;
        }
        
        if (newPassword.isEmpty()) {
            messageLabel.setText("Please enter a new password.");
            return;
        }
        
        if (oldPassword.equals(newPassword)) {
            messageLabel.setText("New password must be different from old password.");
            return;
        }
        
        messageLabel.setText(" ");
        
        String result = authService.changePassword(userId, oldPassword, newPassword);
        
        if (result.startsWith("Success")) {
            JOptionPane.showMessageDialog(this, result, "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            messageLabel.setText(result);
        }
    }
    
    private void showPasswordHistory() {
        List<String> history = authService.getPasswordHistory(userId);
        
        if (history == null) {
            JOptionPane.showMessageDialog(this, 
                "Error: Could not retrieve password history. The password_history table may not exist.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No password history found. This is your first password change.", 
                "Password History", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JDialog historyDialog = new JDialog(this, "Password History", true);
        historyDialog.setSize(600, 400);
        historyDialog.setLocationRelativeTo(this);
        
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String entry : history) {
            listModel.addElement(entry);
        }
        
        JList<String> historyList = new JList<>(listModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(historyList);
        historyDialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> historyDialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(closeButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        historyDialog.setVisible(true);
    }
}