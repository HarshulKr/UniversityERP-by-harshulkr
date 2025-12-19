package edu.univ.erp.util;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.univ.erp.domain.StudentGrade;

public class CsvExporter {

    public static void exportGradesToCsv(List<StudentGrade> grades, Component parent) {
        if (grades == null || grades.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "No grades to export.", "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Transcript as CSV");
        fileChooser.setSelectedFile(new File("MyTranscript.csv"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            @Override
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });

        int userSelection = fileChooser.showSaveDialog(parent);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (FileWriter csvWriter = new FileWriter(fileToSave)) {
                csvWriter.append("CourseCode,CourseTitle,Component,Score,FinalGrade\n");

                for (StudentGrade grade : grades) {
                    csvWriter.append(escapeCsv(grade.getCourseCode()));
                    csvWriter.append(",");
                    csvWriter.append(escapeCsv(grade.getCourseTitle()));
                    csvWriter.append(",");
                    csvWriter.append(escapeCsv(grade.getComponent()));
                    csvWriter.append(",");
                    csvWriter.append(String.valueOf(grade.getScore()));
                    csvWriter.append(",");
                    csvWriter.append(escapeCsv(grade.getFinalGrade()));
                    csvWriter.append("\n");
                }

                csvWriter.flush();
                JOptionPane.showMessageDialog(parent,
                        "Transcript successfully exported to:\n" + fileToSave.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "An error occurred while saving the file:\n" + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String escapeCsv(String data) {
        if (data == null) {
            return "";
        }
        if (data.contains(",") || data.contains("\"") || data.contains("\n")) {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }
}