package com.smartlaundry.service;

import com.smartlaundry.config.SimulationConfig;
import com.smartlaundry.model.CustomerActivity;
import com.smartlaundry.model.Customer;
import com.smartlaundry.model.Dryer;
import com.smartlaundry.model.EventRecord;
import com.smartlaundry.model.PaymentKiosk;
import com.smartlaundry.model.ResourceSnapshot;
import com.smartlaundry.model.SimulationStatus;
import com.smartlaundry.model.WashingMachine;
import com.smartlaundry.util.Logger;
import com.smartlaundry.util.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Main simulation coordinator. Business logic stays outside the Swing layer. */

public final class LaundryFacility {
    private final ResourceManager resources = new ResourceManager();
    private final StatisticsManager statistics = new StatisticsManager();
    private final SimulationState state = new SimulationState();
    private final Logger logger = new Logger(state);// simulation record rakxa
    private final List<Thread> customerThreads = new CopyOnWriteArrayList<>();

    private volatile Thread dispatcherThread; // handles the customer thread creation
    private volatile Thread ownerResponseThread;
    private volatile boolean stopRequested;



//    simulation starts from here
    public synchronized void start(boolean congestionMode) {
        if (isRunning()) return;
        if (dispatcherThread != null && dispatcherThread.isAlive()) return;
        stopRequested = false;
//        sab simulation  must be clean
        customerThreads.clear();
        statistics.reset();
        resources.reset();
        state.clearForNewRun();
        state.start(congestionMode);

        if (congestionMode) {// both payment method unable
            resources.enableCongestionMode();
            log("SYSTEM", "SYSTEM", "CONGESTION MODE STARTED - both payment kiosks are failed for the day");
        } else {
            log("SYSTEM", "SYSTEM", "NORMAL MODE STARTED");
        }

        dispatcherThread = new Thread(() -> dispatchCustomers(congestionMode), "Simulation-Dispatcher");
        dispatcherThread.start();

        if (congestionMode) {
            Thread monitor = new Thread(this::monitorCongestion, "Congestion-Monitor");
            monitor.setDaemon(true);
            monitor.start();
        }
    }

    public synchronized void stop() {
        if (!isRunning()) return;
        stopRequested = true;
        state.stop();
        log("SYSTEM", "SYSTEM", "STOP requested - interrupting active customer threads safely");

        Thread dispatcher = dispatcherThread;
        if (dispatcher != null) dispatcher.interrupt();
        Thread owner = ownerResponseThread;
        if (owner != null) owner.interrupt();
        for (Thread thread : customerThreads) thread.interrupt();
    }

