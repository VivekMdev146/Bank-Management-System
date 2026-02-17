package bloodbank.controller;

import bloodbank.model.BloodUnit;
import bloodbank.model.Donor;
import bloodbank.model.BloodRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for generating various reports.
 */
public class ReportController {
    private final DatabaseController dbController;
    private final DonorController donorController;
    private final InventoryController inventoryController;
    private final RequestController requestController;

    public ReportController() {
        dbController = DatabaseController.getInstance();
        donorController = new DonorController();
        inventoryController = new InventoryController();
        requestController = new RequestController();
    }

    /**
     * Generate a report of donations within a date range
     * @param startDate start date of the range
     * @param endDate end date of the range
     * @return List of blood units collected in the date range
     */
    public List<BloodUnit> getDonationReport(Date startDate, Date endDate) {
        List<BloodUnit> unitList = new ArrayList<>();
        String sql = "SELECT * FROM blood_inventory WHERE collection_date BETWEEN ? AND ? " +
                     "ORDER BY collection_date DESC";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null) {
                while (rs.next()) {
                    BloodUnit unit = new BloodUnit();
                    unit.setUnitId(rs.getString("unit_id"));
                    unit.setDonorId(rs.getInt("donor_id"));
                    unit.setBloodGroup(rs.getString("blood_group"));
                    unit.setCollectionDate(rs.getDate("collection_date"));
                    unit.setExpiryDate(rs.getDate("expiry_date"));
                    unit.setStatus(rs.getString("status"));
                    unit.setComponent(rs.getString("component"));
                    unitList.add(unit);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating donation report: " + e.getMessage());
        }
        
        return unitList;
    }

    /**
     * Generate a report of blood requests within a date range
     * @param startDate start date of the range
     * @param endDate end date of the range
     * @return List of blood requests in the date range
     */
    public List<BloodRequest> getRequestReport(Date startDate, Date endDate) {
        List<BloodRequest> requestList = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests WHERE request_date BETWEEN ? AND ? " +
                     "ORDER BY request_date DESC";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null) {
                while (rs.next()) {
                    BloodRequest request = new BloodRequest();
                    request.setRequestId(rs.getInt("request_id"));
                    request.setPatientName(rs.getString("patient_name"));
                    request.setBloodGroup(rs.getString("blood_group"));
                    request.setComponent(rs.getString("component"));
                    request.setQuantity(rs.getInt("quantity"));
                    request.setRequestDate(rs.getDate("request_date"));
                    request.setRequiredDate(rs.getDate("required_date"));
                    request.setStatus(rs.getString("status"));
                    request.setHospitalName(rs.getString("hospital_name"));
                    request.setPriority(rs.getString("priority"));
                    requestList.add(request);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating request report: " + e.getMessage());
        }
        
        return requestList;
    }

