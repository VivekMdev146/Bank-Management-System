package bloodbank.view;

import bloodbank.controller.DonorController;
import bloodbank.controller.InventoryController;
import bloodbank.model.BloodUnit;
import bloodbank.model.Donor;
import bloodbank.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * View for managing blood inventory.
 */
public class InventoryView {
    private JPanel mainPanel;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField txtUnitId;
    private JComboBox<String> cmbComponent;
    private JTextField txtDonorId;
    private JTextField txtDonorName;
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtCollectionDate;
    private JTextField txtExpiryDate;
    private JComboBox<String> cmbStatus;
    private JTextField txtLocation;
    private JTextField txtTestedBy;
    private JCheckBox chkTestPassed;
    private JTextArea txtRemarks;
    private JButton btnSave;
    private JButton btnClear;
    private JButton btnDelete;
    private JTextField txtSearch;
    private JComboBox<String> cmbSearchBy;
    private JButton btnSearch;
    private JButton btnFindDonor;
    
    private final InventoryController inventoryController;
    private final DonorController donorController;
    private String selectedUnitId = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private boolean isDonorValid = false;

    /**
     * Constructor for InventoryView.
     */
    public InventoryView() {
        inventoryController = new InventoryController();
        donorController = new DonorController();
        initializeUI();
        loadInventoryData();
    }

    /**
     * Initialize the user interface.
     */
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTitle = new JLabel("Blood Inventory Management");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(lblTitle);
        
        // Create search panel
        JPanel searchPanel = createSearchPanel();
        
        // Create top panel (contains title and search)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        
        // Create dashboard panel
        JPanel dashboardPanel = createDashboardPanel();
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Create center panel (table and dashboard)
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(dashboardPanel, BorderLayout.EAST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        mainPanel.add(formPanel, BorderLayout.SOUTH);
    }

    /**
     * Create the search panel.
     * @return the search panel
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Search field
        txtSearch = new JTextField(15);
        
        // Search by combo box
        cmbSearchBy = new JComboBox<>(new String[] {"Unit ID", "Blood Group", "Component", "Status"});
        
        // Search button
        btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchInventory();
            }
        });
        
        // Add components to search panel
        searchPanel.add(new JLabel("Search By:"));
        searchPanel.add(cmbSearchBy);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        return searchPanel;
    }

    /**
     * Create the table panel.
     * @return the table panel
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Create table model
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Add columns to table model
        tableModel.addColumn("Unit ID");
        tableModel.addColumn("Blood Group");
        tableModel.addColumn("Component");
        tableModel.addColumn("Collection Date");
        tableModel.addColumn("Expiry Date");
        tableModel.addColumn("Status");
        tableModel.addColumn("Test Passed");
        
        // Create table
        inventoryTable = new JTable(tableModel);
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        inventoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add mouse listener to table
        inventoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = inventoryTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedUnitId = (String) inventoryTable.getValueAt(selectedRow, 0);
                    loadBloodUnitDetails(selectedUnitId);
                }
            }
        });
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    /**
     * Create the dashboard panel.
     * @return the dashboard panel
     */
    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(new BorderLayout());
        dashboardPanel.setPreferredSize(new Dimension(250, 300));
        dashboardPanel.setBorder(BorderFactory.createTitledBorder("Inventory Summary"));
        
        // Create stats panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(0, 2, 5, 10));
        statsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Get inventory stats
        loadInventoryStats(statsPanel);
        
        // Add stats panel to dashboard panel
        dashboardPanel.add(statsPanel, BorderLayout.NORTH);
        
        // Add expiring soon panel
        JPanel expiringPanel = new JPanel();
        expiringPanel.setLayout(new BorderLayout());
        expiringPanel.setBorder(BorderFactory.createTitledBorder("Expiring Soon"));
        
