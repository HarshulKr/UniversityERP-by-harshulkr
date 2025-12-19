package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DbConnector;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BackupRestorePanel extends JPanel {
    
    private final JTextArea logArea;
    private final JButton exportButton;
    private final JButton importButton;
    private final JLabel statusLabel;
    
    private static final String[] ERP_TABLES = {
        "courses",
        "instructors",
        "students",
        "sections",
        "enrollments",
        "grades",
        "section_components",
        "settings"
    };
    
    private static final String[] AUTH_TABLES = {
        "users_auth",
        "password_history"
    };
    
    public BackupRestorePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Database Backup & Restore", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        
        JLabel info1 = new JLabel("• Export: Creates CSV backups of all tables from both databases");
        JLabel info2 = new JLabel("• Restore: Imports data from previously created CSV backups");
        JLabel info3 = new JLabel("• Backup location: project_root/backup/ (created automatically)");
        info1.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info3.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info3.setForeground(new Color(100, 100, 100));
        infoPanel.add(info1);
        infoPanel.add(info2);
        infoPanel.add(info3);
        
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        exportButton = new JButton("Export to CSV");
        importButton = new JButton("Restore from CSV");
        
        exportButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        importButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        exportButton.setPreferredSize(new Dimension(150, 40));
        importButton.setPreferredSize(new Dimension(150, 40));
        
        exportButton.addActionListener(e -> handleExport());
        importButton.addActionListener(e -> handleImport());
        
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);
        
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Operation Log"));
        add(logScroll, BorderLayout.SOUTH);
    }
    
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + " - " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    private File getBackupDirectory() {
        File projectRoot = new File(System.getProperty("user.dir"));
        File backupDir = new File(projectRoot, "backup");
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        return backupDir;
    }
    
    private void handleExport() {
        File backupDir = getBackupDirectory();
        exportButton.setEnabled(false);
        statusLabel.setText("Exporting...");
        
        new Thread(() -> {
            try {
                exportDatabases(backupDir);
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Export completed successfully!");
                    exportButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this, 
                        "Backup completed successfully!\nLocation: " + backupDir.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Export failed!");
                    exportButton.setEnabled(true);
                    JOptionPane.showMessageDialog(this, 
                        "Export failed: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    private void handleImport() {
        File backupDir = getBackupDirectory();
        
        boolean hasBackup = false;
        for (String table : ERP_TABLES) {
            if (new File(backupDir, "erp_db_" + table + ".csv").exists()) {
                hasBackup = true;
                break;
            }
        }
        if (!hasBackup) {
            for (String table : AUTH_TABLES) {
                if (new File(backupDir, "auth_db_" + table + ".csv").exists()) {
                    hasBackup = true;
                    break;
                }
            }
        }
        
        if (!hasBackup) {
            JOptionPane.showMessageDialog(this,
                "No backup found in: " + backupDir.getAbsolutePath() + "\n" +
                "Please create a backup first.",
                "No Backup Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "WARNING: This will DELETE all current data and restore from backup.\n" +
            "Backup location: " + backupDir.getAbsolutePath() + "\n" +
            "Are you sure you want to continue?",
            "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            importButton.setEnabled(false);
            statusLabel.setText("Restoring...");
            
            new Thread(() -> {
                try {
                    importDatabases(backupDir);
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Restore completed successfully!");
                        importButton.setEnabled(true);
                        JOptionPane.showMessageDialog(this, 
                            "Restore completed successfully!",
                            "Restore Complete", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Restore failed!");
                        importButton.setEnabled(true);
                        JOptionPane.showMessageDialog(this, 
                            "Restore failed: " + e.getMessage(),
                            "Restore Error", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        }
    }
    
    private void exportDatabases(File backupDir) throws Exception {
        log("Starting export...");
        log("Backup location: " + backupDir.getAbsolutePath());
        
        log("Exporting erp_db...");
        try (Connection conn = DbConnector.getErpConnection()) {
            for (String table : ERP_TABLES) {
                exportTable(conn, "erp_db", table, backupDir);
            }
        }
        
        log("Exporting auth_db...");
        try (Connection conn = DbConnector.getAuthConnection()) {
            for (String table : AUTH_TABLES) {
                exportTable(conn, "auth_db", table, backupDir);
            }
        }
        
        log("Export completed!");
    }
    
    private void exportTable(Connection conn, String database, String tableName, File backupDir) throws Exception {
        String sql = "SELECT * FROM " + database + "." + tableName;
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            File csvFile = new File(backupDir, database + "_" + tableName + ".csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) writer.print(",");
                    writer.print(metaData.getColumnName(i));
                }
                writer.println();
                
                int rowCount = 0;
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        if (i > 1) writer.print(",");
                        String value = rs.getString(i);
                        if (value == null) {
                            writer.print("");
                        } else {
                            if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                                value = "\"" + value.replace("\"", "\"\"") + "\"";
                            }
                            writer.print(value);
                        }
                    }
                    writer.println();
                    rowCount++;
                }
                log("  Exported " + tableName + ": " + rowCount + " rows");
            }
        }
    }
    
    private void importDatabases(File backupDir) throws Exception {
        log("Starting restore...");
        log("Backup location: " + backupDir.getAbsolutePath());
        
        try (Connection erpConn = DbConnector.getErpConnection();
             Connection authConn = DbConnector.getAuthConnection()) {
            
            erpConn.setAutoCommit(false);
            authConn.setAutoCommit(false);
            
            try {
                try (Statement stmt = erpConn.createStatement()) {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                }
                try (Statement stmt = authConn.createStatement()) {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                }
                
                log("Clearing existing data...");
                clearTables(erpConn, "erp_db", ERP_TABLES);
                clearTables(authConn, "auth_db", AUTH_TABLES);
                
                log("Importing erp_db...");
                for (String table : ERP_TABLES) {
                    File csvFile = new File(backupDir, "erp_db_" + table + ".csv");
                    if (csvFile.exists()) {
                        importTable(erpConn, "erp_db", table, csvFile);
                    } else {
                        log("  Warning: " + csvFile.getName() + " not found, skipping");
                    }
                }
                
                log("Importing auth_db...");
                for (String table : AUTH_TABLES) {
                    File csvFile = new File(backupDir, "auth_db_" + table + ".csv");
                    if (csvFile.exists()) {
                        importTable(authConn, "auth_db", table, csvFile);
                    } else {
                        log("  Warning: " + csvFile.getName() + " not found, skipping");
                    }
                }
                
                try (Statement stmt = erpConn.createStatement()) {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
                try (Statement stmt = authConn.createStatement()) {
                    stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
                }
                
                erpConn.commit();
                authConn.commit();
                
                log("Restore completed!");
                
            } catch (Exception e) {
                erpConn.rollback();
                authConn.rollback();
                throw e;
            } finally {
                erpConn.setAutoCommit(true);
                authConn.setAutoCommit(true);
            }
        }
    }
    
    private void clearTables(Connection conn, String database, String[] tables) throws SQLException {
        for (int i = tables.length - 1; i >= 0; i--) {
            String table = tables[i];
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM " + database + "." + table);
                log("  Cleared " + table);
            }
        }
    }
    
    private void importTable(Connection conn, String database, String tableName, File csvFile) throws Exception {
        List<String> columns = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                log("  Warning: " + csvFile.getName() + " is empty");
                return;
            }
            String[] headers = headerLine.split(",");
            for (String header : headers) {
                columns.add(header.trim());
            }
        }
        
        StringBuilder insertSql = new StringBuilder("INSERT INTO " + database + "." + tableName + " (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) insertSql.append(", ");
            insertSql.append(columns.get(i));
        }
        insertSql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) insertSql.append(", ");
            insertSql.append("?");
        }
        insertSql.append(")");
        
        int rowCount = 0;
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql.toString());
             BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            
            reader.readLine();
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] values = parseCSVLine(line);
                for (int i = 0; i < Math.min(values.length, columns.size()); i++) {
                    String value = values[i].trim();
                    if (value.isEmpty()) {
                        pstmt.setNull(i + 1, Types.VARCHAR);
                    } else {
                        pstmt.setString(i + 1, value);
                    }
                }
                pstmt.addBatch();
                rowCount++;
                
                if (rowCount % 100 == 0) {
                    pstmt.executeBatch();
                }
            }
            pstmt.executeBatch();
        }
        
        log("  Imported " + tableName + ": " + rowCount + " rows");
    }
    
    private String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        
        return values.toArray(new String[0]);
    }
}