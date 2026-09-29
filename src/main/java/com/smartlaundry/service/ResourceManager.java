package com.smartlaundry.service;

import com.smartlaundry.config.SimulationConfig;
import com.smartlaundry.model.Dryer;
import com.smartlaundry.model.PaymentKiosk;
import com.smartlaundry.model.ResourceSnapshot;
import com.smartlaundry.model.ResourceStatus;
import com.smartlaundry.model.WashingMachine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared-resource coordinator. Semaphore controls capacity; locks protect
 * safe selection and mutation of individual resource objects.
 */


public final class ResourceManager {


    private final List<WashingMachine> washers = new ArrayList<>();
    private final List<Dryer> dryers = new ArrayList<>();
    private final List<PaymentKiosk> kiosks = new ArrayList<>();


  //resource-capacity control
//    maximum 6 washer permits
    private final Semaphore washerSemaphore = new Semaphore(SimulationConfig.WASHER_COUNT, true);
    private final Semaphore dryerSemaphore = new Semaphore(SimulationConfig.DRYER_COUNT, true);
    private final Semaphore kioskSemaphore = new Semaphore(SimulationConfig.PAYMENT_KIOSK_COUNT, true);


    /** ArrayList is not thread-safe by itself.
     *  Therefore, the corresponding ReentrantLock
     *  is used whenever the list is accessed.*/

    //ReentrantLock identifies and safely selects the specific free washer
    private final ReentrantLock washerLock = new ReentrantLock(true);
    private final ReentrantLock dryerLock = new ReentrantLock(true);
    private final ReentrantLock kioskLock = new ReentrantLock(true);

//Queue fileds
    // It allows you to add and remove elements from both ends.
    // helps to track waiting customers
    private final Deque<String> washerQueue = new ConcurrentLinkedDeque<>();
    private final Deque<String> dryerQueue = new ConcurrentLinkedDeque<>();
    private final Deque<String> paymentQueue = new ConcurrentLinkedDeque<>();

    /**  ScheduledExecutorService is used to schedule tasks to run after a
     * specific delay or at regular intervals.*/
    private final ScheduledExecutorService repairExecutor = Executors.newScheduledThreadPool(3, r
            -> {
        Thread t = new Thread(r, "Resource-Repair");
        t.setDaemon(true); //repair background thread
        return t;
    });

//     volatile = this state can be read write by multiple thread

    private volatile boolean congestionMode;

    /*
     * Ensure that repair tasks scheduled by a previous simulation do not
     * continue running and affect the state of the new simulation.
     */
    private final AtomicInteger lifecycleEpoch = new AtomicInteger();


    // actual resources create
    public ResourceManager() {
        for (int i = 1; i <= SimulationConfig.WASHER_COUNT; i++) washers.add(new WashingMachine("Washer-" + i));
        for (int i = 1; i <= SimulationConfig.DRYER_COUNT; i++) dryers.add(new Dryer("Dryer-" + i));
        for (int i = 1; i <= SimulationConfig.PAYMENT_KIOSK_COUNT; i++) kiosks.add(new PaymentKiosk("Kiosk-" + i));
    }

// gives washer to the customer safely
    public WashingMachine acquireWasher(String customerId) throws InterruptedException {
        boolean permitAcquired = false;
        // customers add to the waiting list
        washerQueue.addLast(customerId);
        try {
            washerSemaphore.acquire(); // resource can be bloc here
            permitAcquired = true;
            washerLock.lockInterruptibly();
            try {
                WashingMachine washer = findFreeWasher();
                if (washer == null) throw new IllegalStateException("Washer semaphore/resource state mismatch"); // semaphore gives
                // permits but there is not any free washer
                markBusy(washer, customerId, "WASHING");
                return washer;
            } finally {
                washerLock.unlock();
            }

        } catch (InterruptedException ex) {
            if (permitAcquired) washerSemaphore.release();
            throw ex;
        } catch (RuntimeException ex) {
            if (permitAcquired) washerSemaphore.release();
            throw ex;
        } finally {
            washerQueue.removeFirstOccurrence(customerId);
        }
    }
    // Dryer queue → Semaphore(4) → Lock → find free dryer → mark BUSY → return selected dryer
    public Dryer acquireDryer(String customerId) throws InterruptedException {
        boolean permitAcquired = false;
        dryerQueue.addLast(customerId);
        try {
            dryerSemaphore.acquire();
            permitAcquired = true;
            dryerLock.lockInterruptibly();
            try {
                Dryer dryer = findFreeDryer();
                if (dryer == null) throw new IllegalStateException("Dryer semaphore/resource state mismatch");
                markBusy(dryer, customerId, "DRYING");
                return dryer;
            } finally {
                dryerLock.unlock();
            }
        } catch (InterruptedException ex) {
            if (permitAcquired) dryerSemaphore.release();
            throw ex;
        } catch (RuntimeException ex) {
            if (permitAcquired) dryerSemaphore.release();
            throw ex;
        } finally {
            dryerQueue.removeFirstOccurrence(customerId);
        }
    }

