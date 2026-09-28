package com.smartlaundry.gui;

import com.smartlaundry.config.SimulationConfig;
import com.smartlaundry.service.LaundryFacility;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public final class QueuePanel extends JPanel {
    private final JTextArea washerArea = createArea();
    private final JTextArea dryerArea = createArea();
    private final JTextArea paymentArea = createArea();

    public QueuePanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(UiTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel title = new JLabel("QUEUES — ACTUAL WAITING CUSTOMERS");
        title.setFont(UiTheme.SECTION);
        title.setForeground(UiTheme.TEXT);
        add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 3, 8, 0));
        grid.setOpaque(false);
        grid.add(queueBox("WASHER QUEUE", washerArea));
        grid.add(queueBox("DRYER QUEUE", dryerArea));
        grid.add(queueBox("PAYMENT QUEUE", paymentArea));
        add(grid, BorderLayout.CENTER);
    }

    public void refresh(LaundryFacility facility) {
        updateArea(washerArea, facility.washerQueueSnapshot(), "All 6 washers are occupied");
        updateArea(dryerArea, facility.dryerQueueSnapshot(), "All 4 dryers are occupied");
        String paymentReason = facility.isCongestionMode() && !facility.isOwnerCalled()
                ? "Both payment kiosks are failed for the day"
                : "Both payment kiosks are occupied";
        updateArea(paymentArea, facility.paymentQueueSnapshot(), paymentReason);
    }

    private static JPanel queueBox(String title, JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBackground(UiTheme.SURFACE_ALT);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER),
                BorderFactory.createEmptyBorder(6, 6, 6, 6)));
        JLabel label = new JLabel(title);
        label.setFont(UiTheme.LABEL_BOLD);
        label.setForeground(UiTheme.TEXT);
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private static JTextArea createArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(UiTheme.MONO);
        area.setBackground(ColorFor.textAreaBg());
        area.setForeground(UiTheme.TEXT);
        return area;
    }

    private static void updateArea(JTextArea area, List<String> queue, String reason) {
        StringBuilder sb = new StringBuilder();
        sb.append("Queue size: ").append(queue.size()).append('\n');
        if (queue.isEmpty()) {
            sb.append("No customers waiting.");
        } else {
            int i = 1;
            for (String customer : queue) {
                sb.append(String.format("#%d  %s\n    %s\n", i++, customer, reason));
                if (i > 16) {
                    sb.append("... +").append(queue.size() - 16).append(" more");
                    break;
                }
            }
        }
        area.setText(sb.toString());
        area.setCaretPosition(0);
    }

    private static final class ColorFor {
        private static java.awt.Color textAreaBg() { return java.awt.Color.WHITE; }
    }
}
