package edu.univ.erp.domain;

public class Course {
    private int courseId;
    private String code;
    private String title;
    private int credits;

    public Course(int courseId, String code, String title, int credits) {
        this.courseId = courseId;
        this.code = code;
        this.title = title;
        this.credits = credits;
    }

    public Course(String code, String title, int credits) {
        this(-1, code, title, credits);
    }

    public int getCourseId() { return courseId; }
    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }

    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }
    public void setCredits(int credits) { this.credits = credits; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
}