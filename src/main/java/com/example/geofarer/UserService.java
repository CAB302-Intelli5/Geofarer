package com.example.geofarer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean addUser(String email, String password) {
        String sql = "INSERT INTO users(email, password_hash) VALUES(?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, email);
            stmt.setString(2, hashPassword(password));
            stmt.executeUpdate();

            int userId = -1;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                     userId = generatedKeys.getInt(1);
                }
            }

            String checkSql = "SELECT email, password_hash FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    System.out.println("Database entry - User ID: " + userId +
                            ", Email: " + rs.getString("email") +
                            ", Password Hash: " + rs.getString("password_hash"));
                }
            }
            return true;
        } catch (SQLException e) {
            System.out.println("Adding user error: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateLogin(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            System.out.println("Email or password field is empty");
            return false;
        }
        String sql = "SELECT * FROM users where email = ? AND password_hash = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, hashPassword(password));
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("Login validation error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteUser(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Delete user error: " + e.getMessage());
            return false;
        }
    }
}
