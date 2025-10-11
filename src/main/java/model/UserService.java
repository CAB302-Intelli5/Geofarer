package model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    private static String hashPassword(String salt, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String salted = salt + password;
            byte[] hash = md.digest(salted.getBytes(StandardCharsets.UTF_8));
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
        String insertSql = "INSERT INTO users(email, password_hash) VALUES(?, ?)";
        String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            insertStmt.setString(1, email);
            insertStmt.setString(2, "TEMP");
            insertStmt.executeUpdate();

            int userId = -1;
            try (ResultSet generatedKeys = insertStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                     userId = generatedKeys.getInt(1);
                }
            }

            if (userId != -1) {
                String saltedHash = hashPassword(String.valueOf(userId), password);
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setString(1, saltedHash);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();
                }
            }

            return true;
        } catch (SQLException e) {
            System.out.println("Adding user error: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateLogin(String email, String password) {
       String getIdSql = "SELECT user_id FROM users WHERE email = ?";
       try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(getIdSql)) {
           stmt.setString(1, email);
           ResultSet rs = stmt.executeQuery();
           if (rs.next()) {
               int userId = rs.getInt("user_id");
               String sql = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
               try (PreparedStatement checkStmt = conn.prepareStatement(sql)) {
                   checkStmt.setString(1, email);
                   checkStmt.setString(2, hashPassword(String.valueOf(userId), password));
                   ResultSet checkRs = checkStmt.executeQuery();
                   return checkRs.next();
               }
           }

           return false;
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
