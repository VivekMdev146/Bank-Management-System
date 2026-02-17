package bloodbank.view;

import bloodbank.controller.ReportController;
import bloodbank.model.BloodRequest;
import bloodbank.model.BloodUnit;
import bloodbank.model.Donor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * View for generating and displaying reports.
 */
public class ReportView {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextArea summaryTextArea;
    
    private JComboBox<String> cmbReportType;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JComboBox<String> cmbBloodGroup;
    private JSpinner spnMinDonations;
    private JButton btnGenerateReport;
    private JButton btnPrintReport;
    private JButton btnExportReport;
    
    private final ReportController reportController;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String currentReportType = "";

    /**
     * Constructor for ReportView.
     */
    public ReportView() {
        reportController = new ReportController();
        initializeUI();
    }

    /**
     * Initialize the user interface.
     */
    private void initializeUI() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTitle = new JLabel("Reports");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        titlePanel.add(lblTitle);
        
        // Create controls panel
        JPanel controlsPanel = createControlsPanel();
        
        // Create top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titlePanel, BorderLayout.WEST);
        
        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        tabbedPane.addTab("Report Details", tablePanel);
        
        // Create summary panel
        JPanel summaryPanel = createSummaryPanel();
        tabbedPane.addTab("Summary", summaryPanel);
        
