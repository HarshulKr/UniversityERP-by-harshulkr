package edu.univ.erp.ui.admin;

import edu.univ.erp.service.SettingsService;

import javax.swing.*;
import java.awt.*;

public class SystemSettingsPanel extends JPanel {

    private final SettingsService settingsService;
    private final JCheckBox maintenanceModeBox;
    private final JLabel statusLabel;

    public SystemSettingsPanel() {
        this.settingsService = new SettingsService();

        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        maintenanceModeBox = new JCheckBox("Enable Maintenance Mode");
        maintenanceModeBox.setFont(new Font("SansSerif", Font.BOLD, 16));

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        boolean isCurrentlyOn = settingsService.isMaintenanceModeOn();
        updateViewState(isCurrentlyOn);

        maintenanceModeBox.addActionListener(e -> {
            boolean isSelected = maintenanceModeBox.isSelected();

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to turn Maintenance Mode " + (isSelected ? "ON" : "OFF") + "?\n" +
                            (isSelected ? "Students and instructors will be blocked from making changes." : "Students and instructors will regain write access."),
                    "Confirm Action",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                String message = settingsService.setMaintenanceMode(isSelected);
                updateViewState(isSelected);
                JOptionPane.showMessageDialog(this, message);
            } else {
                maintenanceModeBox.setSelected(!isSelected);
            }
        });

        add(maintenanceModeBox);
        add(statusLabel);
    }

    private void updateViewState(boolean isEnabled) {
        maintenanceModeBox.setSelected(isEnabled);
        if (isEnabled) {
            statusLabel.setText("Current Status: ON (Write actions are BLOCKED)");
            statusLabel.setForeground(Color.RED.darker());
        } else {
            statusLabel.setText("Current Status: OFF (System is fully operational)");
            statusLabel.setForeground(Color.GREEN.darker());
        }
    }
}