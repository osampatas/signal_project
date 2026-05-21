package com.cardio_generator.outputs;

/**
 * Defines where generated patient data should be sent.
 */
public interface OutputStrategy {
    /**
     * Sends one generated data point to the selected output destination.
     *
     * @param patientId the simulated patient's identifier
     * @param timestamp the time when the data was generated
     * @param label the type of data, such as ECG or Saturation
     * @param data the generated data value
     */
    void output(int patientId, long timestamp, String label, String data);
}
