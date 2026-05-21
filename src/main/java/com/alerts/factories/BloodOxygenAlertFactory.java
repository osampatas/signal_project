package com.alerts.factories;

import com.alerts.Alert;

/**
 * Factory Method Pattern: creates alerts related to blood oxygen saturation.
 */
public class BloodOxygenAlertFactory implements AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, condition, timestamp);
    }
}