    private void dispatchCustomers(boolean congestionMode) {
        try {
            for (int i = 1; i <= SimulationConfig.CUSTOMER_COUNT && !stopRequested; i++) {
                int delay = RandomUtils.randomInclusive(SimulationConfig.ARRIVAL_MIN_MS, SimulationConfig.ARRIVAL_MAX_MS);
                Thread.sleep(delay);
                if (stopRequested) break;

                String customerId = String.format("Customer-%02d", i);
                Thread customerThread = new Thread(new Customer(customerId, this), customerId);
                customerThreads.add(customerThread);
                customerThread.start();
            }

            if (stopRequested) joinCustomerThreads();
            else joinCustomerThreads();

            if (!stopRequested) {
                state.complete();
                log("SYSTEM", "SYSTEM", "SIMULATION COMPLETED - " + statistics.getCustomersServed() + "/" + SimulationConfig.CUSTOMER_COUNT + " customers served");
                log("SYSTEM", "SYSTEM", String.format("Average total customer time: %.2f seconds", statistics.getAverageCustomerTimeSeconds()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            joinCustomerThreads();
        } finally {
            dispatcherThread = null;
        }
    }

    private void joinCustomerThreads() {
        boolean interrupted = false;
        for (Thread thread : customerThreads) {
            while (thread.isAlive()) {
                try { thread.join(200); }
                catch (InterruptedException e) { interrupted = true; }
            }
        }
        if (interrupted) Thread.currentThread().interrupt();
    }

    private void monitorCongestion() {
        try {
            while (!stopRequested && isRunning() && state.isCongestionMode() && !state.isOwnerCalled()) {
                if (resources.paymentQueueSize() >= SimulationConfig.OWNER_CALL_QUEUE_THRESHOLD && state.markOwnerCalled()) {
                    log("SYSTEM", "QUEUE", "PAYMENT QUEUE REACHED " + resources.paymentQueueSize() + " CUSTOMERS");
                    log("SYSTEM", "SYSTEM", "OWNER CALLED - congestion threshold reached");
                    startOwnerResponse();
                    break;
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void startOwnerResponse() {
        ownerResponseThread = new Thread(() -> {
            try {
                Thread.sleep(SimulationConfig.OWNER_RESPONSE_DELAY_MS);
                if (stopRequested) return;
                int restored = resources.restoreKiosksAfterOwnerCall();
                log("SYSTEM", "SYSTEM", "Owner responded - restored " + restored + " payment kiosks; congestion is clearing");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Owner-Response");
        ownerResponseThread.setDaemon(true);
        ownerResponseThread.start();
    }

    public void customerArrived(String id) {
        statistics.customerArrived();
        log(id, "ARRIVAL", "Customer arrived at the facility");
    }


    public void customerCompleted(String id, long durationNanos) {
        statistics.customerServed(durationNanos);
    }

    public void customerInterrupted(String id) {
        statistics.customerInterrupted();
    }

    public void washerAcquired() { statistics.washerAcquired(); }
    public void washerReleased() { statistics.washerReleased(); }
    public void washerFailed() { statistics.washerFailed(); }
    public void dryerAcquired() { statistics.dryerAcquired(); }
    public void dryerReleased() { statistics.dryerReleased(); }
    public void paymentAcquired() { statistics.paymentAcquired(); }
    public void paymentReleased() { statistics.paymentReleased(); }
    public void paymentFailed() { statistics.paymentFailed(); }

    public void failWasher(WashingMachine washer) { statistics.washerReleased(); resources.failWasher(washer); }
    public void failPaymentKiosk(PaymentKiosk kiosk) { statistics.paymentReleased(); resources.failPaymentKiosk(kiosk); }

    public WashingMachine acquireWasher(String id) throws InterruptedException { return resources.acquireWasher(id); }
    public Dryer acquireDryer(String id) throws InterruptedException { return resources.acquireDryer(id); }
    public PaymentKiosk acquirePaymentKiosk(String id) throws InterruptedException { return resources.acquirePaymentKiosk(id); }

    public void releaseWasher(WashingMachine w) { statistics.washerReleased(); resources.releaseWasher(w); }
    public void releaseDryer(Dryer d) { statistics.dryerReleased(); resources.releaseDryer(d); }
    public void releasePaymentKiosk(PaymentKiosk k) { statistics.paymentReleased(); resources.releasePaymentKiosk(k); }

    public void setWasherTiming(WashingMachine w, long start, long end) { resources.setWasherTiming(w, start, end); }
    public void setDryerTiming(Dryer d, long start, long end) { resources.setDryerTiming(d, start, end); }
    public void setKioskTiming(PaymentKiosk k, long start, long end) { resources.setKioskTiming(k, start, end); }

    public void activity(String customerId, String stage, String resource, String reason,
                         long startNanos, long endNanos, boolean active) {
        state.activity(customerId, stage, resource, reason, startNanos, endNanos, active);
    }

    public void log(String thread, String stage, String message) { logger.log(thread, stage, message); }

    public SimulationStatus getStatus() { return state.getStatus(); }
    public boolean isRunning() {
        SimulationStatus status = state.getStatus();
        return status == SimulationStatus.RUNNING || status == SimulationStatus.CONGESTION_MODE;
    }
    public boolean isCongestionMode() {
        return state.isCongestionMode();
    }
    public boolean isOwnerCalled() { return state.isOwnerCalled(); }
    public double getElapsedSeconds() { return state.getElapsedSeconds(); }
    public StatisticsManager getStatistics() { return statistics; }
    public List<ResourceSnapshot> resourceSnapshots() { return resources.resourceSnapshots(); }
    public List<CustomerActivity> activitySnapshots() { return state.activitySnapshot(); }
    public List<EventRecord> eventSnapshots() { return state.eventSnapshot(); }
    public List<String> washerQueueSnapshot() { return resources.washerQueueSnapshot(); }
    public List<String> dryerQueueSnapshot() { return resources.dryerQueueSnapshot(); }
    public List<String> paymentQueueSnapshot() { return resources.paymentQueueSnapshot(); }
    public int washerQueueSize() { return resources.washerQueueSize(); }
    public int dryerQueueSize() { return resources.dryerQueueSize(); }
    public int paymentQueueSize() { return resources.paymentQueueSize(); }
    public int activeThreads() { return statistics.getActiveCustomers(); }
    public int waitingThreads() { return washerQueueSize() + dryerQueueSize() + paymentQueueSize(); }
    public boolean canStart() { return !isRunning() && (dispatcherThread == null || !dispatcherThread.isAlive()); }

    public void shutdown() {
        stop();
        resources.shutdown();
    }
}
