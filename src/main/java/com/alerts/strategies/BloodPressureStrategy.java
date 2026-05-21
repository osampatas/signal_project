package com.alerts.strategies;

import com.alerts.Alert;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy Pattern: checks blood pressure critical values, trends, and the
 * combined hypotensive hypoxemia rule.
 */
public class BloodPressureStrategy implements AlertStrategy {
    private final AlertFactory alertFactory;

    /**
     * Creates a blood pressure strategy with its matching alert factory.
     */
    public BloodPressureStrategy() {
        this.alertFactory = new BloodPressureAlertFactory();
    }

    @Override
    public List<Alert> checkAlert(Patient patient, List<PatientRecord> records) {
        List<Alert> alerts = new ArrayList<>();
        checkCriticalValues(patient, records, alerts);
        checkTrend(patient, records, alerts, "SystolicPressure");
        checkTrend(patient, records, alerts, "DiastolicPressure");
        checkCombinedHypotensiveHypoxemia(patient, records, alerts);
        return alerts;
    }

    private void checkCriticalValues(Patient patient, List<PatientRecord> records, List<Alert> alerts) {
        for (PatientRecord record : records) {
            if ("SystolicPressure".equals(record.getRecordType())
                    && (record.getMeasurementValue() > 180 || record.getMeasurementValue() < 90)) {
                alerts.add(create(patient, "Critical systolic blood pressure", record.getTimestamp()));
            }

            if ("DiastolicPressure".equals(record.getRecordType())
                    && (record.getMeasurementValue() > 120 || record.getMeasurementValue() < 60)) {
                alerts.add(create(patient, "Critical diastolic blood pressure", record.getTimestamp()));
            }
        }
    }

    private void checkTrend(Patient patient, List<PatientRecord> records, List<Alert> alerts, String recordType) {
        List<PatientRecord> pressureRecords = filterByType(records, recordType);
        for (int i = 2; i < pressureRecords.size(); i++) {
            double firstChange = pressureRecords.get(i - 1).getMeasurementValue()
                    - pressureRecords.get(i - 2).getMeasurementValue();
            double secondChange = pressureRecords.get(i).getMeasurementValue()
                    - pressureRecords.get(i - 1).getMeasurementValue();

            if (firstChange > 10 && secondChange > 10) {
                alerts.add(create(patient, recordType + " increasing trend", pressureRecords.get(i).getTimestamp()));
            }
            if (firstChange < -10 && secondChange < -10) {
                alerts.add(create(patient, recordType + " decreasing trend", pressureRecords.get(i).getTimestamp()));
            }
        }
    }

    private void checkCombinedHypotensiveHypoxemia(Patient patient, List<PatientRecord> records, List<Alert> alerts) {
        for (PatientRecord systolic : filterByType(records, "SystolicPressure")) {
            for (PatientRecord saturation : filterByType(records, "Saturation")) {
                // Assumption: readings within one minute can describe the same event.
                boolean closeInTime = Math.abs(systolic.getTimestamp() - saturation.getTimestamp()) <= 60 * 1000;
                if (closeInTime && systolic.getMeasurementValue() < 90 && saturation.getMeasurementValue() < 92) {
                    alerts.add(create(patient, "Combined hypotensive hypoxemia",
                            Math.max(systolic.getTimestamp(), saturation.getTimestamp())));
                }
            }
        }
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
