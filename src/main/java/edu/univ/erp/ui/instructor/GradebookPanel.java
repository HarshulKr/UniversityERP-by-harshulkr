package edu.univ.erp.ui.instructor;

import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.InstructorSection;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class GradebookPanel extends JPanel {

    private final InstructorService instructorService;
    private JComboBox<InstructorSection> sectionComboBox;
    private DefaultTableModel tableModel;
    private JTable studentTable;
    private JLabel courseInfoLabel;
    private List<GradeComponent> currentGradebook;

    private List<String> componentNames = new java.util.ArrayList<>();
    private java.util.Map<String, Integer> componentWeights = new java.util.LinkedHashMap<>();

    private int selectedSectionId = -1;
    
    private JLabel statsLabel;

    public GradebookPanel() {
        this.instructorService = new InstructorService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initUI();
        loadMySections();
    }

    private void initUI() {
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        selectorPanel.add(new JLabel("Select Section:"));

        sectionComboBox = new JComboBox<>();
        sectionComboBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        selectorPanel.add(sectionComboBox);

        courseInfoLabel = new JLabel("Status: Please select a section to begin grading.");
        courseInfoLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        selectorPanel.add(courseInfoLabel);

        add(selectorPanel, BorderLayout.NORTH);

        String[] initialColumns = {"Enrollment ID", "Roll No", "Student Name", "Final Grade"};
        tableModel = new DefaultTableModel(initialColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 3 && column < 3 + componentNames.size();
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(25);
        studentTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        studentTable.putClientProperty("terminateEditOnFocusLost", true);

        TableColumnModel columnModel = studentTable.getColumnModel();
        columnModel.getColumn(0).setMinWidth(0);
        columnModel.getColumn(0).setMaxWidth(0);
        columnModel.getColumn(0).setWidth(0);

        add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        JButton editComponentsButton = new JButton("Edit Components");
        JButton setDeadlineButton = new JButton("Set Drop Deadline");
        JButton saveButton = new JButton("Save Scores");
        JButton computeButton = new JButton("Compute Final Grades");
        JButton exportButton = new JButton("Export CSV");

        editComponentsButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        setDeadlineButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        computeButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        exportButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        buttonPanel.add(editComponentsButton);
        buttonPanel.add(setDeadlineButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(computeButton);
        buttonPanel.add(exportButton);
        
        statsLabel = new JLabel("Class Stats: --");
        statsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(statsLabel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        sectionComboBox.addActionListener(e -> {
            InstructorSection selectedSection = (InstructorSection) sectionComboBox.getSelectedItem();
            if (selectedSection != null && selectedSection.getSectionId() != -1) {
                selectedSectionId = selectedSection.getSectionId();
                courseInfoLabel.setText("Section: " + selectedSection);
                loadComponentsAndGradebook(selectedSectionId);
            } else {
                selectedSectionId = -1;
                courseInfoLabel.setText("Status: No section selected.");
                componentNames.clear();
                rebuildTableStructure();
                tableModel.setRowCount(0);
            }
        });

        editComponentsButton.addActionListener(e -> handleEditComponents());

        setDeadlineButton.addActionListener(e -> handleSetDropDeadline());

        saveButton.addActionListener(e -> handleSaveScores());

        computeButton.addActionListener(e -> handleComputeGrades());

        exportButton.addActionListener(e -> handleExportCSV());
    }

    private void loadMySections() {
        List<InstructorSection> sections = instructorService.getMySections();
        sectionComboBox.removeAllItems();

        if (sections.isEmpty()) {
            sectionComboBox.addItem(new InstructorSection(-1, "No Sections Assigned", "", "", ""));
            courseInfoLabel.setText("Status: No sections currently assigned to you.");
            return;
        }

        for (InstructorSection section : sections) {
            sectionComboBox.addItem(section);
        }
        sectionComboBox.setSelectedIndex(0);
    }

    private void loadComponentsAndGradebook(int sectionId) {
        componentWeights = instructorService.getComponentWeightsForSection(sectionId);
        
        if (componentWeights.isEmpty()) {
            componentWeights.put("Quiz/Assignments", 20);
            componentWeights.put("Midterm", 30);
            componentWeights.put("Final Exam", 50);
        }
        
        componentNames = new java.util.ArrayList<>(componentWeights.keySet());
        
        rebuildTableStructure();
        loadGradebookData(sectionId);
    }
    
    private void rebuildTableStructure() {
        String[] fixedColumns = {"Enrollment ID", "Roll No", "Student Name"};
        int totalColumns = fixedColumns.length + componentNames.size() + 1;
        String[] columnNames = new String[totalColumns];
        
        System.arraycopy(fixedColumns, 0, columnNames, 0, fixedColumns.length);
        for (int i = 0; i < componentNames.size(); i++) {
            String name = componentNames.get(i);
            Integer weight = componentWeights.getOrDefault(name, 0);
            columnNames[3 + i] = name + " (" + weight + "%)";
        }
        columnNames[columnNames.length - 1] = "Final Grade";
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 3 && column < 3 + componentNames.size();
            }
        };
        
        studentTable.setModel(tableModel);
        hideEnrollmentIdColumn();
    }
    
    private void hideEnrollmentIdColumn() {
        TableColumnModel columnModel = studentTable.getColumnModel();
        if (columnModel.getColumnCount() > 0) {
            columnModel.getColumn(0).setMinWidth(0);
            columnModel.getColumn(0).setMaxWidth(0);
            columnModel.getColumn(0).setPreferredWidth(0);
        }
    }

    private void loadGradebookData(int sectionId) {
        currentGradebook = instructorService.getGradebookData(sectionId);
        tableModel.setRowCount(0);

        for (GradeComponent student : currentGradebook) {
            Vector<Object> row = new Vector<>();
            row.add(student.getEnrollmentId());
            row.add(student.getRollNo());
            row.add(student.getUsername());

            for (String componentKey : componentNames) {
                Double score = student.getScore(componentKey);
                row.add(score);
            }

            row.add(student.getFinalGrade());

            tableModel.addRow(row);
        }
        
        updateStats();
    }
    
    private void updateStats() {
        if (currentGradebook == null || currentGradebook.isEmpty()) {
            statsLabel.setText("Class Stats: No students");
            return;
        }
        
        java.util.List<Double> allScores = new java.util.ArrayList<>();
        for (GradeComponent student : currentGradebook) {
            for (String key : componentNames) {
                Double score = student.getScore(key);
                if (score != null) {
                    allScores.add(score);
                }
            }
        }
        
        if (allScores.isEmpty()) {
            statsLabel.setText("Class Stats: Students: " + currentGradebook.size() + " | Mean: -- | Median: --");
            return;
        }
        
        double sum = 0;
        for (Double s : allScores) {
            sum += s;
        }
        double mean = sum / allScores.size();
        
        java.util.Collections.sort(allScores);
        double median;
        int size = allScores.size();
        if (size % 2 == 0) {
            median = (allScores.get(size / 2 - 1) + allScores.get(size / 2)) / 2.0;
        } else {
            median = allScores.get(size / 2);
        }
        
        statsLabel.setText(String.format("Class Stats: Students: %d | Mean: %.2f | Median: %.2f", 
                currentGradebook.size(), mean, median));
    }

    private void handleSaveScores() {
        if (selectedSectionId == -1) return;

        if (instructorService.isMaintenanceModeOn()) {
            JOptionPane.showMessageDialog(this,
                    "System is in Maintenance Mode. Grade entry is blocked.",
                    "Action Blocked",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (componentNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No components defined. Use 'Edit Components' to add some.", "No Components", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            GradeComponent student = currentGradebook.get(i);

            for (int j = 0; j < componentNames.size(); j++) {
                Object scoreValue = tableModel.getValueAt(i, j + 3);

                try {
                    Double score = null;
                    if (scoreValue != null && !scoreValue.toString().trim().isEmpty()) {
                        score = Double.parseDouble(scoreValue.toString().trim());
                        if (score < 0 || score > 100) {
                            JOptionPane.showMessageDialog(this, "Error: Score for " + student.getUsername() + " must be between 0 and 100.", "Input Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    student.setScore(componentNames.get(j), score);
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Invalid score '" + scoreValue + "' entered for " + student.getUsername() + ". Please enter numbers only.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        String result = instructorService.saveScores(currentGradebook, selectedSectionId, componentNames);

        JOptionPane.showMessageDialog(this, result);
        if (result.startsWith("Success")) {
            loadGradebookData(selectedSectionId);
        }
    }

    private void handleComputeGrades() {
        if (selectedSectionId == -1) return;

        if (instructorService.isMaintenanceModeOn()) {
            JOptionPane.showMessageDialog(this,
                    "System is in Maintenance Mode. Grade calculation is blocked.",
                    "Action Blocked",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        handleSaveScores();

        String result = instructorService.computeAndSaveFinalGrades(currentGradebook, componentWeights, selectedSectionId);

        JOptionPane.showMessageDialog(this, result);
        if (result.startsWith("Success")) {
            loadGradebookData(selectedSectionId);
        }
    }
    
    private void handleEditComponents() {
        if (selectedSectionId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "No Section", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Grade Components", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        String[] columnNames = {"Component Name", "Weight (%)"};
        DefaultTableModel compTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        
        for (String comp : componentNames) {
            Integer weight = componentWeights.getOrDefault(comp, 0);
            compTableModel.addRow(new Object[]{comp, weight});
        }
        
        JTable compTable = new JTable(compTableModel);
        compTable.setRowHeight(25);
        compTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        compTable.putClientProperty("terminateEditOnFocusLost", true);
        
        compTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        compTable.getColumnModel().getColumn(1).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(compTable);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JLabel totalLabel = new JLabel("Total Weight: 0%");
        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        Runnable updateTotal = () -> {
            int total = 0;
            for (int i = 0; i < compTableModel.getRowCount(); i++) {
                try {
                    Object val = compTableModel.getValueAt(i, 1);
                    if (val != null) {
                        total += Integer.parseInt(val.toString().trim());
                    }
                } catch (NumberFormatException ex) {
                }
            }
            totalLabel.setText("Total Weight: " + total + "%" + (total != 100 ? " (should be 100%)" : " ✓"));
            totalLabel.setForeground(total == 100 ? new Color(0, 128, 0) : Color.RED);
        };
        
        compTableModel.addTableModelListener(e -> updateTotal.run());
        updateTotal.run();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        JButton addButton = new JButton("Add Component");
        JButton removeButton = new JButton("Remove Selected");
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            compTableModel.addRow(new Object[]{"New Component", 0});
        });

        removeButton.addActionListener(e -> {
            int selectedRow = compTable.getSelectedRow();
            if (selectedRow >= 0) {
                compTableModel.removeRow(selectedRow);
            }
        });

        okButton.addActionListener(e -> {
            if (compTable.isEditing()) {
                compTable.getCellEditor().stopCellEditing();
            }
            
            if (compTableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dialog, "You must have at least one component.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.util.List<String> newNames = new java.util.ArrayList<>();
            java.util.Map<String, Integer> newWeights = new java.util.LinkedHashMap<>();
            int totalWeight = 0;
            
            for (int i = 0; i < compTableModel.getRowCount(); i++) {
                String name = compTableModel.getValueAt(i, 0).toString().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Component name cannot be empty (row " + (i+1) + ").", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (newNames.contains(name)) {
                    JOptionPane.showMessageDialog(dialog, "Duplicate component name: " + name, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int weight;
                try {
                    weight = Integer.parseInt(compTableModel.getValueAt(i, 1).toString().trim());
                    if (weight < 0 || weight > 100) {
                        JOptionPane.showMessageDialog(dialog, "Weight must be between 0 and 100 (row " + (i+1) + ").", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid weight in row " + (i+1) + ". Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                newNames.add(name);
                newWeights.put(name, weight);
                totalWeight += weight;
            }
            
            if (totalWeight != 100) {
                int confirm = JOptionPane.showConfirmDialog(dialog, 
                    "Total weight is " + totalWeight + "% (not 100%). Continue anyway?", 
                    "Weight Warning", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            componentNames.clear();
            componentNames.addAll(newNames);
            componentWeights.clear();
            componentWeights.putAll(newWeights);
            
            String saveResult = instructorService.saveComponentWeights(selectedSectionId, newWeights);
            if (!saveResult.startsWith("Success") && !saveResult.contains("doesn't exist")) {
                System.out.println("Note: " + saveResult);
            }
            
            dialog.dispose();
            rebuildTableStructure();
            loadGradebookData(selectedSectionId);
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.add(okButton);
        actionPanel.add(cancelButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(totalLabel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.add(actionPanel, BorderLayout.SOUTH);
        
        dialog.add(southPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void handleSetDropDeadline() {
        if (selectedSectionId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "No Section", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Set Drop Deadline", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        java.sql.Date currentDeadline = instructorService.getDropDeadline(selectedSectionId);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        datePanel.add(new JLabel("Drop Deadline:"));
        
        java.util.Calendar cal = java.util.Calendar.getInstance();
        if (currentDeadline != null) {
            cal.setTime(currentDeadline);
        }
        
        JSpinner dateSpinner = new JSpinner(new javax.swing.SpinnerDateModel(
            cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        dateSpinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        datePanel.add(dateSpinner);
        mainPanel.add(datePanel, BorderLayout.CENTER);
        
        JLabel currentLabel = new JLabel();
        if (currentDeadline != null) {
            currentLabel.setText("Current deadline: " + currentDeadline.toString());
        } else {
            currentLabel.setText("No deadline set");
        }
        currentLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        currentLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        mainPanel.add(currentLabel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JButton clearButton = new JButton("Clear Deadline");

        saveButton.addActionListener(e -> {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());
            
            String result = instructorService.setDropDeadline(selectedSectionId, sqlDate);
            JOptionPane.showMessageDialog(dialog, result);
            if (result.startsWith("Success")) {
                dialog.dispose();
            }
        });

        clearButton.addActionListener(e -> {
            String sql = "UPDATE erp_db.sections SET drop_deadline = NULL WHERE section_id = ?";
            try (java.sql.Connection conn = edu.univ.erp.data.DbConnector.getErpConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, selectedSectionId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Success! Drop deadline cleared.");
                dialog.dispose();
            } catch (java.sql.SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error clearing deadline: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(cancelButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void handleExportCSV() {
        if (selectedSectionId == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section first.", "No Section", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (currentGradebook == null || currentGradebook.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No gradebook data to export.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        InstructorSection selectedSection = (InstructorSection) sectionComboBox.getSelectedItem();
        String sectionName = selectedSection != null ? selectedSection.getCourseCode() : "Section";
        String filename = "Gradebook_" + sectionName + ".csv";
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Gradebook to CSV");
        fileChooser.setSelectedFile(new java.io.File(filename));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(java.io.File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".csv");
            }
            
            try (java.io.FileWriter csvWriter = new java.io.FileWriter(fileToSave)) {
                String sectionInfo = selectedSection != null ? selectedSection.toString() : "Section " + selectedSectionId;
                
                csvWriter.append("Section: ").append(escapeCsv(sectionInfo)).append("\n");
                csvWriter.append("Export Date: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append("\n");
                csvWriter.append("\n");
                
                StringBuilder header = new StringBuilder("Roll No,Student Name");
                for (String component : componentNames) {
                    header.append(",").append(escapeCsv(component));
                }
                header.append(",Final Grade\n");
                csvWriter.append(header.toString());
                
                java.util.List<Double> allScores = new java.util.ArrayList<>();
                
                for (GradeComponent student : currentGradebook) {
                    csvWriter.append(escapeCsv(student.getRollNo()));
                    csvWriter.append(",");
                    csvWriter.append(escapeCsv(student.getUsername()));
                    
                    for (String component : componentNames) {
                        csvWriter.append(",");
                        Double score = student.getScore(component);
                        if (score != null) {
                            csvWriter.append(String.valueOf(score));
                            allScores.add(score);
                        } else {
                            csvWriter.append("");
                        }
                    }
                    
                    csvWriter.append(",");
                    csvWriter.append(escapeCsv(student.getFinalGrade()));
                    csvWriter.append("\n");
                }
                
                csvWriter.append("\n");
                csvWriter.append("Statistics\n");
                
                if (!allScores.isEmpty()) {
                    double sum = 0;
                    for (Double s : allScores) {
                        sum += s;
                    }
                    double mean = sum / allScores.size();
                    
                    java.util.Collections.sort(allScores);
                    double median;
                    int size = allScores.size();
                    if (size % 2 == 0) {
                        median = (allScores.get(size / 2 - 1) + allScores.get(size / 2)) / 2.0;
                    } else {
                        median = allScores.get(size / 2);
                    }
                    
                    csvWriter.append("Total Students,").append(String.valueOf(currentGradebook.size())).append("\n");
                    csvWriter.append("Total Scores,").append(String.valueOf(allScores.size())).append("\n");
                    csvWriter.append("Mean,").append(String.format("%.2f", mean)).append("\n");
                    csvWriter.append("Median,").append(String.format("%.2f", median)).append("\n");
                } else {
                    csvWriter.append("Total Students,").append(String.valueOf(currentGradebook.size())).append("\n");
                    csvWriter.append("Mean,N/A\n");
                    csvWriter.append("Median,N/A\n");
                }
                
                csvWriter.flush();
                
                JOptionPane.showMessageDialog(this,
                    "Gradebook successfully exported to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (java.io.IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "An error occurred while saving the file:\n" + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String escapeCsv(String data) {
        if (data == null) {
            return "";
        }
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}