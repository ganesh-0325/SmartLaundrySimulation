package com.smartlaundry.model;

public final class ResourceSnapshot {
    private final String id;
    private final String type;
    private final ResourceStatus status;
    private final String customerId;
    private final String operation;
    private final long startNanos;
    private final long endNanos;
    private final boolean failedForDay;

    public ResourceSnapshot(
            String id,
            String type,
            ResourceStatus status,
            String customerId,
            String operation,
            long startNanos,
            long endNanos,
            boolean failedForDay) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.customerId = customerId;
        this.operation = operation;
        this.startNanos = startNanos;
        this.endNanos = endNanos;
        this.failedForDay = failedForDay;
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public ResourceStatus getStatus() { return status; }
    public String getCustomerId() { return customerId; }
    public String getOperation() { return operation; }
    public long getStartNanos() { return startNanos; }
    public long getEndNanos() { return endNanos; }
    public boolean isFailedForDay() { return failedForDay; }

    public double getRemainingSeconds() {
        if (endNanos <= 0) return 0.0;
        return Math.max(0.0, (endNanos - System.nanoTime()) / 1_000_000_000.0);
    }

    public double getProgressPercent() {
        if (startNanos <= 0 || endNanos <= startNanos) return 0.0;
        double total = endNanos - startNanos;
        double elapsed = System.nanoTime() - startNanos;
        return Math.max(0.0, Math.min(100.0, elapsed * 100.0 / total));
    }
}
