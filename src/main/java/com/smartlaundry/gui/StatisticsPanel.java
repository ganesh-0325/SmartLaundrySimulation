package com.smartlaundry.gui;

import com.smartlaundry.service.LaundryFacility;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;

public final class StatisticsPanel extends JPanel {
    private final JLabel arrived = value();
    private final JLabel served = value();
    private final JLabel waiting = value();
    private final JLabel activeThreads = value();
    private final JLabel avgTime = value();
    private final JLabel maxWashers = value();
    private final JLabel maxDryers = value();
    private final JLabel failures = value();

    public StatisticsPanel() {
        setLayout(new GridLayout(4, 2, 7, 7));
        setBackground(UiTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        add(card("Customers Arrived", arrived));
        add(card("Customers Served", served));
        add(card("Waiting Customers", waiting));
        add(card("Active Threads", activeThreads));
        add(card("Average Total Time", avgTime));
        add(card("Max Washers", maxWashers));
        add(card("Max Dryers", maxDryers));
        add(card("Failures W / P", failures));
    }

    public void refresh(LaundryFacility facility) {
        arrived.setText(String.valueOf(facility.getStatistics().getCustomersArrived()));
        served.setText(String.valueOf(facility.getStatistics().getCustomersServed()));
        waiting.setText(String.valueOf(facility.waitingThreads()));
        avgTime.setText(String.format("%.2fs", facility.getStatistics().getAverageCustomerTimeSeconds()));
        maxWashers.setText(facility.getStatistics().getMaxWashers() + " / 6");
        maxDryers.setText(facility.getStatistics().getMaxDryers() + " / 4");
        failures.setText(facility.getStatistics().getWasherFailures() + " / " + facility.getStatistics().getPaymentFailures());
        activeThreads.setText(String.valueOf(facility.activeThreads()));
    }

    private static JLabel value() {
        JLabel label = new JLabel("0");
        label.setFont(UiTheme.VALUE);
        label.setForeground(UiTheme.TEXT);
        return label;
    }

    private static JPanel card(String caption, JLabel value) {
        JPanel p = new JPanel(new GridLayout(2, 1));
        p.setBackground(UiTheme.SURFACE_ALT);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        JLabel title = new JLabel(caption);
        title.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 11));
        title.setForeground(UiTheme.SUBTLE);
        p.add(title);
        p.add(value);
        return p;
    }
}
