package com.smartlaundry.model;

public final class Dryer {
    private final String id;
    private ResourceStatus status = ResourceStatus.FREE;
    private String customerId = ""; // which customer used this dryer
    private String operation = "Available";
    private long startNanos;
    private long endNanos;

//    constructor created
    public Dryer(String id) {
        this.id = id;
    }


    public String getId() { return id; }

//    getter
    public ResourceStatus getStatus() { return status; }

     // setter
    public void setStatus(ResourceStatus status) { this.status = status; }
    public String getCustomerId() { return customerId; }

    public void setCustomerId(String customerId) {
        this.customerId = customerId == null ? "" : customerId;
    }

    public String getOperation() { return operation; }


    public void setOperation(String operation) {
        this.operation = operation == null ? "" : operation;
    }
    public long getStartNanos() { return startNanos; }
    public long getEndNanos() { return endNanos; }

//    when dryer status started and when ends
    public void setTiming(long startNanos, long endNanos) {
        this.startNanos = startNanos;
        this.endNanos = endNanos;
    }
//     removes the dryer previous timing
    public void clearTiming() {
        this.startNanos = 0;
        this.endNanos = 0;
    }

    public ResourceSnapshot snapshot() {
        return new ResourceSnapshot(
                id, "Dryer",
                status,
                customerId,
                operation,
                startNanos,
                endNanos, false);
    }
}
