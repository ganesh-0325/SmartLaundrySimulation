package com.smartlaundry.model;

public final class EventRecord {
    private final String simulationTime;//Stores the time at which the event occurred in the simulation.

    /**  * Stores the name of the thread that generated the event.
     * In this project, this is normally a customer thread such as
     * "Customer-01" or "Customer-25".*/

    private final String threadName;
    //Stores the stage/category of the event.
    private final String stage;
    private final String message;//Stores the detailed description of what happened.


    public EventRecord(
            String simulationTime,
            String threadName,
            String stage,
            String message) {
        this.simulationTime = simulationTime;
        this.threadName = threadName;
        this.stage = stage;
        this.message = message;
    }

    public String getSimulationTime() {
        return simulationTime;
    }
    public String getThreadName() {
        return threadName;
    }
    public String getStage() {
        return stage;
    }
    public String getMessage() {
        return message;
    }

    /** in event logs it look like [00:08.521] [Customer-07] [WASHER] Acquired Washer-3 */

    public String toDisplayString() {
        return String.format("[%s] [%s] [%s] %s", simulationTime, threadName, stage, message);
    }

}