    // Queue → Semaphore(2) → kioskLock → find free kiosk → mark BUSY → return selected kiosk

    public PaymentKiosk acquirePaymentKiosk(String customerId) throws InterruptedException {
        boolean permitAcquired = false;
        paymentQueue.addLast(customerId);
        try {
            kioskSemaphore.acquire();
            permitAcquired = true;
            kioskLock.lockInterruptibly();
            try {
                PaymentKiosk kiosk = findFreeKiosk();
                if (kiosk == null) throw new IllegalStateException("Kiosk semaphore/resource state mismatch");
                markBusy(kiosk, customerId, "PAYMENT");
                return kiosk;
            } finally {
                kioskLock.unlock();
            }
        } catch (InterruptedException ex) {
            if (permitAcquired) kioskSemaphore.release();
            throw ex;
        } catch (RuntimeException ex) {
            if (permitAcquired) kioskSemaphore.release();
            throw ex;
        } finally {
            paymentQueue.removeFirstOccurrence(customerId);
        }
    }

    public void setWasherTiming(WashingMachine washer, long startNanos, long endNanos) {
        washerLock.lock();
        try { washer.setTiming(startNanos, endNanos); } finally { washerLock.unlock(); }
    }

    public void setDryerTiming(Dryer dryer, long startNanos, long endNanos) {
        dryerLock.lock();
        try { dryer.setTiming(startNanos, endNanos); } finally { dryerLock.unlock(); }
    }

    public void setKioskTiming(PaymentKiosk kiosk, long startNanos, long endNanos) {
        kioskLock.lock();
        try { kiosk.setTiming(startNanos, endNanos); } finally { kioskLock.unlock(); }
    }

    public void releaseWasher(WashingMachine washer) {
        washerLock.lock();
        try {
            washer.setStatus(ResourceStatus.FREE);
            washer.setCustomerId("");
            washer.setOperation("Available");
            washer.clearTiming();
            washerSemaphore.release();
        } finally {
            washerLock.unlock();
        }
    }

    public void releaseDryer(Dryer dryer) {
        dryerLock.lock();
        try {
            dryer.setStatus(ResourceStatus.FREE);
            dryer.setCustomerId("");
            dryer.setOperation("Available");
            dryer.clearTiming();
            dryerSemaphore.release();
        } finally {
            dryerLock.unlock();
        }
    }

    public void releasePaymentKiosk(PaymentKiosk kiosk) {
        kioskLock.lock();
        try {
            if (kiosk.isFailedForDay()) return;
            kiosk.setStatus(ResourceStatus.FREE);
            kiosk.setCustomerId("");
            kiosk.setOperation("Available");
            kiosk.clearTiming();
            kioskSemaphore.release();
        } finally {
            kioskLock.unlock();
        }
    }

