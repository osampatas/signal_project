package com.cardio_generator.outputs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Writes simulated patient data to text files in a chosen directory.
 * Each data label is written to its own file, such as {@code ECG.txt}.
 */
// Google Java Style: renamed the class to UpperCamelCase.
public class FileOutputStrategy implements OutputStrategy {

    // Google Java Style: renamed the field to lowerCamelCase.
    private final String baseDirectory;

    // Google Java Style: renamed the field to lowerCamelCase.
    public final ConcurrentHashMap<String, String> fileMap = new ConcurrentHashMap<>();

    /**
     * Creates a file output strategy that stores files in the given directory.
     *
     * @param baseDirectory directory where output files should be created
     */
    public FileOutputStrategy(String baseDirectory) {

        this.baseDirectory = baseDirectory;
    }

    /**
     * Saves one generated data point to the file for its label.
     *
     * @param patientId the simulated patient's identifier
     * @param timestamp the time when the data was generated
     * @param label the type of data being written
     * @param data the generated data value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        try {
            // Create the directory
            Files.createDirectories(Paths.get(baseDirectory));
        } catch (IOException e) {
            System.err.println("Error creating base directory: " + e.getMessage());
            return;
        }
        // Google Java Style: renamed the local variable to lowerCamelCase.
        String filePath = fileMap.computeIfAbsent(label,
                k -> Paths.get(baseDirectory, label + ".txt").toString());

        // Write the data to the file
        try (PrintWriter out = new PrintWriter(
                Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND))) {
            out.printf("Patient ID: %d, Timestamp: %d, Label: %s, Data: %s%n", patientId, timestamp, label, data);
        } catch (Exception e) {
            System.err.println("Error writing to file " + filePath + ": " + e.getMessage());
        }
    }
}
