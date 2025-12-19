package edu.univ.erp.ui.student;

import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.StudentDAO;
import edu.univ.erp.domain.StudentGrade;
import edu.univ.erp.util.CsvExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

public class GradesPanel extends JPanel {

    private JPanel coursesContainer;
    private StudentDAO studentDAO;
    private UserSession session;

    private List<StudentGrade> currentGrades;

    public GradesPanel() {
        this.studentDAO = new StudentDAO();
        this.session = UserSession.getInstance();

        setLayout(new BorderLayout(10, 10));
        initUI();
        loadGradesData();
    }

    private void initUI() {
        JLabel titleLabel = new JLabel("My Grades");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        coursesContainer = new JPanel();
        coursesContainer.setLayout(new BoxLayout(coursesContainer, BoxLayout.Y_AXIS));
        coursesContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(coursesContainer);
        add(scrollPane, BorderLayout.CENTER);

        JButton downloadButton = new JButton("Download Transcript (CSV)");
        downloadButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDownloadTranscript();
            }
        });
        add(downloadButton, BorderLayout.SOUTH);
    }

    private void loadGradesData() {
        int studentId = session.getUserId();
        currentGrades = studentDAO.getStudentGrades(studentId);

        coursesContainer.removeAll();

        if (currentGrades == null || currentGrades.isEmpty()) {
            JLabel empty = new JLabel("No grades found.");
            empty.setFont(new Font("SansSerif", Font.ITALIC, 14));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            coursesContainer.add(empty);
            revalidate();
            repaint();
            return;
        }

        Map<String, CourseGroup> groups = new LinkedHashMap<>();

        for (StudentGrade g : currentGrades) {
            String courseCode = safeString(g.getCourseCode());
            String courseTitle = safeString(g.getCourseTitle());
            String key = courseCode + "||" + courseTitle;

            CourseGroup group = groups.computeIfAbsent(key, k -> new CourseGroup(courseCode, courseTitle));

            String comp = safeString(g.getComponent());
            String finalGrade = safeString(g.getFinalGrade());

            long ts = extractTimestamp(g);

            if (!comp.isEmpty()) {
                StudentGrade existing = group.components.get(comp);
                if (existing == null || ts >= extractTimestamp(existing)) {
                    group.components.put(comp, g);
                }
            }

            if (!finalGrade.isEmpty()) {
                StudentGrade currentFinal = group.latestFinalGrade;
                if (currentFinal == null || ts >= extractTimestamp(currentFinal)) {
                    group.latestFinalGrade = g;
                }
            }
        }

        for (CourseGroup group : groups.values()) {
            JPanel coursePanel = buildCoursePanel(group);
            coursePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            coursesContainer.add(coursePanel);
            coursesContainer.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        revalidate();
        repaint();
    }

    private JPanel buildCoursePanel(CourseGroup group) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        String finalGradeLabel = group.latestFinalGrade != null ? group.latestFinalGrade.getFinalGrade() : "";
        String title = group.courseCode + " — " + group.courseTitle + (finalGradeLabel.isEmpty() ? "" : "    Final Grade: " + finalGradeLabel);

        TitledBorder tb = BorderFactory.createTitledBorder(title);
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 14));
        panel.setBorder(tb);

        String[] columns = {"Component", "Score"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        for (Map.Entry<String, StudentGrade> e : group.components.entrySet()) {
            StudentGrade sg = e.getValue();
            Object scoreObj = sg.getScore();
            String scoreStr = scoreObj != null ? String.valueOf(scoreObj) : "";
            model.addRow(new Object[]{e.getKey(), scoreStr});
        }

        JTable table = new JTable(model);
        table.setRowHeight(22);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(600, Math.min(6, group.components.size()) * 26 + 24));
        panel.add(tableScroll, BorderLayout.CENTER);

        return panel;
    }

    private void handleDownloadTranscript() {
        CsvExporter.exportGradesToCsv(currentGrades, this);
    }

    private static String safeString(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static long extractTimestamp(StudentGrade g) {
        if (g == null) return 0L;

        String[] candidates = {"getUpdatedAt", "getUpdatedOn", "getTimestamp", "getCreatedAt", "getDate", "getTime", "getUpdated"};

        for (String methodName : candidates) {
            try {
                Method m = g.getClass().getMethod(methodName);
                Object val = m.invoke(g);
                if (val == null) continue;
                if (val instanceof Long) return (Long) val;
                if (val instanceof Integer) return ((Integer) val).longValue();
                if (val instanceof Date) return ((Date) val).getTime();
                if (val instanceof java.sql.Timestamp) return ((java.sql.Timestamp) val).getTime();
                if (val instanceof String) {
                    try { return Long.parseLong(((String) val).trim()); } catch (NumberFormatException ignored) {}
                }
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ignored) {
            }
        }

        return 0L;
    }

    private static class CourseGroup {
        final String courseCode;
        final String courseTitle;
        final Map<String, StudentGrade> components = new LinkedHashMap<>();
        StudentGrade latestFinalGrade = null;

        CourseGroup(String code, String title) {
            this.courseCode = code;
            this.courseTitle = title;
        }
    }
}