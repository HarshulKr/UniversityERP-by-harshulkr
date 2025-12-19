package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public class HashGenerator {

    public static void main(String[] args) {
        String passwordToHash = "stu2";

        String newHash = BCrypt.hashpw(passwordToHash, BCrypt.gensalt());

        System.out.println("Password: " + passwordToHash);
        System.out.println("New Hash: " + newHash);
        System.out.println("\nCopy this new hash and use it in your SQL UPDATE command.");
    }
}