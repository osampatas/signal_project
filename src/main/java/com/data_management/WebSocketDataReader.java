package com.data_management;

import com.alerts.Alert;
import com.alerts.AlertGenerator;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

/**
 * Reads real-time patient data from a WebSocket stream and stores it in
 * {@link DataStorage}.
 */
public class WebSocketDataReader extends WebSocketClient implements DataReader {
    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile(
            "\"(patientId|timestamp|label|value)\"\\s*:\\s*(\"[^\"]*\"|[^,}\\s]+)");
    private static final Pattern OLD_CSV_PATTERN = Pattern.compile("([^,]+),([^,]+),([^,]+),(.+)");

    private final DataStorage dataStorage;
    private final AlertGenerator alertGenerator;
    private final List<Alert> generatedAlerts;

    /**
     * Creates a WebSocket reader.
     *
     * @param serverUri WebSocket URI, for example ws://localhost:8080
     * @param dataStorage storage that receives parsed records
     */
    public WebSocketDataReader(URI serverUri, DataStorage dataStorage) {
        super(serverUri);
        this.dataStorage = dataStorage;
        this.alertGenerator = new AlertGenerator(dataStorage);
        this.generatedAlerts = new ArrayList<>();
    }

    /**
     * Starts the real-time connection. The WebSocket library then calls
     * {@link #onMessage(String)} whenever data arrives.
     *
     * @param ignoredStorage kept for compatibility with the DataReader interface
     * @throws IOException if the connection cannot be started
     */
    @Override
    public void readData(DataStorage ignoredStorage) throws IOException {
        try {
            connect();
        } catch (IllegalStateException e) {
            throw new IOException("Could not start WebSocket reader.", e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Connected to WebSocket data stream.");
    }

    @Override
    public void onMessage(String message) {
        handleMessage(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket data stream closed: " + reason);
    }

    @Override
    public void onError(Exception exception) {
        System.err.println("WebSocket data stream error: " + exception.getMessage());
    }

    /**
     * Parses one message, stores it, and evaluates alerts for the patient.
     * Bad messages are ignored so real-time processing can continue.
     *
     * @param message raw WebSocket message
     * @return true when a record was stored
     */
    public boolean handleMessage(String message) {
        PatientRecord record = parseMessage(message);
        if (record == null) {
            return false;
        }

        dataStorage.addPatientData(record.getPatientId(), record.getMeasurementValue(),
                record.getRecordType(), record.getTimestamp());
        evaluateAlertsForPatient(record.getPatientId());
        return true;
    }

    /**
     * Parses JSON messages from Week 5 and old CSV messages from earlier output.
     *
     * @param message raw WebSocket message
     * @return parsed patient record, or null if the message is invalid
     */
    public PatientRecord parseMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        try {
            String trimmedMessage = message.trim();
            if (trimmedMessage.startsWith("{")) {
                return parseJsonMessage(trimmedMessage);
            }
            return parseCsvMessage(trimmedMessage);
        } catch (NumberFormatException e) {
            // Invalid patient ID, timestamp, or numeric value. Ignore safely.
            return null;
        }
    }

    /**
     * Returns alerts produced while handling WebSocket messages.
     *
     * @return copy of generated alerts
     */
    public List<Alert> getGeneratedAlerts() {
        return new ArrayList<>(generatedAlerts);
    }

    private PatientRecord parseJsonMessage(String message) {
        String patientIdText = null;
        String timestampText = null;
        String label = null;
        String valueText = null;

        Matcher matcher = JSON_FIELD_PATTERN.matcher(message);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            String fieldValue = removeQuotes(matcher.group(2));
            if ("patientId".equals(fieldName)) {
                patientIdText = fieldValue;
            } else if ("timestamp".equals(fieldName)) {
                timestampText = fieldValue;
            } else if ("label".equals(fieldName)) {
                label = fieldValue;
            } else if ("value".equals(fieldName)) {
                valueText = fieldValue;
            }
        }

        return createRecord(patientIdText, timestampText, label, valueText);
    }

    private PatientRecord parseCsvMessage(String message) {
        Matcher matcher = OLD_CSV_PATTERN.matcher(message);
        if (!matcher.matches()) {
            return null;
        }
        return createRecord(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
    }

    private PatientRecord createRecord(String patientIdText, String timestampText, String label, String valueText) {
        if (patientIdText == null || timestampText == null || label == null || valueText == null) {
            return null;
        }

        int patientId = Integer.parseInt(patientIdText.trim());
        long timestamp = Long.parseLong(timestampText.trim());
        double value = parseValue(label.trim(), valueText.trim());
        return new PatientRecord(patientId, value, label.trim(), timestamp);
    }

    private String removeQuotes(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.startsWith("\"") && trimmedValue.endsWith("\"")) {
            return trimmedValue.substring(1, trimmedValue.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return trimmedValue;
    }

    private double parseValue(String label, String rawValue) {
        if ("Alert".equalsIgnoreCase(label)) {
            return "triggered".equalsIgnoreCase(rawValue) ? 1.0 : 0.0;
        }
        return Double.parseDouble(rawValue.replace("%", ""));
    }

    private void evaluateAlertsForPatient(int patientId) {
        Patient patient = dataStorage.getPatient(patientId);
        if (patient == null) {
            return;
        }

        int previousAlertCount = alertGenerator.getTriggeredAlerts().size();
        alertGenerator.evaluateData(patient);
        List<Alert> allAlerts = alertGenerator.getTriggeredAlerts();
        for (int i = previousAlertCount; i < allAlerts.size(); i++) {
            generatedAlerts.add(allAlerts.get(i));
        }
    }
}
