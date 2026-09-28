package com.smartlaundry.model;

public final class EventRecord {
    private final String simulationTime;
    private final String threadName;
    private final String stage;
    private final String message;

    public EventRecord(String simulationTime, String threadName, String stage, String message) {
        this.simulationTime = simulationTime;
        this.threadName = threadName;
        this.stage = stage;
        this.message = message;
    }

    public String getSimulationTime() { return simulationTime; }
    public String getThreadName() { return threadName; }
    public String getStage() { return stage; }
    public String getMessage() { return message; }

    public String toDisplayString() {
        return String.format("[%s] [%s] [%s] %s", simulationTime, threadName, stage, message);
    }
}
