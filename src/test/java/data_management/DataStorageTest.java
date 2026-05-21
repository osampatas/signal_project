package data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.data_management.DataStorage;
import com.data_management.FileDataReader;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DataStorageTest {

    @Test
    void addAndGetRecordsReturnsStoredPatientData() {
        DataStorage storage = new DataStorage();
        storage.addPatientData(1, 100.0, "WhiteBloodCells", 1714376789050L);
        storage.addPatientData(1, 200.0, "WhiteBloodCells", 1714376789051L);

        List<PatientRecord> records = storage.getRecords(1, 1714376789050L, 1714376789051L);
        assertEquals(2, records.size());
        assertEquals(100.0, records.get(0).getMeasurementValue());
    }

    @Test
    void getRecordsReturnsOnlyRecordsInsideTimeRange() {
        Patient patient = new Patient(2);
        patient.addRecord(10.0, "ECG", 1000L);
        patient.addRecord(20.0, "ECG", 2000L);
        patient.addRecord(30.0, "ECG", 3000L);

        List<PatientRecord> records = patient.getRecords(1000L, 2000L);

        assertEquals(2, records.size());
        assertEquals(10.0, records.get(0).getMeasurementValue());
        assertEquals(20.0, records.get(1).getMeasurementValue());
    }

    @Test
    void getRecordsReturnsEmptyListWhenNothingMatches() {
        Patient patient = new Patient(3);
        patient.addRecord(10.0, "ECG", 1000L);

        List<PatientRecord> records = patient.getRecords(2000L, 3000L);

        assertTrue(records.isEmpty());
    }

    @Test
    void fileDataReaderReadsValidSimulatorOutput(@TempDir Path tempDirectory) throws IOException {
        Files.write(tempDirectory.resolve("Saturation.txt"), List.of(
                "Patient ID: 4, Timestamp: 1000, Label: Saturation, Data: 96.0%"));
        Files.write(tempDirectory.resolve("Alert.txt"), List.of(
                "Patient ID: 4, Timestamp: 2000, Label: Alert, Data: triggered"));

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDirectory.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(4, 0L, 3000L);
        assertEquals(2, records.size());
        assertEquals("Saturation", records.get(0).getRecordType());
        assertEquals(96.0, records.get(0).getMeasurementValue());
        assertEquals("Alert", records.get(1).getRecordType());
        assertEquals(1.0, records.get(1).getMeasurementValue());
    }

    @Test
    void fileDataReaderSkipsMissingAndMalformedData(@TempDir Path tempDirectory) throws IOException {
        Files.write(tempDirectory.resolve("ECG.txt"), List.of(
                "This line is malformed",
                "Patient ID: 5, Timestamp: 1000, Label: ECG, Data: not-a-number",
                "Patient ID: 5, Timestamp: 2000, Label: ECG, Data: 0.75"));

        DataStorage storage = new DataStorage();
        FileDataReader reader = new FileDataReader(tempDirectory.toString());
        reader.readData(storage);

        List<PatientRecord> records = storage.getRecords(5, 0L, 3000L);
        assertEquals(1, records.size());
        assertEquals(0.75, records.get(0).getMeasurementValue());
    }
}
