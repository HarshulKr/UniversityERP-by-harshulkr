package edu.univ.erp.domain;

public class EnrolledCourse {

    private final int enrollmentId;
    private final String courseCode;
    private final String courseTitle;
    private final String dayTime;
    private final String room;
    private final String instructorDepartment;

    public EnrolledCourse(int enrollmentId, String courseCode, String courseTitle,
                          String dayTime, String room, String instructorDepartment) {
        this.enrollmentId = enrollmentId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.dayTime = dayTime;
        this.room = room;
        this.instructorDepartment = instructorDepartment;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getDayTime() {
        return dayTime;
    }

    public String getRoom() {
        return room;
    }

    public String getInstructorDepartment() {
        return instructorDepartment;
    }
}