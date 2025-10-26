package utils;

/**
 * Manages the current user session across the application
 */
public class SessionManager {
    private static SessionManager instance;
    private Integer currentUserId;
    private String currentUserEmail;
    private boolean isLoggedIn;

    private SessionManager() {
        this.isLoggedIn = false;
        this.currentUserId = null;
        this.currentUserEmail = null;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Sets the current logged-in user
     */
    public void login(int userId, String email) {
        this.currentUserId = userId;
        this.currentUserEmail = email;
        this.isLoggedIn = true;
        System.out.println("Session: User logged in - ID: " + userId + ", Email: " + email);
    }

    /**
     * Clears the current session
     */
    public void logout() {
        this.currentUserId = null;
        this.currentUserEmail = null;
        this.isLoggedIn = false;
        System.out.println("Session: User logged out");
    }

    /**
     * Checks if a user is currently logged in
     */
    public boolean isLoggedIn() {
        return isLoggedIn && currentUserId != null;
    }

    /**
     * Gets the current user ID
     * @return user ID or null if not logged in
     */
    public Integer getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Gets the current user email
     * @return user email or null if not logged in
     */
    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    /**
     * Checks if user is logged in, returns true if yes, false if no
     * Can be used for navigation guards
     */
    public boolean requireLogin() {
        return isLoggedIn();
    }
}