package detection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result returned by the Python sample detector.
 */
public final class SampleDetectionPrediction {
    private final String predictedClass;
    private final double confidence;
    private final String modelName;
    private final Map<String, Double> probabilities;

    public SampleDetectionPrediction(String predictedClass,
                                     double confidence,
                                     String modelName,
                                     Map<String, Double> probabilities) {
        this.predictedClass = predictedClass;
        this.confidence = confidence;
        this.modelName = modelName;
        this.probabilities = Collections.unmodifiableMap(new LinkedHashMap<>(probabilities));
    }

    public String getPredictedClass() {
        return predictedClass;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getModelName() {
        return modelName;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

    public String formatProbabilities() {
        return probabilities.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.format(Locale.ROOT, "%.4f", entry.getValue()))
                .collect(Collectors.joining(", "));
    }
}

