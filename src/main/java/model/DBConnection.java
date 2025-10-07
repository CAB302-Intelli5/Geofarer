package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton-style Database access class.
 *
 * This is the database that uses the singleton pattern
 * so that the databse is only accessed ones reducing the overhead
 *
 */
public class DBConnection {
    private static final String URL = "jdbc:sqlite:src/main/resources/db/geofarer.db"; //Path to Geofarer database
    private static final String factbookURL = "jdbc:sqlite:src/main/resources/factbook.db"; // Path to the factbook database
    private static DBConnection instance;

    private DBConnection () { }

    /**
     * Public method to provide global access to the singleton. Creates new instance if none is stored.
     * @return instance of the database connection
     */
    public static DBConnection getInstance () {
        if (instance == null){
            instance = new DBConnection();
        }
        return instance;
    }
    /**
     * Establishes a connection to the Geofarer SQLite database.
     *
     * @return a {@link Connection} object to interact with the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    /**
     * Establishes a connection to the CIA Factbook SQLite database.
     *
     * @return a {@link Connection} object to interact with the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getFactbookConnection() throws SQLException {
        return DriverManager.getConnection(factbookURL);
    }
}
