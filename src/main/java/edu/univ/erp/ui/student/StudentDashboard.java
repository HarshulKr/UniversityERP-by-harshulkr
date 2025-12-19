package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.SettingsService;
import edu.univ.erp.ui.auth.ChangePasswordDialog;
import edu.univ.erp.ui.auth.LoginView;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class StudentDashboard extends JFrame {

    private final Color PRIMARY_COLOR = new Color(52, 73, 94);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color WARNING_COLOR = new Color(230, 126, 34);
    private final Font MENU_FONT = new Font("SansSerif", Font.BOLD, 14);
    private final Font TAB_FONT = new Font("SansSerif", Font.BOLD, 16);
    
    private JPanel maintenanceBanner;
    private final SettingsService settingsService = new SettingsService();

    public StudentDashboard() {
        UserSession session = UserSession.getInstance();
        setTitle("Student Dashboard - " + session.getUsername());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(PRIMARY_COLOR);
        menuBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(MENU_FONT);
        fileMenu.setForeground(Color.WHITE);

        JMenuItem changePasswordItem = new JMenuItem("Change Password");
        JMenuItem logoutItem = new JMenuItem("Logout");
        JMenuItem exitItem = new JMenuItem("Exit");

        changePasswordItem.setFont(MENU_FONT);
        logoutItem.setFont(MENU_FONT);
        exitItem.setFont(MENU_FONT);

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

        maintenanceBanner = createMaintenanceBanner();
        add(maintenanceBanner, BorderLayout.NORTH);
        updateMaintenanceBanner();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(TAB_FONT);
        tabbedPane.setForeground(PRIMARY_COLOR);
        tabbedPane.setBackground(Color.WHITE);

        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        TimetablePanel timetablePanel = new TimetablePanel();
        tabbedPane.addTab("🗓️ My Timetable", timetablePanel);

        CatalogPanel catalogPanel = new CatalogPanel(timetablePanel);
        tabbedPane.addTab("📚 Course Catalog", catalogPanel);

        GradesPanel gradesPanel = new GradesPanel();
        tabbedPane.addTab("💯 My Grades", gradesPanel);

        NotificationPanel notificationPanel = new NotificationPanel();
        tabbedPane.addTab("📬 Notifications", notificationPanel);

        JPanel tabContainer = new JPanel(new BorderLayout());
        tabContainer.setBorder(new EmptyBorder(10, 10, 10, 10));
        tabContainer.setBackground(Color.WHITE);
        tabContainer.add(tabbedPane, BorderLayout.CENTER);

        add(tabContainer, BorderLayout.CENTER);
    }
    
    private JPanel createMaintenanceBanner() {
        JPanel banner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        banner.setBackground(WARNING_COLOR);
        
        JLabel icon = new JLabel("⚠️");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 16));
        
        JLabel message = new JLabel("MAINTENANCE MODE: Course registration and drops are temporarily disabled.");
        message.setFont(new Font("SansSerif", Font.BOLD, 13));
        message.setForeground(Color.WHITE);
        
        banner.add(icon);
        banner.add(message);
        banner.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        
        return banner;
    }
    
    private void updateMaintenanceBanner() {
        boolean isMaintenanceOn = settingsService.isMaintenanceModeOn();
        maintenanceBanner.setVisible(isMaintenanceOn);
    }
}