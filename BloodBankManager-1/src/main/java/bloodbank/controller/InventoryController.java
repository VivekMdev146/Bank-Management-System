package bloodbank.controller;

import bloodbank.model.BloodUnit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling blood inventory operations.
 */
public class InventoryController {
    private final DatabaseController dbController;

    public InventoryController() {
        dbController = DatabaseController.getInstance();
    }

    /**
     * Get a blood unit by ID
     * @param unitId unit ID to look up
     * @return BloodUnit object if found, null otherwise
     */
    public BloodUnit getBloodUnitById(String unitId) {
        String sql = "SELECT * FROM blood_inventory WHERE unit_id = ?";
        
        try (ResultSet rs = dbController.executeQuery(sql, unitId)) {
            if (rs != null && rs.next()) {
                return extractBloodUnitFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting blood unit by ID: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all blood units
     * @return List of all blood units
     */
    public List<BloodUnit> getAllBloodUnits() {
        List<BloodUnit> unitList = new ArrayList<>();
        String sql = "SELECT * FROM blood_inventory ORDER BY collection_date DESC";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    unitList.add(extractBloodUnitFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all blood units: " + e.getMessage());
        }
        
        return unitList;
    }

    /**
     * Get available blood units (not reserved, not discarded, not issued)
     * @return List of available blood units
     */
    public List<BloodUnit> getAvailableBloodUnits() {
        List<BloodUnit> unitList = new ArrayList<>();
        String sql = "SELECT * FROM blood_inventory WHERE status = 'available' AND is_test_passed = TRUE AND " +
                     "expiry_date > CURDATE() ORDER BY expiry_date ASC";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    unitList.add(extractBloodUnitFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting available blood units: " + e.getMessage());
        }
        
        return unitList;
    }

    /**
     * Get available blood units by blood group
     * @param bloodGroup blood group to filter by
     * @return List of available blood units of the specified blood group
     */
    public List<BloodUnit> getAvailableBloodUnitsByBloodGroup(String bloodGroup) {
        List<BloodUnit> unitList = new ArrayList<>();
        String sql = "SELECT * FROM blood_inventory WHERE status = 'available' AND is_test_passed = TRUE AND " +
                     "expiry_date > CURDATE() AND blood_group = ? ORDER BY expiry_date ASC";
        
        try (ResultSet rs = dbController.executeQuery(sql, bloodGroup)) {
            if (rs != null) {
                while (rs.next()) {
                    unitList.add(extractBloodUnitFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting blood units by blood group: " + e.getMessage());
        }
        
        return unitList;
    }

    /**
     * Get available blood units by component
     * @param component component to filter by
     * @return List of available blood units of the specified component
     */
    public List<BloodUnit> getAvailableBloodUnitsByComponent(String component) {
        List<BloodUnit> unitList = new ArrayList<>();
        String sql = "SELECT * FROM blood_inventory WHERE status = 'available' AND is_test_passed = TRUE AND " +
                     "expiry_date > CURDATE() AND component = ? ORDER BY expiry_date ASC";
        
        try (ResultSet rs = dbController.executeQuery(sql, component)) {
            if (rs != null) {
                while (rs.next()) {
                    unitList.add(extractBloodUnitFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting blood units by component: " + e.getMessage());
        }
        
        return unitList;
    }

    /**
     * Add a new blood unit to inventory
     * @param bloodUnit BloodUnit object to add
     * @return true if successful, false otherwise
     */
    public boolean addBloodUnit(BloodUnit bloodUnit) {
        String sql = "INSERT INTO blood_inventory (unit_id, donor_id, blood_group, collection_date, " +
                     "expiry_date, status, location, component, remarks, tested_by, is_test_passed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        return dbController.executeUpdate(sql, 
            bloodUnit.getUnitId(), 
            bloodUnit.getDonorId(), 
            bloodUnit.getBloodGroup(), 
            bloodUnit.getCollectionDate(), 
            bloodUnit.getExpiryDate(), 
            bloodUnit.getStatus(), 
            bloodUnit.getLocation(), 
            bloodUnit.getComponent(), 
            bloodUnit.getRemarks(), 
            bloodUnit.getTestedBy(), 
            bloodUnit.isTestPassed()
        );
    }

    /**
     * Update an existing blood unit
     * @param bloodUnit BloodUnit object to update
     * @return true if successful, false otherwise
     */
    public boolean updateBloodUnit(BloodUnit bloodUnit) {
        String sql = "UPDATE blood_inventory SET donor_id = ?, blood_group = ?, collection_date = ?, " +
                     "expiry_date = ?, status = ?, location = ?, component = ?, remarks = ?, " +
                     "tested_by = ?, is_test_passed = ? WHERE unit_id = ?";
        
        return dbController.executeUpdate(sql, 
            bloodUnit.getDonorId(), 
            bloodUnit.getBloodGroup(), 
            bloodUnit.getCollectionDate(), 
            bloodUnit.getExpiryDate(), 
            bloodUnit.getStatus(), 
            bloodUnit.getLocation(), 
            bloodUnit.getComponent(), 
            bloodUnit.getRemarks(), 
            bloodUnit.getTestedBy(), 
            bloodUnit.isTestPassed(), 
            bloodUnit.getUnitId()
        );
    }

    /**
     * Delete a blood unit
     * @param unitId ID of the blood unit to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteBloodUnit(String unitId) {
        String sql = "DELETE FROM blood_inventory WHERE unit_id = ?";
        return dbController.executeUpdate(sql, unitId);
    }

    /**
     * Update the status of a blood unit
     * @param unitId ID of the blood unit
     * @param status new status
     * @return true if successful, false otherwise
     */
    public boolean updateBloodUnitStatus(String unitId, String status) {
        String sql = "UPDATE blood_inventory SET status = ? WHERE unit_id = ?";
        return dbController.executeUpdate(sql, status, unitId);
    }

    /**
     * Get blood inventory statistics
     * @return Map containing statistics about blood inventory
     */
    public Map<String, Integer> getBloodInventoryStats() {
        Map<String, Integer> stats = new HashMap<>();
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        // Initialize stats with zeros
        for (String group : bloodGroups) {
            stats.put(group, 0);
        }
        
        // Get counts from database
        String sql = "SELECT blood_group, COUNT(*) as count FROM blood_inventory " +
                     "WHERE status = 'available' AND is_test_passed = TRUE AND " +
                     "expiry_date > CURDATE() GROUP BY blood_group";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    String bloodGroup = rs.getString("blood_group");
                    int count = rs.getInt("count");
                    stats.put(bloodGroup, count);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting blood inventory statistics: " + e.getMessage());
        }
        
        return stats;
    }

    /**
     * Get nearly expired blood units (expiring within 7 days)
     * @return List of blood units expiring soon
     */
    public List<BloodUnit> getNearlyExpiredUnits() {
        List<BloodUnit> unitList = new ArrayList<>();
        String sql = "SELECT * FROM blood_inventory WHERE status = 'available' AND " +
                     "expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) " +
                     "ORDER BY expiry_date ASC";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    unitList.add(extractBloodUnitFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting nearly expired blood units: " + e.getMessage());
        }
        
        return unitList;
    }

    /**
     * Extract a BloodUnit object from a ResultSet
     * @param rs ResultSet containing blood unit data
     * @return BloodUnit object
     * @throws SQLException if a database error occurs
     */
    private BloodUnit extractBloodUnitFromResultSet(ResultSet rs) throws SQLException {
        BloodUnit bloodUnit = new BloodUnit();
        bloodUnit.setUnitId(rs.getString("unit_id"));
        bloodUnit.setDonorId(rs.getInt("donor_id"));
        bloodUnit.setBloodGroup(rs.getString("blood_group"));
        bloodUnit.setCollectionDate(rs.getDate("collection_date"));
        bloodUnit.setExpiryDate(rs.getDate("expiry_date"));
        bloodUnit.setStatus(rs.getString("status"));
        bloodUnit.setLocation(rs.getString("location"));
        bloodUnit.setComponent(rs.getString("component"));
        bloodUnit.setRemarks(rs.getString("remarks"));
        bloodUnit.setTestedBy(rs.getString("tested_by"));
        bloodUnit.setTestPassed(rs.getBoolean("is_test_passed"));
        return bloodUnit;
    }
}
