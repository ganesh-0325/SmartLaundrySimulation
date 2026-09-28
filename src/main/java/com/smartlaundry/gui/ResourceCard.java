package com.smartlaundry.gui;

import com.smartlaundry.model.ResourceSnapshot;
import com.smartlaundry.model.ResourceStatus;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

public final class ResourceCard extends JPanel {
    private final JLabel nameLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JLabel customerLabel = new JLabel();
    private final JLabel operationLabel = new JLabel();
    private final JLabel timeLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar(0, 1000);

    public ResourceCard(String id) {
        setLayout(new BorderLayout(6, 4));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        setPreferredSize(new Dimension(150, 100));
        setMinimumSize(new Dimension(110, 96));

        nameLabel.setText(id);
        nameLabel.setFont(UiTheme.SECTION);
        nameLabel.setForeground(UiTheme.TEXT);

        statusLabel.setFont(UiTheme.LABEL_BOLD);
        customerLabel.setFont(UiTheme.LABEL);
        operationLabel.setFont(UiTheme.LABEL);
        timeLabel.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 11));

        JPanel details = new JPanel(new GridLayout(4, 1, 2, 2));
        details.setOpaque(false);
        details.add(statusLabel);
        details.add(customerLabel);
        details.add(operationLabel);
        details.add(timeLabel);

        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 7));

        add(nameLabel, BorderLayout.NORTH);
        add(details, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }

    public void update(ResourceSnapshot snapshot) {
        ResourceStatus status = snapshot.getStatus();
        String statusText;
        String operation = snapshot.getOperation();
        String customer = snapshot.getCustomerId().isBlank() ? "—" : snapshot.getCustomerId();
        Color background;
        Color border;

        switch (status) {
            case FREE -> {
                statusText = "● FREE";
                background = UiTheme.GREEN_LIGHT;
                border = new Color(175, 215, 188);
            }
            case BUSY -> {
                statusText = "● BUSY";
                background = UiTheme.BLUE_LIGHT;
                border = new Color(170, 195, 225);
            }
            case FAILED -> {
                statusText = snapshot.isFailedForDay() ? "✕ FAILED FOR DAY" : "✕ FAILED";
                background = UiTheme.RED_LIGHT;
                border = new Color(225, 180, 180);
            }
            case RETRYING -> {
                statusText = "↻ RETRYING";
                background = UiTheme.AMBER_LIGHT;
                border = new Color(228, 204, 155);
            }
            default -> throw new IllegalStateException("Unexpected resource status");
        }

        statusLabel.setText("Status: " + statusText);
        statusLabel.setForeground(status == ResourceStatus.FREE ? UiTheme.GREEN
                : status == ResourceStatus.BUSY ? UiTheme.BLUE
                : status == ResourceStatus.FAILED ? UiTheme.RED : UiTheme.AMBER);
        customerLabel.setText("Customer: " + customer);
        operationLabel.setText("Action: " + operation);

        double remaining = snapshot.getRemainingSeconds();
        if (remaining > 0.05) {
            timeLabel.setText(String.format("Time: %.1fs remaining", remaining));
            progressBar.setValue((int) Math.round(snapshot.getProgressPercent() * 10));
            progressBar.setVisible(true);
        } else {
            timeLabel.setText(status == ResourceStatus.FREE ? "Available" : "Time: —");
            progressBar.setValue(0);
            progressBar.setVisible(status == ResourceStatus.BUSY || status == ResourceStatus.FAILED);
        }

        setBackground(background);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                BorderFactory.createEmptyBorder(7, 8, 7, 8)));
        progressBar.setForeground(status == ResourceStatus.FAILED ? UiTheme.RED : status == ResourceStatus.BUSY ? UiTheme.BLUE : UiTheme.GREEN);
    }
}
