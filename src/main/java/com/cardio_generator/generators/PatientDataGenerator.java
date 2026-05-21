package com.cardio_generator.generators;

import com.cardio_generator.outputs.OutputStrategy;

/**
 * Defines the common behavior for all patient data generators.
 */
public interface PatientDataGenerator {
    /**
     * Generates one piece of simulated data for a patient.
     *
     * @param patientId the simulated patient's identifier
     * @param outputStrategy the output method that receives the generated data
     */
    void generate(int patientId, OutputStrategy outputStrategy);
}