        JLabel lblExpiring = new JLabel("Units expiring in 7 days: ");
        lblExpiring.setFont(new Font("Arial", Font.BOLD, 14));
        lblExpiring.setHorizontalAlignment(SwingConstants.CENTER);
        lblExpiring.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JButton btnViewExpiring = new JButton("View Expiring Units");
        btnViewExpiring.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadNearlyExpiredUnits();
            }
        });
        
        expiringPanel.add(lblExpiring, BorderLayout.NORTH);
        expiringPanel.add(btnViewExpiring, BorderLayout.SOUTH);
        
        dashboardPanel.add(expiringPanel, BorderLayout.CENTER);
        
        return dashboardPanel;
    }

    /**
     * Create the form panel.
     * @return the form panel
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Blood Unit Details"));
        
        // Form fields panel
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Unit ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Unit ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        txtUnitId = new JTextField(20);
        fieldsPanel.add(txtUnitId, gbc);
        
        // Component
        gbc.gridx = 2;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Component:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 0;
        cmbComponent = new JComboBox<>(new String[] {"whole blood", "plasma", "platelets", "red cells"});
        fieldsPanel.add(cmbComponent, gbc);
        
        // Donor ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Donor ID:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        JPanel donorPanel = new JPanel(new BorderLayout());
        txtDonorId = new JTextField(10);
        btnFindDonor = new JButton("Find");
        btnFindDonor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findDonor();
            }
        });
        donorPanel.add(txtDonorId, BorderLayout.CENTER);
        donorPanel.add(btnFindDonor, BorderLayout.EAST);
        fieldsPanel.add(donorPanel, gbc);
        
        // Donor Name (read-only)
        gbc.gridx = 2;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Donor Name:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 1;
        txtDonorName = new JTextField(20);
        txtDonorName.setEditable(false);
        fieldsPanel.add(txtDonorName, gbc);
        
        // Blood Group
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Blood Group:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        cmbBloodGroup = new JComboBox<>(new String[] {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        fieldsPanel.add(cmbBloodGroup, gbc);
        
        // Collection Date
        gbc.gridx = 2;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Collection Date (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 2;
        txtCollectionDate = new JTextField(10);
        fieldsPanel.add(txtCollectionDate, gbc);
        
        // Expiry Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Expiry Date (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        txtExpiryDate = new JTextField(10);
        fieldsPanel.add(txtExpiryDate, gbc);
        
        // Status
        gbc.gridx = 2;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 3;
        cmbStatus = new JComboBox<>(new String[] {"available", "reserved", "issued", "discarded"});
        fieldsPanel.add(cmbStatus, gbc);
        
        // Location
        gbc.gridx = 0;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Storage Location:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        txtLocation = new JTextField(20);
        fieldsPanel.add(txtLocation, gbc);
        
        // Tested By
        gbc.gridx = 2;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Tested By:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 4;
        txtTestedBy = new JTextField(20);
        fieldsPanel.add(txtTestedBy, gbc);
        
        // Test Passed
        gbc.gridx = 0;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Test Passed:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        chkTestPassed = new JCheckBox();
        chkTestPassed.setSelected(true);
        fieldsPanel.add(chkTestPassed, gbc);
        
        // Remarks
        gbc.gridx = 2;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Remarks:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 5;
        txtRemarks = new JTextArea(3, 20);
        txtRemarks.setLineWrap(true);
        JScrollPane remarksScrollPane = new JScrollPane(txtRemarks);
        fieldsPanel.add(remarksScrollPane, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // Save button
        btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveBloodUnit();
            }
        });
        buttonPanel.add(btnSave);
        
        // Clear button
        btnClear = new JButton("Clear Form");
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        buttonPanel.add(btnClear);
        
        // Delete button
        btnDelete = new JButton("Delete");
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBloodUnit();
            }
        });
        buttonPanel.add(btnDelete);
        
        // Add components to form panel
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return formPanel;
    }
    
    /**
     * Get the main panel of the inventory view.
     * @return the main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Load blood inventory data into the table.
     */
    private void loadInventoryData() {
        // Clear the table model
        tableModel.setRowCount(0);
        
        // Get all blood units
        List<BloodUnit> units = inventoryController.getAllBloodUnits();
        
        // Add blood units to table model
        for (BloodUnit unit : units) {
            Object[] rowData = {
                unit.getUnitId(),
                unit.getBloodGroup(),
                unit.getComponent(),
                unit.getCollectionDate() != null ? dateFormat.format(unit.getCollectionDate()) : "",
                unit.getExpiryDate() != null ? dateFormat.format(unit.getExpiryDate()) : "",
                unit.getStatus(),
                unit.isTestPassed() ? "Yes" : "No"
            };
            tableModel.addRow(rowData);
        }
        
        // Update inventory stats
        JPanel statsPanel = (JPanel) ((JPanel) mainPanel.getComponent(1))
                                   .getComponent(1);
        statsPanel.removeAll();
        loadInventoryStats(statsPanel);
        statsPanel.revalidate();
        statsPanel.repaint();
        
        // Update expiring units count
        JLabel lblExpiring = (JLabel) ((JPanel) ((JPanel) mainPanel.getComponent(1))
                                     .getComponent(1))
                                     .getComponent(0);
        lblExpiring.setText("Units expiring in 7 days: " + inventoryController.getNearlyExpiredUnits().size());
    }
    
    /**
     * Load inventory statistics.
     * @param statsPanel the panel to add stats to
     */
    private void loadInventoryStats(JPanel statsPanel) {
        statsPanel.removeAll();
        
        // Get inventory stats
        java.util.Map<String, Integer> stats = inventoryController.getBloodInventoryStats();
        
        // Add stats to panel
        for (java.util.Map.Entry<String, Integer> entry : stats.entrySet()) {
            JLabel lblGroup = new JLabel(entry.getKey() + ":");
            lblGroup.setFont(new Font("Arial", Font.BOLD, 14));
            
            JLabel lblCount = new JLabel(entry.getValue().toString() + " units");
            lblCount.setFont(new Font("Arial", Font.PLAIN, 14));
            
            statsPanel.add(lblGroup);
            statsPanel.add(lblCount);
        }
    }
    
    /**
     * Load blood unit details into the form.
     * @param unitId unit ID to load
     */
    private void loadBloodUnitDetails(String unitId) {
        BloodUnit unit = inventoryController.getBloodUnitById(unitId);
        if (unit != null) {
            txtUnitId.setText(unit.getUnitId());
            txtUnitId.setEditable(false);
            cmbComponent.setSelectedItem(unit.getComponent());
            txtDonorId.setText(String.valueOf(unit.getDonorId()));
            
            // Load donor name
            Donor donor = donorController.getDonorById(unit.getDonorId());
            if (donor != null) {
                txtDonorName.setText(donor.getName());
                isDonorValid = true;
            } else {
                txtDonorName.setText("Unknown Donor");
                isDonorValid = false;
            }
            
            cmbBloodGroup.setSelectedItem(unit.getBloodGroup());
            txtCollectionDate.setText(unit.getCollectionDate() != null ? dateFormat.format(unit.getCollectionDate()) : "");
            txtExpiryDate.setText(unit.getExpiryDate() != null ? dateFormat.format(unit.getExpiryDate()) : "");
            cmbStatus.setSelectedItem(unit.getStatus());
            txtLocation.setText(unit.getLocation());
            txtTestedBy.setText(unit.getTestedBy());
            chkTestPassed.setSelected(unit.isTestPassed());
            txtRemarks.setText(unit.getRemarks());
            
            // Enable delete button
            btnDelete.setEnabled(true);
        }
    }
    
    /**
     * Find a donor by ID.
     */
    private void findDonor() {
        try {
            int donorId = Integer.parseInt(txtDonorId.getText().trim());
            Donor donor = donorController.getDonorById(donorId);
            
            if (donor != null) {
                txtDonorName.setText(donor.getName());
                cmbBloodGroup.setSelectedItem(donor.getBloodGroup());
                
                // Check if donor is eligible
                if (!donorController.isEligibleToDonate(donorId)) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Warning: This donor is not eligible to donate at this time.",
                        "Eligibility Warning", 
                        JOptionPane.WARNING_MESSAGE);
                }
                
                isDonorValid = true;
            } else {
                txtDonorName.setText("Donor not found");
                isDonorValid = false;
                JOptionPane.showMessageDialog(mainPanel, 
                    "No donor found with ID: " + donorId,
                    "Donor Not Found", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            txtDonorName.setText("");
            isDonorValid = false;
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter a valid donor ID number.",
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Save blood unit information.
     */
    private void saveBloodUnit() {
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        try {
            // Create blood unit object
            BloodUnit unit = new BloodUnit();
            unit.setUnitId(txtUnitId.getText().trim());
            unit.setComponent((String) cmbComponent.getSelectedItem());
            unit.setDonorId(Integer.parseInt(txtDonorId.getText().trim()));
            unit.setBloodGroup((String) cmbBloodGroup.getSelectedItem());
            unit.setStatus((String) cmbStatus.getSelectedItem());
            unit.setLocation(txtLocation.getText().trim());
            unit.setTestedBy(txtTestedBy.getText().trim());
            unit.setTestPassed(chkTestPassed.isSelected());
            unit.setRemarks(txtRemarks.getText().trim());
            
            // Parse collection date
            String collectionDateString = txtCollectionDate.getText().trim();
            if (!collectionDateString.isEmpty()) {
                try {
                    unit.setCollectionDate(dateFormat.parse(collectionDateString));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid collection date format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Default to current date
                unit.setCollectionDate(new Date());
            }
            
            // Parse expiry date
            String expiryDateString = txtExpiryDate.getText().trim();
            if (!expiryDateString.isEmpty()) {
                try {
                    unit.setExpiryDate(dateFormat.parse(expiryDateString));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid expiry date format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Calculate expiry date based on component (default: 35 days for whole blood)
                Calendar cal = Calendar.getInstance();
                cal.setTime(unit.getCollectionDate());
                
                switch (unit.getComponent()) {
                    case "whole blood":
                        cal.add(Calendar.DAY_OF_MONTH, 35);
                        break;
                    case "red cells":
                        cal.add(Calendar.DAY_OF_MONTH, 42);
                        break;
                    case "plasma":
                        cal.add(Calendar.YEAR, 1);
                        break;
                    case "platelets":
                        cal.add(Calendar.DAY_OF_MONTH, 5);
                        break;
                    default:
                        cal.add(Calendar.DAY_OF_MONTH, 35);
                }
                
                unit.setExpiryDate(cal.getTime());
            }
            
            boolean success;
            if (selectedUnitId == null) {
                // Add new blood unit
                success = inventoryController.addBloodUnit(unit);
            } else {
                // Update existing blood unit
                success = inventoryController.updateBloodUnit(unit);
            }
            
            if (success) {
                // Update donor's last donation date
                donorController.updateLastDonationDate(unit.getDonorId(), unit.getCollectionDate());
                
                JOptionPane.showMessageDialog(mainPanel, 
                    "Blood unit information saved successfully.",
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload inventory data and clear form
                loadInventoryData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Failed to save blood unit information.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "An error occurred: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Delete a blood unit.
     */
    private void deleteBloodUnit() {
        if (selectedUnitId == null) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please select a blood unit to delete.",
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(mainPanel, 
            "Are you sure you want to delete this blood unit?",
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = inventoryController.deleteBloodUnit(selectedUnitId);
            
            if (success) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Blood unit deleted successfully.",
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload inventory data and clear form
                loadInventoryData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Failed to delete blood unit. It may be referenced in blood requests.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Search blood inventory based on search criteria.
     */
    private void searchInventory() {
        String searchTerm = txtSearch.getText().trim();
        String searchBy = (String) cmbSearchBy.getSelectedItem();
        
        if (searchTerm.isEmpty()) {
            loadInventoryData();
            return;
        }
        
        tableModel.setRowCount(0);
        List<BloodUnit> units;
        
        // Search based on criteria
        switch (searchBy) {
            case "Unit ID":
                BloodUnit unit = inventoryController.getBloodUnitById(searchTerm);
                if (unit != null) {
                    Object[] rowData = {
                        unit.getUnitId(),
                        unit.getBloodGroup(),
                        unit.getComponent(),
                        unit.getCollectionDate() != null ? dateFormat.format(unit.getCollectionDate()) : "",
                        unit.getExpiryDate() != null ? dateFormat.format(unit.getExpiryDate()) : "",
                        unit.getStatus(),
                        unit.isTestPassed() ? "Yes" : "No"
                    };
                    tableModel.addRow(rowData);
                }
                break;
            case "Blood Group":
                units = inventoryController.getAvailableBloodUnitsByBloodGroup(searchTerm);
                for (BloodUnit u : units) {
                    Object[] rowData = {
                        u.getUnitId(),
                        u.getBloodGroup(),
                        u.getComponent(),
                        u.getCollectionDate() != null ? dateFormat.format(u.getCollectionDate()) : "",
                        u.getExpiryDate() != null ? dateFormat.format(u.getExpiryDate()) : "",
                        u.getStatus(),
                        u.isTestPassed() ? "Yes" : "No"
                    };
                    tableModel.addRow(rowData);
                }
                break;
            case "Component":
                units = inventoryController.getAvailableBloodUnitsByComponent(searchTerm);
                for (BloodUnit u : units) {
                    Object[] rowData = {
                        u.getUnitId(),
                        u.getBloodGroup(),
                        u.getComponent(),
                        u.getCollectionDate() != null ? dateFormat.format(u.getCollectionDate()) : "",
                        u.getExpiryDate() != null ? dateFormat.format(u.getExpiryDate()) : "",
                        u.getStatus(),
                        u.isTestPassed() ? "Yes" : "No"
                    };
                    tableModel.addRow(rowData);
                }
                break;
            case "Status":
                // Get all blood units and filter by status
                units = inventoryController.getAllBloodUnits();
                for (BloodUnit u : units) {
                    if (u.getStatus().equalsIgnoreCase(searchTerm)) {
                        Object[] rowData = {
                            u.getUnitId(),
                            u.getBloodGroup(),
                            u.getComponent(),
                            u.getCollectionDate() != null ? dateFormat.format(u.getCollectionDate()) : "",
                            u.getExpiryDate() != null ? dateFormat.format(u.getExpiryDate()) : "",
                            u.getStatus(),
                            u.isTestPassed() ? "Yes" : "No"
                        };
                        tableModel.addRow(rowData);
                    }
                }
                break;
        }
    }
    
    /**
     * Load nearly expired blood units.
     */
    private void loadNearlyExpiredUnits() {
        tableModel.setRowCount(0);
        
        // Get nearly expired units
        List<BloodUnit> units = inventoryController.getNearlyExpiredUnits();
        
        // Add units to table model
        for (BloodUnit unit : units) {
            Object[] rowData = {
                unit.getUnitId(),
                unit.getBloodGroup(),
                unit.getComponent(),
                unit.getCollectionDate() != null ? dateFormat.format(unit.getCollectionDate()) : "",
                unit.getExpiryDate() != null ? dateFormat.format(unit.getExpiryDate()) : "",
                unit.getStatus(),
                unit.isTestPassed() ? "Yes" : "No"
            };
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * Clear the form.
     */
    private void clearForm() {
        txtUnitId.setText("");
        txtUnitId.setEditable(true);
        cmbComponent.setSelectedIndex(0);
        txtDonorId.setText("");
        txtDonorName.setText("");
        cmbBloodGroup.setSelectedIndex(0);
        txtCollectionDate.setText(dateFormat.format(new Date())); // Default to current date
        txtExpiryDate.setText("");
        cmbStatus.setSelectedIndex(0);
        txtLocation.setText("");
        txtTestedBy.setText("");
        chkTestPassed.setSelected(true);
        txtRemarks.setText("");
        
        selectedUnitId = null;
        isDonorValid = false;
        btnDelete.setEnabled(false);
    }
    
    /**
     * Validate the form.
     * @return true if form is valid, false otherwise
     */
    private boolean validateForm() {
        // Unit ID validation
        if (txtUnitId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter a unit ID.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtUnitId.requestFocus();
            return false;
        }
        
        // Donor ID validation
        if (txtDonorId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter a donor ID.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtDonorId.requestFocus();
            return false;
        }
        
        try {
            Integer.parseInt(txtDonorId.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Donor ID must be a number.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtDonorId.requestFocus();
            return false;
        }
        
        if (!isDonorValid) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please select a valid donor using the Find button.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            btnFindDonor.requestFocus();
            return false;
        }
        
        // Collection date validation
        if (!txtCollectionDate.getText().trim().isEmpty()) {
            try {
                Date collectionDate = dateFormat.parse(txtCollectionDate.getText().trim());
                
                // Check if date is in the future
                if (collectionDate.after(new Date())) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Collection date cannot be in the future.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    txtCollectionDate.requestFocus();
                    return false;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Invalid collection date format. Use yyyy-MM-dd format.",
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtCollectionDate.requestFocus();
                return false;
            }
        }
        
        // Expiry date validation
        if (!txtExpiryDate.getText().trim().isEmpty()) {
            try {
                Date expiryDate = dateFormat.parse(txtExpiryDate.getText().trim());
                
                // Collection date must be before expiry date
                if (!txtCollectionDate.getText().trim().isEmpty()) {
                    Date collectionDate = dateFormat.parse(txtCollectionDate.getText().trim());
                    if (expiryDate.before(collectionDate)) {
                        JOptionPane.showMessageDialog(mainPanel, 
                            "Expiry date cannot be before collection date.",
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                        txtExpiryDate.requestFocus();
                        return false;
                    }
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Invalid expiry date format. Use yyyy-MM-dd format.",
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtExpiryDate.requestFocus();
                return false;
            }
        }
        
        return true;
    }
}
