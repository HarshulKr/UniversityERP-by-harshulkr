package edu.univ.erp.domain;

public class InstructorDropdownItem {
    private final int instructorId;
    private final String instructorName;

    public InstructorDropdownItem(int instructorId, String instructorName) {
        this.instructorId = instructorId;
        this.instructorName = instructorName;
    }

    public int getInstructorId() {
        return instructorId;
    }

    @Override
    public String toString() {
        return instructorName;
    }
}