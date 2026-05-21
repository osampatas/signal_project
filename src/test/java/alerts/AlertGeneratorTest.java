package alerts;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import java.util.List;
import org.junit.jupiter.api.Test;

class AlertGeneratorTest {

    @Test
    void detectsCriticalHighAndLowBloodPressure() {
        Patient patient = new Patient(1);
        patient.addRecord(181.0, "SystolicPressure", 1000L);
        patient.addRecord(59.0, "DiastolicPressure", 2000L);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "Critical systolic blood pressure");
        assertHasCondition(generator, "Critical diastolic blood pressure");
    }

    @Test
    void detectsIncreasingBloodPressureTrend() {
        Patient patient = new Patient(2);
        patient.addRecord(100.0, "SystolicPressure", 1000L);
        patient.addRecord(112.0, "SystolicPressure", 2000L);
        patient.addRecord(125.0, "SystolicPressure", 3000L);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "SystolicPressure increasing trend");
    }

    @Test
    void detectsDecreasingBloodPressureTrend() {
        Patient patient = new Patient(3);
        patient.addRecord(130.0, "DiastolicPressure", 1000L);
        patient.addRecord(118.0, "DiastolicPressure", 2000L);
        patient.addRecord(105.0, "DiastolicPressure", 3000L);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "DiastolicPressure decreasing trend");
    }

    @Test
    void detectsLowOxygenSaturation() {
        Patient patient = new Patient(4);
        patient.addRecord(91.0, "Saturation", 1000L);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "Low oxygen saturation");
    }

    @Test
    void detectsRapidOxygenDropWithinTenMinutes() {
        Patient patient = new Patient(5);
        patient.addRecord(98.0, "Saturation", 1000L);
        patient.addRecord(93.0, "Saturation", 1000L + 5 * 60 * 1000);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "Rapid oxygen saturation drop");
    }

    @Test
    void detectsCombinedHypotensiveHypoxemia() {
        Patient patient = new Patient(6);
        patient.addRecord(85.0, "SystolicPressure", 1000L);
        patient.addRecord(90.0, "Saturation", 1500L);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "Combined hypotensive hypoxemia");
    }

    @Test
    void detectsAbnormalEcgPeak() {
        Patient patient = new Patient(7);
        patient.addRecord(0.1, "ECG", 1000L);
        patient.addRecord(0.2, "ECG", 2000L);
        patient.addRecord(0.1, "ECG", 3000L);
        patient.addRecord(2.0, "ECG", 4000L);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "Abnormal ECG peak");
    }

    @Test
    void detectsManualTriggeredAlertData() {
        Patient patient = new Patient(8);
        patient.addRecord(1.0, "Alert", 1000L);

        AlertGenerator generator = evaluate(patient);

        assertHasCondition(generator, "Manual alert triggered");
    }

    private AlertGenerator evaluate(Patient patient) {
        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);
        return generator;
    }

    private void assertHasCondition(AlertGenerator generator, String condition) {
        List<Alert> alerts = generator.getTriggeredAlerts();
        boolean found = alerts.stream()
                .anyMatch(alert -> condition.equals(alert.getCondition()));
        assertTrue(found, "Expected alert condition: " + condition);
    }
}
