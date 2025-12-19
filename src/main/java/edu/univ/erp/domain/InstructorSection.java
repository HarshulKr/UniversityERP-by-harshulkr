package edu.univ.erp.domain;

public class InstructorSection {
    private final int sectionId;
    private final String courseCode;
    private final String title;
    private final String dayTime;
    private final String room;

    public InstructorSection(int sectionId, String courseCode, String title, String dayTime, String room) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.title = title;
        this.dayTime = dayTime;
        this.room = room;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return courseCode + " - " + title;
    }
}