package com.alerts;

/**
 * Represents an alert created when patient data may need medical attention.
 */
public class Alert {
    private String patientId;
    private String condition;
    private long timestamp;

    /**
     * Creates an alert for a patient and a specific condition.
     *
     * @param patientId the patient identifier
     * @param condition short explanation of the alert condition
     * @param timestamp time when the alert was created
     */
    public Alert(String patientId, String condition, long timestamp) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
    }

    /**
     * Returns the patient identifier for this alert.
     *
     * @return the patient identifier
     */
    public String getPatientId() {
        return patientId;
    }

    /**
     * Returns the alert condition.
     *
     * @return the alert condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Returns the time when the alert was created.
     *
     * @return the alert timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
