package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.ECGAlertFactory;
import com.alerts.strategies.AlertStrategy;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    // Google Java Style: marked the field final because it is assigned only once.
    private final DataStorage dataStorage;
    private final List<Alert> triggeredAlerts;
    private final List<AlertStrategy> alertStrategies;
    private final AlertFactory manualAlertFactory;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        this.triggeredAlerts = new ArrayList<>();
        this.alertStrategies = new ArrayList<>();
        // Strategy Pattern: each strategy owns one family of alert rules.
        this.alertStrategies.add(new BloodPressureStrategy());
        this.alertStrategies.add(new OxygenSaturationStrategy());
        this.alertStrategies.add(new HeartRateStrategy());
        this.manualAlertFactory = new ECGAlertFactory();
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        List<PatientRecord> records = patient.getAllRecords();
        records.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        for (AlertStrategy alertStrategy : alertStrategies) {
            for (Alert alert : alertStrategy.checkAlert(patient, records)) {
                triggerAlert(alert);
            }
        }
        checkTriggeredAlertButton(patient, records);
    }

    /**
     * Returns a copy of the alerts generated so tests and callers can inspect them.
     *
     * @return generated alerts
     */
    public List<Alert> getTriggeredAlerts() {
        return new ArrayList<>(triggeredAlerts);
    }

    private void checkTriggeredAlertButton(Patient patient, List<PatientRecord> records) {
        for (PatientRecord record : filterByType(records, "Alert")) {
            if (record.getMeasurementValue() == 1.0) {
                triggerAlert(manualAlertFactory.createAlert(String.valueOf(patient.getPatientId()),
                        "Manual alert triggered", record.getTimestamp()));
            }
        }
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

    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        triggeredAlerts.add(alert);
    }
}
