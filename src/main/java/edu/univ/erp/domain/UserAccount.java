package edu.univ.erp.domain;

public class UserAccount {

    private int userId;
    private String username;
    private String role;

    private String rollNo;
    private String program;

    private String department;

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getRollNo() { return rollNo; }
    public String getProgram() { return program; }
    public String getDepartment() { return department; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setRole(String role) { this.role = role; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public void setProgram(String program) { this.program = program; }
    public void setDepartment(String department) { this.department = department; }
}