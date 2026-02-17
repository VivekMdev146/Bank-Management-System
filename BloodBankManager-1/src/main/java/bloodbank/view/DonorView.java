package bloodbank.view;

import bloodbank.controller.DonorController;
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
import java.util.Date;
import java.util.List;

/**
 * View for managing blood donors.
 */
public class DonorView {
    private JPanel mainPanel;
    private JTable donorTable;
    private DefaultTableModel tableModel;
    private JTextField txtName;
    private JComboBox<String> cmbBloodGroup;
    private JTextField txtDOB;
    private JComboBox<String> cmbGender;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextArea txtAddress;
    private JCheckBox chkEligible;
    private JTextArea txtMedicalHistory;
    private JTextField txtLastDonation;
    private JButton btnSave;
    private JButton btnClear;
    private JButton btnDelete;
    private JTextField txtSearch;
    private JComboBox<String> cmbSearchBy;
    private JButton btnSearch;
    
    private final DonorController donorController;
    private int selectedDonorId = 0;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Constructor for DonorView.
     */
    public DonorView() {
        donorController = new DonorController();
        initializeUI();
        loadDonorData();
    }

    /**
     * Initialize the user interface.
     */
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTitle = new JLabel("Donor Management");
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
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
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
        cmbSearchBy = new JComboBox<>(new String[] {"Name", "Blood Group", "Phone", "Email"});
        
        // Search button
        btnSearch = new JButton("Search");
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchDonors();
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
        tableModel.addColumn("ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Blood Group");
        tableModel.addColumn("Gender");
        tableModel.addColumn("Phone");
        tableModel.addColumn("Last Donation");
        tableModel.addColumn("Eligible");
        
