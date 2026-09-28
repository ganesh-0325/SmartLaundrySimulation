package com.smartlaundry.service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class StatisticsManager {
    private final AtomicInteger customersArrived = new AtomicInteger();
    private final AtomicInteger customersServed = new AtomicInteger();
    private final AtomicInteger customersInterrupted = new AtomicInteger();
    private final AtomicLong totalCustomerTimeNanos = new AtomicLong();

    private final AtomicInteger currentWashers = new AtomicInteger();
    private final AtomicInteger maxWashers = new AtomicInteger();
    private final AtomicInteger currentDryers = new AtomicInteger();
    private final AtomicInteger maxDryers = new AtomicInteger();
    private final AtomicInteger currentPayments = new AtomicInteger();

    private final AtomicInteger washerFailures = new AtomicInteger();
    private final AtomicInteger paymentFailures = new AtomicInteger();

    public void customerArrived() { customersArrived.incrementAndGet(); }
    public void customerServed(long totalTimeNanos) {
        customersServed.incrementAndGet();
        totalCustomerTimeNanos.addAndGet(totalTimeNanos);
    }
    public void customerInterrupted() { customersInterrupted.incrementAndGet(); }

    public int getCustomersArrived() { return customersArrived.get(); }
    public int getCustomersServed() { return customersServed.get(); }
    public int getCustomersInterrupted() { return customersInterrupted.get(); }
    public int getActiveCustomers() {
        return Math.max(0, customersArrived.get() - customersServed.get() - customersInterrupted.get());
    }
    public double getAverageCustomerTimeSeconds() {
        int served = customersServed.get();
        return served == 0 ? 0.0 : (totalCustomerTimeNanos.get() / 1_000_000_000.0) / served;
    }

    public void washerAcquired() { int current = currentWashers.incrementAndGet(); updateMaximum(maxWashers, current); }
    public void washerReleased() { currentWashers.updateAndGet(v -> Math.max(0, v - 1)); }
    public void dryerAcquired() { int current = currentDryers.incrementAndGet(); updateMaximum(maxDryers, current); }
    public void dryerReleased() { currentDryers.updateAndGet(v -> Math.max(0, v - 1)); }
    public void paymentAcquired() { currentPayments.incrementAndGet(); }
    public void paymentReleased() { currentPayments.updateAndGet(v -> Math.max(0, v - 1)); }

    public void washerFailed() { washerFailures.incrementAndGet(); }
    public void paymentFailed() { paymentFailures.incrementAndGet(); }

    public int getCurrentWashers() { return currentWashers.get(); }
    public int getMaxWashers() { return maxWashers.get(); }
    public int getCurrentDryers() { return currentDryers.get(); }
    public int getMaxDryers() { return maxDryers.get(); }
    public int getCurrentPayments() { return currentPayments.get(); }
    public int getWasherFailures() { return washerFailures.get(); }
    public int getPaymentFailures() { return paymentFailures.get(); }

    private static void updateMaximum(AtomicInteger target, int value) {
        while (true) {
            int current = target.get();
            if (value <= current) return;
            if (target.compareAndSet(current, value)) return;
        }
    }

    public void reset() {
        customersArrived.set(0);
        customersServed.set(0);
        customersInterrupted.set(0);
        totalCustomerTimeNanos.set(0);
        currentWashers.set(0);
        maxWashers.set(0);
        currentDryers.set(0);
        maxDryers.set(0);
        currentPayments.set(0);
        washerFailures.set(0);
        paymentFailures.set(0);
    }
}
