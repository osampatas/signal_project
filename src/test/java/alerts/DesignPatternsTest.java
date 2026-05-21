package alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.alerts.Alert;
import com.alerts.decorators.PriorityAlertDecorator;
import com.alerts.decorators.RepeatedAlertDecorator;
import com.alerts.factories.AlertFactory;
import com.alerts.factories.BloodOxygenAlertFactory;
import com.alerts.factories.BloodPressureAlertFactory;
import com.alerts.factories.ECGAlertFactory;
import com.alerts.strategies.AlertStrategy;
import com.alerts.strategies.BloodPressureStrategy;
import com.alerts.strategies.HeartRateStrategy;
import com.alerts.strategies.OxygenSaturationStrategy;
import com.cardio_generator.HealthDataSimulator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import java.util.List;
import org.junit.jupiter.api.Test;

class DesignPatternsTest {

    @Test
    void factoriesCreateAlertsWithCorrectData() {
        AlertFactory bloodPressureFactory = new BloodPressureAlertFactory();
        AlertFactory oxygenFactory = new BloodOxygenAlertFactory();
        AlertFactory ecgFactory = new ECGAlertFactory();

        Alert bloodPressureAlert = bloodPressureFactory.createAlert("1", "Critical systolic blood pressure", 1000L);
        Alert oxygenAlert = oxygenFactory.createAlert("2", "Low oxygen saturation", 2000L);
        Alert ecgAlert = ecgFactory.createAlert("3", "Abnormal ECG peak", 3000L);

        assertEquals("1", bloodPressureAlert.getPatientId());
        assertEquals("Critical systolic blood pressure", bloodPressureAlert.getCondition());
        assertEquals("Low oxygen saturation", oxygenAlert.getCondition());
        assertEquals("Abnormal ECG peak", ecgAlert.getCondition());
    }

    @Test
    void bloodPressureStrategyDetectsCriticalPressure() {
        Patient patient = new Patient(1);
        patient.addRecord(185.0, "SystolicPressure", 1000L);

        AlertStrategy strategy = new BloodPressureStrategy();
        List<Alert> alerts = strategy.checkAlert(patient, patient.getAllRecords());

        assertTrue(containsCondition(alerts, "Critical systolic blood pressure"));
    }

    @Test
    void oxygenStrategyDetectsLowSaturation() {
        Patient patient = new Patient(2);
        patient.addRecord(91.0, "Saturation", 1000L);

        AlertStrategy strategy = new OxygenSaturationStrategy();
        List<Alert> alerts = strategy.checkAlert(patient, patient.getAllRecords());

        assertTrue(containsCondition(alerts, "Low oxygen saturation"));
    }

    @Test
    void heartRateStrategyDetectsEcgPeak() {
        Patient patient = new Patient(3);
        patient.addRecord(0.1, "ECG", 1000L);
        patient.addRecord(0.2, "ECG", 2000L);
        patient.addRecord(0.1, "ECG", 3000L);
        patient.addRecord(2.0, "ECG", 4000L);

        AlertStrategy strategy = new HeartRateStrategy();
        List<Alert> alerts = strategy.checkAlert(patient, patient.getAllRecords());

        assertTrue(containsCondition(alerts, "Abnormal ECG peak"));
    }

    @Test
    void decoratorsAddPriorityAndRepeatedMetadata() {
        Alert alert = new Alert("4", "Low oxygen saturation", 1000L);

        PriorityAlertDecorator priorityAlert = new PriorityAlertDecorator(alert, "HIGH");
        RepeatedAlertDecorator repeatedAlert = new RepeatedAlertDecorator(alert, 3);

        assertEquals("HIGH", priorityAlert.getPriority());
        assertEquals("HIGH priority: Low oxygen saturation", priorityAlert.getCondition());
        assertEquals(3, repeatedAlert.getRepeatCount());
        assertEquals("Low oxygen saturation (repeated 3 times)", repeatedAlert.getCondition());
    }

    @Test
    void singletonGetInstanceReturnsSameObjects() {
        assertSame(DataStorage.getInstance(), DataStorage.getInstance());
        assertSame(HealthDataSimulator.getInstance(), HealthDataSimulator.getInstance());
    }

    private boolean containsCondition(List<Alert> alerts, String condition) {
        return alerts.stream().anyMatch(alert -> condition.equals(alert.getCondition()));
    }
}
