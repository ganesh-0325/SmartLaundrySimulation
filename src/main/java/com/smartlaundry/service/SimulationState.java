package com.smartlaundry.service;

import com.smartlaundry.config.SimulationConfig;
import com.smartlaundry.model.CustomerActivity;
import com.smartlaundry.model.EventRecord;
import com.smartlaundry.model.SimulationStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Thread-safe, read-oriented state shared between simulation threads and Swing. */
public final class SimulationState {
    private final AtomicReference<SimulationStatus> status = new AtomicReference<>(SimulationStatus.READY);
    private final AtomicBoolean congestionMode = new AtomicBoolean(false);
    private final AtomicBoolean ownerCalled = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, CustomerActivity> activities = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<EventRecord> events = new ConcurrentLinkedDeque<>();

    private volatile long startNanos;
    private volatile long endNanos;

    public void start(boolean congestion) {
        congestionMode.set(congestion);
        ownerCalled.set(false);
        startNanos = System.nanoTime();
        endNanos = 0;
        status.set(congestion ? SimulationStatus.CONGESTION_MODE : SimulationStatus.RUNNING);
    }

    public void stop() {
        endNanos = System.nanoTime();
        status.set(SimulationStatus.STOPPED);
    }

    public void complete() {
        endNanos = System.nanoTime();
        status.set(SimulationStatus.COMPLETED);
    }

    public void clearForNewRun() {
        activities.clear();
        events.clear();
        status.set(SimulationStatus.READY);
        congestionMode.set(false);
        ownerCalled.set(false);
        startNanos = 0;
        endNanos = 0;
    }

    public SimulationStatus getStatus() { return status.get(); }
    public boolean isCongestionMode() { return congestionMode.get(); }
    public boolean isOwnerCalled() { return ownerCalled.get(); }
    public boolean markOwnerCalled() { return ownerCalled.compareAndSet(false, true); }

    public void activity(String customerId, String stage, String resource, String reason,
                         long startNanos, long endNanos, boolean active) {
        if (active) {
            activities.put(customerId, new CustomerActivity(customerId, stage, resource, reason, startNanos, endNanos, true));
        } else {
            activities.remove(customerId);
        }
    }

    public List<CustomerActivity> activitySnapshot() {
        List<CustomerActivity> copy = new ArrayList<>(activities.values());
        copy.removeIf(a -> !a.isActive());
        copy.sort(Comparator.comparing(CustomerActivity::getCustomerId));
        return copy;
    }

    public void addEvent(EventRecord event) {
        events.addLast(event);
        while (events.size() > SimulationConfig.MAX_EVENT_LOG_SIZE) events.pollFirst();
    }

    public List<EventRecord> eventSnapshot() { return new ArrayList<>(events); }

    public double getElapsedSeconds() {
        if (startNanos == 0) return 0.0;
        long reference = endNanos > 0 ? endNanos : System.nanoTime();
        return Math.max(0.0, (reference - startNanos) / 1_000_000_000.0);
    }
}
