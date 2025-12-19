package edu.univ.erp.domain;

public class StudentGrade {

    private final String courseCode;
    private final String courseTitle;
    private final String component;
    private final double score;
    private final String finalGrade;

    public StudentGrade(String courseCode, String courseTitle, String component,
                        double score, String finalGrade) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.component = component;
        this.score = score;
        this.finalGrade = finalGrade;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getComponent() {
        return component;
    }

    public double getScore() {
        return score;
    }

    public String getFinalGrade() {
        return (finalGrade == null || finalGrade.isEmpty()) ? "N/A" : finalGrade;
    }
}