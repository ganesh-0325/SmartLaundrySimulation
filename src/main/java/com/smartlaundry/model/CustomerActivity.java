package com.smartlaundry.model;

public final class CustomerActivity {
    private final String customerId;
    private final String stage;
    private final String resource;
    private final String reason;
    private final long startNanos;
    private final long endNanos;
    private final boolean active;

    public CustomerActivity(
            String customerId,
            String stage,
            String resource,
            String reason,
            long startNanos,
            long endNanos,
            boolean active) {
        this.customerId = customerId;
        this.stage = stage;
        this.resource = resource == null ? "" : resource;
        this.reason = reason == null ? "" : reason;
        this.startNanos = startNanos;
        this.endNanos = endNanos;
        this.active = active;
    }

    public String getCustomerId() { return customerId; }
    public String getStage() { return stage; }
    public String getResource() { return resource; }
    public String getReason() { return reason; }
    public long getStartNanos() { return startNanos; }
    public long getEndNanos() { return endNanos; }
    public boolean isActive() { return active; }

    public double getRemainingSeconds() {
        if (endNanos <= 0) return 0.0;
        return Math.max(0.0, (endNanos - System.nanoTime()) / 1_000_000_000.0);
    }
}
