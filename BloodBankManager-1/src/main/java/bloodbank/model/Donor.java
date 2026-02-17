package bloodbank.model;

import java.util.Date;

/**
 * Represents a blood donor in the blood bank management system.
 */
public class Donor {
    private int donorId;
    private String name;
    private String bloodGroup;
    private Date dateOfBirth;
    private String gender;
    private String phone;
    private String email;
    private String address;
    private Date lastDonationDate;
    private boolean isEligible;
    private String medicalHistory;
    private Date registrationDate;

    // Default constructor
    public Donor() {
    }

    // Parameterized constructor
    public Donor(int donorId, String name, String bloodGroup, Date dateOfBirth, 
                 String gender, String phone, String email, String address, 
                 Date lastDonationDate, boolean isEligible, String medicalHistory, 
                 Date registrationDate) {
        this.donorId = donorId;
        this.name = name;
        this.bloodGroup = bloodGroup;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.lastDonationDate = lastDonationDate;
        this.isEligible = isEligible;
        this.medicalHistory = medicalHistory;
        this.registrationDate = registrationDate;
    }

    // Getters and Setters
    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(Date lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public boolean isEligible() {
        return isEligible;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    @Override
    public String toString() {
        return "Donor{" +
                "donorId=" + donorId +
                ", name='" + name + '\'' +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", gender='" + gender + '\'' +
                ", phone='" + phone + '\'' +
                ", isEligible=" + isEligible +
                '}';
    }
}
