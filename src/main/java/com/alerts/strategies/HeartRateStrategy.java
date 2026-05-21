package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.ECGAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy Pattern: checks ECG data using a simple moving average.
 */
public class HeartRateStrategy implements AlertStrategy {
    private final AlertFactory alertFactory;

    /**
     * Creates an ECG strategy with its matching alert factory.
     */
    public HeartRateStrategy() {
        this.alertFactory = new ECGAlertFactory();
    }

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        List<Double> window = new ArrayList<>();

        for (PatientRecord record : filterByType(records, "ECG")) {
            if (window.size() >= 3) {
                double average = average(window);
                // Simple rule: a value far above recent normal ECG data is abnormal.
                if (record.getMeasurementValue() > average + 1.0) {
                    alerts.add(alertFactory.createAlert(String.valueOf(patient.getPatientId()),
                            "Abnormal ECG peak", record.getTimestamp()));
                }
                window.remove(0);
            }
            window.add(record.getMeasurementValue());
        }

        return alerts;
    }

    private List<PatientRecord> filterByType(List<PatientRecord> records, String recordType) {
        List<PatientRecord> matchingRecords = new ArrayList<>();
        for (PatientRecord record : records) {
            if (recordType.equals(record.getRecordType())) {
                matchingRecords.add(record);
            }
        }
        return matchingRecords;
    }

    private double average(List<Double> values) {
        double total = 0;
        for (double value : values) {
            total += value;
        }
        return total / values.size();
    }
}
