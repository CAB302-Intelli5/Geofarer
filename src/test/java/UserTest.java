import model.DBConnection;
import model.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {
    private static final String TEST_EMAIL = "user@email.com";
    private static final String TEST_PASSWORD = "Testing.123";
    private static final String WRONG_PASSWORD = "Wrong.123";

    @BeforeEach
    public void setUp() {
        // Ensure test user does not exist before each test
        UserService.deleteUser(TEST_EMAIL);
    }

    @AfterEach
    public void tearDown() {
        // Delete test user after testing is complete
        UserService.deleteUser(TEST_EMAIL);
    try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM sqlite_sequence WHERE name='users'"))    {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Test
    public void testAddUser() {
        boolean added = UserService.addUser(TEST_EMAIL, TEST_PASSWORD);
        assertTrue(added, "User added successfully");

        // Adding the same user again should fail
        boolean addedAgain = UserService.addUser(TEST_EMAIL, TEST_PASSWORD);
        assertFalse(addedAgain, "Duplicate user not added");
    }

    @Test
    public void testValidateLogin_Success() {
        UserService.addUser(TEST_EMAIL, TEST_PASSWORD);
        assertTrue(UserService.validateLogin(TEST_EMAIL, TEST_PASSWORD),
                "Login succeeded with correct credentials");
    }

    @Test
    public void testValidateLogin_WrongPassword() {
        UserService.addUser(TEST_EMAIL, TEST_PASSWORD);
        assertFalse(UserService.validateLogin(TEST_EMAIL, WRONG_PASSWORD),
                "Login failed with incorrect password");
    }

    @Test
    public void testValidateLogin_EmptyFields() {
        assertFalse(UserService.validateLogin("", TEST_PASSWORD), "Empty email: login failed");
        assertFalse(UserService.validateLogin(TEST_EMAIL, ""), "Empty password: login failed");
        assertFalse(UserService.validateLogin(null, TEST_PASSWORD), "Null email: login failed");
        assertFalse(UserService.validateLogin(TEST_EMAIL, null), "Null password: login failed");
    }

    @Test
    public void testDeleteUser() {
        UserService.addUser(TEST_EMAIL, TEST_PASSWORD);
        boolean deleted = UserService.deleteUser(TEST_EMAIL);
        assertTrue(deleted, "User deleted successfully");

        // Deleting again should return false
        boolean deletedAgain = UserService.deleteUser(TEST_EMAIL);
        assertFalse(deletedAgain, "User doesn't exist: deletion failed");
    }
}

