package edu.univ.erp.domain;

public class CourseDropdownItem {
    private final int courseId;
    private final String courseCode;

    public CourseDropdownItem(int courseId, String courseCode) {
        this.courseId = courseId;
        this.courseCode = courseCode;
    }

    public int getCourseId() {
        return courseId;
    }

    @Override
    public String toString() {
        return courseCode;
    }
}