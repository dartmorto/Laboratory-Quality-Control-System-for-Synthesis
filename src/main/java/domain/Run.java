package domain;

import java.io.Serializable;
import java.time.Instant;

/**
 * Experiment run linked to an experiment by experimentId.
 */
public final class Run implements Comparable<Run>, Serializable {

    private static final long serialVersionUID = 10734L;

    private final long id;
    private final long experimentId;
    private final String name;
    private final String operatorUsername;
    private final Instant createdAt;

    public Run(long id, long experimentId, String name, String operatorUsername) {
        this(id, experimentId, name, operatorUsername, Instant.now());
    }

    public Run(long id, long experimentId, String name, String operatorUsername, Instant createdAt) {
        this.id = id;
        this.experimentId = experimentId;
        this.name = name;
        this.operatorUsername = operatorUsername;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
    }

    public long getId() {
        return id;
    }

    public long getExperimentId() {
        return experimentId;
    }

    public String getName() {
        return name;
    }

    public String getOperatorUsername() {
        return operatorUsername;
    }

    public String getOperator() {
        return operatorUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public int compareTo(Run other) {
        return Long.compare(id, other.id);
    }
}
