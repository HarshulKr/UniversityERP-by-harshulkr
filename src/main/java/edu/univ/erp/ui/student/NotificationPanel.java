package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.service.NotificationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class NotificationPanel extends JPanel {
    
    private final NotificationService notificationService;
    private final UserSession session;
    private JPanel notificationsContainer;
    private JLabel unreadLabel;
    private JButton refreshButton;
    
    public NotificationPanel() {
        this.notificationService = new NotificationService();
        this.session = UserSession.getInstance();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(Color.WHITE);
        
        initUI();
        loadNotifications();
    }
    
    private void initUI() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("📬 Notifications");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        unreadLabel = new JLabel("Unread: 0");
        unreadLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        unreadLabel.setForeground(new Color(52, 152, 219));
        
        refreshButton = new JButton("🔄 Refresh");
        refreshButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        refreshButton.addActionListener(e -> loadNotifications());
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(unreadLabel);
        rightPanel.add(refreshButton);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void loadNotifications() {
        notificationsContainer.removeAll();
        
        int studentId = session.getUserId();
        List<NotificationService.Notification> notifications = notificationService.getNotifications(studentId);
        
        int unreadCount = notificationService.getUnreadCount(studentId);
        unreadLabel.setText("Unread: " + unreadCount);
        
        if (notifications.isEmpty()) {
            JLabel emptyLabel = new JLabel("No notifications yet.");
            emptyLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
            notificationsContainer.add(emptyLabel);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm");
            
            for (NotificationService.Notification notification : notifications) {
                JPanel notificationPanel = createNotificationCard(notification, dateFormat);
                notificationsContainer.add(notificationPanel);
                notificationsContainer.add(Box.createVerticalStrut(10));
            }
        }
        
        notificationsContainer.revalidate();
        notificationsContainer.repaint();
    }
    
    private JPanel createNotificationCard(NotificationService.Notification notification, SimpleDateFormat dateFormat) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(notification.isRead() ? Color.LIGHT_GRAY : new Color(52, 152, 219), 2),
            new EmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(notification.isRead() ? Color.WHITE : new Color(240, 248, 255));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel messageLabel = new JLabel(notification.getMessage());
        messageLabel.setFont(new Font("SansSerif", notification.isRead() ? Font.PLAIN : Font.BOLD, 14));
        card.add(messageLabel, BorderLayout.CENTER);
        
        String timestamp = dateFormat.format(notification.getCreatedAt());
        JLabel timeLabel = new JLabel(timestamp);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        timeLabel.setForeground(Color.GRAY);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(card.getBackground());
        rightPanel.add(timeLabel);
        
        if (!notification.isRead()) {
            JButton markReadButton = new JButton("Mark as Read");
            markReadButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
            markReadButton.addActionListener(e -> {
                notificationService.markAsRead(notification.getNotificationId());
                loadNotifications();
            });
            rightPanel.add(markReadButton);
        }
        
        card.add(rightPanel, BorderLayout.SOUTH);
        
        return card;
    }
}