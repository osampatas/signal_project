package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory Method Pattern: this interface defines how alert objects are created.
 * Different factories can create alerts for different medical areas.
 */
public interface AlertFactory {
    /**
     * Creates an alert for a patient.
     *
     * @param patientId the patient identifier
     * @param condition short explanation of the alert condition
     * @param timestamp time when the alert was created
     * @return a new alert
     */
    Alert createAlert(String patientId, String condition, long timestamp);
}
