package edu.univ.erp.auth;

public class LoginResult {
    private final boolean success;
    private final String message;
    private final int userId;
    private final String role;

    public LoginResult(boolean success, String message, int userId, String role) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.role = role;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public int getUserId() { return userId; }
    public String getRole() { return role; }
}