        // Add components to main panel
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(controlsPanel, BorderLayout.WEST);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    /**
     * Create the controls panel.
     * @return the controls panel
     */
    private JPanel createControlsPanel() {
        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Report Controls"));
        controlsPanel.setPreferredSize(new Dimension(250, 400));
        
        // Report type
        JPanel reportTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reportTypePanel.add(new JLabel("Report Type:"));
        
        cmbReportType = new JComboBox<>(new String[] {
            "Donation Report", 
            "Request Report", 
            "Inventory Status", 
            "Expired Units Report", 
            "Frequent Donors Report",
            "Summary Report"
        });
        cmbReportType.setPreferredSize(new Dimension(220, 25));
        cmbReportType.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateControlsForReportType();
            }
        });
        
        JPanel cmbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cmbPanel.add(cmbReportType);
        
        // Date range
        JPanel dateRangePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        dateRangePanel.add(new JLabel("Start Date (yyyy-MM-dd):"));
        
        // Set default start date to first day of current month
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        txtStartDate = new JTextField(dateFormat.format(cal.getTime()));
        dateRangePanel.add(txtStartDate);
        
        dateRangePanel.add(new JLabel("End Date (yyyy-MM-dd):"));
        
        // Set default end date to today
        txtEndDate = new JTextField(dateFormat.format(new Date()));
        dateRangePanel.add(txtEndDate);
        
        // Blood group
        JPanel bloodGroupPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bloodGroupPanel.add(new JLabel("Blood Group:"));
        
        cmbBloodGroup = new JComboBox<>(new String[] {
            "All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        });
        cmbBloodGroup.setPreferredSize(new Dimension(100, 25));
        bloodGroupPanel.add(cmbBloodGroup);
        
        // Min donations
        JPanel minDonationsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        minDonationsPanel.add(new JLabel("Min Donations:"));
        
        spnMinDonations = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        spnMinDonations.setPreferredSize(new Dimension(60, 25));
        minDonationsPanel.add(spnMinDonations);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        btnGenerateReport = new JButton("Generate Report");
        btnGenerateReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateReport();
            }
        });
        
        btnPrintReport = new JButton("Print Report");
        btnPrintReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                printReport();
            }
        });
        
        btnExportReport = new JButton("Export Report");
        btnExportReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });
        
        buttonPanel.add(btnGenerateReport);
        buttonPanel.add(btnPrintReport);
        buttonPanel.add(btnExportReport);
        
        // Add components to controls panel
        controlsPanel.add(reportTypePanel);
        controlsPanel.add(cmbPanel);
        controlsPanel.add(Box.createVerticalStrut(15));
        controlsPanel.add(dateRangePanel);
        controlsPanel.add(Box.createVerticalStrut(15));
        controlsPanel.add(bloodGroupPanel);
        controlsPanel.add(Box.createVerticalStrut(5));
        controlsPanel.add(minDonationsPanel);
        controlsPanel.add(Box.createVerticalStrut(15));
        controlsPanel.add(buttonPanel);
        controlsPanel.add(Box.createVerticalGlue());
        
        // Initially hide components that aren't applicable to the default report type
        updateControlsForReportType();
        
        return controlsPanel;
    }

    /**
     * Create the table panel.
     * @return the table panel
     */
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create table model
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table
        reportTable = new JTable(tableModel);
        reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportTable.getTableHeader().setReorderingAllowed(false);
        reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        
        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(reportTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }

    /**
     * Create the summary panel.
     * @return the summary panel
     */
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Create text area for summary
        summaryTextArea = new JTextArea();
        summaryTextArea.setEditable(false);
        summaryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        // Add text area to scroll pane
        JScrollPane scrollPane = new JScrollPane(summaryTextArea);
        summaryPanel.add(scrollPane, BorderLayout.CENTER);
        
        return summaryPanel;
    }
    
    /**
     * Update the controls based on the selected report type.
     */
    private void updateControlsForReportType() {
        String reportType = (String) cmbReportType.getSelectedItem();
        
        // Disable all components initially
        txtStartDate.setEnabled(false);
        txtEndDate.setEnabled(false);
        cmbBloodGroup.setEnabled(false);
        spnMinDonations.setEnabled(false);
        
        // Enable components based on report type
        switch (reportType) {
            case "Donation Report":
            case "Request Report":
            case "Expired Units Report":
            case "Summary Report":
                txtStartDate.setEnabled(true);
                txtEndDate.setEnabled(true);
                break;
            case "Inventory Status":
                cmbBloodGroup.setEnabled(true);
                break;
            case "Frequent Donors Report":
                spnMinDonations.setEnabled(true);
                break;
        }
    }
    
    /**
     * Generate a report based on the selected type and parameters.
     */
    private void generateReport() {
        String reportType = (String) cmbReportType.getSelectedItem();
        currentReportType = reportType;
        
        try {
            // Parse date parameters if needed
            Date startDate = null;
            Date endDate = null;
            if (txtStartDate.isEnabled()) {
                try {
                    startDate = dateFormat.parse(txtStartDate.getText().trim());
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid start date format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            if (txtEndDate.isEnabled()) {
                try {
                    endDate = dateFormat.parse(txtEndDate.getText().trim());
                } catch (ParseException e) {
                    JOptionPane.showMessageDialog(mainPanel, 
                        "Invalid end date format. Use yyyy-MM-dd format.",
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Clear existing table model
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);
            summaryTextArea.setText("");
            
            // Generate the appropriate report
            switch (reportType) {
                case "Donation Report":
                    generateDonationReport(startDate, endDate);
                    break;
                case "Request Report":
                    generateRequestReport(startDate, endDate);
                    break;
                case "Inventory Status":
                    generateInventoryStatusReport();
                    break;
                case "Expired Units Report":
                    generateExpiredUnitsReport(startDate, endDate);
                    break;
                case "Frequent Donors Report":
                    generateFrequentDonorsReport();
                    break;
                case "Summary Report":
                    generateSummaryReport(startDate, endDate);
                    break;
            }
            
            // Switch to the appropriate tab
            if (reportType.equals("Summary Report")) {
                tabbedPane.setSelectedIndex(1); // Summary tab
            } else {
                tabbedPane.setSelectedIndex(0); // Table tab
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Error generating report: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Generate a donation report.
     * @param startDate start date of the report period
     * @param endDate end date of the report period
     */
    private void generateDonationReport(Date startDate, Date endDate) {
        // Set up columns
        tableModel.addColumn("Unit ID");
        tableModel.addColumn("Donor ID");
        tableModel.addColumn("Blood Group");
        tableModel.addColumn("Component");
        tableModel.addColumn("Collection Date");
        tableModel.addColumn("Expiry Date");
        tableModel.addColumn("Status");
        
        // Get donation data
        List<BloodUnit> units = reportController.getDonationReport(startDate, endDate);
        
        // Add data to table
        for (BloodUnit unit : units) {
            Object[] rowData = {
                unit.getUnitId(),
                unit.getDonorId(),
                unit.getBloodGroup(),
                unit.getComponent(),
                unit.getCollectionDate() != null ? dateFormat.format(unit.getCollectionDate()) : "",
                unit.getExpiryDate() != null ? dateFormat.format(unit.getExpiryDate()) : "",
                unit.getStatus()
            };
            tableModel.addRow(rowData);
        }
        
        // Generate summary text
        StringBuilder summaryText = new StringBuilder();
        summaryText.append("DONATION REPORT\n");
        summaryText.append("==============\n\n");
        summaryText.append("Period: ")
                  .append(dateFormat.format(startDate))
                  .append(" to ")
                  .append(dateFormat.format(endDate))
                  .append("\n\n");
        summaryText.append("Total donations: ").append(units.size()).append("\n\n");
        
        // Count donations by blood group
        Map<String, Integer> bloodGroupCounts = new java.util.HashMap<>();
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (String bg : bloodGroups) {
            bloodGroupCounts.put(bg, 0);
        }
        
        for (BloodUnit unit : units) {
            String bg = unit.getBloodGroup();
            bloodGroupCounts.put(bg, bloodGroupCounts.getOrDefault(bg, 0) + 1);
        }
        
        summaryText.append("Donations by Blood Group:\n");
        for (String bg : bloodGroups) {
            summaryText.append(bg).append(": ").append(bloodGroupCounts.get(bg)).append("\n");
        }
        
        summaryTextArea.setText(summaryText.toString());
    }
    
    /**
     * Generate a request report.
     * @param startDate start date of the report period
     * @param endDate end date of the report period
     */
    private void generateRequestReport(Date startDate, Date endDate) {
        // Set up columns
        tableModel.addColumn("Request ID");
        tableModel.addColumn("Patient Name");
        tableModel.addColumn("Blood Group");
        tableModel.addColumn("Component");
        tableModel.addColumn("Quantity");
        tableModel.addColumn("Request Date");
        tableModel.addColumn("Required Date");
        tableModel.addColumn("Status");
        tableModel.addColumn("Hospital");
        tableModel.addColumn("Priority");
        
        // Get request data
        List<BloodRequest> requests = reportController.getRequestReport(startDate, endDate);
        
        // Add data to table
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
                request.getHospitalName(),
                request.getPriority()
            };
            tableModel.addRow(rowData);
        }
        
        // Generate summary text
        StringBuilder summaryText = new StringBuilder();
        summaryText.append("BLOOD REQUEST REPORT\n");
        summaryText.append("====================\n\n");
        summaryText.append("Period: ")
                  .append(dateFormat.format(startDate))
                  .append(" to ")
                  .append(dateFormat.format(endDate))
                  .append("\n\n");
        summaryText.append("Total requests: ").append(requests.size()).append("\n\n");
        
        // Count requests by status
        int pending = 0;
        int fulfilled = 0;
        int partiallyFulfilled = 0;
        int cancelled = 0;
        
        for (BloodRequest request : requests) {
            switch (request.getStatus()) {
                case "pending": pending++; break;
                case "fulfilled": fulfilled++; break;
                case "partially fulfilled": partiallyFulfilled++; break;
                case "cancelled": cancelled++; break;
            }
        }
        
        summaryText.append("Request Status Summary:\n");
        summaryText.append("Pending: ").append(pending).append("\n");
        summaryText.append("Fulfilled: ").append(fulfilled).append("\n");
        summaryText.append("Partially Fulfilled: ").append(partiallyFulfilled).append("\n");
        summaryText.append("Cancelled: ").append(cancelled).append("\n");
        
        summaryTextArea.setText(summaryText.toString());
    }
    
    /**
     * Generate an inventory status report.
     */
    private void generateInventoryStatusReport() {
        // Set up columns
        tableModel.addColumn("Blood Group");
        tableModel.addColumn("Whole Blood");
        tableModel.addColumn("Plasma");
        tableModel.addColumn("Platelets");
        tableModel.addColumn("Red Cells");
        tableModel.addColumn("Total");
        
        // Get inventory data
        Map<String, Map<String, Integer>> inventoryStatus = reportController.getInventoryStatusReport();
        
        // Filter by selected blood group if not "All"
        String selectedBloodGroup = (String) cmbBloodGroup.getSelectedItem();
        String[] bloodGroups;
        if (selectedBloodGroup.equals("All")) {
            bloodGroups = new String[] {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        } else {
            bloodGroups = new String[] {selectedBloodGroup};
        }
        
        // Add data to table
        for (String bg : bloodGroups) {
            Map<String, Integer> componentMap = inventoryStatus.get(bg);
            if (componentMap != null) {
                int wholeBlood = componentMap.getOrDefault("whole blood", 0);
                int plasma = componentMap.getOrDefault("plasma", 0);
                int platelets = componentMap.getOrDefault("platelets", 0);
                int redCells = componentMap.getOrDefault("red cells", 0);
                int total = wholeBlood + plasma + platelets + redCells;
                
                Object[] rowData = {
                    bg,
                    wholeBlood,
                    plasma,
                    platelets,
                    redCells,
                    total
                };
                tableModel.addRow(rowData);
            }
        }
        
        // Generate summary text
        StringBuilder summaryText = new StringBuilder();
        summaryText.append("BLOOD INVENTORY STATUS REPORT\n");
        summaryText.append("=============================\n\n");
        summaryText.append("Date: ").append(dateFormat.format(new Date())).append("\n\n");
        
        // Calculate total units
        int totalUnits = 0;
        for (String bg : bloodGroups) {
            Map<String, Integer> componentMap = inventoryStatus.get(bg);
            if (componentMap != null) {
                for (int count : componentMap.values()) {
                    totalUnits += count;
                }
            }
        }
        
        summaryText.append("Total Available Units: ").append(totalUnits).append("\n\n");
        
        // Add detailed counts
        summaryText.append("Inventory by Blood Group and Component:\n\n");
        summaryText.append(String.format("%-5s %-15s %-10s %-10s %-10s %-10s\n", 
            "Group", "Whole Blood", "Plasma", "Platelets", "Red Cells", "Total"));
        summaryText.append("--------------------------------------------------------\n");
        
        for (String bg : bloodGroups) {
            Map<String, Integer> componentMap = inventoryStatus.get(bg);
            if (componentMap != null) {
                int wholeBlood = componentMap.getOrDefault("whole blood", 0);
                int plasma = componentMap.getOrDefault("plasma", 0);
                int platelets = componentMap.getOrDefault("platelets", 0);
                int redCells = componentMap.getOrDefault("red cells", 0);
                int total = wholeBlood + plasma + platelets + redCells;
                
                summaryText.append(String.format("%-5s %-15d %-10d %-10d %-10d %-10d\n", 
                    bg, wholeBlood, plasma, platelets, redCells, total));
            }
        }
        
        summaryTextArea.setText(summaryText.toString());
    }
    
    /**
     * Generate an expired units report.
     * @param startDate start date of the report period
     * @param endDate end date of the report period
     */
    private void generateExpiredUnitsReport(Date startDate, Date endDate) {
        // Set up columns
        tableModel.addColumn("Unit ID");
        tableModel.addColumn("Blood Group");
        tableModel.addColumn("Component");
        tableModel.addColumn("Collection Date");
        tableModel.addColumn("Expiry Date");
        tableModel.addColumn("Status");
        tableModel.addColumn("Donor ID");
        
        // Get expired units data
        List<BloodUnit> units = reportController.getExpiredUnitsReport(startDate, endDate);
        
        // Add data to table
        for (BloodUnit unit : units) {
            Object[] rowData = {
                unit.getUnitId(),
                unit.getBloodGroup(),
                unit.getComponent(),
                unit.getCollectionDate() != null ? dateFormat.format(unit.getCollectionDate()) : "",
                unit.getExpiryDate() != null ? dateFormat.format(unit.getExpiryDate()) : "",
                unit.getStatus(),
                unit.getDonorId()
            };
            tableModel.addRow(rowData);
        }
        
        // Generate summary text
        StringBuilder summaryText = new StringBuilder();
        summaryText.append("EXPIRED BLOOD UNITS REPORT\n");
        summaryText.append("=========================\n\n");
        summaryText.append("Period: ")
                  .append(dateFormat.format(startDate))
                  .append(" to ")
                  .append(dateFormat.format(endDate))
                  .append("\n\n");
        summaryText.append("Total expired units: ").append(units.size()).append("\n\n");
        
        // Count expired units by blood group
        Map<String, Integer> bloodGroupCounts = new java.util.HashMap<>();
        for (BloodUnit unit : units) {
            String bg = unit.getBloodGroup();
            bloodGroupCounts.put(bg, bloodGroupCounts.getOrDefault(bg, 0) + 1);
        }
        
        summaryText.append("Expired units by Blood Group:\n");
        for (Map.Entry<String, Integer> entry : bloodGroupCounts.entrySet()) {
            summaryText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Count expired units by component
        Map<String, Integer> componentCounts = new java.util.HashMap<>();
        for (BloodUnit unit : units) {
            String component = unit.getComponent();
            componentCounts.put(component, componentCounts.getOrDefault(component, 0) + 1);
        }
        
        summaryText.append("\nExpired units by Component:\n");
        for (Map.Entry<String, Integer> entry : componentCounts.entrySet()) {
            summaryText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        summaryTextArea.setText(summaryText.toString());
    }
    
    /**
     * Generate a frequent donors report.
     */
    private void generateFrequentDonorsReport() {
        // Set up columns
        tableModel.addColumn("Donor ID");
        tableModel.addColumn("Name");
        tableModel.addColumn("Blood Group");
        tableModel.addColumn("Gender");
        tableModel.addColumn("Phone");
        tableModel.addColumn("Email");
        tableModel.addColumn("Last Donation");
        
        // Get minimum donations count
        int minDonations = (Integer) spnMinDonations.getValue();
        
        // Get frequent donors data
        List<Donor> donors = reportController.getFrequentDonorsReport(minDonations);
        
        // Add data to table
        for (Donor donor : donors) {
            Object[] rowData = {
                donor.getDonorId(),
                donor.getName(),
                donor.getBloodGroup(),
                donor.getGender(),
                donor.getPhone(),
                donor.getEmail(),
                donor.getLastDonationDate() != null ? dateFormat.format(donor.getLastDonationDate()) : "Never"
            };
            tableModel.addRow(rowData);
        }
        
        // Generate summary text
        StringBuilder summaryText = new StringBuilder();
        summaryText.append("FREQUENT DONORS REPORT\n");
        summaryText.append("=====================\n\n");
        summaryText.append("Minimum donations: ").append(minDonations).append("\n\n");
        summaryText.append("Total frequent donors: ").append(donors.size()).append("\n\n");
        
        // Count donors by blood group
        Map<String, Integer> bloodGroupCounts = new java.util.HashMap<>();
        for (Donor donor : donors) {
            String bg = donor.getBloodGroup();
            bloodGroupCounts.put(bg, bloodGroupCounts.getOrDefault(bg, 0) + 1);
        }
        
        summaryText.append("Frequent donors by Blood Group:\n");
        for (Map.Entry<String, Integer> entry : bloodGroupCounts.entrySet()) {
            summaryText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        // Count donors by gender
        Map<String, Integer> genderCounts = new java.util.HashMap<>();
        for (Donor donor : donors) {
            String gender = donor.getGender();
            genderCounts.put(gender, genderCounts.getOrDefault(gender, 0) + 1);
        }
        
        summaryText.append("\nFrequent donors by Gender:\n");
        for (Map.Entry<String, Integer> entry : genderCounts.entrySet()) {
            summaryText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        summaryTextArea.setText(summaryText.toString());
    }
    
    /**
     * Generate a summary report.
     * @param startDate start date of the report period
     * @param endDate end date of the report period
     */
    private void generateSummaryReport(Date startDate, Date endDate) {
        // Get summary data
        Map<String, Integer> summary = reportController.getSummaryReport(startDate, endDate);
        
        // Generate summary text
        StringBuilder summaryText = new StringBuilder();
        summaryText.append("BLOOD BANK SUMMARY REPORT\n");
        summaryText.append("=========================\n\n");
        summaryText.append("Period: ")
                  .append(dateFormat.format(startDate))
                  .append(" to ")
                  .append(dateFormat.format(endDate))
                  .append("\n\n");
        
        summaryText.append("DONATIONS\n");
        summaryText.append("---------\n");
        summaryText.append("Total Donations: ").append(summary.get("totalDonations")).append("\n\n");
        
        summaryText.append("REQUESTS\n");
        summaryText.append("--------\n");
        summaryText.append("Total Requests: ").append(summary.get("totalRequests")).append("\n");
        summaryText.append("Fulfilled Requests: ").append(summary.get("fulfilledRequests")).append("\n");
        summaryText.append("Pending Requests: ").append(summary.get("pendingRequests")).append("\n\n");
        
        summaryText.append("INVENTORY\n");
        summaryText.append("---------\n");
        summaryText.append("Expired Units: ").append(summary.get("expiredUnits")).append("\n\n");
        
        // Add inventory status
        Map<String, Map<String, Integer>> inventoryStatus = reportController.getInventoryStatusReport();
        
        // Calculate total units
        int totalAvailableUnits = 0;
        for (Map<String, Integer> componentMap : inventoryStatus.values()) {
            for (int count : componentMap.values()) {
                totalAvailableUnits += count;
            }
        }
        
        summaryText.append("Current Available Units: ").append(totalAvailableUnits).append("\n\n");
        
        // Calculate efficiency metrics
        int totalRequests = summary.get("totalRequests");
        int fulfilledRequests = summary.get("fulfilledRequests");
        double fulfillmentRate = totalRequests > 0 ? (double) fulfilledRequests / totalRequests * 100 : 0;
        
        int totalDonations = summary.get("totalDonations");
        int expiredUnits = summary.get("expiredUnits");
        double wastageRate = totalDonations > 0 ? (double) expiredUnits / totalDonations * 100 : 0;
        
        summaryText.append("EFFICIENCY METRICS\n");
        summaryText.append("-----------------\n");
        summaryText.append(String.format("Request Fulfillment Rate: %.2f%%\n", fulfillmentRate));
        summaryText.append(String.format("Blood Wastage Rate: %.2f%%\n", wastageRate));
        
        summaryTextArea.setText(summaryText.toString());
    }
    
    /**
     * Print the current report.
     */
    private void printReport() {
        try {
            if (currentReportType.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Please generate a report first.",
                    "No Report", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // For this implementation, just show a message
            JOptionPane.showMessageDialog(mainPanel, 
                "Printing functionality would be implemented here.\nThe report would be sent to the default printer.",
                "Print Report", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Error printing report: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Export the current report.
     */
    private void exportReport() {
        try {
            if (currentReportType.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, 
                    "Please generate a report first.",
                    "No Report", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // For this implementation, just show a message
            JOptionPane.showMessageDialog(mainPanel, 
                "Export functionality would be implemented here.\nThe report would be saved as CSV or PDF.",
                "Export Report", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainPanel, 
                "Error exporting report: " + e.getMessage(),
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Get the main panel of the report view.
     * @return the main panel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }
}
