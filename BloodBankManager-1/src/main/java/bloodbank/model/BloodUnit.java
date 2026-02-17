package bloodbank.model;

import java.util.Date;

/**
 * Represents a blood unit in the blood bank inventory.
 */
public class BloodUnit {
    private String unitId;
    private int donorId;
    private String bloodGroup;
    private Date collectionDate;
    private Date expiryDate;
    private String status; // e.g., "available", "reserved", "discarded", "issued"
    private String location; // storage location
    private String component; // e.g., "whole blood", "plasma", "platelets"
    private String remarks;
    private String testedBy; // staff who tested the blood
    private boolean isTestPassed;

    // Default constructor
    public BloodUnit() {
    }

    // Parameterized constructor
    public BloodUnit(String unitId, int donorId, String bloodGroup, Date collectionDate,
                    Date expiryDate, String status, String location, String component,
                    String remarks, String testedBy, boolean isTestPassed) {
        this.unitId = unitId;
        this.donorId = donorId;
        this.bloodGroup = bloodGroup;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.location = location;
        this.component = component;
        this.remarks = remarks;
        this.testedBy = testedBy;
        this.isTestPassed = isTestPassed;
    }

    // Getters and Setters
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Date getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(Date collectionDate) {
        this.collectionDate = collectionDate;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTestedBy() {
        return testedBy;
    }

    public void setTestedBy(String testedBy) {
        this.testedBy = testedBy;
    }

    public boolean isTestPassed() {
        return isTestPassed;
    }

    public void setTestPassed(boolean testPassed) {
        isTestPassed = testPassed;
    }

    @Override
    public String toString() {
        return "BloodUnit{" +
                "unitId='" + unitId + '\'' +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", collectionDate=" + collectionDate +
                ", expiryDate=" + expiryDate +
                ", status='" + status + '\'' +
                ", component='" + component + '\'' +
                '}';
    }
}
