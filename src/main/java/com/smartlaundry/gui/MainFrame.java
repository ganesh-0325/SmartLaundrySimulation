package com.smartlaundry.gui;

import com.smartlaundry.config.SimulationConfig;
import com.smartlaundry.model.ResourceSnapshot;
import com.smartlaundry.model.ResourceStatus;
import com.smartlaundry.model.SimulationStatus;
import com.smartlaundry.service.LaundryFacility;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MainFrame extends JFrame {
    private final LaundryFacility facility;
    private final JLabel statusLabel = new JLabel("READY");
    private final JLabel elapsedLabel = new JLabel("00:00");
    private final JLabel progressLabel = new JLabel("0 / 50 COMPLETED");
    private final JLabel resourceLabel = new JLabel("W 0/6   D 0/4   P 0/2");
    private final JLabel ownerLabel = new JLabel("Owner: —");
    private final JProgressBar overallProgress = new JProgressBar(0, SimulationConfig.CUSTOMER_COUNT);

    private final JButton startNormal = new JButton("START NORMAL");
    private final JButton startCongestion = new JButton("START CONGESTION");
    private final JButton stop = new JButton("STOP");

    private final Map<String, ResourceCard> resourceCards = new HashMap<>();
    private final ActivityPanel activityPanel = new ActivityPanel();
    private final QueuePanel queuePanel = new QueuePanel();
    private final StatisticsPanel statisticsPanel = new StatisticsPanel();
    private final EventLogPanel eventLogPanel = new EventLogPanel();
    private final Timer refreshTimer;

    public MainFrame(LaundryFacility facility) {
        super("Smart Laundry Facility Simulation");
        this.facility = facility;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int frameWidth = Math.min(1500, Math.max(1200, screen.width - 40));
        int frameHeight = Math.min(950, Math.max(820, screen.height - 80));
        setMinimumSize(new Dimension(1200, 820));
        setSize(frameWidth, frameHeight);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UiTheme.APP_BG);

        buildUi();
        bindActions();
        refreshTimer = new Timer(SimulationConfig.GUI_REFRESH_MS, e -> refreshUi());
        refreshTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                refreshTimer.stop();
                facility.shutdown();
                dispose();
                System.exit(0);
            }
        });
        refreshUi();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        root.setBackground(UiTheme.APP_BG);

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel dashboard = new JPanel();
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));
        dashboard.setBackground(UiTheme.APP_BG);
        dashboard.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        dashboard.add(section("WASHING MACHINES", buildResourceRow("Washer", SimulationConfig.WASHER_COUNT)));
        dashboard.add(section("DRYERS", buildResourceRow("Dryer", SimulationConfig.DRYER_COUNT)));
        dashboard.add(section("PAYMENT KIOSKS", buildResourceRow("Kiosk", SimulationConfig.PAYMENT_KIOSK_COUNT)));

        JPanel lower = new JPanel(new GridLayout(1, 3, 10, 0));
        lower.setOpaque(false);
        lower.setMinimumSize(new Dimension(0, 220));
        lower.setPreferredSize(new Dimension(0, 220));
        lower.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));
        lower.add(activityPanel);
        lower.add(queuePanel);
        lower.add(statisticsPanel);
        dashboard.add(Box.createVerticalStrut(8));
        dashboard.add(lower);

        JScrollPane dashboardScroll = new JScrollPane(dashboard);
        dashboardScroll.setBorder(BorderFactory.createEmptyBorder());
        dashboardScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        dashboardScroll.getVerticalScrollBar().setUnitIncrement(18);
        dashboardScroll.getViewport().setBackground(UiTheme.APP_BG);

        JSplitPane vertical = new JSplitPane(JSplitPane.VERTICAL_SPLIT, dashboardScroll, eventLogPanel);
        vertical.setBorder(BorderFactory.createEmptyBorder());
        vertical.setDividerLocation(0.86);
        vertical.setResizeWeight(0.86);
        vertical.setContinuousLayout(true);
        root.add(vertical, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(UiTheme.SURFACE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("SMART LAUNDRY FACILITY");
        title.setFont(UiTheme.TITLE);
        title.setForeground(UiTheme.TEXT);

        header.add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new BorderLayout(10, 6));
        center.setOpaque(false);
        JPanel statusLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statusLine.setOpaque(false);
        styleStatusLabel();
        elapsedLabel.setFont(UiTheme.VALUE);
        progressLabel.setFont(UiTheme.LABEL_BOLD);
        resourceLabel.setFont(UiTheme.LABEL_BOLD);
        ownerLabel.setFont(UiTheme.LABEL_BOLD);
        statusLine.add(statusLabel);
        statusLine.add(elapsedLabel);
        statusLine.add(progressLabel);
        statusLine.add(resourceLabel);
        statusLine.add(ownerLabel);

        overallProgress.setStringPainted(false);
        overallProgress.setForeground(UiTheme.BLUE);
        overallProgress.setBackground(UiTheme.GRAY_LIGHT);
        overallProgress.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        overallProgress.setPreferredSize(new Dimension(360, 10));

        center.add(statusLine, BorderLayout.NORTH);
        center.add(overallProgress, BorderLayout.SOUTH);
        header.add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        styleButton(startNormal, UiTheme.GREEN);
        styleButton(startCongestion, UiTheme.AMBER);
        styleButton(stop, UiTheme.RED);
        buttons.add(startNormal);
        buttons.add(startCongestion);
        buttons.add(stop);
        header.add(buttons, BorderLayout.EAST);
        return header;
    }

    private JPanel section(String title, JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 5));
        wrapper.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setFont(UiTheme.SECTION);
        label.setForeground(UiTheme.TEXT);
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        return wrapper;
    }

    private JPanel buildResourceRow(String type, int count) {
        JPanel row = new JPanel(new GridLayout(1, count, 8, 0));
        row.setOpaque(false);
        for (int i = 1; i <= count; i++) {
            String id = type + "-" + i;
            ResourceCard card = new ResourceCard(id);
            resourceCards.put(id, card);
            row.add(card);
        }
        return row;
    }

    private void bindActions() {
        startNormal.addActionListener(e -> facility.start(false));
        startCongestion.addActionListener(e -> facility.start(true));
        stop.addActionListener(e -> facility.stop());
    }

    private void refreshUi() {
        List<ResourceSnapshot> snapshots = facility.resourceSnapshots();
        int busyWashers = 0, busyDryers = 0, busyPayments = 0;
        for (ResourceSnapshot snapshot : snapshots) {
            ResourceCard card = resourceCards.get(snapshot.getId());
            if (card != null) card.update(snapshot);
            if (snapshot.getStatus() == ResourceStatus.BUSY) {
                if (snapshot.getType().equals("Washer")) busyWashers++;
                else if (snapshot.getType().equals("Dryer")) busyDryers++;
                else busyPayments++;
            }
        }

        activityPanel.refresh(facility);
        queuePanel.refresh(facility);
        statisticsPanel.refresh(facility);
        eventLogPanel.refresh(facility);

        int served = facility.getStatistics().getCustomersServed();
        overallProgress.setValue(Math.min(SimulationConfig.CUSTOMER_COUNT, served));
        progressLabel.setText(served + " / " + SimulationConfig.CUSTOMER_COUNT + " COMPLETED");
        resourceLabel.setText(String.format("W %d/%d   D %d/%d   P %d/%d",
                busyWashers, SimulationConfig.WASHER_COUNT,
                busyDryers, SimulationConfig.DRYER_COUNT,
                busyPayments, SimulationConfig.PAYMENT_KIOSK_COUNT));
        elapsedLabel.setText(formatElapsed(facility.getElapsedSeconds()));

        SimulationStatus status = facility.getStatus();
        String statusText = switch (status) {
            case READY -> "READY";
            case RUNNING -> "● RUNNING";
            case CONGESTION_MODE -> "● CONGESTION MODE";
            case STOPPED -> "■ STOPPED";
            case COMPLETED -> "✓ COMPLETED";
        };
        statusLabel.setText(statusText);
        statusLabel.setForeground(status == SimulationStatus.RUNNING || status == SimulationStatus.COMPLETED
                ? UiTheme.GREEN
                : status == SimulationStatus.CONGESTION_MODE ? UiTheme.AMBER
                : status == SimulationStatus.STOPPED ? UiTheme.RED : UiTheme.SUBTLE);

        ownerLabel.setText(facility.isOwnerCalled() ? "Owner: CALLED" : "Owner: —");
        ownerLabel.setForeground(facility.isOwnerCalled() ? UiTheme.RED : UiTheme.SUBTLE);

        boolean running = facility.isRunning();
        boolean canStart = facility.canStart();
        startNormal.setEnabled(canStart);
        startCongestion.setEnabled(canStart);
        stop.setEnabled(running);
    }

    private void styleStatusLabel() {
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(UiTheme.GRAY_LIGHT);
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        statusLabel.setFont(UiTheme.LABEL_BOLD);
    }

    private void styleButton(JButton button, java.awt.Color color) {
        button.setFont(UiTheme.LABEL_BOLD);
        button.setForeground(java.awt.Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 13, 9, 13));
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    private static String formatElapsed(double seconds) {
        int total = (int) seconds;
        return String.format("%02d:%02d", total / 60, total % 60);
    }
}