    /**
     * Generate a report of blood inventory status
     * @return Map containing blood inventory statistics by blood group
     */
    public Map<String, Map<String, Integer>> getInventoryStatusReport() {
        Map<String, Map<String, Integer>> inventoryStatus = new HashMap<>();
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        String[] components = {"whole blood", "plasma", "platelets", "red cells"};
        
        // Initialize the map with zeros
        for (String group : bloodGroups) {
            Map<String, Integer> componentCounts = new HashMap<>();
            for (String component : components) {
                componentCounts.put(component, 0);
            }
            inventoryStatus.put(group, componentCounts);
        }
        
        // Get counts from database
        String sql = "SELECT blood_group, component, COUNT(*) as count FROM blood_inventory " +
                     "WHERE status = 'available' AND is_test_passed = TRUE AND " +
                     "expiry_date > CURDATE() GROUP BY blood_group, component";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    String bloodGroup = rs.getString("blood_group");
                    String component = rs.getString("component");
                    int count = rs.getInt("count");
                    
                    if (inventoryStatus.containsKey(bloodGroup)) {
                        Map<String, Integer> componentMap = inventoryStatus.get(bloodGroup);
                        if (componentMap.containsKey(component)) {
                            componentMap.put(component, count);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating inventory status report: " + e.getMessage());
        }
        
        return inventoryStatus;
    }

    /**
     * Generate a report of expired blood units within a date range
     * @param startDate start date of the range
     * @param endDate end date of the range
     * @return List of expired blood units in the date range
     */
    public List<BloodUnit> getExpiredUnitsReport(Date startDate, Date endDate) {
        List<BloodUnit> unitList = new ArrayList<>();
        String sql = "SELECT * FROM blood_inventory WHERE expiry_date BETWEEN ? AND ? " +
                     "AND (status = 'available' OR status = 'discarded') " +
                     "ORDER BY expiry_date DESC";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null) {
                while (rs.next()) {
                    BloodUnit unit = new BloodUnit();
                    unit.setUnitId(rs.getString("unit_id"));
                    unit.setDonorId(rs.getInt("donor_id"));
                    unit.setBloodGroup(rs.getString("blood_group"));
                    unit.setCollectionDate(rs.getDate("collection_date"));
                    unit.setExpiryDate(rs.getDate("expiry_date"));
                    unit.setStatus(rs.getString("status"));
                    unit.setComponent(rs.getString("component"));
                    unitList.add(unit);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating expired units report: " + e.getMessage());
        }
        
        return unitList;
    }

    /**
     * Generate a report of donors who have donated a specified number of times
     * @param minDonations minimum number of donations
     * @return List of donors who have donated at least the specified number of times
     */
    public List<Donor> getFrequentDonorsReport(int minDonations) {
        List<Donor> donorList = new ArrayList<>();
        String sql = "SELECT d.*, COUNT(bi.unit_id) as donation_count " +
                     "FROM donors d " +
                     "JOIN blood_inventory bi ON d.donor_id = bi.donor_id " +
                     "GROUP BY d.donor_id " +
                     "HAVING donation_count >= ? " +
                     "ORDER BY donation_count DESC";
        
        try (ResultSet rs = dbController.executeQuery(sql, minDonations)) {
            if (rs != null) {
                while (rs.next()) {
                    Donor donor = new Donor();
                    donor.setDonorId(rs.getInt("donor_id"));
                    donor.setName(rs.getString("name"));
                    donor.setBloodGroup(rs.getString("blood_group"));
                    donor.setDateOfBirth(rs.getDate("date_of_birth"));
                    donor.setGender(rs.getString("gender"));
                    donor.setPhone(rs.getString("phone"));
                    donor.setEmail(rs.getString("email"));
                    donor.setLastDonationDate(rs.getDate("last_donation_date"));
                    donorList.add(donor);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating frequent donors report: " + e.getMessage());
        }
        
        return donorList;
    }

    /**
     * Generate a summary report for a specified date range
     * @param startDate start date of the range
     * @param endDate end date of the range
     * @return Map containing summary statistics
     */
    public Map<String, Integer> getSummaryReport(Date startDate, Date endDate) {
        Map<String, Integer> summary = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDateString = dateFormat.format(startDate);
        String endDateString = dateFormat.format(endDate);
        
        // Initialize summary values
        summary.put("totalDonations", 0);
        summary.put("totalRequests", 0);
        summary.put("fulfilledRequests", 0);
        summary.put("pendingRequests", 0);
        summary.put("expiredUnits", 0);
        
        // Get total donations
        String sql = "SELECT COUNT(*) as count FROM blood_inventory " +
                     "WHERE collection_date BETWEEN ? AND ?";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null && rs.next()) {
                summary.put("totalDonations", rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error counting donations: " + e.getMessage());
        }
        
        // Get total requests
        sql = "SELECT COUNT(*) as count FROM blood_requests " +
              "WHERE request_date BETWEEN ? AND ?";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null && rs.next()) {
                summary.put("totalRequests", rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error counting requests: " + e.getMessage());
        }
        
        // Get fulfilled requests
        sql = "SELECT COUNT(*) as count FROM blood_requests " +
              "WHERE request_date BETWEEN ? AND ? " +
              "AND (status = 'fulfilled' OR status = 'partially fulfilled')";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null && rs.next()) {
                summary.put("fulfilledRequests", rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error counting fulfilled requests: " + e.getMessage());
        }
        
        // Get pending requests
        sql = "SELECT COUNT(*) as count FROM blood_requests " +
              "WHERE request_date BETWEEN ? AND ? " +
              "AND status = 'pending'";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null && rs.next()) {
                summary.put("pendingRequests", rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error counting pending requests: " + e.getMessage());
        }
        
        // Get expired units
        sql = "SELECT COUNT(*) as count FROM blood_inventory " +
              "WHERE expiry_date BETWEEN ? AND ? " +
              "AND (status = 'discarded' OR (status = 'available' AND expiry_date < CURDATE()))";
        
        try (ResultSet rs = dbController.executeQuery(sql, startDate, endDate)) {
            if (rs != null && rs.next()) {
                summary.put("expiredUnits", rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Error counting expired units: " + e.getMessage());
        }
        
        return summary;
    }
}
