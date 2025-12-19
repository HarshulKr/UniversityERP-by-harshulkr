package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnector {

    private static final String AUTH_DB_URL = "jdbc:mysql://localhost:3306/auth_db";
    private static final String AUTH_DB_USER = "root";
    private static final String AUTH_DB_PASS = "harshul";

    private static final String ERP_DB_URL = "jdbc:mysql://localhost:3306/erp_db";
    private static final String ERP_DB_USER = "root";
    private static final String ERP_DB_PASS = "harshul";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL JDBC Driver", e);
        }
    }

    public static Connection getAuthConnection() throws SQLException {
        return DriverManager.getConnection(AUTH_DB_URL, AUTH_DB_USER, AUTH_DB_PASS);
    }

    public static Connection getErpConnection() throws SQLException {
        return DriverManager.getConnection(ERP_DB_URL, ERP_DB_USER, ERP_DB_PASS);
    }
}