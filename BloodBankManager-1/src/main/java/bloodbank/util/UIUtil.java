package bloodbank.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for common UI operations.
 */
public class UIUtil {
    
    // Common UI colors
    public static final Color PRIMARY_COLOR = new Color(153, 0, 0); // Dark red
    public static final Color SECONDARY_COLOR = new Color(220, 220, 220); // Light gray
    public static final Color TEXT_COLOR = new Color(33, 33, 33); // Dark gray
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Off white
    
    // Common UI fonts
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    public static final Font SUBTITLE_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    public static final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 14);
    
    // Common UI borders
    public static final Border PANEL_BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    // Date format
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get the current date as a formatted string.
     * @return the current date as a string in the format "yyyy-MM-dd"
     */
    public static String getCurrentDateString() {
        return DATE_FORMAT.format(new Date());
    }
    
    /**
     * Get the current date and time as a formatted string.
     * @return the current date and time as a string in the format "yyyy-MM-dd HH:mm:ss"
     */
    public static String getCurrentDateTimeString() {
        return DATE_TIME_FORMAT.format(new Date());
    }
    
    /**
     * Create a standardized button.
     * @param text the button text
     * @return a JButton with standardized appearance
     */
    public static JButton createStandardButton(String text) {
        JButton button = new JButton(text);
        button.setFont(TEXT_FONT);
        button.setFocusPainted(false);
        return button;
    }
    
    /**
     * Create a standardized panel with a title.
     * @param title the panel title
     * @return a JPanel with standardized appearance and title
     */
    public static JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(PANEL_BORDER, title));
        return panel;
    }
    
    /**
     * Create a standardized label.
     * @param text the label text
     * @return a JLabel with standardized appearance
     */
    public static JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }
    
    /**
     * Create a standardized text field.
     * @return a JTextField with standardized appearance
     */
    public static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(TEXT_FONT);
        return textField;
    }
    
    /**
     * Create a standardized text field with specified columns.
     * @param columns the number of columns
     * @return a JTextField with standardized appearance
     */
    public static JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setFont(TEXT_FONT);
        return textField;
    }
    
    /**
     * Create a standardized combo box.
     * @param items the items for the combo box
     * @return a JComboBox with standardized appearance
     */
    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> comboBox = new JComboBox<>(items);
        comboBox.setFont(TEXT_FONT);
        return comboBox;
    }
    
    /**
     * Center a JFrame on the screen.
     * @param frame the JFrame to center
     */
    public static void centerOnScreen(JFrame frame) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (dim.width - frame.getWidth()) / 2;
        int y = (dim.height - frame.getHeight()) / 2;
        frame.setLocation(x, y);
    }
    
    /**
     * Set up a standardized look and feel for the application.
     */
    public static void setupLookAndFeel() {
        try {
            // Try to set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Default to cross-platform look and feel if system look and feel fails
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore if cross-platform look and feel fails
            }
        }
    }
    
    /**
     * Show a standardized error message dialog.
     * @param parentComponent the parent component
     * @param message the error message
     */
    public static void showErrorMessage(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(
            parentComponent,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Show a standardized information message dialog.
     * @param parentComponent the parent component
     * @param message the information message
     */
    public static void showInfoMessage(Component parentComponent, String message) {
        JOptionPane.showMessageDialog(
            parentComponent,
            message,
            "Information",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Show a standardized confirmation dialog.
     * @param parentComponent the parent component
     * @param message the confirmation message
     * @return the user's selection (JOptionPane.YES_OPTION or JOptionPane.NO_OPTION)
     */
    public static int showConfirmDialog(Component parentComponent, String message) {
        return JOptionPane.showConfirmDialog(
            parentComponent,
            message,
            "Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
    }
    
    /**
     * Create a standardized form layout panel.
     * @return a JPanel with a form layout (GridBagLayout)
     */
    public static JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }
    
    /**
     * Create a standardized button panel with FlowLayout.
     * @return a JPanel with standardized button layout
     */
    public static JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        return panel;
    }
    
    /**
     * Add a component to a GridBagLayout panel.
     * @param panel the panel with GridBagLayout
     * @param component the component to add
     * @param x the grid x position
     * @param y the grid y position
     * @param width the grid width
     * @param height the grid height
     * @param fill the fill constraint
     */
    public static void addToGridBag(JPanel panel, Component component, int x, int y, int width, int height, int fill) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.fill = fill;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(component, gbc);
    }
}
