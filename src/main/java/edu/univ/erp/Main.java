package edu.univ.erp;

import edu.univ.erp.ui.auth.LoginView;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting University ERP Application...");

        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
}