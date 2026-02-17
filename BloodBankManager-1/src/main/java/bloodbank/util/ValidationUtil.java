package bloodbank.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for data validation.
 */
public class ValidationUtil {
    
    /**
     * Regular expression for validating email addresses.
     */
    private static final String EMAIL_REGEX = 
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    /**
     * Regular expression for validating phone numbers.
     * Allows formats like: 1234567890, 123-456-7890, 123.456.7890, 123 456 7890, (123) 456 7890
     */
    private static final String PHONE_REGEX = 
            "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";
    
    /**
     * Regular expression for validating blood unit IDs.
     * Expected format: BU-YYYYMMDD-XXXX where XXXX is a sequence number
     */
    private static final String BLOOD_UNIT_ID_REGEX = 
            "^BU-\\d{8}-\\d{4}$";
    
    /**
     * Check if the provided string is a valid email address.
     * @param email email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    
    /**
     * Check if the provided string is a valid phone number.
     * @param phone phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        
        Pattern pattern = Pattern.compile(PHONE_REGEX);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }
    
    /**
     * Check if the provided string is a valid blood unit ID.
     * @param unitId blood unit ID to validate
     * @return true if the blood unit ID is valid, false otherwise
     */
    public static boolean isValidBloodUnitId(String unitId) {
        if (unitId == null || unitId.isEmpty()) {
            return false;
        }
        
        Pattern pattern = Pattern.compile(BLOOD_UNIT_ID_REGEX);
        Matcher matcher = pattern.matcher(unitId);
        return matcher.matches();
    }
    
    /**
     * Check if the provided string contains only alphabetic characters.
     * @param text text to validate
     * @return true if the text contains only alphabetic characters, false otherwise
     */
    public static boolean isAlphabetic(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        return text.matches("^[a-zA-Z ]+$");
    }
    
    /**
     * Check if the provided string contains only numeric characters.
     * @param text text to validate
     * @return true if the text contains only numeric characters, false otherwise
     */
    public static boolean isNumeric(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        return text.matches("^[0-9]+$");
    }
    
    /**
     * Check if the provided string is a valid blood group.
     * @param bloodGroup blood group to validate
     * @return true if the blood group is valid, false otherwise
     */
    public static boolean isValidBloodGroup(String bloodGroup) {
        if (bloodGroup == null || bloodGroup.isEmpty()) {
            return false;
        }
        
        String[] validBloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        
        for (String group : validBloodGroups) {
            if (group.equals(bloodGroup)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the provided string is a valid gender.
     * @param gender gender to validate
     * @return true if the gender is valid, false otherwise
     */
    public static boolean isValidGender(String gender) {
        if (gender == null || gender.isEmpty()) {
            return false;
        }
        
        String[] validGenders = {"Male", "Female", "Other"};
        
        for (String g : validGenders) {
            if (g.equals(gender)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the provided string is a valid blood component.
     * @param component blood component to validate
     * @return true if the blood component is valid, false otherwise
     */
    public static boolean isValidBloodComponent(String component) {
        if (component == null || component.isEmpty()) {
            return false;
        }
        
        String[] validComponents = {"whole blood", "plasma", "platelets", "red cells"};
        
        for (String c : validComponents) {
            if (c.equals(component)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the provided string is a valid blood unit status.
     * @param status blood unit status to validate
     * @return true if the blood unit status is valid, false otherwise
     */
    public static boolean isValidBloodUnitStatus(String status) {
        if (status == null || status.isEmpty()) {
            return false;
        }
        
        String[] validStatuses = {"available", "reserved", "issued", "discarded"};
        
        for (String s : validStatuses) {
            if (s.equals(status)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the provided string is a valid blood request status.
     * @param status blood request status to validate
     * @return true if the blood request status is valid, false otherwise
     */
    public static boolean isValidRequestStatus(String status) {
        if (status == null || status.isEmpty()) {
            return false;
        }
        
        String[] validStatuses = {"pending", "fulfilled", "partially fulfilled", "cancelled"};
        
        for (String s : validStatuses) {
            if (s.equals(status)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the provided string is a valid blood request priority.
     * @param priority blood request priority to validate
     * @return true if the blood request priority is valid, false otherwise
     */
    public static boolean isValidRequestPriority(String priority) {
        if (priority == null || priority.isEmpty()) {
            return false;
        }
        
        String[] validPriorities = {"normal", "urgent", "emergency"};
        
        for (String p : validPriorities) {
            if (p.equals(priority)) {
                return true;
            }
        }
        
        return false;
    }
}
