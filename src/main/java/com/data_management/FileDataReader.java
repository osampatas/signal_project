package com.data_management;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads simulator output files from a directory and stores the records in
 * {@link DataStorage}.
 */
public class FileDataReader implements DataReader {
    private static final List<String> EXPECTED_FILES = Arrays.asList(
            "ECG.txt",
            "Saturation.txt",
            "SystolicPressure.txt",
            "DiastolicPressure.txt",
            "Alert.txt");

    private static final Pattern OUTPUT_LINE_PATTERN = Pattern.compile(
            "Patient ID: (\\d+), Timestamp: (\\d+), Label: ([^,]+), Data: (.+)");

    private final Path outputDirectory;

    /**
     * Creates a reader for the directory produced by {@code --output file:<dir>}.
     *
     * @param outputDirectory directory that may contain simulator output files
     */
    public FileDataReader(String outputDirectory) {
        this.outputDirectory = Paths.get(outputDirectory);
    }

    /**
     * Reads all known simulator output files that exist in the directory.
     * Missing files and malformed lines are skipped so one bad file does not stop
     * the whole import.
     *
     * @param dataStorage the storage where parsed data will be stored
     * @throws IOException if an existing file cannot be read
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        for (String fileName : EXPECTED_FILES) {
            Path filePath = outputDirectory.resolve(fileName);
            if (Files.exists(filePath)) {
                readFile(filePath, dataStorage);
            }
        }
    }

    private void readFile(Path filePath, DataStorage dataStorage) throws IOException {
        for (String line : Files.readAllLines(filePath)) {
            parseLine(line, dataStorage);
        }
    }

    private void parseLine(String line, DataStorage dataStorage) {
        Matcher matcher = OUTPUT_LINE_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return;
        }

        try {
            int patientId = Integer.parseInt(matcher.group(1));
            long timestamp = Long.parseLong(matcher.group(2));
            String label = matcher.group(3).trim();
            double value = parseValue(label, matcher.group(4).trim());

            dataStorage.addPatientData(patientId, value, label, timestamp);
        } catch (NumberFormatException e) {
            // Assumption: malformed values should be ignored instead of crashing.
        }
    }

    private double parseValue(String label, String rawValue) {
        if ("Alert".equalsIgnoreCase(label)) {
            return "triggered".equalsIgnoreCase(rawValue) ? 1.0 : 0.0;
        }

        // Saturation values may include a percent sign, for example "96.0%".
        String numericValue = rawValue.replace("%", "");
        return Double.parseDouble(numericValue);
    }
}
