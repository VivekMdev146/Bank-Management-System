package bloodbank.controller;

import bloodbank.model.BloodRequest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller for handling blood request operations.
 */
public class RequestController {
    private final DatabaseController dbController;
    private final InventoryController inventoryController;

    public RequestController() {
        dbController = DatabaseController.getInstance();
        inventoryController = new InventoryController();
    }

    /**
     * Get a blood request by ID
     * @param requestId request ID to look up
     * @return BloodRequest object if found, null otherwise
     */
    public BloodRequest getRequestById(int requestId) {
        String sql = "SELECT * FROM blood_requests WHERE request_id = ?";
        
        try (ResultSet rs = dbController.executeQuery(sql, requestId)) {
            if (rs != null && rs.next()) {
                return extractRequestFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting blood request by ID: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all blood requests
     * @return List of all blood requests
     */
    public List<BloodRequest> getAllRequests() {
        List<BloodRequest> requestList = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests ORDER BY request_date DESC";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    requestList.add(extractRequestFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all blood requests: " + e.getMessage());
        }
        
        return requestList;
    }

    /**
     * Get blood requests by status
     * @param status status to filter by
     * @return List of blood requests with the specified status
     */
    public List<BloodRequest> getRequestsByStatus(String status) {
        List<BloodRequest> requestList = new ArrayList<>();
        String sql = "SELECT * FROM blood_requests WHERE status = ? ORDER BY request_date DESC";
        
        try (ResultSet rs = dbController.executeQuery(sql, status)) {
            if (rs != null) {
                while (rs.next()) {
                    requestList.add(extractRequestFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting blood requests by status: " + e.getMessage());
        }
        
        return requestList;
    }

    /**
     * Add a new blood request
     * @param request BloodRequest object to add
     * @return true if successful, false otherwise
     */
    public boolean addRequest(BloodRequest request) {
        String sql = "INSERT INTO blood_requests (patient_name, blood_group, component, quantity, " +
                     "request_date, required_date, status, hospital_name, doctor_name, " +
                     "contact_person, contact_phone, reason, priority, processed_by, remarks) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        return dbController.executeUpdate(sql, 
            request.getPatientName(), 
            request.getBloodGroup(), 
            request.getComponent(), 
            request.getQuantity(), 
            request.getRequestDate() != null ? request.getRequestDate() : new Date(), 
            request.getRequiredDate(), 
            request.getStatus(), 
            request.getHospitalName(), 
            request.getDoctorName(), 
            request.getContactPerson(), 
            request.getContactPhone(), 
            request.getReason(), 
            request.getPriority(), 
            request.getProcessedBy(), 
            request.getRemarks()
        );
    }

    /**
     * Update an existing blood request
     * @param request BloodRequest object to update
     * @return true if successful, false otherwise
     */
    public boolean updateRequest(BloodRequest request) {
        String sql = "UPDATE blood_requests SET patient_name = ?, blood_group = ?, component = ?, " +
                     "quantity = ?, required_date = ?, status = ?, hospital_name = ?, " +
                     "doctor_name = ?, contact_person = ?, contact_phone = ?, reason = ?, " +
                     "priority = ?, processed_by = ?, remarks = ? WHERE request_id = ?";
        
        return dbController.executeUpdate(sql, 
            request.getPatientName(), 
            request.getBloodGroup(), 
            request.getComponent(), 
            request.getQuantity(), 
            request.getRequiredDate(), 
            request.getStatus(), 
            request.getHospitalName(), 
            request.getDoctorName(), 
            request.getContactPerson(), 
            request.getContactPhone(), 
            request.getReason(), 
            request.getPriority(), 
            request.getProcessedBy(), 
            request.getRemarks(), 
            request.getRequestId()
        );
    }

    /**
     * Delete a blood request
     * @param requestId ID of the blood request to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteRequest(int requestId) {
        String sql = "DELETE FROM blood_requests WHERE request_id = ?";
        return dbController.executeUpdate(sql, requestId);
    }

    /**
     * Update the status of a blood request
     * @param requestId ID of the blood request
     * @param status new status
     * @param processedBy username of the staff who processed the request
     * @return true if successful, false otherwise
     */
    public boolean updateRequestStatus(int requestId, String status, String processedBy) {
        String sql = "UPDATE blood_requests SET status = ?, processed_by = ? WHERE request_id = ?";
        return dbController.executeUpdate(sql, status, processedBy, requestId);
    }

    /**
     * Fulfill a blood request (update request status and blood unit statuses)
     * @param requestId ID of the request to fulfill
     * @param unitIds IDs of the blood units to issue
     * @param processedBy username of the staff who processed the request
     * @return true if successful, false otherwise
     */
    public boolean fulfillRequest(int requestId, List<String> unitIds, String processedBy) {
        // Get the request
        BloodRequest request = getRequestById(requestId);
        if (request == null) {
            return false;
        }
        
        // Begin transaction
        boolean success = true;
        
        // Update request status
        String status;
        if (unitIds.size() >= request.getQuantity()) {
            status = "fulfilled";
        } else if (unitIds.size() > 0) {
            status = "partially fulfilled";
        } else {
            status = "pending";
        }
        
        success = updateRequestStatus(requestId, status, processedBy);
        
        // Update blood unit statuses
        if (success) {
            for (String unitId : unitIds) {
                success = inventoryController.updateBloodUnitStatus(unitId, "issued");
                if (!success) {
                    break;
                }
            }
        }
        
        return success;
    }

    /**
     * Search blood requests by various criteria
     * @param searchTerm term to search for
     * @param searchBy field to search in (patient_name, hospital_name, blood_group, etc.)
     * @return List of matching blood requests
     */
    public List<BloodRequest> searchRequests(String searchTerm, String searchBy) {
        List<BloodRequest> requestList = new ArrayList<>();
        String sql;
        
        switch (searchBy.toLowerCase()) {
            case "patient_name":
                sql = "SELECT * FROM blood_requests WHERE patient_name LIKE ? ORDER BY request_date DESC";
                break;
            case "hospital_name":
                sql = "SELECT * FROM blood_requests WHERE hospital_name LIKE ? ORDER BY request_date DESC";
                break;
            case "blood_group":
                sql = "SELECT * FROM blood_requests WHERE blood_group = ? ORDER BY request_date DESC";
                break;
            case "priority":
                sql = "SELECT * FROM blood_requests WHERE priority = ? ORDER BY request_date DESC";
                break;
            default:
                sql = "SELECT * FROM blood_requests WHERE patient_name LIKE ? OR hospital_name LIKE ? " +
                      "OR blood_group LIKE ? OR priority LIKE ? ORDER BY request_date DESC";
                try (ResultSet rs = dbController.executeQuery(
                        sql, 
                        "%" + searchTerm + "%", 
                        "%" + searchTerm + "%", 
                        "%" + searchTerm + "%", 
                        "%" + searchTerm + "%")) {
                    if (rs != null) {
                        while (rs.next()) {
                            requestList.add(extractRequestFromResultSet(rs));
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error searching blood requests: " + e.getMessage());
                }
                return requestList;
        }
        
        try (ResultSet rs = dbController.executeQuery(sql, "%" + searchTerm + "%")) {
            if (rs != null) {
                while (rs.next()) {
                    requestList.add(extractRequestFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching blood requests: " + e.getMessage());
        }
        
        return requestList;
    }

    /**
     * Extract a BloodRequest object from a ResultSet
     * @param rs ResultSet containing blood request data
     * @return BloodRequest object
     * @throws SQLException if a database error occurs
     */
    private BloodRequest extractRequestFromResultSet(ResultSet rs) throws SQLException {
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
        request.setDoctorName(rs.getString("doctor_name"));
        request.setContactPerson(rs.getString("contact_person"));
        request.setContactPhone(rs.getString("contact_phone"));
        request.setReason(rs.getString("reason"));
        request.setPriority(rs.getString("priority"));
        request.setProcessedBy(rs.getString("processed_by"));
        request.setRemarks(rs.getString("remarks"));
        return request;
    }
}
