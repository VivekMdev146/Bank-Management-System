package bloodbank;

import bloodbank.controller.DatabaseController;
import bloodbank.view.LoginView;

import javax.swing.*;

/**
 * Main entry point for the Blood Bank Management System
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Set the look and feel to the system's look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set Look and Feel: " + e.getMessage());
        }

        // Initialize the database connection
        if (!DatabaseController.getInstance().initializeConnection()) {
            JOptionPane.showMessageDialog(null, 
                "Failed to connect to the database. Please check your configuration.",
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Start the application with the login screen
        SwingUtilities.invokeLater(); -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        };
    }
}
