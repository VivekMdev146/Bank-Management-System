package bloodbank.view;

import bloodbank.controller.InventoryController;
import bloodbank.controller.RequestController;
import bloodbank.model.User;
import bloodbank.util.UIUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/**
 * Dashboard view for the blood bank management system.
 */
public class DashboardView extends JFrame {
    private final User currentUser;
    private final InventoryController inventoryController;
    private final RequestController requestController;
    
    // UI Components
    private JPanel contentPanel;
    private JLabel lblWelcome;
    private JLabel lblDate;

    /**
     * Constructor for the dashboard view.
     * @param user the currently logged-in user
     */
    public DashboardView(User user) {
        this.currentUser = user;
        this.inventoryController = new InventoryController();
        this.requestController = new RequestController();
        initializeUI();
        loadDashboardData();
    }

    /**
     * Initialize the user interface
     */
    private void initializeUI() {
        setTitle("Blood Bank Management System - Dashboard");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1000, 600));

        // Create main panels
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel headerPanel = createHeaderPanel();
        JPanel menuPanel = createMenuPanel();
        contentPanel = new JPanel();
        
        // Set up the content panel
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(menuPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Set the content pane
        setContentPane(mainPanel);
    }

    /**
     * Create the header panel
     * @return the header panel
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(new Color(153, 0, 0)); // Dark red color
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        
        // Logo/Title
        JLabel lblTitle = new JLabel("Blood Bank Management System");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(new EmptyBorder(0, 20, 0, 0));
        
        // User info panel
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setOpaque(false);
        userInfoPanel.setBorder(new EmptyBorder(0, 0, 0, 20));
        
        lblWelcome = new JLabel("Welcome, " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        lblWelcome.setFont(new Font("Arial", Font.PLAIN, 14));
        lblWelcome.setForeground(Color.WHITE);
        
        lblDate = new JLabel(UIUtil.getCurrentDateString());
        lblDate.setFont(new Font("Arial", Font.PLAIN, 14));
        lblDate.setForeground(Color.WHITE);
        
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        userInfoPanel.add(lblWelcome);
        userInfoPanel.add(new JLabel(" | "));
        userInfoPanel.add(lblDate);
        userInfoPanel.add(Box.createHorizontalStrut(20));
        userInfoPanel.add(btnLogout);
        
        // Add components to header panel
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);
        
        return headerPanel;
    }

    /**
     * Create the menu panel
     * @return the menu panel
     */
    private JPanel createMenuPanel() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(240, 240, 240));
        menuPanel.setPreferredSize(new Dimension(200, getHeight()));
        menuPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        
        // Menu buttons
        JButton btnDashboard = createMenuButton("Dashboard", true);
        JButton btnDonors = createMenuButton("Donor Management", false);
        JButton btnInventory = createMenuButton("Blood Inventory", false);
        JButton btnRequests = createMenuButton("Blood Requests", false);
        JButton btnReports = createMenuButton("Reports", false);
        
        // Add action listeners
        btnDashboard.addActionListener(e -> loadDashboardContent());
        btnDonors.addActionListener(e -> openDonorView());
        btnInventory.addActionListener(e -> openInventoryView());
        btnRequests.addActionListener(e -> openRequestView());
        btnReports.addActionListener(e -> openReportView());
        
        // Add buttons to menu panel
        menuPanel.add(Box.createVerticalStrut(20));
        menuPanel.add(btnDashboard);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnDonors);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnInventory);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnRequests);
        menuPanel.add(Box.createVerticalStrut(10));
        menuPanel.add(btnReports);
        menuPanel.add(Box.createVerticalGlue());
        
        return menuPanel;
    }

    /**
     * Create a menu button
     * @param text the button text
     * @param isSelected whether the button is currently selected
     * @return the menu button
     */
    private JButton createMenuButton(String text, boolean isSelected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 40));
        button.setFocusPainted(false);
        
        if (isSelected) {
            button.setBackground(new Color(153, 0, 0));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(240, 240, 240));
            button.setForeground(Color.BLACK);
        }
        
        return button;
    }

    /**
     * Load the dashboard data
     */
    private void loadDashboardData() {
        SwingUtilities.invokeLater(this::loadDashboardContent);
    }

    /**
     * Load the dashboard content
     */
    private void loadDashboardContent() {
        contentPanel.removeAll();
        
        // Create dashboard panel
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblDashboardTitle = new JLabel("Dashboard");
        lblDashboardTitle.setFont(new Font("Arial", Font.BOLD, 18));
        titlePanel.add(lblDashboardTitle);
        
        // Create stats panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Add blood inventory stats
        Map<String, Integer> inventoryStats = inventoryController.getBloodInventoryStats();
        JPanel inventoryStatsPanel = createStatsPanel("Blood Inventory", inventoryStats);
        statsPanel.add(inventoryStatsPanel);
        
        // Add pending requests stats
        JPanel pendingRequestsPanel = createRequestStatsPanel("Pending Requests", "pending");
        statsPanel.add(pendingRequestsPanel);
        
        // Add expiring soon stats
        JPanel expiringPanel = createExpiringStatsPanel();
        statsPanel.add(expiringPanel);
        
        // Add quick actions panel
        JPanel actionsPanel = createQuickActionsPanel();
        statsPanel.add(actionsPanel);
        
        // Add components to dashboard panel
        dashboardPanel.add(titlePanel, BorderLayout.NORTH);
        dashboardPanel.add(statsPanel, BorderLayout.CENTER);
        
        // Add dashboard panel to content panel
        contentPanel.add(dashboardPanel, BorderLayout.CENTER);
        
        // Refresh the panel
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Create a statistics panel
     * @param title the panel title
     * @param stats the statistics to display
     * @return the statistics panel
     */
    private JPanel createStatsPanel(String title, Map<String, Integer> stats) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Stats content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(0, 2, 10, 5));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Add blood group stats
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            JLabel lblGroup = new JLabel(entry.getKey() + ":");
            lblGroup.setFont(new Font("Arial", Font.BOLD, 14));
            
            JLabel lblCount = new JLabel(entry.getValue().toString() + " units");
            lblCount.setFont(new Font("Arial", Font.PLAIN, 14));
            
            contentPanel.add(lblGroup);
            contentPanel.add(lblCount);
        }
        
        // Add components to panel
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Create a request statistics panel
     * @param title the panel title
     * @param status the request status to filter by
     * @return the request statistics panel
     */
    private JPanel createRequestStatsPanel(String title, String status) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Title
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Get requests by status
        int requestCount = requestController.getRequestsByStatus(status).size();
        
        JLabel lblCount = new JLabel("Total: " + requestCount + " requests");
        lblCount.setFont(new Font("Arial", Font.BOLD, 16));
        lblCount.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton btnViewRequests = new JButton("View Requests");
        btnViewRequests.addActionListener(e -> openRequestView());
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnViewRequests);
        
        // Add components to content panel
        contentPanel.add(lblCount, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add components to panel
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Create an expiring soon statistics panel
     * @return the expiring soon statistics panel
     */
    private JPanel createExpiringStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Title
        JLabel lblTitle = new JLabel("Expiring Soon");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Get nearly expired units
        int expiringCount = inventoryController.getNearlyExpiredUnits().size();
        
        JLabel lblCount = new JLabel("Units expiring in 7 days: " + expiringCount);
        lblCount.setFont(new Font("Arial", Font.BOLD, 16));
        lblCount.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton btnViewInventory = new JButton("View Inventory");
        btnViewInventory.addActionListener(e -> openInventoryView());
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnViewInventory);
        
        // Add components to content panel
        contentPanel.add(lblCount, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add components to panel
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Create a quick actions panel
     * @return the quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Title
        JLabel lblTitle = new JLabel("Quick Actions");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(4, 1, 10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Add buttons
        JButton btnAddDonor = new JButton("Add New Donor");
        btnAddDonor.addActionListener(e -> openDonorView());
        
        JButton btnAddUnit = new JButton("Record Blood Donation");
        btnAddUnit.addActionListener(e -> openInventoryView());
        
        JButton btnAddRequest = new JButton("Register Blood Request");
        btnAddRequest.addActionListener(e -> openRequestView());
        
        JButton btnGenerateReport = new JButton("Generate Reports");
        btnGenerateReport.addActionListener(e -> openReportView());
        
        // Add buttons to content panel
        contentPanel.add(btnAddDonor);
        contentPanel.add(btnAddUnit);
        contentPanel.add(btnAddRequest);
        contentPanel.add(btnGenerateReport);
        
        // Add components to panel
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Open the donor view
     */
    private void openDonorView() {
        contentPanel.removeAll();
        
        DonorView donorView = new DonorView();
        contentPanel.add(donorView.getMainPanel());
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Open the inventory view
     */
    private void openInventoryView() {
        contentPanel.removeAll();
        
        InventoryView inventoryView = new InventoryView();
        contentPanel.add(inventoryView.getMainPanel());
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Open the request view
     */
    private void openRequestView() {
        contentPanel.removeAll();
        
        RequestView requestView = new RequestView();
        contentPanel.add(requestView.getMainPanel());
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Open the report view
     */
    private void openReportView() {
        contentPanel.removeAll();
        
        ReportView reportView = new ReportView();
        contentPanel.add(reportView.getMainPanel());
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Logout of the application
     */
    private void logout() {
        int response = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (response == JOptionPane.YES_OPTION) {
            this.dispose();
            new LoginView().setVisible(true);
        }
    }
}
