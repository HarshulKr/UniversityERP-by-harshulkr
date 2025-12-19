package edu.univ.erp.auth;

public class UserSession {

    private static UserSession instance;

    private int userId;
    private String username;
    private String role;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void startSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public void endSession() {
        this.userId = 0;
        this.username = null;
        this.role = null;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public boolean isLoggedIn() { return username != null; }
}