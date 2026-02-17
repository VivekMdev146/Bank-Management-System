package bloodbank.view;

import bloodbank.controller.InventoryController;
import bloodbank.controller.RequestController;
import bloodbank.model.BloodRequest;
import bloodbank.model.BloodUnit;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * View for managing blood requests.
 */
public class RequestView {
    private JPanel mainPanel;
    private JTable requestTable;
    private DefaultTableModel requestTableModel;
    private JTable inventoryTable;
    private DefaultTableModel inventoryTableModel;
    private JTabbedPane tabbedPane;
    
    private JTextField txtPatientName;
    private JComboBox<String> cmbBloodGroup;
    private JComboBox<String> cmbComponent;
    private JSpinner spnQuantity;
    private JTextField txtRequestDate;
    private JTextField txtRequiredDate;
    private JComboBox<String> cmbStatus;
    private JTextField txtHospitalName;
    private JTextField txtDoctorName;
    private JTextField txtContactPerson;
    private JTextField txtContactPhone;
    private JComboBox<String> cmbPriority;
    private JTextArea txtReason;
    private JTextField txtProcessedBy;
    private JTextArea txtRemarks;
    
    private JButton btnSave;
    private JButton btnClear;
    private JButton btnDelete;
    private JButton btnFulfill;
    private JTextField txtSearch;
    private JComboBox<String> cmbSearchBy;
    private JButton btnSearch;
    
    private final RequestController requestController;
    private final InventoryController inventoryController;
    private int selectedRequestId = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private List<String> selectedInventoryUnitIds = new ArrayList<>();

    /**
     * Constructor for RequestView.
     */
    public RequestView() {
        requestController = new RequestController();
        inventoryController = new InventoryController();
        initializeUI();
        loadRequestData();
    }

    /**
     * Initialize the user interface.
     */
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTitle = new JLabel("Blood Request Management");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(lblTitle);
        
        // Create search panel
        JPanel searchPanel = createSearchPanel();
        
        // Create top panel (contains title and search)
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create request table panel
        JPanel requestTablePanel = createRequestTablePanel();
        tabbedPane.addTab("Blood Requests", requestTablePanel);
        
        // Create inventory table panel
        JPanel inventoryTablePanel = createInventoryTablePanel();
        tabbedPane.addTab("Available Inventory", inventoryTablePanel);
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
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
        cmbSearchBy = new JComboBox<>(new String[] {"Patient Name", "Hospital", "Blood Group", "Status", "Priority"});
        
        // Search button
        btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchRequests();
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
     * Create the request table panel.
     * @return the request table panel
     */
    private JPanel createRequestTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Create table model
        requestTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Add columns to table model
        requestTableModel.addColumn("ID");
        requestTableModel.addColumn("Patient Name");
        requestTableModel.addColumn("Blood Group");
        requestTableModel.addColumn("Component");
        requestTableModel.addColumn("Quantity");
        requestTableModel.addColumn("Request Date");
        requestTableModel.addColumn("Required Date");
        requestTableModel.addColumn("Status");
        requestTableModel.addColumn("Priority");
        
