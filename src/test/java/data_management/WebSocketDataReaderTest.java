package data_management;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cardio_generator.outputs.WebSocketOutputStrategy;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class WebSocketDataReaderTest {

    @Test
    void parsesValidJsonWebSocketMessage() throws Exception {
        WebSocketDataReader reader = newReader(new DataStorage());
        String message = WebSocketOutputStrategy.createMessage(1, 1000L, "Saturation", "96.0%");

        PatientRecord record = reader.parseMessage(message);

        assertEquals(1, record.getPatientId());
        assertEquals(1000L, record.getTimestamp());
        assertEquals("Saturation", record.getRecordType());
        assertEquals(96.0, record.getMeasurementValue());
    }

    @Test
    void parsesOldCsvWebSocketMessageForBackwardCompatibility() throws Exception {
        WebSocketDataReader reader = newReader(new DataStorage());

        PatientRecord record = reader.parseMessage("2,2000,ECG,0.75");

        assertEquals(2, record.getPatientId());
        assertEquals(2000L, record.getTimestamp());
        assertEquals("ECG", record.getRecordType());
        assertEquals(0.75, record.getMeasurementValue());
    }

    @Test
    void ignoresCorruptedEmptyAndMissingFieldMessages() throws Exception {
        WebSocketDataReader reader = newReader(new DataStorage());

        assertNull(reader.parseMessage(""));
        assertNull(reader.parseMessage("not a valid message"));
        assertNull(reader.parseMessage("{\"patientId\":1,\"label\":\"ECG\",\"value\":\"0.4\"}"));
    }

    @Test
    void ignoresInvalidNumericValueWithoutCrashing() throws Exception {
        WebSocketDataReader reader = newReader(new DataStorage());
        String message = "{\"patientId\":1,\"timestamp\":1000,\"label\":\"ECG\",\"value\":\"bad\"}";

        assertNull(reader.parseMessage(message));
    }

    @Test
    void handleMessageStoresParsedRecordInDataStorage() throws Exception {
        DataStorage storage = new DataStorage();
        WebSocketDataReader reader = newReader(storage);
        String message = WebSocketOutputStrategy.createMessage(3, 3000L, "SystolicPressure", "120.0");

        boolean handled = reader.handleMessage(message);
        List<PatientRecord> records = storage.getRecords(3, 0L, 4000L);

        assertTrue(handled);
        assertEquals(1, records.size());
        assertEquals("SystolicPressure", records.get(0).getRecordType());
    }

    @Test
    void handleMessageReturnsFalseForBadData() throws Exception {
        DataStorage storage = new DataStorage();
        WebSocketDataReader reader = newReader(storage);

        boolean handled = reader.handleMessage("bad data");

        assertFalse(handled);
        assertTrue(storage.getAllPatients().isEmpty());
    }

    @Test
    void handleMessageConnectsRealTimeDataToAlertLogic() throws Exception {
        DataStorage storage = new DataStorage();
        WebSocketDataReader reader = newReader(storage);

        reader.handleMessage(WebSocketOutputStrategy.createMessage(4, 1000L, "Saturation", "91.0%"));

        assertTrue(reader.getGeneratedAlerts().stream()
                .anyMatch(alert -> "Low oxygen saturation".equals(alert.getCondition())));
    }

    private WebSocketDataReader newReader(DataStorage storage) throws Exception {
        return new WebSocketDataReader(new URI("ws://localhost:8080"), storage);
    }
}
