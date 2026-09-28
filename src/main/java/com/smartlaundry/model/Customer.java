package com.smartlaundry.model;

import com.smartlaundry.config.SimulationConfig;
import com.smartlaundry.service.LaundryFacility;
import com.smartlaundry.util.RandomUtils;

/** One customer is one Runnable executed by its own Java Thread. */
public final class Customer implements Runnable {
    /** other class can not extend it because final keyword is used */
    private final String customerId;
    private final LaundryFacility facility;
    private long arrivalNanos; // total customers journey time

//    constructor is created for the customer class
    public Customer(String customerId, LaundryFacility facility) {
        this.customerId = customerId;
        this.facility = facility;
    }

    @Override
    public void run() {
        arrivalNanos = System.nanoTime();//  it records when customer  thread was started
        facility.customerArrived(customerId);
        facility.activity(customerId, "ARRIVAL", "Entrance", "Customer arrived", 0, 0, true);

        try {
            wash();
            dry();
            pay();

            facility.customerCompleted(customerId, System.nanoTime() - arrivalNanos);
            facility.log(customerId, "EXIT", "Customer completed the full laundry journey");
            facility.activity(customerId, "COMPLETED", "Exit", "Completed", 0, 0, false);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            facility.customerInterrupted(customerId);
            facility.activity(customerId, "STOPPED", "", "Thread interrupted safely", 0, 0, false);
            facility.log(customerId, "SYSTEM", "Customer thread interrupted and terminated safely");
        } catch (RuntimeException e) {
            facility.customerInterrupted(customerId);
            facility.activity(customerId, "ERROR", "", e.getMessage() == null ? "Unexpected error" : e.getMessage(), 0, 0, false);
            facility.log(customerId, "SYSTEM", "Customer terminated due to error: " + e.getClass().getSimpleName());
        }
    }

    private void wash() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            facility.activity(customerId, "WAITING", "Washer Queue", "Waiting because washer capacity may be full", 0, 0, true);
            facility.log(customerId, "QUEUE", "Waiting for an available washing machine");
            WashingMachine washer = facility.acquireWasher(customerId);
            facility.washerAcquired();

            int durationMs = RandomUtils.randomInclusive(SimulationConfig.WASH_MIN_MS, SimulationConfig.WASH_MAX_MS);
            long start = System.nanoTime();
            long end = start + durationMs * 1_000_000L;
            facility.setWasherTiming(washer, start, end);
            facility.activity(customerId, "WASHING", washer.getId(), "Washing clothes", start, end, true);
            facility.log(customerId, "WASHER", "Acquired " + washer.getId());
            facility.log(customerId, "WASHING", String.format("Started washing for %.2f seconds", durationMs / 1000.0));

            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                facility.releaseWasher(washer);
                throw e;
            }

            if (RandomUtils.chance(SimulationConfig.WASHER_FAILURE_PROBABILITY)) {
                facility.washerFailed();
                facility.log(customerId, "FAILURE", washer.getId() + " failed during the wash cycle (5% event)");
                facility.activity(customerId, "FAILED", washer.getId(), "Washer failure - retrying", System.nanoTime(),
                        System.nanoTime() + SimulationConfig.WASHER_RETRY_DELAY_MS * 1_000_000L, true);
                facility.failWasher(washer);
                Thread.sleep(SimulationConfig.WASHER_RETRY_DELAY_MS);
                facility.log(customerId, "RETRY", "Retrying washing after washer failure");
                continue;
            }

            facility.releaseWasher(washer);
            facility.log(customerId, "WASHING", "Completed washing and released " + washer.getId());
            return;
        }
        throw new InterruptedException("Interrupted while washing");
    }

    private void dry() throws InterruptedException {
        facility.activity(customerId, "WAITING", "Dryer Queue", "Waiting because dryer capacity may be full", 0, 0, true);
        facility.log(customerId, "QUEUE", "Waiting for an available dryer");
        Dryer dryer = facility.acquireDryer(customerId);
        facility.dryerAcquired();

        int durationMs = RandomUtils.randomInclusive(SimulationConfig.DRY_MIN_MS, SimulationConfig.DRY_MAX_MS);
        long start = System.nanoTime();
        long end = start + durationMs * 1_000_000L;
        facility.setDryerTiming(dryer, start, end);
        facility.activity(customerId, "DRYING", dryer.getId(), "Drying clothes", start, end, true);
        facility.log(customerId, "DRYER", "Acquired " + dryer.getId());
        facility.log(customerId, "DRYING", String.format("Started drying for %.2f seconds", durationMs / 1000.0));

        try {
            Thread.sleep(durationMs);
        } catch (InterruptedException e) {
            facility.releaseDryer(dryer);
            throw e;
        }

        facility.releaseDryer(dryer);
        facility.log(customerId, "DRYING", "Completed drying and released " + dryer.getId());
    }

    private void pay() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            String reason = facility.isCongestionMode()
                    ? "Waiting for a payment kiosk - congestion mode may have both kiosks unavailable"
                    : "Waiting for an available payment kiosk";
            facility.activity(customerId, "WAITING", "Payment Queue", reason, 0, 0, true);
            facility.log(customerId, "QUEUE", reason);

            PaymentKiosk kiosk = facility.acquirePaymentKiosk(customerId);
            facility.paymentAcquired();

            int durationMs = RandomUtils.randomInclusive(SimulationConfig.PAYMENT_MIN_MS, SimulationConfig.PAYMENT_MAX_MS);
            long start = System.nanoTime();
            long end = start + durationMs * 1_000_000L;
            facility.setKioskTiming(kiosk, start, end);
            facility.activity(customerId, "PAYMENT", kiosk.getId(), "Processing payment", start, end, true);
            facility.log(customerId, "PAYMENT", "Acquired " + kiosk.getId());
            facility.log(customerId, "PAYMENT", String.format("Started payment for %.2f seconds", durationMs / 1000.0));

            try {
                Thread.sleep(durationMs);
            } catch (InterruptedException e) {
                facility.releasePaymentKiosk(kiosk);
                throw e;
            }

            if (!facility.isCongestionMode() && RandomUtils.chance(SimulationConfig.PAYMENT_FAILURE_PROBABILITY)) {
                facility.paymentFailed();
                facility.log(customerId, "FAILURE", kiosk.getId() + " failed during payment (5% event)");
                facility.activity(customerId, "RETRYING", kiosk.getId(), "Payment failure - retrying after 2 seconds", System.nanoTime(),
                        System.nanoTime() + SimulationConfig.PAYMENT_RETRY_DELAY_MS * 1_000_000L, true);
                facility.failPaymentKiosk(kiosk);
                Thread.sleep(SimulationConfig.PAYMENT_RETRY_DELAY_MS);
                facility.log(customerId, "RETRY", "Retrying payment after the required 2-second delay");
                continue;
            }

            facility.releasePaymentKiosk(kiosk);
            facility.log(customerId, "PAYMENT", "Payment successful and released " + kiosk.getId());
            return;
        }
        throw new InterruptedException("Interrupted while paying");
    }
}
