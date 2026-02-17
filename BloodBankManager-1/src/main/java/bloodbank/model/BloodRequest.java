package bloodbank.model;

import java.util.Date;

/**
 * Represents a blood request in the blood bank management system.
 */
public class BloodRequest {
    private int requestId;
    private String patientName;
    private String bloodGroup;
    private String component; // e.g., "whole blood", "plasma", "platelets"
    private int quantity; // number of units
    private Date requestDate;
    private Date requiredDate;
    private String status; // e.g., "pending", "fulfilled", "partially fulfilled", "cancelled"
    private String hospitalName;
    private String doctorName;
    private String contactPerson;
    private String contactPhone;
    private String reason;
    private String priority; // e.g., "normal", "urgent", "emergency"
    private String processedBy; // staff who processed the request
    private String remarks;

    // Default constructor
    public BloodRequest() {
    }

    // Parameterized constructor
    public BloodRequest(int requestId, String patientName, String bloodGroup, String component,
                       int quantity, Date requestDate, Date requiredDate, String status,
                       String hospitalName, String doctorName, String contactPerson,
                       String contactPhone, String reason, String priority, String processedBy,
                       String remarks) {
        this.requestId = requestId;
        this.patientName = patientName;
        this.bloodGroup = bloodGroup;
        this.component = component;
        this.quantity = quantity;
        this.requestDate = requestDate;
        this.requiredDate = requiredDate;
        this.status = status;
        this.hospitalName = hospitalName;
        this.doctorName = doctorName;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
        this.reason = reason;
        this.priority = priority;
        this.processedBy = processedBy;
        this.remarks = remarks;
    }

    // Getters and Setters
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(Date requestDate) {
        this.requestDate = requestDate;
    }

    public Date getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(Date requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "BloodRequest{" +
                "requestId=" + requestId +
                ", patientName='" + patientName + '\'' +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}
