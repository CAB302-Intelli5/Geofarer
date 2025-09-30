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
public class Database {
    private static final String URL = "jdbc:sqlite:src/main/resources/db/geofarer.db";

    /**
     * Establishes a connection to the SQLite database.
     *
     * @return a {@link Connection} object to interact with the database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
