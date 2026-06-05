package cli.commands;

import detection.SampleDetectionPrediction;
import detection.SampleDetector;
import domain.MeasurementParam;
import domain.Result;
import manager.CollectionManager;
import user.AuthService;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

/**
 * Runs sample image detection and stores model confidence as a run result.
 */
public class SampleDetectCommand extends Command {
    private final SampleDetector detector;

    public SampleDetectCommand(CollectionManager manager, Scanner scanner, AuthService authService) {
        super(manager, scanner, authService);
        this.detector = new SampleDetector();
    }

    @Override
    public String name() {
        return "detect_sample";
    }

    @Override
    public void execute(String[] parts) {
        requireLogin();

        long runId = readRunId(parts);
        manager.getRunById(runId);

        String imagePath = readImagePath(parts);
        SampleDetectionPrediction prediction = detector.predict(Path.of(imagePath));

        String comment = "class=" + prediction.getPredictedClass()
                + "; model=" + prediction.getModelName()
                + "; image=" + Path.of(imagePath).toAbsolutePath().normalize()
                + "; probabilities=" + prediction.formatProbabilities();

        Result result = manager.createResult(
                runId,
                MeasurementParam.SAMPLE_DETECTION_CONFIDENCE,
                prediction.getConfidence(),
                "probability",
                comment
        );

        System.out.println("Результат детекции: " + prediction.getPredictedClass());
        System.out.printf(Locale.ROOT, "Уверенность: %.4f%n", prediction.getConfidence());
        System.out.println("Вероятности: " + prediction.formatProbabilities());
        System.out.println("Результат сохранен. ID: " + result.getId());
    }

    private long readRunId(String[] parts) {
        if (parts.length >= 2) {
            return parseId(parts[1]);
        }

        System.out.print("ID запуска: ");
        String input = scanner.nextLine();
        cancelIfCancelled(input);
        return parseId(input);
    }

    private String readImagePath(String[] parts) {
        if (parts.length >= 3) {
            return String.join(" ", Arrays.copyOfRange(parts, 2, parts.length)).trim();
        }

        System.out.print("Путь к изображению: ");
        String input = scanner.nextLine().trim();
        cancelIfCancelled(input);
        if (input.isBlank()) {
            throw new IllegalArgumentException("Путь к изображению не указан");
        }
        return input;
    }
}