        // Create table
        requestTable = new JTable(requestTableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.getTableHeader().setReorderingAllowed(false);
        requestTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add mouse listener to table
        requestTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = requestTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedRequestId = (int) requestTable.getValueAt(selectedRow, 0);
                    loadRequestDetails(selectedRequestId);
                    
                    // Switch to inventory tab when a request is selected
                    tabbedPane.setSelectedIndex(1);
                    
                    // Filter inventory by blood group
                    String bloodGroup = (String) requestTable.getValueAt(selectedRow, 2);
                    String component = (String) requestTable.getValueAt(selectedRow, 3);
                    loadAvailableInventory(bloodGroup, component);
                }
            }
        });
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(requestTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    /**
     * Create the inventory table panel.
     * @return the inventory table panel
     */
    private JPanel createInventoryTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Create table model
        inventoryTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 7; // Only allow selection column to be edited
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 7 ? Boolean.class : Object.class;
            }
        };
        
        // Add columns to table model
        inventoryTableModel.addColumn("Unit ID");
        inventoryTableModel.addColumn("Blood Group");
        inventoryTableModel.addColumn("Component");
        inventoryTableModel.addColumn("Collection Date");
        inventoryTableModel.addColumn("Expiry Date");
        inventoryTableModel.addColumn("Location");
        inventoryTableModel.addColumn("Status");
        inventoryTableModel.addColumn("Select");
        
        // Create table
        inventoryTable = new JTable(inventoryTableModel);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        inventoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnApplySelection = new JButton("Apply Selection");
        btnApplySelection.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedUnits();
            }
        });
        buttonPanel.add(btnApplySelection);
        
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return tablePanel;
    }

    /**
     * Create the form panel.
     * @return the form panel
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Blood Request Details"));
        
        // Form fields panel
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Patient Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Patient Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        txtPatientName = new JTextField(20);
        fieldsPanel.add(txtPatientName, gbc);
        
        // Blood Group
        gbc.gridx = 2;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Blood Group:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 0;
        cmbBloodGroup = new JComboBox<>(new String[] {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        fieldsPanel.add(cmbBloodGroup, gbc);
        
        // Component
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Component:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        cmbComponent = new JComboBox<>(new String[] {"whole blood", "plasma", "platelets", "red cells"});
        fieldsPanel.add(cmbComponent, gbc);
        
        // Quantity
        gbc.gridx = 2;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Quantity:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 1;
        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        fieldsPanel.add(spnQuantity, gbc);
        
        // Request Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Request Date (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        txtRequestDate = new JTextField(10);
        txtRequestDate.setText(dateFormat.format(new Date())); // Default to current date
        fieldsPanel.add(txtRequestDate, gbc);
        
        // Required Date
        gbc.gridx = 2;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Required Date (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 2;
        txtRequiredDate = new JTextField(10);
        fieldsPanel.add(txtRequiredDate, gbc);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        cmbStatus = new JComboBox<>(new String[] {"pending", "fulfilled", "partially fulfilled", "cancelled"});
        fieldsPanel.add(cmbStatus, gbc);
        
        // Priority
        gbc.gridx = 2;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Priority:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 3;
        cmbPriority = new JComboBox<>(new String[] {"normal", "urgent", "emergency"});
        fieldsPanel.add(cmbPriority, gbc);
        
        // Hospital Name
        gbc.gridx = 0;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Hospital Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        txtHospitalName = new JTextField(20);
        fieldsPanel.add(txtHospitalName, gbc);
        
        // Doctor Name
        gbc.gridx = 2;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Doctor Name:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 4;
        txtDoctorName = new JTextField(20);
        fieldsPanel.add(txtDoctorName, gbc);
        
        // Contact Person
        gbc.gridx = 0;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Contact Person:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        txtContactPerson = new JTextField(20);
        fieldsPanel.add(txtContactPerson, gbc);
        
        // Contact Phone
        gbc.gridx = 2;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Contact Phone:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 5;
        txtContactPhone = new JTextField(20);
        fieldsPanel.add(txtContactPhone, gbc);
        
        // Reason
        gbc.gridx = 0;
        gbc.gridy = 6;
        fieldsPanel.add(new JLabel("Reason:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        txtReason = new JTextArea(2, 20);
        txtReason.setLineWrap(true);
        JScrollPane reasonScrollPane = new JScrollPane(txtReason);
        fieldsPanel.add(reasonScrollPane, gbc);
        
        // Processed By
        gbc.gridx = 2;
        gbc.gridy = 6;
        fieldsPanel.add(new JLabel("Processed By:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 6;
        txtProcessedBy = new JTextField(20);
        fieldsPanel.add(txtProcessedBy, gbc);
        
        // Remarks
        gbc.gridx = 0;
        gbc.gridy = 7;
        fieldsPanel.add(new JLabel("Remarks:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        txtRemarks = new JTextArea(2, 40);
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
                saveRequest();
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
                deleteRequest();
            }
        });
        buttonPanel.add(btnDelete);
        
        // Fulfill button
        btnFulfill = new JButton("Fulfill Request");
        btnFulfill.setEnabled(false);
        btnFulfill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fulfillRequest();
            }
        });
        buttonPanel.add(btnFulfill);
        
        // Add components to form panel
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return formPanel;
    }
    
    /**
     * Get the main panel of the request view.
     * @return the main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    /**
     * Load blood request data into the table.
     */
    private void loadRequestData() {
        // Clear the table model
        requestTableModel.setRowCount(0);
        
        // Get all blood requests
        List<BloodRequest> requests = requestController.getAllRequests();
        
        // Add requests to table model
        for (BloodRequest request : requests) {
            Object[] rowData = {
                request.getRequestId(),
                request.getPatientName(),
                request.getBloodGroup(),
                request.getComponent(),
                request.getQuantity(),
                request.getRequestDate() != null ? dateFormat.format(request.getRequestDate()) : "",
                request.getRequiredDate() != null ? dateFormat.format(request.getRequiredDate()) : "",
                request.getStatus(),
                request.getPriority()
            };
            requestTableModel.addRow(rowData);
        }
    }
    
    /**
     * Load available inventory data into the inventory table.
     * @param bloodGroup blood group to filter by
     * @param component component to filter by
     */
    private void loadAvailableInventory(String bloodGroup, String component) {
        // Clear the table model
        inventoryTableModel.setRowCount(0);
        selectedInventoryUnitIds.clear();
        
        // Get available blood units by blood group and component
        List<BloodUnit> units;
        
        if (component != null && !component.isEmpty()) {
            // Get units by blood group and component
            units = inventoryController.getAvailableBloodUnitsByComponent(component);
            // Filter by blood group
            List<BloodUnit> filteredUnits = new ArrayList<>();
            for (BloodUnit unit : units) {
                if (unit.getBloodGroup().equals(bloodGroup)) {
                    filteredUnits.add(unit);
                }
            }
            units = filteredUnits;
        } else {
            // Get units by blood group only
            units = inventoryController.getAvailableBloodUnitsByBloodGroup(bloodGroup);
        }
        
        // Add units to table model
        for (BloodUnit unit : units) {
            Object[] rowData = {
                unit.getUnitId(),
                unit.getBloodGroup(),
                unit.getComponent(),
                unit.getCollectionDate() != null ? dateFormat.format(unit.getCollectionDate()) : "",
                unit.getExpiryDate() != null ? dateFormat.format(unit.getExpiryDate()) : "",
                unit.getLocation(),
                unit.getStatus(),
                false // Default selection is false
            };
            inventoryTableModel.addRow(rowData);
        }
    }
    
    /**
     * Load request details into the form.
     * @param requestId request ID to load
     */
    private void loadRequestDetails(int requestId) {
        BloodRequest request = requestController.getRequestById(requestId);
        if (request != null) {
            txtPatientName.setText(request.getPatientName());
            cmbBloodGroup.setSelectedItem(request.getBloodGroup());
            cmbComponent.setSelectedItem(request.getComponent());
            spnQuantity.setValue(request.getQuantity());
            txtRequestDate.setText(request.getRequestDate() != null ? dateFormat.format(request.getRequestDate()) : "");
            txtRequiredDate.setText(request.getRequiredDate() != null ? dateFormat.format(request.getRequiredDate()) : "");
            cmbStatus.setSelectedItem(request.getStatus());
            txtHospitalName.setText(request.getHospitalName());
            txtDoctorName.setText(request.getDoctorName());
            txtContactPerson.setText(request.getContactPerson());
            txtContactPhone.setText(request.getContactPhone());
            txtReason.setText(request.getReason());
            cmbPriority.setSelectedItem(request.getPriority());
            txtProcessedBy.setText(request.getProcessedBy());
            txtRemarks.setText(request.getRemarks());
            
            // Enable delete and fulfill buttons
            btnDelete.setEnabled(true);
            btnFulfill.setEnabled(true);
            
            // Disable fulfill button if request is already fulfilled or cancelled
            if (request.getStatus().equals("fulfilled") || request.getStatus().equals("cancelled")) {
                btnFulfill.setEnabled(false);
            }
        }
    }
    
    /**
     * Update the list of selected blood units.
     */
    private void updateSelectedUnits() {
        selectedInventoryUnitIds.clear();
        
        for (int i = 0; i < inventoryTableModel.getRowCount(); i++) {
            Boolean selected = (Boolean) inventoryTableModel.getValueAt(i, 7);
            if (selected) {
                String unitId = (String) inventoryTableModel.getValueAt(i, 0);
                selectedInventoryUnitIds.add(unitId);
            }
        }
        
        // Update the Fulfill button status
        btnFulfill.setEnabled(selectedRequestId > 0 && !selectedInventoryUnitIds.isEmpty());
        
        // Show selection summary
        JOptionPane.showMessageDialog(mainPanel, 
            "Selected " + selectedInventoryUnitIds.size() + " blood units.",
            "Selection Updated", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Save blood request information.
     */
    private void saveRequest() {
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        try {
            // Create blood request object
            BloodRequest request = new BloodRequest();
            request.setPatientName(txtPatientName.getText().trim());
            request.setBloodGroup((String) cmbBloodGroup.getSelectedItem());
            request.setComponent((String) cmbComponent.getSelectedItem());
            request.setQuantity((Integer) spnQuantity.getValue());
            request.setStatus((String) cmbStatus.getSelectedItem());
            request.setHospitalName(txtHospitalName.getText().trim());
            request.setDoctorName(txtDoctorName.getText().trim());
            request.setContactPerson(txtContactPerson.getText().trim());
            request.setContactPhone(txtContactPhone.getText().trim());
            request.setReason(txtReason.getText().trim());
            request.setPriority((String) cmbPriority.getSelectedItem());
            request.setProcessedBy(txtProcessedBy.getText().trim());
            request.setRemarks(txtRemarks.getText().trim());
            
            // Parse request date
            String requestDateString = txtRequestDate.getText().trim();
            if (!requestDateString.isEmpty()) {
                try {
                    request.setRequestDate(dateFormat.parse(requestDateString));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid request date format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                // Default to current date
                request.setRequestDate(new Date());
            }
            
            // Parse required date
            String requiredDateString = txtRequiredDate.getText().trim();
            if (!requiredDateString.isEmpty()) {
                try {
                    request.setRequiredDate(dateFormat.parse(requiredDateString));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid required date format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            boolean success;
            if (selectedRequestId == 0) {
                // Add new blood request
                success = requestController.addRequest(request);
            } else {
                // Update existing blood request
                request.setRequestId(selectedRequestId);
                success = requestController.updateRequest(request);
            }
            
            if (success) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Blood request information saved successfully.",
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload request data and clear form
                loadRequestData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Failed to save blood request information.",
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
     * Delete a blood request.
     */
    private void deleteRequest() {
        if (selectedRequestId == 0) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please select a blood request to delete.",
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(mainPanel, 
            "Are you sure you want to delete this blood request?",
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = requestController.deleteRequest(selectedRequestId);
            
            if (success) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Blood request deleted successfully.",
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload request data and clear form
                loadRequestData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Failed to delete blood request.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Fulfill a blood request.
     */
    private void fulfillRequest() {
        if (selectedRequestId == 0) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please select a blood request to fulfill.",
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedInventoryUnitIds.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please select blood units to fulfill this request.",
                "No Blood Units Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        BloodRequest request = requestController.getRequestById(selectedRequestId);
        if (request == null) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Error loading request details.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if selected units are sufficient
        if (selectedInventoryUnitIds.size() < request.getQuantity()) {
            int confirmation = JOptionPane.showConfirmDialog(mainPanel, 
                "Selected units (" + selectedInventoryUnitIds.size() + ") are less than requested (" + 
                request.getQuantity() + "). Proceed with partial fulfillment?",
                "Confirm Partial Fulfillment", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
        // Get processing staff name
        String processedBy = txtProcessedBy.getText().trim();
        if (processedBy.isEmpty()) {
            processedBy = "Unknown Staff";
        }
        
        // Fulfill the request
        boolean success = requestController.fulfillRequest(selectedRequestId, selectedInventoryUnitIds, processedBy);
        
        if (success) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Blood request fulfilled successfully.",
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            // Reload data and clear form
            loadRequestData();
            loadAvailableInventory(request.getBloodGroup(), request.getComponent());
            clearForm();
        } else {
            JOptionPane.showMessageDialog(mainPanel, 
                "Failed to fulfill blood request.",
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Search blood requests based on search criteria.
     */
    private void searchRequests() {
        String searchTerm = txtSearch.getText().trim();
        String searchBy = (String) cmbSearchBy.getSelectedItem();
        
        if (searchTerm.isEmpty()) {
            loadRequestData();
            return;
        }
        
        // Convert search field to database column name
        String searchField;
        switch (searchBy) {
            case "Patient Name": 
                searchField = "patient_name"; 
                break;
            case "Hospital": 
                searchField = "hospital_name"; 
                break;
            case "Blood Group": 
                searchField = "blood_group"; 
                break;
            case "Status": 
                searchField = "status"; 
                break;
            case "Priority": 
                searchField = "priority"; 
                break;
            default: 
                searchField = "patient_name";
        }
        
        // Search requests
        List<BloodRequest> requests = requestController.searchRequests(searchTerm, searchField);
        
        // Clear table model
        requestTableModel.setRowCount(0);
        
        // Add requests to table model
        for (BloodRequest request : requests) {
            Object[] rowData = {
                request.getRequestId(),
                request.getPatientName(),
                request.getBloodGroup(),
                request.getComponent(),
                request.getQuantity(),
                request.getRequestDate() != null ? dateFormat.format(request.getRequestDate()) : "",
                request.getRequiredDate() != null ? dateFormat.format(request.getRequiredDate()) : "",
                request.getStatus(),
                request.getPriority()
            };
            requestTableModel.addRow(rowData);
        }
    }
    
    /**
     * Clear the form.
     */
    private void clearForm() {
        txtPatientName.setText("");
        cmbBloodGroup.setSelectedIndex(0);
        cmbComponent.setSelectedIndex(0);
        spnQuantity.setValue(1);
        txtRequestDate.setText(dateFormat.format(new Date())); // Default to current date
        txtRequiredDate.setText("");
        cmbStatus.setSelectedIndex(0);
        txtHospitalName.setText("");
        txtDoctorName.setText("");
        txtContactPerson.setText("");
        txtContactPhone.setText("");
        txtReason.setText("");
        cmbPriority.setSelectedIndex(0);
        txtProcessedBy.setText("");
        txtRemarks.setText("");
        
        selectedRequestId = 0;
        selectedInventoryUnitIds.clear();
        btnDelete.setEnabled(false);
        btnFulfill.setEnabled(false);
        
        // Clear selection in inventory table
        for (int i = 0; i < inventoryTableModel.getRowCount(); i++) {
            inventoryTableModel.setValueAt(false, i, 7);
        }
    }
    
    /**
     * Validate the form.
     * @return true if form is valid, false otherwise
     */
    private boolean validateForm() {
        // Patient name validation
        if (txtPatientName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter patient name.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtPatientName.requestFocus();
            return false;
        }
        
        // Hospital name validation
        if (txtHospitalName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter hospital name.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtHospitalName.requestFocus();
            return false;
        }
        
        // Contact phone validation
        if (txtContactPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter contact phone number.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtContactPhone.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidPhone(txtContactPhone.getText().trim())) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter a valid phone number.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtContactPhone.requestFocus();
            return false;
        }
        
        // Request date validation
        if (!txtRequestDate.getText().trim().isEmpty()) {
            try {
                Date requestDate = dateFormat.parse(txtRequestDate.getText().trim());
                
                // Check if date is in the future
                if (requestDate.after(new Date())) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Request date cannot be in the future.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    txtRequestDate.requestFocus();
                    return false;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Invalid request date format. Use yyyy-MM-dd format.",
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtRequestDate.requestFocus();
                return false;
            }
        }
        
        // Required date validation
        if (!txtRequiredDate.getText().trim().isEmpty()) {
            try {
                Date requiredDate = dateFormat.parse(txtRequiredDate.getText().trim());
                
                // Required date can be in the future, but should be after request date
                if (!txtRequestDate.getText().trim().isEmpty()) {
                    Date requestDate = dateFormat.parse(txtRequestDate.getText().trim());
                    if (requiredDate.before(requestDate)) {
                        JOptionPane.showMessageDialog(mainPanel, 
                            "Required date cannot be before request date.",
                            "Validation Error", 
                            JOptionPane.ERROR_MESSAGE);
                        txtRequiredDate.requestFocus();
                        return false;
                    }
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Invalid required date format. Use yyyy-MM-dd format.",
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtRequiredDate.requestFocus();
                return false;
            }
        }
        
        return true;
    }
}