        // Create table
        donorTable = new JTable(tableModel);
        donorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        donorTable.getTableHeader().setReorderingAllowed(false);
        donorTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add mouse listener to table
        donorTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = donorTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedDonorId = (int) donorTable.getValueAt(selectedRow, 0);
                    loadDonorDetails(selectedDonorId);
                }
            }
        });
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(donorTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    /**
     * Create the form panel.
     * @return the form panel
     */
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Donor Details"));
        
        // Form fields panel
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Name:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        txtName = new JTextField(20);
        fieldsPanel.add(txtName, gbc);
        
        // Blood Group
        gbc.gridx = 2;
        gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Blood Group:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 0;
        cmbBloodGroup = new JComboBox<>(new String[] {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        fieldsPanel.add(cmbBloodGroup, gbc);
        
        // Date of Birth
        gbc.gridx = 0;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Date of Birth (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        txtDOB = new JTextField(10);
        fieldsPanel.add(txtDOB, gbc);
        
        // Gender
        gbc.gridx = 2;
        gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Gender:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 1;
        cmbGender = new JComboBox<>(new String[] {"Male", "Female", "Other"});
        fieldsPanel.add(cmbGender, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Phone:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        txtPhone = new JTextField(10);
        fieldsPanel.add(txtPhone, gbc);
        
        // Email
        gbc.gridx = 2;
        gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Email:"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 2;
        txtEmail = new JTextField(20);
        fieldsPanel.add(txtEmail, gbc);
        
        // Address
        gbc.gridx = 0;
        gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        txtAddress = new JTextArea(2, 40);
        txtAddress.setLineWrap(true);
        JScrollPane addressScrollPane = new JScrollPane(txtAddress);
        fieldsPanel.add(addressScrollPane, gbc);
        
        // Eligible
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        fieldsPanel.add(new JLabel("Eligible to Donate:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        chkEligible = new JCheckBox();
        chkEligible.setSelected(true);
        fieldsPanel.add(chkEligible, gbc);
        
        // Last Donation
        gbc.gridx = 2;
        gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Last Donation (yyyy-MM-dd):"), gbc);
        
        gbc.gridx = 3;
        gbc.gridy = 4;
        txtLastDonation = new JTextField(10);
        fieldsPanel.add(txtLastDonation, gbc);
        
        // Medical History
        gbc.gridx = 0;
        gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Medical History:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        txtMedicalHistory = new JTextArea(3, 40);
        txtMedicalHistory.setLineWrap(true);
        JScrollPane medHistoryScrollPane = new JScrollPane(txtMedicalHistory);
        fieldsPanel.add(medHistoryScrollPane, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        // Save button
        btnSave = new JButton("Save");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDonor();
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
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteDonor();
            }
        });
        buttonPanel.add(btnDelete);
        
        // Add components to form panel
        formPanel.add(fieldsPanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return formPanel;
    }

    /**
     * Get the main panel of the donor view.
     * @return the main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Load donor data into the table.
     */
    private void loadDonorData() {
        // Clear the table model
        tableModel.setRowCount(0);
        
        // Get all donors
        List<Donor> donors = donorController.getAllDonors();
        
        // Add donors to table model
        for (Donor donor : donors) {
            Object[] rowData = {
                donor.getDonorId(),
                donor.getName(),
                donor.getBloodGroup(),
                donor.getGender(),
                donor.getPhone(),
                donor.getLastDonationDate() != null ? dateFormat.format(donor.getLastDonationDate()) : "Never",
                donor.isEligible() ? "Yes" : "No"
            };
            tableModel.addRow(rowData);
        }
    }

    /**
     * Load donor details into the form.
     * @param donorId donor ID to load
     */
    private void loadDonorDetails(int donorId) {
        Donor donor = donorController.getDonorById(donorId);
        if (donor != null) {
            txtName.setText(donor.getName());
            cmbBloodGroup.setSelectedItem(donor.getBloodGroup());
            txtDOB.setText(donor.getDateOfBirth() != null ? dateFormat.format(donor.getDateOfBirth()) : "");
            cmbGender.setSelectedItem(donor.getGender());
            txtPhone.setText(donor.getPhone());
            txtEmail.setText(donor.getEmail());
            txtAddress.setText(donor.getAddress());
            chkEligible.setSelected(donor.isEligible());
            txtLastDonation.setText(donor.getLastDonationDate() != null ? dateFormat.format(donor.getLastDonationDate()) : "");
            txtMedicalHistory.setText(donor.getMedicalHistory());
            
            // Enable delete button
            btnDelete.setEnabled(true);
        }
    }

    /**
     * Save donor information.
     */
    private void saveDonor() {
        // Validate form
        if (!validateForm()) {
            return;
        }
        
        try {
            // Create donor object
            Donor donor = new Donor();
            donor.setName(txtName.getText().trim());
            donor.setBloodGroup((String) cmbBloodGroup.getSelectedItem());
            donor.setGender((String) cmbGender.getSelectedItem());
            donor.setPhone(txtPhone.getText().trim());
            donor.setEmail(txtEmail.getText().trim());
            donor.setAddress(txtAddress.getText().trim());
            donor.setEligible(chkEligible.isSelected());
            donor.setMedicalHistory(txtMedicalHistory.getText().trim());
            
            // Parse date of birth
            String dobString = txtDOB.getText().trim();
            if (!dobString.isEmpty()) {
                try {
                    donor.setDateOfBirth(dateFormat.parse(dobString));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid date of birth format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Parse last donation date
            String lastDonationString = txtLastDonation.getText().trim();
            if (!lastDonationString.isEmpty()) {
                try {
                    donor.setLastDonationDate(dateFormat.parse(lastDonationString));
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid last donation date format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Set registration date for new donors
            if (selectedDonorId == 0) {
                donor.setRegistrationDate(new Date());
            }
            
            boolean success;
            if (selectedDonorId == 0) {
                // Add new donor
                success = donorController.addDonor(donor);
            } else {
                // Update existing donor
                donor.setDonorId(selectedDonorId);
                success = donorController.updateDonor(donor);
            }
            
            if (success) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Donor information saved successfully.",
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload donor data and clear form
                loadDonorData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Failed to save donor information.",
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
     * Delete a donor.
     */
    private void deleteDonor() {
        if (selectedDonorId == 0) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please select a donor to delete.",
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirmation = JOptionPane.showConfirmDialog(mainPanel, 
            "Are you sure you want to delete this donor?",
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = donorController.deleteDonor(selectedDonorId);
            
            if (success) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Donor deleted successfully.",
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Reload donor data and clear form
                loadDonorData();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Failed to delete donor. This donor may have blood units recorded.",
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Search donors based on search criteria.
     */
    private void searchDonors() {
        String searchTerm = txtSearch.getText().trim();
        String searchBy = (String) cmbSearchBy.getSelectedItem();
        
        if (searchTerm.isEmpty()) {
            loadDonorData();
            return;
        }
        
        // Convert search field to database column name
        String searchField;
        switch (searchBy) {
            case "Name": 
                searchField = "name"; 
                break;
            case "Blood Group": 
                searchField = "blood_group"; 
                break;
            case "Phone": 
                searchField = "phone"; 
                break;
            case "Email": 
                searchField = "email"; 
                break;
            default: 
                searchField = "name";
        }
        
        // Search donors
        List<Donor> donors = donorController.searchDonors(searchTerm, searchField);
        
        // Clear table model
        tableModel.setRowCount(0);
        
        // Add donors to table model
        for (Donor donor : donors) {
            Object[] rowData = {
                donor.getDonorId(),
                donor.getName(),
                donor.getBloodGroup(),
                donor.getGender(),
                donor.getPhone(),
                donor.getLastDonationDate() != null ? dateFormat.format(donor.getLastDonationDate()) : "Never",
                donor.isEligible() ? "Yes" : "No"
            };
            tableModel.addRow(rowData);
        }
    }

    /**
     * Clear the form.
     */
    private void clearForm() {
        txtName.setText("");
        cmbBloodGroup.setSelectedIndex(0);
        txtDOB.setText("");
        cmbGender.setSelectedIndex(0);
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        chkEligible.setSelected(true);
        txtLastDonation.setText("");
        txtMedicalHistory.setText("");
        
        selectedDonorId = 0;
        btnDelete.setEnabled(false);
    }

    /**
     * Validate the form.
     * @return true if form is valid, false otherwise
     */
    private boolean validateForm() {
        // Name validation
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter donor name.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtName.requestFocus();
            return false;
        }
        
        // Phone validation
        if (txtPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter phone number.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }
        
        if (!ValidationUtil.isValidPhone(txtPhone.getText().trim())) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter a valid phone number.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtPhone.requestFocus();
            return false;
        }
        
        // Email validation (if provided)
        if (!txtEmail.getText().trim().isEmpty() && !ValidationUtil.isValidEmail(txtEmail.getText().trim())) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Please enter a valid email address.",
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            txtEmail.requestFocus();
            return false;
        }
        
        // Date of birth validation (if provided)
        if (!txtDOB.getText().trim().isEmpty()) {
            try {
                Date dob = dateFormat.parse(txtDOB.getText().trim());
                
                // Check if date is in the future
                if (dob.after(new Date())) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Date of birth cannot be in the future.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    txtDOB.requestFocus();
                    return false;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Invalid date of birth format. Use yyyy-MM-dd format.",
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtDOB.requestFocus();
                return false;
            }
        }
        
        // Last donation date validation (if provided)
        if (!txtLastDonation.getText().trim().isEmpty()) {
            try {
                Date lastDonation = dateFormat.parse(txtLastDonation.getText().trim());
                
                // Check if date is in the future
                if (lastDonation.after(new Date())) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Last donation date cannot be in the future.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    txtLastDonation.requestFocus();
                    return false;
                }
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Invalid last donation date format. Use yyyy-MM-dd format.",
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
                txtLastDonation.requestFocus();
                return false;
            }
        }
        
        return true;
    }
}
