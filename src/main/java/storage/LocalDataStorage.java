package storage;

import domain.Experiment;
import domain.Result;
import domain.Run;
import manager.CollectionManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public final class LocalDataStorage {
    private final Path dataFile;

    public LocalDataStorage(Path dataFile) {
        this.dataFile = dataFile.toAbsolutePath().normalize();
    }

    public boolean exists() {
        return Files.isRegularFile(dataFile);
    }

    public Path dataFile() {
        return dataFile;
    }

    public void save(CollectionManager manager) {
        try {
            Path parent = dataFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Snapshot snapshot = new Snapshot(
                    manager.getAllExperiments(),
                    manager.getAllRuns(),
                    manager.getAllResults()
            );
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(dataFile))) {
                output.writeObject(snapshot);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось сохранить данные: " + dataFile, e);
        }
    }

    public void load(CollectionManager manager) {
        if (!exists()) {
            return;
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(dataFile))) {
            Object loaded = input.readObject();
            if (!(loaded instanceof Snapshot snapshot)) {
                throw new IllegalStateException("Файл данных имеет неподдерживаемый формат: " + dataFile);
            }
            manager.replaceData(snapshot.experiments(), snapshot.runs(), snapshot.results());
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Не удалось загрузить данные: " + dataFile, e);
        }
    }

    private record Snapshot(
            Map<Long, Experiment> experiments,
            Map<Long, Run> runs,
            Map<Long, Result> results
    ) implements Serializable {
        private static final long serialVersionUID = 1L;

        private Snapshot(Map<Long, Experiment> experiments,
                         Map<Long, Run> runs,
                         Map<Long, Result> results) {
            this.experiments = new TreeMap<>(experiments);
            this.runs = new TreeMap<>(runs);
            this.results = new TreeMap<>(results);
        }
    }
}
