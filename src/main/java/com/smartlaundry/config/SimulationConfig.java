package com.smartlaundry.config; //this  class belongs to this package

/** Central configuration for the Smart Laundry Facility Simulation. */
public final class SimulationConfig {
   /** this class is final so other class
    cannot extend it but access it because we
    public access modifier her */



//     the requirement defines the following values must be used

    public static final int CUSTOMER_COUNT = 50;//  there will be
    // 50 customers in the simulation
    public static final int WASHER_COUNT = 6; // total washer
    public static final int DRYER_COUNT = 4; // total dryer
    public static final int PAYMENT_KIOSK_COUNT = 2; // total payment methods

    //** this defines the customers arrival time in between 0 and 3 milliseconds  */
    public static final int ARRIVAL_MIN_MS = 0;
    public static final int ARRIVAL_MAX_MS = 3_000;
    /**  when customer starts washing it generates the random values*/
    public static final int WASH_MIN_MS = 4_000;
    public static final int WASH_MAX_MS = 6_000;
    /**  frying time in between 3 to 5 second */
    public static final int DRY_MIN_MS = 3_000;
    public static final int DRY_MAX_MS = 5_000;

     // Payment = 1–2 sec
    public static final int PAYMENT_MIN_MS = 1_000;
    public static final int PAYMENT_MAX_MS = 2_000;
/**  Washer failure probability  is 5 % and same for payment failure */
    public static final double WASHER_FAILURE_PROBABILITY = 0.05;
    public static final double PAYMENT_FAILURE_PROBABILITY = 0.05;
    // after payment failure it adds extra 2 sec
    public static final int PAYMENT_RETRY_DELAY_MS = 2_000;

   /***  extra assumption like it  used for the failure
    * during  recovery  1 sec simulation delay hunx
    * after washer failure it waits 1 sec
    *
    *
    *
    * */
    public static final int WASHER_REPAIR_DELAY_MS = 1_000;
    public static final int WASHER_RETRY_DELAY_MS = 1_000;
    public static final int PAYMENT_REPAIR_DELAY_MS = 1_000;
    public static final int OWNER_RESPONSE_DELAY_MS = 5_000;

    //  extra requirement
    // owner is called after 30 customers are in the payment queue
    public static final int OWNER_CALL_QUEUE_THRESHOLD = 30;
    // GUI
//    GUI roughly refresh in every 250 millisecond
    public static final int GUI_REFRESH_MS = 250;
    // only 600 event entries in the gui
    public static final int MAX_EVENT_LOG_SIZE = 600;
}
