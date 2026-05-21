package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy Pattern: checks oxygen saturation rules.
 */
public class OxygenSaturationStrategy implements AlertStrategy {
    private final AlertFactory alertFactory;

    /**
     * Creates an oxygen strategy with its matching alert factory.
     */
    public OxygenSaturationStrategy() {
        this.alertFactory = new BloodOxygenAlertFactory();
    }

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        List<PatientRecord> saturationRecords = filterByType(records, "Saturation");

        for (PatientRecord record : saturationRecords) {
            if (record.getMeasurementValue() < 92) {
                alerts.add(create(patient, "Low oxygen saturation", record.getTimestamp()));
            }
        }

        for (int i = 1; i < saturationRecords.size(); i++) {
            PatientRecord previous = saturationRecords.get(i - 1);
            PatientRecord current = saturationRecords.get(i);
            long timeDifference = current.getTimestamp() - previous.getTimestamp();
            double saturationDrop = previous.getMeasurementValue() - current.getMeasurementValue();

            if (timeDifference <= 10 * 60 * 1000 && saturationDrop >= 5) {
                alerts.add(create(patient, "Rapid oxygen saturation drop", current.getTimestamp()));
            }
        }

        return alerts;
    }

    private Alert create(Patient patient, String condition, long timestamp) {
        return alertFactory.createAlert(String.valueOf(patient.getPatientId()), condition, timestamp);
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
}
