package com.smartlaundry;

import com.smartlaundry.gui.MainFrame;
import com.smartlaundry.service.LaundryFacility;

import javax.swing.SwingUtilities;

public final class Main {

    public static void main(String[] args) throws InterruptedException {
        boolean console = false;
        boolean congestion = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg)) console = true;
            if ("--congestion".equalsIgnoreCase(arg)) congestion = true;
        }

        if (console) {
            runConsole(congestion);
        } else {
            SwingUtilities.invokeLater(() -> {
                LaundryFacility facility = new LaundryFacility();
                new MainFrame(facility).setVisible(true);
            });
        }
    }

    private static void runConsole(boolean congestion) throws InterruptedException {
        LaundryFacility facility = new LaundryFacility();
        Runtime.getRuntime().addShutdownHook(new Thread(facility::shutdown, "Shutdown-Hook"));
        facility.start(congestion);
        while (facility.isRunning()) Thread.sleep(500);


        System.out.println();
        System.out.println("============================================================");
        System.out.println("SMART LAUNDRY - FINAL STATISTICS");
        System.out.println("============================================================");
        System.out.println("Customers arrived     : " + facility.getStatistics().getCustomersArrived());
        System.out.println("Customers served      : " + facility.getStatistics().getCustomersServed());
        System.out.printf("Average customer time : %.2f seconds%n", facility.getStatistics().getAverageCustomerTimeSeconds());
        System.out.println("Max concurrent washers: " + facility.getStatistics().getMaxWashers());
        System.out.println("Max concurrent dryers : " + facility.getStatistics().getMaxDryers());
        System.out.println("Washer failures       : " + facility.getStatistics().getWasherFailures());
        System.out.println("Payment failures      : " + facility.getStatistics().getPaymentFailures());
        System.out.println("Interrupted customers : " + facility.getStatistics().getCustomersInterrupted());
        System.out.println("Final status          : " + facility.getStatus());
        System.out.println("============================================================");
        facility.shutdown();
    }
}