    /** Marks the currently owned washer failed and schedules independent recovery. */
    public void failWasher(WashingMachine washer) {
        int epoch = lifecycleEpoch.get();
        washerLock.lock();
        try {
            long now = System.nanoTime();
            washer.setStatus(ResourceStatus.FAILED);
            washer.setOperation("FAILED - REPAIRING");
            washer.setTiming(now, now + SimulationConfig.WASHER_REPAIR_DELAY_MS * 1_000_000L);
        } finally {
            washerLock.unlock();
        }

        repairExecutor.schedule(() -> {
            if (lifecycleEpoch.get() != epoch) return;
            washerLock.lock();
            try {
                washer.setStatus(ResourceStatus.FREE);
                washer.setCustomerId("");
                washer.setOperation("Available");
                washer.clearTiming();
                washerSemaphore.release();
            } finally {
                washerLock.unlock();
            }
        }, SimulationConfig.WASHER_REPAIR_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /** Normal kiosk failure is transient; its permit stays reserved until repair. */
    public void failPaymentKiosk(PaymentKiosk kiosk) {
        if (congestionMode) return;
        int epoch = lifecycleEpoch.get();
        kioskLock.lock();
        try {
            long now = System.nanoTime();
            kiosk.setStatus(ResourceStatus.FAILED);
            kiosk.setOperation("FAILED - REPAIRING");
            kiosk.setTiming(now, now + SimulationConfig.PAYMENT_REPAIR_DELAY_MS * 1_000_000L);
        } finally {
            kioskLock.unlock();
        }

        repairExecutor.schedule(() -> {
            if (lifecycleEpoch.get() != epoch) return;
            kioskLock.lock();
            try {
                if (!kiosk.isFailedForDay()) {
                    kiosk.setStatus(ResourceStatus.FREE);
                    kiosk.setCustomerId("");
                    kiosk.setOperation("Available");
                    kiosk.clearTiming();
                    kioskSemaphore.release();
                }
            } finally {
                kioskLock.unlock();
            }
        }, SimulationConfig.PAYMENT_REPAIR_DELAY_MS, TimeUnit.MILLISECONDS);
    }

    /** Bonus: both kiosks are unavailable for the day until the owner responds. */
    public void enableCongestionMode() {
        congestionMode = true;
        kioskLock.lock();
        try {
            kioskSemaphore.drainPermits();
            for (PaymentKiosk kiosk : kiosks) {
                kiosk.setFailedForDay(true);
                kiosk.setStatus(ResourceStatus.FAILED);
                kiosk.setCustomerId("");
                kiosk.setOperation("FAILED FOR DAY");
                kiosk.clearTiming();
            }
        } finally {
            kioskLock.unlock();
        }
    }

    public int restoreKiosksAfterOwnerCall() {
        kioskLock.lock();
        try {
            int restored = 0;
            for (PaymentKiosk kiosk : kiosks) {
                if (kiosk.isFailedForDay()) {
                    kiosk.setFailedForDay(false);
                    kiosk.setStatus(ResourceStatus.FREE);
                    kiosk.setCustomerId("");
                    kiosk.setOperation("Available");
                    kiosk.clearTiming();
                    restored++;
                }
            }
            if (restored > 0) kioskSemaphore.release(restored);
            return restored;
        } finally {
            kioskLock.unlock();
        }
    }

    public List<ResourceSnapshot> resourceSnapshots() {
        List<ResourceSnapshot> snapshots = new ArrayList<>();
        washerLock.lock();
        try { for (WashingMachine w : washers) snapshots.add(w.snapshot()); } finally { washerLock.unlock(); }
        dryerLock.lock();
        try { for (Dryer d : dryers) snapshots.add(d.snapshot()); } finally { dryerLock.unlock(); }
        kioskLock.lock();
        try { for (PaymentKiosk k : kiosks) snapshots.add(k.snapshot()); } finally { kioskLock.unlock(); }
        return Collections.unmodifiableList(snapshots);
    }

    public List<String> washerQueueSnapshot() { return new ArrayList<>(washerQueue); }
    public List<String> dryerQueueSnapshot() { return new ArrayList<>(dryerQueue); }
    public List<String> paymentQueueSnapshot() { return new ArrayList<>(paymentQueue); }
    public int washerQueueSize() { return washerQueue.size(); }
    public int dryerQueueSize() { return dryerQueue.size(); }
    public int paymentQueueSize() { return paymentQueue.size(); }

    public boolean isAllKiosksFailedForDay() {
        kioskLock.lock();
        try { return kiosks.stream().allMatch(PaymentKiosk::isFailedForDay); }
        finally { kioskLock.unlock(); }
    }

    public void reset() {
        lifecycleEpoch.incrementAndGet();
        congestionMode = false;

        washerLock.lock();
        try {
            washerSemaphore.drainPermits();
            washerSemaphore.release(SimulationConfig.WASHER_COUNT);
            washerQueue.clear();
            for (WashingMachine w : washers) {
                w.setStatus(ResourceStatus.FREE); w.setCustomerId(""); w.setOperation("Available"); w.clearTiming();
            }
        } finally { washerLock.unlock(); }

        dryerLock.lock();
        try {
            dryerSemaphore.drainPermits();
            dryerSemaphore.release(SimulationConfig.DRYER_COUNT);
            dryerQueue.clear();
            for (Dryer d : dryers) {
                d.setStatus(ResourceStatus.FREE); d.setCustomerId(""); d.setOperation("Available"); d.clearTiming();
            }
        } finally { dryerLock.unlock(); }

        kioskLock.lock();
        try {
            kioskSemaphore.drainPermits();
            kioskSemaphore.release(SimulationConfig.PAYMENT_KIOSK_COUNT);
            paymentQueue.clear();
            for (PaymentKiosk k : kiosks) {
                k.setStatus(ResourceStatus.FREE); k.setCustomerId(""); k.setOperation("Available");
                k.setFailedForDay(false); k.clearTiming();
            }
        } finally { kioskLock.unlock(); }
    }

    public void shutdown() {
        lifecycleEpoch.incrementAndGet();
        repairExecutor.shutdownNow();
    }

    private WashingMachine findFreeWasher() {
        for (WashingMachine w : washers) if (w.getStatus() == ResourceStatus.FREE) return w;
        return null;
    }

    private Dryer findFreeDryer() {
        for (Dryer d : dryers) if (d.getStatus() == ResourceStatus.FREE) return d;
        return null;
    }

    private PaymentKiosk findFreeKiosk() {
        for (PaymentKiosk k : kiosks) {
            if (!k.isFailedForDay() && k.getStatus() == ResourceStatus.FREE) return k;
        }
        return null;
    }

    private static void markBusy(WashingMachine w, String customerId, String operation) {
        w.setStatus(ResourceStatus.BUSY); w.setCustomerId(customerId); w.setOperation(operation); w.clearTiming();
    }

    private static void markBusy(Dryer d, String customerId, String operation) {
        d.setStatus(ResourceStatus.BUSY); d.setCustomerId(customerId); d.setOperation(operation); d.clearTiming();
    }

    private static void markBusy(PaymentKiosk k, String customerId, String operation) {
        k.setStatus(ResourceStatus.BUSY); k.setCustomerId(customerId); k.setOperation(operation); k.clearTiming();
    }
}
