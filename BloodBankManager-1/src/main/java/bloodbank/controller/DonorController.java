package bloodbank.controller;

import bloodbank.model.Donor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Controller for handling donor-related operations.
 */
public class DonorController {
    private final DatabaseController dbController;

    public DonorController() {
        dbController = DatabaseController.getInstance();
    }

    /**
     * Get a donor by ID
     * @param donorId donor ID to look up
     * @return Donor object if found, null otherwise
     */
    public Donor getDonorById(int donorId) {
        String sql = "SELECT * FROM donors WHERE donor_id = ?";
        
        try (ResultSet rs = dbController.executeQuery(sql, donorId)) {
            if (rs != null && rs.next()) {
                return extractDonorFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting donor by ID: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get all donors
     * @return List of all donors
     */
    public List<Donor> getAllDonors() {
        List<Donor> donorList = new ArrayList<>();
        String sql = "SELECT * FROM donors ORDER BY name";
        
        try (ResultSet rs = dbController.executeQuery(sql)) {
            if (rs != null) {
                while (rs.next()) {
                    donorList.add(extractDonorFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting all donors: " + e.getMessage());
        }
        
        return donorList;
    }

    /**
     * Search donors by various criteria
     * @param searchTerm term to search for
     * @param searchBy field to search in (name, blood_group, phone, etc.)
     * @return List of matching donors
     */
    public List<Donor> searchDonors(String searchTerm, String searchBy) {
        List<Donor> donorList = new ArrayList<>();
        String sql;
        
        switch (searchBy.toLowerCase()) {
            case "name":
                sql = "SELECT * FROM donors WHERE name LIKE ? ORDER BY name";
                break;
            case "blood_group":
                sql = "SELECT * FROM donors WHERE blood_group = ? ORDER BY name";
                break;
            case "phone":
                sql = "SELECT * FROM donors WHERE phone LIKE ? ORDER BY name";
                break;
            case "email":
                sql = "SELECT * FROM donors WHERE email LIKE ? ORDER BY name";
                break;
            default:
                sql = "SELECT * FROM donors WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? OR blood_group LIKE ? ORDER BY name";
                try (ResultSet rs = dbController.executeQuery(
                        sql, 
                        "%" + searchTerm + "%", 
                        "%" + searchTerm + "%", 
                        "%" + searchTerm + "%", 
                        "%" + searchTerm + "%")) {
                    if (rs != null) {
                        while (rs.next()) {
                            donorList.add(extractDonorFromResultSet(rs));
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error searching donors: " + e.getMessage());
                }
                return donorList;
        }
        
        try (ResultSet rs = dbController.executeQuery(sql, "%" + searchTerm + "%")) {
            if (rs != null) {
                while (rs.next()) {
                    donorList.add(extractDonorFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching donors: " + e.getMessage());
        }
        
        return donorList;
    }

    /**
     * Add a new donor
     * @param donor Donor object to add
     * @return true if successful, false otherwise
     */
    public boolean addDonor(Donor donor) {
        String sql = "INSERT INTO donors (name, blood_group, date_of_birth, gender, phone, email, " +
                     "address, last_donation_date, is_eligible, medical_history, registration_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        return dbController.executeUpdate(sql, 
            donor.getName(), 
            donor.getBloodGroup(), 
            donor.getDateOfBirth(), 
            donor.getGender(), 
            donor.getPhone(), 
            donor.getEmail(), 
            donor.getAddress(), 
            donor.getLastDonationDate(), 
            donor.isEligible(), 
            donor.getMedicalHistory(), 
            donor.getRegistrationDate() != null ? donor.getRegistrationDate() : new Date()
        );
    }

    /**
     * Update an existing donor
     * @param donor Donor object to update
     * @return true if successful, false otherwise
     */
    public boolean updateDonor(Donor donor) {
        String sql = "UPDATE donors SET name = ?, blood_group = ?, date_of_birth = ?, gender = ?, " +
                     "phone = ?, email = ?, address = ?, last_donation_date = ?, is_eligible = ?, " +
                     "medical_history = ? WHERE donor_id = ?";
        
        return dbController.executeUpdate(sql, 
            donor.getName(), 
            donor.getBloodGroup(), 
            donor.getDateOfBirth(), 
            donor.getGender(), 
            donor.getPhone(), 
            donor.getEmail(), 
            donor.getAddress(), 
            donor.getLastDonationDate(), 
            donor.isEligible(), 
            donor.getMedicalHistory(), 
            donor.getDonorId()
        );
    }

    /**
     * Delete a donor
     * @param donorId ID of the donor to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteDonor(int donorId) {
        String sql = "DELETE FROM donors WHERE donor_id = ?";
        return dbController.executeUpdate(sql, donorId);
    }

    /**
     * Update a donor's last donation date
     * @param donorId ID of the donor
     * @param lastDonationDate new last donation date
     * @return true if successful, false otherwise
     */
    public boolean updateLastDonationDate(int donorId, Date lastDonationDate) {
        String sql = "UPDATE donors SET last_donation_date = ? WHERE donor_id = ?";
        return dbController.executeUpdate(sql, lastDonationDate, donorId);
    }

    /**
     * Check if a donor is eligible to donate
     * @param donorId ID of the donor
     * @return true if eligible, false otherwise
     */
    public boolean isEligibleToDonate(int donorId) {
        Donor donor = getDonorById(donorId);
        if (donor != null) {
            // Check eligibility status
            if (!donor.isEligible()) {
                return false;
            }
            
            // Check if enough time has passed since last donation (usually 3 months)
            if (donor.getLastDonationDate() != null) {
                Date lastDonation = donor.getLastDonationDate();
                Date now = new Date();
                
                // 3 months in milliseconds (approximately)
                long threeMonths = 90L * 24L * 60L * 60L * 1000L;
                
                return (now.getTime() - lastDonation.getTime()) >= threeMonths;
            }
            
            // If donor has never donated, they are eligible
            return true;
        }
        return false;
    }

    /**
     * Extract a Donor object from a ResultSet
     * @param rs ResultSet containing donor data
     * @return Donor object
     * @throws SQLException if a database error occurs
     */
    private Donor extractDonorFromResultSet(ResultSet rs) throws SQLException {
        Donor donor = new Donor();
        donor.setDonorId(rs.getInt("donor_id"));
        donor.setName(rs.getString("name"));
        donor.setBloodGroup(rs.getString("blood_group"));
        donor.setDateOfBirth(rs.getDate("date_of_birth"));
        donor.setGender(rs.getString("gender"));
        donor.setPhone(rs.getString("phone"));
        donor.setEmail(rs.getString("email"));
        donor.setAddress(rs.getString("address"));
        donor.setLastDonationDate(rs.getDate("last_donation_date"));
        donor.setEligible(rs.getBoolean("is_eligible"));
        donor.setMedicalHistory(rs.getString("medical_history"));
        donor.setRegistrationDate(rs.getDate("registration_date"));
        return donor;
    }
}
