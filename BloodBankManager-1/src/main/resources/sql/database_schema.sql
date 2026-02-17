-- Blood Bank Management System Database Schema

-- Drop existing tables if they exist
DROP TABLE IF EXISTS blood_requests;
DROP TABLE IF EXISTS blood_inventory;
DROP TABLE IF EXISTS donors;
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create donors table
CREATE TABLE donors (
    donor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(10),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT,
    last_donation_date DATE,
    is_eligible BOOLEAN NOT NULL DEFAULT TRUE,
    medical_history TEXT,
    registration_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create blood inventory table
CREATE TABLE blood_inventory (
    unit_id VARCHAR(20) PRIMARY KEY,
    donor_id INT NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    collection_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'available',
    location VARCHAR(50),
    component VARCHAR(20) NOT NULL DEFAULT 'whole blood',
    remarks TEXT,
    tested_by VARCHAR(50),
    is_test_passed BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES donors(donor_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create blood requests table
CREATE TABLE blood_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_name VARCHAR(100) NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    component VARCHAR(20) NOT NULL DEFAULT 'whole blood',
    quantity INT NOT NULL DEFAULT 1,
    request_date DATE NOT NULL,
    required_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    hospital_name VARCHAR(100) NOT NULL,
    doctor_name VARCHAR(100),
    contact_person VARCHAR(100) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    reason TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'normal',
    processed_by VARCHAR(50),
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create request_items table to track which blood units were issued for which requests
CREATE TABLE request_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    request_id INT NOT NULL,
    unit_id VARCHAR(20) NOT NULL,
    issued_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (request_id) REFERENCES blood_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (unit_id) REFERENCES blood_inventory(unit_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create donation_history table to track donation history
CREATE TABLE donation_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    donor_id INT NOT NULL,
    unit_id VARCHAR(20) NOT NULL,
    donation_date DATE NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    component VARCHAR(20) NOT NULL DEFAULT 'whole blood',
    notes TEXT,
    FOREIGN KEY (donor_id) REFERENCES donors(donor_id) ON DELETE CASCADE,
    FOREIGN KEY (unit_id) REFERENCES blood_inventory(unit_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert default admin user
-- Username: admin, Password: admin123
INSERT INTO users (username, password, full_name, email, phone, role, active)
VALUES ('admin', 'admin123', 'System Administrator', 'admin@bloodbank.org', '555-123-4567', 'admin', TRUE);

-- Create indexes for frequently accessed fields
CREATE INDEX idx_donors_blood_group ON donors(blood_group);
CREATE INDEX idx_donors_last_donation_date ON donors(last_donation_date);
CREATE INDEX idx_blood_inventory_blood_group ON blood_inventory(blood_group);
CREATE INDEX idx_blood_inventory_status ON blood_inventory(status);
CREATE INDEX idx_blood_inventory_expiry_date ON blood_inventory(expiry_date);
CREATE INDEX idx_blood_requests_status ON blood_requests(status);
CREATE INDEX idx_blood_requests_blood_group ON blood_requests(blood_group);
CREATE INDEX idx_blood_requests_request_date ON blood_requests(request_date);

-- Create triggers for maintaining data integrity
DELIMITER //

-- Trigger to ensure expiry date is after collection date
CREATE TRIGGER check_expiry_date 
BEFORE INSERT ON blood_inventory
FOR EACH ROW
BEGIN
    IF NEW.expiry_date <= NEW.collection_date THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = 'Expiry date must be after collection date';
    END IF;
END//

-- Trigger to update donor's last donation date
CREATE TRIGGER update_last_donation_date 
AFTER INSERT ON blood_inventory
FOR EACH ROW
BEGIN
    -- Only update if the new donation date is more recent than the current last donation date
    UPDATE donors SET last_donation_date = NEW.collection_date
    WHERE donor_id = NEW.donor_id AND 
          (last_donation_date IS NULL OR last_donation_date < NEW.collection_date);
    
    -- Add entry to donation history
    INSERT INTO donation_history (donor_id, unit_id, donation_date, blood_group, component)
    VALUES (NEW.donor_id, NEW.unit_id, NEW.collection_date, NEW.blood_group, NEW.component);
END//

DELIMITER ;

-- Create event for checking expired blood units
CREATE EVENT check_expired_blood_units
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP
DO
BEGIN
    UPDATE blood_inventory
    SET status = 'discarded'
    WHERE expiry_date < CURDATE() AND status = 'available';
END//

-- Create views for frequently accessed reports
CREATE VIEW available_blood_inventory AS
SELECT blood_group, component, COUNT(*) as available_units
FROM blood_inventory
WHERE status = 'available' AND is_test_passed = TRUE AND expiry_date > CURDATE()
GROUP BY blood_group, component;

CREATE VIEW donor_donation_count AS
SELECT d.donor_id, d.name, d.blood_group, d.phone, COUNT(bi.unit_id) as donation_count
FROM donors d
LEFT JOIN blood_inventory bi ON d.donor_id = bi.donor_id
GROUP BY d.donor_id, d.name, d.blood_group, d.phone;

CREATE VIEW pending_blood_requests AS
SELECT request_id, patient_name, blood_group, component, quantity, request_date, required_date, 
       priority, hospital_name, contact_person, contact_phone
FROM blood_requests
WHERE status = 'pending'
ORDER BY 
    CASE 
        WHEN priority = 'emergency' THEN 1
        WHEN priority = 'urgent' THEN 2
        ELSE 3
    END, 
    request_date;

CREATE VIEW expiring_blood_units AS
SELECT unit_id, blood_group, component, collection_date, expiry_date, location
FROM blood_inventory
WHERE status = 'available' AND is_test_passed = TRUE 
AND expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY)
ORDER BY expiry_date;
