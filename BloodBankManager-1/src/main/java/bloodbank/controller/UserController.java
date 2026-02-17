package bloodbank.controller;

import bloodbank.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for handling user-related operations.
 */
public class UserController {
    private final DatabaseController dbController;

    public UserController() {
        dbController = DatabaseController.getInstance();
    }

    /**
     * Authenticate a user by username and password
     * @param username username to authenticate
     * @param password password to authenticate
     * @return User object if authentication successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND active = TRUE";
        
        try (ResultSet rs = dbController.executeQuery(sql, username, password)) {
            if (rs != null && rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get a user by ID
     * @param userId user ID to look up
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (ResultSet rs = dbController.executeQuery(sql, userId)) {
            if (rs != null && rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    userList.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        
        return userList;
    }

    /**
     * Add a new user
     * @param user User object to add
     * @return true if successful, false otherwise
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password, full_name, email, phone, role, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return dbController.executeUpdate(sql, 
            user.getUsername(), 
            user.getPassword(), 
            user.getFullName(), 
            user.getEmail(), 
            user.getPhone(), 
            user.getRole(), 
            user.isActive()
        );
    }

    /**
     * Update an existing user
     * @param user User object to update
     * @return true if successful, false otherwise
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, full_name = ?, " +
                     "email = ?, phone = ?, role = ?, active = ? WHERE user_id = ?";
        
        return dbController.executeUpdate(sql, 
            user.getUsername(), 
            user.getPassword(), 
            user.getFullName(), 
            user.getEmail(), 
            user.getPhone(), 
            user.getRole(), 
            user.isActive(), 
            user.getUserId()
        );
    }

    /**
     * Delete a user
     * @param userId ID of the user to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        return dbController.executeUpdate(sql, userId);
    }

    /**
     * Change a user's password
     * @param userId ID of the user
     * @param newPassword new password
     * @return true if successful, false otherwise
     */
    public boolean changePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        return dbController.executeUpdate(sql, newPassword, userId);
    }

    /**
     * Extract a User object from a ResultSet
     * @param rs ResultSet containing user data
     * @return User object
     * @throws SQLException if a database error occurs
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));
        return user;
    }
}
