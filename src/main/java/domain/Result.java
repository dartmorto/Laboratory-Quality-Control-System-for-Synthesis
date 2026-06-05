package domain;

import java.io.Serializable;
import java.time.Instant;

/**
 * Measurement result linked to a run by runId.
 */
public final class Result implements Comparable<Result>, Serializable {

    private static final long serialVersionUID = 204679L;

    private final long id;
    private final long runId;
    private final MeasurementParam param;
    private final double value;
    private final String unit;
    private final String comment;
    private final Instant createdAt;

    public Result(long id,
                  long runId,
                  String comment,
                  double value,
                  String unit,
                  Instant createdAt,
                  MeasurementParam param) {
        this.id = id;
        this.runId = runId;
        this.comment = comment;
        this.value = value;
        this.unit = unit;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.param = param;
    }

    public long getId() {
        return id;
    }

    public long getRunId() {
        return runId;
    }

    public MeasurementParam getParam() {
        return param;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public int compareTo(Result other) {
        return Long.compare(id, other.id);
    }

    @Override
    public String toString() {
        return "Result{" +
                "id=" + id +
                ", runId='" + runId + '\'' +
                ", comment='" + comment + '\'' +
                ", param='" + param + '\'' +
                ", unit='" + unit + '\'' +
                ", value='" + value + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
