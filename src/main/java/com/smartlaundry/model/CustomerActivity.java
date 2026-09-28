package com.smartlaundry.model;

public final class CustomerActivity {
    private final String customerId;
    private final String stage;
    private final String resource;
    private final String reason;
    private final long startNanos;
    private final long endNanos;
    private final boolean active;


//    this is the constructor
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
        this.resource = resource == null ? "" : resource; // if resource is null then put empty string otherwise put actual data
        this.reason = reason == null ? "" : reason;
        this.startNanos = startNanos;
        this.endNanos = endNanos;
        this.active = active;
    }

//    getter setter

    public String getCustomerId() { return customerId; }
    public String getStage() { return stage; }
    public String getResource() { return resource; }
    public String getReason() { return reason; }
    public long getStartNanos() { return startNanos; }
    public long getEndNanos() { return endNanos; }
    public boolean isActive() { return active; }

    public double getRemainingSeconds()  { // how many second seconds a activity runs
        if (endNanos <= 0) // if there is not valid endtime then return 0 second
            return 0.0;
        return Math.max(0.0, (endNanos - System.nanoTime()) / 1_000_000_000.0);  //nanoseconds is used
    }
}
