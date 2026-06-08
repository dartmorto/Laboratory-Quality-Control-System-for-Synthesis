package detection;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Runs the bundled Python detector as a subprocess and parses a properties response.
 */
public final class SampleDetector {
    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private final Path projectRoot;
    private final Path scriptPath;
    private final Path checkpointPath;
    private final String pythonExecutable;

    public SampleDetector() {
        this(resolveProjectRoot(), resolvePythonExecutable());
    }

    public SampleDetector(Path projectRoot) {
        this(projectRoot, resolvePythonExecutable());
    }

    public SampleDetector(Path projectRoot, String pythonExecutable) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.scriptPath = this.projectRoot.resolve("ml").resolve("predict.py");
        this.checkpointPath = this.projectRoot.resolve("ml").resolve("checkpoints").resolve("sample_detector_resnet18.pt");
        this.pythonExecutable = normalizePythonExecutable(pythonExecutable);
    }

    public SampleDetectionPrediction predict(Path imagePath) {
        Path normalizedImage = imagePath.toAbsolutePath().normalize();
        requireFile(scriptPath, "Python script");
        requireFile(checkpointPath, "Model checkpoint");
        requireFile(normalizedImage, "Image");

        List<String> command = List.of(
                pythonExecutable,
                scriptPath.toString(),
                "--checkpoint", checkpointPath.toString(),
                "--image", normalizedImage.toString(),
                "--format", "properties"
        );

        ProcessResult processResult = run(command);
        if (processResult.exitCode() != 0) {
            throw new IllegalStateException("Sample detection model failed: " + processResult.stderr().strip());
        }
        return parsePrediction(processResult.stdout());
    }

    private ProcessResult run(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(projectRoot.toFile());
        builder.environment().put("PYTHONIOENCODING", "utf-8");
        builder.environment().put("PYTHONUTF8", "1");

        try {
            Process process = builder.start();
            boolean finished = process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Sample detection model timed out after " + TIMEOUT.toSeconds() + " seconds");
            }

            String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ProcessResult(process.exitValue(), stdout, stderr);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось запустить Python: " + pythonExecutable + ". Выберите python.exe от окружения с torch, torchvision и Pillow.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Sample detection model execution was interrupted", e);
        }
    }

    private SampleDetectionPrediction parsePrediction(String stdout) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(stdout));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot parse sample detection model output", e);
        }

        String predictedClass = required(properties, "predicted_class");
        double confidence = parseDouble(required(properties, "confidence"), "confidence");
        String modelName = properties.getProperty("model_name", "unknown");
        int count = (int) parseDouble(properties.getProperty("probability.count", "0"), "probability.count");

        Map<String, Double> probabilities = new LinkedHashMap<>();
        for (int index = 0; index < count; index++) {
            String label = properties.getProperty("probability." + index + ".label");
            String value = properties.getProperty("probability." + index + ".value");
            if (label != null && value != null) {
                probabilities.put(label, parseDouble(value, "probability." + index + ".value"));
            }
        }

        return new SampleDetectionPrediction(predictedClass, confidence, modelName, probabilities);
    }

    private static Path resolveProjectRoot() {
        String configured = System.getProperty("sampleDetection.projectRoot");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("SAMPLE_DETECTION_PROJECT_ROOT");
        }
        if (configured == null || configured.isBlank()) {
            return Path.of("");
        }
        return Path.of(configured);
    }

    private static String normalizePythonExecutable(String configured) {
        if (configured == null || configured.isBlank()) {
            return resolvePythonExecutable();
        }
        String trimmed = configured.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private static String resolvePythonExecutable() {
        String configured = System.getProperty("sampleDetection.python");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("SAMPLE_DETECTION_PYTHON");
        }
        if (configured != null && !configured.isBlank()) {
            return configured.trim();
        }

        String home = System.getProperty("user.home");
        Path[] candidates = {
                Path.of("").toAbsolutePath().normalize().resolve(".venv").resolve("Scripts").resolve("python.exe"),
                Path.of(home, "PycharmProjects", "FeSO4detection", ".venv", "Scripts", "python.exe")
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return candidate.toString();
            }
        }
        return "python";
    }

    private static void requireFile(Path path, String label) {
        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(label + " not found: " + path);
        }
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Sample detection model output does not contain " + key);
        }
        return value;
    }

    private static double parseDouble(String value, String label) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Cannot parse " + label + ": " + value, e);
        }
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {
    }
}



