package com.alerts.strategies;

import com.alerts.Alert;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.List;

/**
 * Strategy Pattern: each strategy checks one family of alert rules.
 */
public interface AlertStrategy {
    /**
     * Checks patient records and returns any alerts found.
     *
     * @param patient the patient being evaluated
     * @param records patient records sorted by timestamp
     * @return alerts found by this strategy
     */
    List<Alert> checkAlert(Patient patient, List<PatientRecord> records);
}
