package manager;

import domain.Experiment;
import domain.MeasurementParam;
import domain.Result;
import domain.Run;
import validation.Validator;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Manages experiments, runs and measurement results in memory.
 */
public class CollectionManager {

    private final Map<Long, Experiment> experiments = new TreeMap<>();
    private final Map<Long, Run> runs = new TreeMap<>();
    private final Map<Long, Result> results = new TreeMap<>();

    private long currentExperimentId = 1;
    private long currentRunId = 1;
    private long currentResultId = 1;

    private long generateExperimentId() {
        return currentExperimentId++;
    }

    private long generateRunId() {
        return currentRunId++;
    }

    private long generateResultId() {
        return currentResultId++;
    }

    public Experiment createExperiment(String name, String description, String ownerUsername) {
        Validator.requireNonBlank(name, "Название");
        Validator.requireNonBlank(ownerUsername, "Владелец");

        Experiment experiment = new Experiment(
                generateExperimentId(),
                name.trim(),
                description == null ? "" : description.trim(),
                ownerUsername.trim()
        );
        addExperiment(experiment);
        return experiment;
    }

    public void addExperiment(Experiment experiment) {
        Validator.requireNotNull(experiment, "Эксперимент");
        Validator.requirePositive(experiment.getId(), "ID эксперимента");
        Validator.requireExists(!experiments.containsKey(experiment.getId()), "Эксперимент с таким ID уже существует");

        long nextExperimentId = nextAfter(currentExperimentId, experiment.getId(), "эксперимента");
        experiments.put(experiment.getId(), experiment);
        currentExperimentId = nextExperimentId;
    }

    public void updateExperiment(long id, Experiment updated) {
        Validator.requirePositive(id, "ID");
        Validator.requireNotNull(updated, "Эксперимент");
        Validator.requireNonBlank(updated.getName(), "Название");
        Validator.requireNonBlank(updated.getOwnerUsername(), "Владелец");

        if (updated.getId() != id) {
            throw new IllegalArgumentException("ID не совпадает с объектом эксперимента");
        }
        if (!experiments.containsKey(id)) {
            throw new IllegalArgumentException("Эксперимент не найден");
        }
        experiments.put(id, updated);
    }

    public boolean isExperimentOwner(long experimentId, String username) {
        Validator.requirePositive(experimentId, "ID");
        Validator.requireNonBlank(username, "Пользователь");
        return getById(experimentId).getOwnerUsername().equals(username.trim());
    }

    public Experiment getExperiment(long id) {
        return getById(id);
    }

    public Experiment getById(long id) {
        Validator.requirePositive(id, "ID");
        return Optional.ofNullable(experiments.get(id))
                .orElseThrow(() -> new IllegalArgumentException("Эксперимент не найден"));
    }

    public Map<Long, Experiment> getAllExperiments() {
        return new TreeMap<>(experiments);
    }

    public void replaceData(Map<Long, Experiment> loadedExperiments,
                            Map<Long, Run> loadedRuns,
                            Map<Long, Result> loadedResults) {
        Validator.requireNotNull(loadedExperiments, "Эксперименты");
        Validator.requireNotNull(loadedRuns, "Запуски");
        Validator.requireNotNull(loadedResults, "Результаты");

        TreeMap<Long, Experiment> experimentCopy = new TreeMap<>(loadedExperiments);
        TreeMap<Long, Run> runCopy = new TreeMap<>(loadedRuns);
        TreeMap<Long, Result> resultCopy = new TreeMap<>(loadedResults);

        currentExperimentId = nextId(experimentCopy, "эксперимента");
        currentRunId = nextId(runCopy, "запуска");
        currentResultId = nextId(resultCopy, "результата");

        experiments.clear();
        experiments.putAll(experimentCopy);
        runs.clear();
        runs.putAll(runCopy);
        results.clear();
        results.putAll(resultCopy);
    }

    public void remove(long id) {
        removeExperiment(id);
    }

    public void removeExperiment(long id) {
        Validator.requirePositive(id, "ID");

        if (experiments.remove(id) == null) {
            throw new IllegalArgumentException("Эксперимент не найден");
        }
        runs.values().removeIf(run -> run.getExperimentId() == id);
        results.values().removeIf(result -> !runs.containsKey(result.getRunId()));
    }

    public Run createRun(long experimentId, String name, String operatorUsername) {
        return createRun(generateRunId(), experimentId, name, operatorUsername);
    }

    private Run createRun(long id, long experimentId, String name, String operatorUsername) {
        Validator.requirePositive(id, "ID");
        Validator.requirePositive(experimentId, "ID эксперимента");
        Validator.requireNonBlank(name, "Название");
        Validator.requireNonBlank(operatorUsername, "Оператор");
        Validator.requireExists(experiments.containsKey(experimentId), "Эксперимент не найден");
        Validator.requireExists(!runs.containsKey(id), "Запуск с таким ID уже существует");

        Run run = new Run(id, experimentId, name.trim(), operatorUsername.trim());
        addRun(run);
        return run;
    }

