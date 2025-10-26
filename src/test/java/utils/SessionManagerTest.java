package utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless tests for SessionManager utility following AAA pattern
 * Tests session management logic without UI components
 */
public class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    public void setUp() {
        // Arrange - Get a fresh SessionManager instance
        sessionManager = SessionManager.getInstance();
        // Ensure clean state by logging out any existing session
        sessionManager.logout();
    }

    @AfterEach
    public void tearDown() {
        // Clean up - Ensure session is logged out after each test
        if (sessionManager != null) {
            sessionManager.logout();
        }
    }

    @Test
    public void testGetInstanceReturnsSameInstance() {
        // Arrange & Act
        SessionManager instance1 = SessionManager.getInstance();
        SessionManager instance2 = SessionManager.getInstance();

        // Assert
        assertSame(instance1, instance2, 
                "getInstance should return the same singleton instance");
    }

    @Test
    public void testInitialStateNotLoggedIn() {
        // Arrange - Fresh session from setUp

        // Act & Assert
        assertFalse(sessionManager.isLoggedIn(), 
                "User should not be logged in initially");
        assertNull(sessionManager.getCurrentUserId(), 
                "User ID should be null when not logged in");
        assertNull(sessionManager.getCurrentUserEmail(), 
                "Email should be null when not logged in");
    }

    @Test
    public void testLoginSuccessfully() {
        // Arrange
        int testUserId = 42;
        String testEmail = "test@example.com";

        // Act
        sessionManager.login(testUserId, testEmail);

        // Assert
        assertTrue(sessionManager.isLoggedIn(), 
                "User should be logged in after login");
        assertEquals(testUserId, sessionManager.getCurrentUserId(), 
                "User ID should match the logged in user");
        assertEquals(testEmail, sessionManager.getCurrentUserEmail(), 
                "Email should match the logged in user");
    }

    @Test
    public void testLoginWithDifferentUsers() {
        // Arrange
        int firstUserId = 1;
        String firstEmail = "user1@example.com";
        int secondUserId = 2;
        String secondEmail = "user2@example.com";

        // Act
        sessionManager.login(firstUserId, firstEmail);
        sessionManager.login(secondUserId, secondEmail);

        // Assert
        assertEquals(secondUserId, sessionManager.getCurrentUserId(), 
                "User ID should be updated to the latest login");
        assertEquals(secondEmail, sessionManager.getCurrentUserEmail(), 
                "Email should be updated to the latest login");
        assertTrue(sessionManager.isLoggedIn());
    }

    @Test
    public void testLogoutClearsSession() {
        // Arrange
        sessionManager.login(123, "user@example.com");
        assertTrue(sessionManager.isLoggedIn());

        // Act
        sessionManager.logout();

        // Assert
        assertFalse(sessionManager.isLoggedIn(), 
                "User should not be logged in after logout");
        assertNull(sessionManager.getCurrentUserId(), 
                "User ID should be null after logout");
        assertNull(sessionManager.getCurrentUserEmail(), 
                "Email should be null after logout");
    }

    @Test
    public void testLogoutWhenNotLoggedIn() {
        // Arrange - User is not logged in

        // Act
        sessionManager.logout();

        // Assert
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUserId());
        assertNull(sessionManager.getCurrentUserEmail());
    }

    @Test
    public void testMultipleLogoutCalls() {
        // Arrange
        sessionManager.login(456, "test@test.com");

        // Act
        sessionManager.logout();
        sessionManager.logout();
        sessionManager.logout();

        // Assert
        assertFalse(sessionManager.isLoggedIn());
        assertNull(sessionManager.getCurrentUserId());
        assertNull(sessionManager.getCurrentUserEmail());
    }

    @Test
    public void testRequireLoginWhenLoggedIn() {
        // Arrange
        sessionManager.login(789, "logged@example.com");

        // Act
        boolean result = sessionManager.requireLogin();

        // Assert
        assertTrue(result, 
                "requireLogin should return true when user is logged in");
    }

    @Test
    public void testRequireLoginWhenNotLoggedIn() {
        // Arrange - No login

        // Act
        boolean result = sessionManager.requireLogin();

        // Assert
        assertFalse(result, 
                "requireLogin should return false when user is not logged in");
    }

    @Test
    public void testLoginWithNullEmail() {
        // Arrange
        int userId = 100;
        String nullEmail = null;

        // Act
        sessionManager.login(userId, nullEmail);

        // Assert
        assertTrue(sessionManager.isLoggedIn(), 
                "Should be logged in even with null email");
        assertEquals(userId, sessionManager.getCurrentUserId());
        assertNull(sessionManager.getCurrentUserEmail(), 
                "Email should be null as provided");
    }

    @Test
    public void testLoginWithEmptyEmail() {
        // Arrange
        int userId = 200;
        String emptyEmail = "";

        // Act
        sessionManager.login(userId, emptyEmail);

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(userId, sessionManager.getCurrentUserId());
        assertEquals(emptyEmail, sessionManager.getCurrentUserEmail());
    }

    @Test
    public void testLoginWithZeroUserId() {
        // Arrange
        int zeroUserId = 0;
        String email = "zero@example.com";

        // Act
        sessionManager.login(zeroUserId, email);

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(zeroUserId, sessionManager.getCurrentUserId());
    }

    @Test
    public void testLoginWithNegativeUserId() {
        // Arrange
        int negativeUserId = -1;
        String email = "negative@example.com";

        // Act
        sessionManager.login(negativeUserId, email);

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(negativeUserId, sessionManager.getCurrentUserId());
    }

    @Test
    public void testIsLoggedInRequiresBothUserIdAndFlag() {
        // Arrange
        sessionManager.login(999, "test@example.com");
        assertTrue(sessionManager.isLoggedIn());

        // Act - Logout sets userId to null and flag to false
        sessionManager.logout();

        // Assert
        assertFalse(sessionManager.isLoggedIn(), 
                "isLoggedIn should be false when userId is null");
    }

    @Test
    public void testSessionPersistenceAcrossMultipleCalls() {
        // Arrange
        int userId = 555;
        String email = "persistent@example.com";
        sessionManager.login(userId, email);

        // Act - Call getters multiple times
        Integer id1 = sessionManager.getCurrentUserId();
        Integer id2 = sessionManager.getCurrentUserId();
        String email1 = sessionManager.getCurrentUserEmail();
        String email2 = sessionManager.getCurrentUserEmail();

        // Assert
        assertEquals(id1, id2, "User ID should remain consistent");
        assertEquals(email1, email2, "Email should remain consistent");
        assertEquals(userId, id1);
        assertEquals(email, email1);
    }

    @Test
    public void testLoginOverwritesPreviousSession() {
        // Arrange
        sessionManager.login(1, "first@example.com");
        Integer firstId = sessionManager.getCurrentUserId();
        String firstEmail = sessionManager.getCurrentUserEmail();

        // Act
        sessionManager.login(2, "second@example.com");

        // Assert
        assertNotEquals(firstId, sessionManager.getCurrentUserId(), 
                "User ID should change after new login");
        assertNotEquals(firstEmail, sessionManager.getCurrentUserEmail(), 
                "Email should change after new login");
    }

    @Test
    public void testRequireLoginMatchesIsLoggedIn() {
        // Test case 1: Not logged in
        assertFalse(sessionManager.requireLogin());
        assertFalse(sessionManager.isLoggedIn());

        // Test case 2: Logged in
        sessionManager.login(777, "match@example.com");
        assertTrue(sessionManager.requireLogin());
        assertTrue(sessionManager.isLoggedIn());

        // Test case 3: Logged out
        sessionManager.logout();
        assertFalse(sessionManager.requireLogin());
        assertFalse(sessionManager.isLoggedIn());
    }

    @Test
    public void testGetCurrentUserIdTypeConsistency() {
        // Arrange & Act
        sessionManager.login(12345, "type@example.com");
        Integer userId = sessionManager.getCurrentUserId();

        // Assert
        assertNotNull(userId);
        assertTrue(userId instanceof Integer, 
                "getCurrentUserId should return Integer type");
        assertEquals(12345, userId.intValue());
    }

    @Test
    public void testLoginLogoutLoginSequence() {
        // Arrange & Act - First login
        sessionManager.login(1, "first@example.com");
        assertTrue(sessionManager.isLoggedIn());

        // Act - Logout
        sessionManager.logout();
        assertFalse(sessionManager.isLoggedIn());

        // Act - Second login with different user
        sessionManager.login(2, "second@example.com");

        // Assert
        assertTrue(sessionManager.isLoggedIn());
        assertEquals(2, sessionManager.getCurrentUserId());
        assertEquals("second@example.com", sessionManager.getCurrentUserEmail());
    }

    @Test
    public void testSessionManagerSingletonBehavior() {
        // Arrange
        sessionManager.login(999, "singleton@example.com");

        // Act - Get another instance
        SessionManager anotherReference = SessionManager.getInstance();

        // Assert - State should be shared
        assertTrue(anotherReference.isLoggedIn(), 
                "Singleton should maintain state across references");
        assertEquals(999, anotherReference.getCurrentUserId());
        assertEquals("singleton@example.com", anotherReference.getCurrentUserEmail());
    }

    @Test
    public void testLogoutFromDifferentReference() {
        // Arrange
        sessionManager.login(888, "shared@example.com");
        SessionManager anotherReference = SessionManager.getInstance();

        // Act - Logout from different reference
        anotherReference.logout();

        // Assert - Original reference should also be logged out
        assertFalse(sessionManager.isLoggedIn(), 
                "Logout from one reference should affect all references");
        assertNull(sessionManager.getCurrentUserId());
    }
}
