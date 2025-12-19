package edu.univ.erp.ui.instructor;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.ui.auth.ChangePasswordDialog;
import edu.univ.erp.ui.auth.LoginView;

import javax.swing.*;
import java.awt.*;

public class InstructorDashboard extends JFrame {
    
    private final Color WARNING_COLOR = new Color(230, 126, 34);
    private final SettingsService settingsService = new SettingsService();

    public InstructorDashboard() {
        UserSession session = UserSession.getInstance();
        setTitle("Instructor Dashboard - " + session.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel northPanel = new JPanel(new BorderLayout());
        
        JPanel maintenanceBanner = createMaintenanceBanner();
        maintenanceBanner.setVisible(settingsService.isMaintenanceModeOn());
        northPanel.add(maintenanceBanner, BorderLayout.NORTH);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + session.getUsername() + "! (Role: " + session.getRole() + ")", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        northPanel.add(welcomeLabel, BorderLayout.CENTER);
        
        add(northPanel, BorderLayout.NORTH);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem changePasswordItem = new JMenuItem("Change Password");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");

        changePasswordItem.addActionListener(e -> {
            new ChangePasswordDialog(this).setVisible(true);
        });

        logoutItem.addActionListener(e -> {
            session.endSession();
            new LoginView().setVisible(true);
            this.dispose();
        });

        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(changePasswordItem);
        fileMenu.addSeparator();
        fileMenu.add(logoutItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        add(new GradebookPanel(), BorderLayout.CENTER);
    }
    
    private JPanel createMaintenanceBanner() {
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        banner.setBackground(WARNING_COLOR);
        
        JLabel icon = new JLabel("⚠️");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 16));
        
        JLabel message = new JLabel("MAINTENANCE MODE: Grade entry and calculations are temporarily disabled.");
        message.setFont(new Font("SansSerif", Font.BOLD, 13));
        message.setForeground(Color.WHITE);
        
        banner.add(icon);
        banner.add(message);
        banner.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        return banner;
    }
}