    public void addRun(Run run) {
        Validator.requireNotNull(run, "Запуск");
        Validator.requirePositive(run.getId(), "ID запуска");
        Validator.requirePositive(run.getExperimentId(), "ID эксперимента");
        Validator.requireExists(experiments.containsKey(run.getExperimentId()), "Эксперимент не найден");
        Validator.requireExists(!runs.containsKey(run.getId()), "Запуск с таким ID уже существует");

        long nextRunId = nextAfter(currentRunId, run.getId(), "запуска");
        runs.put(run.getId(), run);
        currentRunId = nextRunId;
    }

    public Run getRunById(long id) {
        Validator.requirePositive(id, "ID");
        return Optional.ofNullable(runs.get(id))
                .orElseThrow(() -> new IllegalArgumentException("Запуск не найден"));
    }

    public Map<Long, Run> getRunsByExperimentId(long experimentId) {
        Validator.requirePositive(experimentId, "ID эксперимента");

        Map<Long, Run> selectedRuns = new TreeMap<>();
        for (Run run : runs.values()) {
            if (run.getExperimentId() == experimentId) {
                selectedRuns.put(run.getId(), run);
            }
        }
        return selectedRuns;
    }

    public Run updateRun(long id, String name, String operatorUsername) {
        Validator.requirePositive(id, "ID");
        Validator.requireNonBlank(name, "Название");
        Validator.requireNonBlank(operatorUsername, "Оператор");

        Run run = getRunById(id);
        Run updated = new Run(run.getId(), run.getExperimentId(), name.trim(), operatorUsername.trim());
        runs.put(id, updated);
        return updated;
    }

    public Map<Long, Run> getAllRuns() {
        return new TreeMap<>(runs);
    }

    public void removeRun(long id) {
        Validator.requirePositive(id, "ID");

        if (runs.remove(id) == null) {
            throw new IllegalArgumentException("Запуск не найден");
        }
        results.values().removeIf(result -> result.getRunId() == id);
    }

    public Result createResult(long runId,
                               MeasurementParam param,
                               double value,
                               String unit,
                               String comment) {
        return createResult(generateResultId(), runId, param, value, unit, comment);
    }

    private Result createResult(long resultId,
                                long runId,
                                MeasurementParam param,
                                double value,
                                String unit,
                                String comment) {
        Validator.requirePositive(resultId, "ID результата");
        Validator.requirePositive(runId, "ID запуска");
        Validator.requireNotNull(param, "Параметр");
        Validator.requireNonBlank(unit, "Единица измерения");
        Validator.requireExists(!results.containsKey(resultId), "Результат с таким ID уже существует");
        getRunById(runId);

        Result result = new Result(
                resultId,
                runId,
                comment == null ? "" : comment.trim(),
                value,
                unit.trim(),
                Instant.now(),
                param
        );
        addResult(result);
        return result;
    }

    public void addResult(Result result) {
        Validator.requireNotNull(result, "Результат");
        Validator.requirePositive(result.getId(), "ID результата");
        Validator.requirePositive(result.getRunId(), "ID запуска");
        Validator.requireExists(runs.containsKey(result.getRunId()), "Запуск не найден");
        Validator.requireExists(!results.containsKey(result.getId()), "Результат с таким ID уже существует");

        long nextResultId = nextAfter(currentResultId, result.getId(), "результата");
        results.put(result.getId(), result);
        currentResultId = nextResultId;
    }

    public Map<Long, Result> getAllResults() {
        return new TreeMap<>(results);
    }

    public Map<Long, Result> getResultsByRunId(long runId) {
        Validator.requirePositive(runId, "ID запуска");

        Map<Long, Result> selectedResults = new TreeMap<>();
        for (Result result : results.values()) {
            if (result.getRunId() == runId) {
                selectedResults.put(result.getId(), result);
            }
        }
        return selectedResults;
    }

    public Result getResultById(long id) {
        Validator.requirePositive(id, "ID");
        return Optional.ofNullable(results.get(id))
                .orElseThrow(() -> new IllegalArgumentException("Результат не найден"));
    }

    public void removeResult(long id) {
        Validator.requirePositive(id, "ID");

        if (results.remove(id) == null) {
            throw new IllegalArgumentException("Результат не найден");
        }
    }

    public Result getResultByRunId(long runId) {
        return getResultsByRunId(runId).values().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Результат не найден"));
    }

    private long nextId(Map<Long, ?> data, String entityName) {
        if (data.isEmpty()) {
            return 1;
        }

        long maxId = new TreeMap<>(data).lastKey();
        if (maxId == Long.MAX_VALUE) {
            throw new IllegalArgumentException("ID " + entityName + " достиг максимального значения");
        }
        return maxId + 1;
    }

    private long nextAfter(long currentId, long usedId, String entityName) {
        if (usedId == Long.MAX_VALUE) {
            throw new IllegalArgumentException("ID " + entityName + " достиг максимального значения");
        }
        return Math.max(currentId, usedId + 1);
    }
}
