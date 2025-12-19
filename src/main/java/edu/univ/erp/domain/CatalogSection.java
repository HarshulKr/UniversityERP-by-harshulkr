package edu.univ.erp.domain;

public class CatalogSection {

    private final int sectionId;
    private final String courseCode;
    private final String courseTitle;
    private final String instructorDepartment;
    private final String dayTime;
    private final String room;
    private final int currentEnrollment;
    private final int capacity;

    public CatalogSection(int sectionId, String courseCode, String courseTitle,
                          String instructorDepartment, String dayTime, String room,
                          int currentEnrollment, int capacity) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.instructorDepartment = instructorDepartment;
        this.dayTime = dayTime;
        this.room = room;
        this.currentEnrollment = currentEnrollment;
        this.capacity = capacity;
    }

    public int getSectionId() {
        return sectionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getInstructorDepartment() {
        return instructorDepartment;
    }

    public String getDayTime() {
        return dayTime;
    }

    public String getRoom() {
        return room;
    }

    public int getCurrentEnrollment() {
        return currentEnrollment;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getEnrollmentStatus() {
        return currentEnrollment + "/" + capacity;
    }

    public boolean isFull() {
        return currentEnrollment >= capacity;
    }
}