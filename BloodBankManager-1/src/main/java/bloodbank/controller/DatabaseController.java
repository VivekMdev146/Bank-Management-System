package bloodbank.controller;

import java.sql.*;
import javax.swing.*;

/**
 * Singleton controller for database operations.
 * Handles the database connection and provides methods for basic database operations.
 */
public class DatabaseController {
    private static DatabaseController instance;
    private Connection connection;
    
    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/Bloodbank";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "rootvivek146"; // In a real application, this should be secured

    // Private constructor to enforce singleton pattern
    private DatabaseController() {
    }

    /**
     * Get the singleton instance of DatabaseController
     * @return DatabaseController instance
     */
    public static synchronized DatabaseController getInstance() {
        if (instance == null) {
            instance = new DatabaseController();
        }
        return instance;
    }

    /**
     * Initialize the database connection
     * @return true if connection successful, false otherwise
     */
    public boolean initializeConnection() {
        try {
            // Load the JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            System.out.println("Database connection established successfully.");
            return true;
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, 
                "MySQL JDBC Driver not found: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Database connection error: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Get the current database connection
     * @return Connection object
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error checking database connection: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return connection;
    }

    /**
     * Close the database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error closing database connection: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Execute a query that doesn't return results (INSERT, UPDATE, DELETE)
     * @param sql SQL statement to execute
     * @param params Parameters for the SQL statement
     * @return true if execution successful, false otherwise
     */
    public boolean executeUpdate(String sql, Object... params) {
        try (PreparedStatement statement = prepareStatement(sql, params)) {
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error executing SQL update: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Execute a query that returns results (SELECT)
     * @param sql SQL statement to execute
     * @param params Parameters for the SQL statement
     * @return ResultSet containing the query results, or null if error
     */
    public ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement statement = prepareStatement(sql, params);
            return statement.executeQuery();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, 
                "Error executing SQL query: " + e.getMessage(),
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * Create a PreparedStatement with provided parameters
     * @param sql SQL statement
     * @param params Parameters for the SQL statement
     * @return PreparedStatement object
     * @throws SQLException if a database error occurs
     */
    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(sql);
        
        for (int i = 0; i < params.length; i++) {
            if (params[i] instanceof String) {
                statement.setString(i + 1, (String) params[i]);
            } else if (params[i] instanceof Integer) {
                statement.setInt(i + 1, (Integer) params[i]);
            } else if (params[i] instanceof Double) {
                statement.setDouble(i + 1, (Double) params[i]);
            } else if (params[i] instanceof Boolean) {
                statement.setBoolean(i + 1, (Boolean) params[i]);
            } else if (params[i] instanceof Date) {
                statement.setDate(i + 1, (Date) params[i]);
            } else if (params[i] instanceof java.util.Date) {
                statement.setDate(i + 1, new Date(((java.util.Date) params[i]).getTime()));
            } else if (params[i] == null) {
                statement.setNull(i + 1, Types.NULL);
            } else {
                statement.setObject(i + 1, params[i]);
            }
        }
        
        return statement;
    }
}
