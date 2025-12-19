package edu.univ.erp.domain;

public class EnrolledStudent {
    private final int userId;
    private final String username;
    private final String rollNo;
    private final String program;
    private final int enrollmentId;

    public EnrolledStudent(int userId, String username, String rollNo, String program, int enrollmentId) {
        this.userId = userId;
        this.username = username;
        this.rollNo = rollNo;
        this.program = program;
        this.enrollmentId = enrollmentId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getProgram() {
        return program;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }
}